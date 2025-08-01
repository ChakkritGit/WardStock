@file:Suppress("KotlinUnreachableCode")

package com.thanes.wardstock.services.machine

import android.util.Log
import com.thanes.wardstock.services.usb.SerialPortManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Dispense private constructor(
  private val serialPortManager: SerialPortManager
) {
  private var ttyS1WriteData = mutableListOf<Int>()
  private val stateMutex = Mutex()
  private var currentTtyS1CommNo: Int = 0
  private var commandRetryCount: Int = 0
  private var expectedTtyS1Response: String = ""

  companion object {
    private var INSTANCE: Dispense? = null
    private const val TAG = "Dispense"

    private var progress = "ready"

    private const val OVERALL_TIMEOUT_MS = 120_000L
    private const val LIFT_MOVEMENT_TIMEOUT_MS = 20_000L
    private const val DOOR_MOVEMENT_TIMEOUT_MS = 7_000L
    private const val DISPENSE_STATUS_TIMEOUT_MS = 15_000L
    private const val COMMUNICATION_TIMEOUT_MS = 5_000L
    private const val MAX_TTY_RETRY = 3
    private const val VMC_ACK_RESPONSE = "fa,fb,42,00,43"
    private const val VMC_POLL_REQUEST = "fa,fb,41,00,40"
    private const val VMC_DISPENSE_STATUS_PREFIX = "fa,fb,04,04"

    fun getInstance(serialPortManager: SerialPortManager): Dispense {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Dispense(serialPortManager).also { INSTANCE = it }
      }
    }
  }

  private fun resetInternalState() {
    if (stateMutex.tryLock()) {
      try {
        ttyS1WriteData.clear()
        currentTtyS1CommNo = 0
        commandRetryCount = 0
        expectedTtyS1Response = ""
        progress = "ready"
        Log.d(TAG, "Internal state has been reset.")
      } finally {
        stateMutex.unlock()
        Log.d(TAG, "Mutex unlocked after state reset.")
      }
    } else {
      Log.w(TAG, "Could not acquire lock to reset state. Another process might be holding it.")
    }
  }

  private fun prepareTtyS1Command(slot: Int) {
    currentTtyS1CommNo = serialPortManager.getRunning()
    currentTtyS1CommNo = if (currentTtyS1CommNo >= 255) 1 else currentTtyS1CommNo + 1

    val command = serialPortManager.createSerialttyS1Command(
      slot = slot,
      communicationNumber = currentTtyS1CommNo,
      enableDropSensor = false,
      enableElevator = false
    )

    ttyS1WriteData.clear()
    ttyS1WriteData.addAll(command)
    commandRetryCount = 0
    expectedTtyS1Response = "ACK_FOR_CMD"
  }

  fun getConnectPort(): Boolean {
    return serialPortManager.isConnected()
  }

  suspend fun sendToMachine(dispenseQty: Int, position: Int): Boolean {
    resetInternalState()

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    return try {
      val success = withTimeoutOrNull(OVERALL_TIMEOUT_MS) {
        suspendCoroutine<Boolean> { continuation ->

          val positionsToDispense = List(dispenseQty) { position }
          var currentDispenseIndex = 0
          var itemsSuccessfullyDispensed = 0

          val isProcessCompleted = AtomicBoolean(false)
          var commandTimeoutJob: Job? = null

          fun cleanupAndComplete(result: Boolean) {
            if (isProcessCompleted.compareAndSet(false, true)) {
              commandTimeoutJob?.cancel()
              if (continuation.context.isActive) {
                continuation.resume(result)
              }
            }
          }

          fun startCommandTimeout(
            commandToRetry: String,
            stream: String,
            timeout: Long = COMMUNICATION_TIMEOUT_MS
          ) {
            commandTimeoutJob?.cancel()
            commandTimeoutJob = scope.launch {
              delay(timeout)
              if (isProcessCompleted.get()) return@launch

              Log.e(TAG, "Timeout on $stream! No response for: $commandToRetry")
              commandRetryCount++
              if (commandRetryCount > MAX_TTY_RETRY) {
                Log.e(TAG, "Max retries reached. Aborting.")
                cleanupAndComplete(false)
              } else {
                Log.w(TAG, "Retrying on $stream (Attempt ${commandRetryCount})...")
                if (stream == "ttyS2") {
                  serialPortManager.writeSerialttyS2(commandToRetry)
                  startCommandTimeout(commandToRetry, "ttyS2")
                } else if (stream == "ttyS1" && commandToRetry == "VMC_COMMAND") {
                  stateMutex.withLock {
                    Log.d(TAG, "Resetting progress to 'waitingForPoll' to retry ttyS1 command.")
                    progress = "waitingForPoll"
                  }
                }
              }
            }
          }

          try {
            val initialCmd = "# 1 1 3 1 6"
            progress = "lockingRack"
            serialPortManager.writeSerialttyS2(initialCmd)
            startCommandTimeout(initialCmd, "ttyS2", COMMUNICATION_TIMEOUT_MS)

            serialPortManager.startReadingSerialttyS1 { data ->
              scope.launch {
                stateMutex.withLock {
                  if (isProcessCompleted.get()) return@withLock
                  val responseHex = data.joinToString(",") { "%02x".format(it) }

                  when (responseHex) {
                    VMC_POLL_REQUEST -> {
                      if (progress == "waitingForPoll" && ttyS1WriteData.isNotEmpty()) {
                        val cmdBytes = ttyS1WriteData.map { it.toByte() }.toByteArray()
                        serialPortManager.writeSerialttyS1Raw(cmdBytes)
                        progress = "waitingForCmdAck"
                        startCommandTimeout("VMC_COMMAND", "ttyS1")
                      } else {
                        serialPortManager.writeSerialttyS1Ack()
                      }
                    }
                    VMC_ACK_RESPONSE -> {
                      if (progress == "waitingForCmdAck") {
                        commandTimeoutJob?.cancel()
                        commandRetryCount = 0
                        serialPortManager.saveRunning(currentTtyS1CommNo)
                        ttyS1WriteData.clear()
                        progress = "waitingForDispenseStatus"
                        startCommandTimeout("DISPENSE_STATUS", "ttyS1", DISPENSE_STATUS_TIMEOUT_MS)
                      }
                    }
                    else -> {
                      if (responseHex.startsWith(VMC_DISPENSE_STATUS_PREFIX) && progress == "waitingForDispenseStatus") {
                        commandTimeoutJob?.cancel()
                        commandRetryCount = 0
                        serialPortManager.writeSerialttyS1Ack()
                        val dispenseStatus = data.getOrNull(5)?.toInt()?.and(0xFF)
                        if (dispenseStatus == 0x02) {
                          itemsSuccessfullyDispensed++
                        }
                        currentDispenseIndex++
                        if (currentDispenseIndex < positionsToDispense.size) {
                          delay(200)
                          prepareTtyS1Command(positionsToDispense[currentDispenseIndex])
                          progress = "waitingForPoll"
                        } else {
                          progress = "liftingDownToFifty"
                          val cmd = "# 1 1 1 50 53"
                          serialPortManager.writeSerialttyS2(cmd)
                          startCommandTimeout(cmd, "ttyS2")
                        }
                      }
                    }
                  }
                }
              }
            }

            serialPortManager.startReadingSerialttyS2 { data ->
              CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                stateMutex.withLock {
                  if (isProcessCompleted.get()) return@withLock
                  val response = data.joinToString(",") { "%02x".format(it) }
                  commandTimeoutJob?.cancel()
                  commandRetryCount = 0
                  var nextCommand = ""
                  var nextTimeout = COMMUNICATION_TIMEOUT_MS

                  when (response) {
                    "26,31,0a,0a,32,0a,0a,33,0a,0a,31,0a,0a,37,0a,0a" -> if (progress == "lockingRack") {
                      nextCommand = "# 1 1 5 10 17"
                      progress = "openingDoor"
                      nextTimeout = DOOR_MOVEMENT_TIMEOUT_MS
                    }

                    "26,31,0a,0a,32,0a,0a,35,0a,0a,31,0a,0a,39,0a,0a" -> if (progress == "openingDoor") {
                      val floor = when {
                        position <= 10 -> 1400; position <= 20 -> 1210; position <= 30 -> 1010; position <= 40 -> 790; position <= 50 -> 580; position <= 60 -> 360; else -> 20
                      }
                      nextCommand = "# 1 1 1 $floor ${floor + 3}"
                      progress = "liftingUp"
                      nextTimeout = LIFT_MOVEMENT_TIMEOUT_MS
                    }

                    "26,31,0a,0a,32,0a,0a,31,0a,0a,31,0a,0a,35,0a,0a" -> when (progress) {
                      "liftingUp" -> {
                        prepareTtyS1Command(positionsToDispense[currentDispenseIndex]); progress =
                          "waitingForPoll"
                      }

                      "liftingDownToFifty" -> {
                        nextCommand = "# 1 1 1 -1 2"
                        progress = "liftingDown"
                        nextTimeout = COMMUNICATION_TIMEOUT_MS
                      }

                      "liftingDown" -> {
                        nextCommand = "# 1 1 6 10 18"; progress = "closingDoor"; nextTimeout =
                          DOOR_MOVEMENT_TIMEOUT_MS
                      }
                    }

                    "26,31,0a,0a,32,0a,0a,36,0a,0a,31,0a,0a,31,30,0a,0a" -> if (progress == "closingDoor") {
                      nextCommand = "# 1 1 3 0 5"; progress = "finalizing"; nextTimeout =
                        COMMUNICATION_TIMEOUT_MS
                    }

                    "26,31,0a,0a,32,0a,0a,33,0a,0a,30,0a,0a,36,0a,0a" -> if (progress == "finalizing") {
                      delay(200)
                      cleanupAndComplete(itemsSuccessfullyDispensed == positionsToDispense.size)
                    }
                  }
                  if (nextCommand.isNotEmpty()) {
                    serialPortManager.writeSerialttyS2(nextCommand)
                    startCommandTimeout(nextCommand, "ttyS2", nextTimeout)
                  }
                }
              }
            }

          } catch (e: Exception) {
            Log.e(TAG, "Error during dispense process setup: ${e.message}", e)
            cleanupAndComplete(false)
          }
        }
      }

      if (success == null) {
        Log.e(TAG, "Overall process timed out after ${OVERALL_TIMEOUT_MS / 1000} seconds.")
        scope.launch { emergencyShutdown() }
        return false
      }

      return success

    } catch (e: Exception) {
      Log.e(TAG, "An unexpected error occurred in sendToMachine: ${e.message}", e)
      return false
    } finally {
      Log.d(TAG, "Executing final cleanup: Stopping serial port readers.")
      serialPortManager.stopReadingSerialttyS1()
      serialPortManager.stopReadingSerialttyS2()
    }
  }

  private suspend fun emergencyShutdown() {
    Log.e(TAG, "!!! INITIATING EMERGENCY SHUTDOWN !!! Current progress: $progress")

    withContext(NonCancellable) {
      val liftWasUp = listOf(
        "liftingUp",
        "waitingForPoll",
        "waitingForCmdAck",
        "waitingForDispenseStatus",
        "liftingDownToFifty"
      ).contains(progress)

      if (!liftWasUp) {
        Log.w(TAG, "Lift was not up. No shutdown sequence needed.")
        return@withContext
      }

      Log.d(TAG, "Starting shutdown sequence: Lift Down -> Close Door -> Unlock Rack")

      val responseChannel = Channel<String>(Channel.CONFLATED)

      val listenerJob = CoroutineScope(Dispatchers.IO).launch {
        serialPortManager.startReadingSerialttyS2 { data ->
          responseChannel.trySend(data.joinToString(",") { "%02x".format(it) })
        }
      }

      try {
        serialPortManager.writeSerialttyS2("# 1 1 1 -1 2")
        val liftDownResponse = withTimeoutOrNull(LIFT_MOVEMENT_TIMEOUT_MS) {
          while (true) {
            if (responseChannel.receive() == "26,31,0d,0a,32,0d,0a,31,0d,0a,31,0d,0a,35,0d,0a") break
          }
        }
        if (liftDownResponse == null) {
          Log.e(TAG, "Timeout waiting for lift to go down during shutdown. Continuing...")
        } else {
          Log.d(TAG, "Shutdown: Lift is down.")
        }

        serialPortManager.writeSerialttyS2("# 1 1 6 10 18")
        val closeDoorResponse = withTimeoutOrNull(DOOR_MOVEMENT_TIMEOUT_MS) {
          while (true) {
            if (responseChannel.receive() == "26,31,0d,0a,32,0d,0a,36,0d,0a,31,0d,0a,31,30,0d,0a") break
          }
        }
        if (closeDoorResponse == null) {
          Log.e(TAG, "Timeout waiting for door to close during shutdown. Continuing...")
        } else {
          Log.d(TAG, "Shutdown: Door is closed.")
        }

        serialPortManager.writeSerialttyS2("# 1 1 3 0 5")
        Log.d(TAG, "Shutdown: Unlock command sent. Sequence complete.")

      } catch (e: Exception) {
        Log.e(TAG, "Exception during emergency shutdown: ${e.message}")
      } finally {
        listenerJob.cancel()
        responseChannel.close()
      }
    }
  }

  suspend fun sendTestModuleStty1(position: Int): Map<String, String>? {
    Log.d(TAG, "[TEST_DISPENSE] Starting test for position: $position")

    val lastKnownCommNo = serialPortManager.getRunning()
    val testCommNo = if (lastKnownCommNo >= 255) 1 else lastKnownCommNo + 1
    val commandList = serialPortManager.createSerialttyS1Command(
      slot = position,
      communicationNumber = testCommNo,
      enableDropSensor = false,
      enableElevator = false
    )
    val commandBytes = commandList.map { it.toByte() }.toByteArray()
    Log.d(
      TAG,
      "[TEST_DISPENSE] Prepared command: ${commandBytes.joinToString(" ") { "%02x".format(it) }}"
    )

    val responseChannel = Channel<ByteArray>(Channel.CONFLATED)

    val listenerJob = CoroutineScope(Dispatchers.IO).launch {
      serialPortManager.startReadingSerialttyS1 { data ->
        val responseHex = data.joinToString(",") { "%02x".format(it) }

        when {
          responseHex == VMC_POLL_REQUEST -> {
            Log.d(TAG, "[TEST_DISPENSE] Poll received. Sending command now.")
            serialPortManager.writeSerialttyS1Raw(commandBytes)
          }

          responseHex == VMC_ACK_RESPONSE -> {
            Log.d(TAG, "[TEST_DISPENSE] ACK received!")
            serialPortManager.saveRunning(testCommNo)
            responseChannel.trySend(data)
          }

          responseHex.startsWith(VMC_DISPENSE_STATUS_PREFIX) -> {
            Log.d(TAG, "[TEST_DISPENSE] Dispense Status received!")
            serialPortManager.writeSerialttyS1Ack()
            responseChannel.trySend(data)
          }

          else -> {
          }
        }
      }
    }

    val results = mutableMapOf<String, String>()
    try {
      val ackResponse = withTimeoutOrNull(5000L) {
        responseChannel.receive()
      }

      if (ackResponse != null && ackResponse.joinToString(",") { "%02x".format(it) } == VMC_ACK_RESPONSE) {
        results["ACK"] = "OK"

        val statusResponse = withTimeoutOrNull(15000L) {
          responseChannel.receive()
        }

        if (statusResponse != null) {
          results["STATUS"] = statusResponse.joinToString(",") { "%02x".format(it) }
        } else {
          results["STATUS"] = "Timeout waiting for status"
          Log.w(TAG, "[TEST_DISPENSE] Timeout waiting for dispense status.")
        }
      } else {
        results["ACK"] = "Timeout or Invalid ACK"
        Log.e(TAG, "[TEST_DISPENSE] Failed to receive ACK.")
      }

    } catch (e: Exception) {
      Log.e(TAG, "[TEST_DISPENSE] Exception during test: ${e.message}")
      return null
    } finally {
      listenerJob.cancel()
      serialPortManager.stopReadingSerialttyS1()
      responseChannel.close()
      Log.d(TAG, "[TEST_DISPENSE] Test finished.")
    }

    return results
  }

  suspend fun sendTestModuleStty2(asciiCommandString: String): ByteArray? {
    Log.d(TAG, "[TEST_S2] Sending command: $asciiCommandString")

    val responseChannel = Channel<ByteArray>(Channel.CONFLATED)

    serialPortManager.startReadingSerialttyS2 { data ->
      responseChannel.trySend(data)
    }

    val success = serialPortManager.writeSerialttyS2(asciiCommandString)
    if (!success) {
      Log.e(TAG, "[TEST_S2] Failed to write to serial port.")
      serialPortManager.stopReadingSerialttyS2()
      responseChannel.close()
      return null
    }

    val response = withTimeoutOrNull(5000L) {
      responseChannel.receive()
    }

    serialPortManager.stopReadingSerialttyS2()
    responseChannel.close()

    if (response != null) {
      Log.d(TAG, "[TEST_S2] Received: ${response.joinToString(" ") { "%02x".format(it) }}")
    } else {
      Log.w(TAG, "[TEST_S2] No response within 5 seconds.")
    }

    return response
  }
}
