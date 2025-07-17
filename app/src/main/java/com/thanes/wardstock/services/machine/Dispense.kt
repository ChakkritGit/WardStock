//package com.thanes.wardstock.services.machine
//
//import android.util.Log
//import kotlinx.coroutines.*
//import kotlinx.coroutines.sync.Mutex
//import kotlinx.coroutines.sync.withLock
//import java.util.concurrent.atomic.AtomicBoolean
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//
//class Dispense private constructor(
//  private val serialPortManager: SerialPortManager
//) {
//  private var writeData = mutableListOf<Int>()
//  private val stateMutex = Mutex()
//
//  companion object {
//    private var INSTANCE: Dispense? = null
//    private const val TAG = "Dispense"
//
//    fun getInstance(serialPortManager: SerialPortManager): Dispense {
//      return INSTANCE ?: synchronized(this) {
//        INSTANCE ?: Dispense(serialPortManager).also { INSTANCE = it }
//      }
//    }
//  }
//
//  suspend fun sendToMachine(dispenseQty: Int, position: Int): Boolean =
//    withContext(Dispatchers.IO) {
//      suspendCoroutine { continuation ->
//        var progress = "ready"
//        var isDispense = false
//        var floor = -1
//        val totalQty = dispenseQty
//        var remainingQty = dispenseQty
//        var currentDispensed = 0
//        val isCompleted = AtomicBoolean(false)
//
//        var timeoutJob: Job? = null
//
//        fun cleanupAndComplete(result: Boolean) {
//          if (isCompleted.compareAndSet(false, true)) {
//            timeoutJob?.cancel()
//            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//              serialPortManager.stopReadingSerialttyS1()
//              serialPortManager.stopReadingSerialttyS2()
//            }
//            continuation.resume(result)
//          }
//        }
//
//        try {
//          serialPortManager.writeSerialttyS2("# 1 1 3 1 6")
//          isDispense = true
//
//          serialPortManager.startReadingSerialttyS1 { data ->
//            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//              try {
//                stateMutex.withLock {
//                  if (isCompleted.get()) return@withLock
//
//                  val response = data.joinToString(",") { "%02x".format(it) }
//                  Log.d(
//                    TAG,
//                    "ttyS1 response: $response (progress: $progress, remainingQty: $remainingQty)"
//                  )
//
//                  when {
//                    response == "fa,fb,41,00,40" -> {
//                      if (writeData.isEmpty()) {
//                        serialPortManager.writeSerialttyS1Ack()
//                      } else {
//                        val cmdBytes = writeData.map { it.toByte() }.toByteArray()
//                        serialPortManager.writeSerialttyS1Raw(cmdBytes)
//                        remainingQty--
//                      }
//                    }
//
//                    response.startsWith("fa,fb,04,04") -> {
//                      writeData.clear()
//                      if (progress == "dispensing") {
//                        currentDispensed++
//
//                        Log.d(
//                          TAG,
//                          "Item dispensed! Current: $currentDispensed, Remaining: $remainingQty"
//                        )
//
//                        if (remainingQty <= 0) {
//                          delay(1000)
//                          progress = "liftDown"
//                          serialPortManager.writeSerialttyS2("# 1 1 1 -1 2")
//                          Log.d(TAG, "All items dispensed, moving lift down")
//                        } else {
//                          delay(100)
//                          progress = "dispensing"
//                          writeSerialttyS1(position)
//                          Log.d(TAG, "Dispensing next item...")
//                        }
//                      }
//                    }
//                  }
//                }
//              } catch (ex: Exception) {
//                Log.e(TAG, "Error in ttyS1 reading coroutine: ${ex.message}")
//              }
//            }
//          }
//
//          serialPortManager.startReadingSerialttyS2 { data ->
//            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//              try {
//                stateMutex.withLock {
//                  if (isCompleted.get()) return@withLock
//
//                  val response = data.joinToString(",") { "%02x".format(it) }
//                  Log.d(TAG, "ttyS2 response: $response (progress: $progress)")
//
//                  if (isDispense) {
//                    when (response) {
//                      "26,31,0d,0a,32,0d,0a,33,0d,0a,31,0d,0a,37,0d,0a" -> {
//                        serialPortManager.writeSerialttyS2("# 1 1 5 10 17")
//                        progress = "doorOpened"
//                        Log.d(TAG, "Door opened")
//                      }
//
//                      "26,31,0d,0a,32,0d,0a,35,0d,0a,31,0d,0a,39,0d,0a" -> {
//                        floor = when {
//                          position <= 10 -> 1400
//                          position <= 20 -> 1210
//                          position <= 30 -> 1010
//                          position <= 40 -> 790
//                          position <= 50 -> 580
//                          position <= 60 -> 360
//                          else -> 20
//                        }
//                        serialPortManager.writeSerialttyS2("# 1 1 1 $floor ${floor + 3}")
//                        progress = "liftUp"
//                        Log.d(TAG, "Lift moving up to floor: $floor")
//                      }
//
//                      "26,31,0d,0a,32,0d,0a,31,0d,0a,31,0d,0a,35,0d,0a" -> {
//                        when (progress) {
//                          "liftUp" -> {
//                            progress = "dispensing"
//                            writeSerialttyS1(position)
//                            Log.d(TAG, "Starting dispensing process for position: $position")
//                          }
//
//                          "liftDown" -> {
//                            serialPortManager.writeSerialttyS2("# 1 1 6 10 18")
//                            progress = "doorClosing"
//                            Log.d(TAG, "Closing door")
//                          }
//                        }
//                      }
//
//                      "26,31,0d,0a,32,0d,0a,36,0d,0a,31,0d,0a,31,30,0d,0a" -> {
//                        progress = "rackUnlocked"
//                        serialPortManager.writeSerialttyS2("# 1 1 3 0 5")
//                        Log.d(TAG, "Rack unlocked, finalizing")
//                      }
//
//                      "26,31,0d,0a,32,0d,0a,33,0d,0a,30,0d,0a,36,0d,0a" -> {
//                        delay(200)
//                        Log.d(
//                          TAG,
//                          "Process completed successfully! Items dispensed: $currentDispensed/$totalQty"
//                        )
//                        progress = "ready"
//                        isDispense = false
//                        cleanupAndComplete(true)
//                      }
//
//                      else -> {
//                        Log.d(TAG, "Unknown ttyS2 response: $response")
//                      }
//                    }
//                  }
//                }
//              } catch (ex: Exception) {
//                Log.e(TAG, "Error in ttyS2 reading coroutine: ${ex.message}")
//              }
//            }
//          }
//
//          timeoutJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//            delay(120000)
//            Log.d(TAG, "Timeout - process took too long")
//            cleanupAndComplete(false)
//          }
//        } catch (error: Exception) {
//          Log.e(TAG, "SerialPortError: ${error.message}")
//          cleanupAndComplete(false)
//        }
//      }
//    }
//
//  private fun writeSerialttyS1(slot: Int) {
//    val command = serialPortManager.createSerialttyS1Command(slot)
//    writeData.clear()
//    writeData.addAll(command)
//  }
//}

//package com.thanes.wardstock.services.machine
//
//import android.util.Log
//import com.thanes.wardstock.services.usb.SerialPortManager
//import kotlinx.coroutines.*
//import kotlinx.coroutines.sync.Mutex
//import kotlinx.coroutines.sync.withLock
//import java.util.concurrent.atomic.AtomicBoolean
//import kotlin.coroutines.resume
//import kotlin.coroutines.suspendCoroutine
//
//class Dispense private constructor(
//  private val serialPortManager: SerialPortManager
//) {
//  private var ttyS1WriteData = mutableListOf<Int>()
//  private val stateMutex = Mutex()
//  private var currentTtyS1CommNo: Int = 0
//  private var ttyS1CommandRetryCount: Int = 0
//  private var expectedTtyS1Response: String = ""
//
//  companion object {
//    private var INSTANCE: Dispense? = null
//    private const val TAG = "Dispense"
//    private const val MAX_TTY_S1_RETRY = 5
//    private const val VMC_ACK_RESPONSE = "fa,fb,42,00,43"
//    private const val VMC_POLL_REQUEST = "fa,fb,41,00,40"
//    private const val VMC_DISPENSE_STATUS_PREFIX = "fa,fb,04,04"
//
//    fun getInstance(serialPortManager: SerialPortManager): Dispense {
//      return INSTANCE ?: synchronized(this) {
//        INSTANCE ?: Dispense(serialPortManager).also { INSTANCE = it }
//      }
//    }
//  }
//
//  private fun prepareTtyS1Command(
//    slot: Int, enableDropSensor: Boolean = false, enableElevator: Boolean = false
//  ) {
//    currentTtyS1CommNo = serialPortManager.getRunning()
//    currentTtyS1CommNo = if (currentTtyS1CommNo >= 255) 1 else currentTtyS1CommNo + 1
//
//    val command = serialPortManager.createSerialttyS1Command(
//      slot, currentTtyS1CommNo, enableDropSensor, enableElevator
//    )
//    ttyS1WriteData.clear()
//    ttyS1WriteData.addAll(command)
//    ttyS1CommandRetryCount = 0
//    expectedTtyS1Response = "ACK_FOR_CMD"
//  }
//
//  suspend fun sendToMachine(dispenseQty: Int, position: Int): Boolean =
//    withContext(Dispatchers.IO) {
//      suspendCoroutine { continuation ->
//        var progress = "ready"
//        var isDispenseActive = false
//        var currentLiftFloorTarget = -1
//
//        val positionsToDispense = mutableListOf<Int>()
//        repeat(dispenseQty) { positionsToDispense.add(position) }
//
//        var currentDispenseIndex = 0
//        var itemsSuccessfullyDispensed = 0
//
//        val isProcessCompleted = AtomicBoolean(false)
//        var timeoutJob: Job? = null
//        var ttyS1ResponseTimeoutJob: Job? = null
//
//        fun cleanupAndComplete(result: Boolean) {
//          if (isProcessCompleted.compareAndSet(false, true)) {
//            timeoutJob?.cancel()
//            ttyS1ResponseTimeoutJob?.cancel()
//            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//              serialPortManager.stopReadingSerialttyS1()
//              serialPortManager.stopReadingSerialttyS2()
//            }
//            continuation.resume(result)
//          }
//        }
//
//        fun startTtyS1ResponseTimeout(durationMillis: Long = 5000) {
//          ttyS1ResponseTimeoutJob?.cancel()
//          ttyS1ResponseTimeoutJob = CoroutineScope(Dispatchers.IO).launch {
//            delay(durationMillis)
//            if (!isProcessCompleted.get()) {
//              Log.e(TAG, "Timeout waiting for ttyS1 response (expected: $expectedTtyS1Response)")
//              if (progress == "waitingForCmdAck" && ttyS1CommandRetryCount < MAX_TTY_S1_RETRY) {
//                Log.d(TAG, "Retrying ttyS1 command. Attempt: ${ttyS1CommandRetryCount + 1}")
//                ttyS1CommandRetryCount++
//                val cmdBytes = ttyS1WriteData.map { it.toByte() }.toByteArray()
//                serialPortManager.writeSerialttyS1Raw(cmdBytes)
//                startTtyS1ResponseTimeout()
//              } else {
//                cleanupAndComplete(false)
//              }
//            }
//          }
//        }
//
//        try {
//          serialPortManager.writeSerialttyS2("# 1 1 3 1 6")
//          progress = "openingDoor"
//        } catch (e: Exception) {
//          Log.e(TAG, "Error in ttyS2 reading coroutine: ${e.message}")
//          if (!isProcessCompleted.get()) cleanupAndComplete(false)
//        }
//
//        try {
//          isDispenseActive = true
//
//          serialPortManager.startReadingSerialttyS1 { data ->
//            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//              try {
//                stateMutex.withLock {
//                  if (isProcessCompleted.get()) return@withLock
//
//                  val responseHex = data.joinToString(",") { "%02x".format(it) }
//                  Log.d(
//                    TAG,
//                    "ttyS1 <<< $responseHex (Progress: $progress, Expected: $expectedTtyS1Response)"
//                  )
//                  ttyS1ResponseTimeoutJob?.cancel()
//
//                  when (responseHex) {
//                    VMC_POLL_REQUEST -> {
//                      if (progress == "waitingForPoll" && ttyS1WriteData.isNotEmpty()) {
//                        val cmdBytes = ttyS1WriteData.map { it.toByte() }.toByteArray()
//                        serialPortManager.writeSerialttyS1Raw(cmdBytes)
//                        Log.d(
//                          TAG,
//                          "ttyS1 >>> Sent command for slot ${positionsToDispense[currentDispenseIndex]}"
//                        )
//                        progress = "waitingForCmdAck"
//                        expectedTtyS1Response = "ACK_FOR_CMD"
//                        startTtyS1ResponseTimeout()
//                      } else {
//                        serialPortManager.writeSerialttyS1Ack()
//                        Log.d(TAG, "ttyS1 >>> Sent ACK to POLL")
//                      }
//                    }
//
//                    VMC_ACK_RESPONSE -> {
//                      if (progress == "waitingForCmdAck" && expectedTtyS1Response == "ACK_FOR_CMD") {
//                        Log.d(TAG, "ttyS1 <<< Received ACK for command.")
//                        serialPortManager.saveRunning(currentTtyS1CommNo)
//
//                        ttyS1WriteData.clear()
//                        progress = "waitingForDispenseStatus"
//                        expectedTtyS1Response = "DISPENSE_STATUS"
//                        startTtyS1ResponseTimeout(15000)
//                      } else {
//                        Log.w(
//                          TAG,
//                          "ttyS1 <<< Received unexpected ACK. Progress: $progress, Expected: $expectedTtyS1Response"
//                        )
//                      }
//                    }
//
//                    else -> {
//                      if (responseHex.startsWith(VMC_DISPENSE_STATUS_PREFIX) && progress == "waitingForDispenseStatus" && expectedTtyS1Response == "DISPENSE_STATUS") {
//
//                        if (data.size >= 8) {
//                          val receivedCommNo = data[4].toInt() and 0xFF
//                          val dispenseStatus = data[5].toInt() and 0xFF
//                          val selectionHigh = data[6].toInt() and 0xFF
//                          val selectionLow = data[7].toInt() and 0xFF
//                          val receivedSelection = (selectionHigh shl 8) or selectionLow
//
//                          Log.d(
//                            TAG,
//                            "ttyS1 <<< Dispense Status: $dispenseStatus for selection $receivedSelection (CommNo: $receivedCommNo)"
//                          )
//                          serialPortManager.writeSerialttyS1Ack()
//                          Log.d(TAG, "ttyS1 >>> Sent ACK for Dispense Status")
//
//                          if (dispenseStatus == 0x02) {
//                            itemsSuccessfullyDispensed++
//                            Log.d(
//                              TAG,
//                              "Item dispensed successfully! Total dispensed: $itemsSuccessfullyDispensed"
//                            )
//                          } else {
//                            Log.e(
//                              TAG,
//                              "Dispense failed for selection $receivedSelection. Status: $dispenseStatus"
//                            )
//                          }
//
//                          currentDispenseIndex++
//                          if (currentDispenseIndex < positionsToDispense.size) {
//                            delay(100)
//                            prepareTtyS1Command(positionsToDispense[currentDispenseIndex])
//                            progress = "waitingForPoll"
//                            expectedTtyS1Response = ""
//                            Log.d(TAG, "Preparing for next item. Index: $currentDispenseIndex")
//                          } else {
//                            Log.d(TAG, "All $dispenseQty items processed via ttyS1.")
//                            progress = "liftingDown"
//                            serialPortManager.writeSerialttyS2("# 1 1 1 -1 2")
//                          }
//                        } else {
//                          Log.w(TAG, "ttyS1 <<< Received dispense status with insufficient length.")
//                        }
//                      } else {
//                        Log.w(TAG, "ttyS1 <<< Unknown or unexpected response: $responseHex")
//                      }
//                    }
//                  }
//                }
//              } catch (ex: Exception) {
//                Log.e(TAG, "Error in ttyS1 reading coroutine: ${ex.message}")
//                if (!isProcessCompleted.get()) cleanupAndComplete(false)
//              }
//            }
//          }
//
//          serialPortManager.startReadingSerialttyS2 { data ->
//            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//              try {
//                stateMutex.withLock {
//                  if (isProcessCompleted.get()) return@withLock
//
//                  val response = data.joinToString(",") { "%02x".format(it) }
//                  Log.d(TAG, "ttyS2 <<< $response (Progress: $progress)")
//
//                  if (isDispenseActive) {
//                    when (response) {
//                      "26,31,0d,0a,32,0d,0a,33,0d,0a,31,0d,0a,37,0d,0a" -> {
//                        if (progress == "openingDoor") {
//                          serialPortManager.writeSerialttyS2("# 1 1 5 10 17")
//                          progress = "liftingUp"
//                          Log.d(TAG, "ttyS2 >>> Door opening command sent.")
//                        }
//                      }
//
//                      "26,31,0d,0a,32,0d,0a,35,0d,0a,31,0d,0a,39,0d,0a" -> {
//                        if (progress == "liftingUp") {
//                          currentLiftFloorTarget = when {
//                            position <= 10 -> 1400
//                            position <= 20 -> 1210
//                            position <= 30 -> 1010
//                            position <= 40 -> 790
//                            position <= 50 -> 580
//                            position <= 60 -> 360
//                            else -> 20
//                          }
//                          serialPortManager.writeSerialttyS2("# 1 1 1 $currentLiftFloorTarget ${currentLiftFloorTarget + 3}")
//                          Log.d(TAG, "ttyS2 >>> Lift moving up to floor: $currentLiftFloorTarget")
//                        }
//                      }
//
//                      "26,31,0d,0a,32,0d,0a,31,0d,0a,31,0d,0a,35,0d,0a" -> {
//                        when (progress) {
//                          "liftingUp" -> {
//                            Log.d(TAG, "Lift reached target for dispensing.")
//                            prepareTtyS1Command(positionsToDispense[currentDispenseIndex])
//                            progress = "waitingForPoll"
//                            expectedTtyS1Response = ""
//                          }
//
//                          "liftingDown" -> {
//                            Log.d(TAG, "Lift reached bottom after dispensing.")
//                            serialPortManager.writeSerialttyS2("# 1 1 6 10 18")
//                            progress = "closingDoor"
//                          }
//                        }
//                      }
//
//                      "26,31,0d,0a,32,0d,0a,36,0d,0a,31,0d,0a,31,30,0d,0a" -> {
//                        if (progress == "closingDoor") {
//                          serialPortManager.writeSerialttyS2("# 1 1 3 0 5")
//                          progress = "finalizing"
//                          Log.d(TAG, "ttyS2 >>> Door closed, locking rack.")
//                        }
//                      }
//
//                      "26,31,0d,0a,32,0d,0a,33,0d,0a,30,0d,0a,36,0d,0a" -> {
//                        if (progress == "finalizing") {
//                          delay(200)
//                          Log.d(
//                            TAG,
//                            "Process completed successfully! Items dispensed: $itemsSuccessfullyDispensed / ${positionsToDispense.size}"
//                          )
//                          isDispenseActive = false
//                          cleanupAndComplete(itemsSuccessfullyDispensed == positionsToDispense.size)
//                        }
//                      }
//
//                      else -> {
//                        Log.w(TAG, "ttyS2 <<< Unknown or unhandled response: $response")
//                      }
//                    }
//                  }
//                }
//              } catch (ex: Exception) {
//                Log.e(TAG, "Error in ttyS2 reading coroutine: ${ex.message}")
//                if (!isProcessCompleted.get()) cleanupAndComplete(false)
//              }
//            }
//          }
//
//          timeoutJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
//            delay(120000)
//            if (!isProcessCompleted.get()) {
//              Log.e(TAG, "Overall process timeout.")
//              cleanupAndComplete(false)
//            }
//          }
//
//        } catch (error: Exception) {
//          Log.e(TAG, "SerialPortError or initial setup error: ${error.message}")
//          cleanupAndComplete(false)
//        }
//      }
//    }
//}

package com.thanes.wardstock.services.machine

import android.util.Log
import com.thanes.wardstock.services.usb.SerialPortManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    private const val OVERALL_TIMEOUT_MS = 120_000L
    private const val MAX_TTY_RETRY = 3
    private const val COMMAND_TIMEOUT_MS = 3000L
    private const val DISPENSE_STATUS_TIMEOUT_MS = 15000L
    private const val VMC_ACK_RESPONSE = "fa,fb,42,00,43"
    private const val VMC_POLL_REQUEST = "fa,fb,41,00,40"
    private const val VMC_DISPENSE_STATUS_PREFIX = "fa,fb,04,04"

    fun getInstance(serialPortManager: SerialPortManager): Dispense {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: Dispense(serialPortManager).also { INSTANCE = it }
      }
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

  suspend fun sendToMachine(dispenseQty: Int, position: Int): Boolean {
    return try {
      val success = withTimeoutOrNull(OVERALL_TIMEOUT_MS) {
        suspendCoroutine<Boolean> { continuation ->
          var progress = "ready"
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
            timeout: Long = COMMAND_TIMEOUT_MS
          ) {
            commandTimeoutJob?.cancel()
            commandTimeoutJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
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
                }
              }
            }
          }

          try {
            val initialCmd = "# 1 1 3 1 6"
            serialPortManager.writeSerialttyS2(initialCmd)
            startCommandTimeout(initialCmd, "ttyS2")
            progress = "openingDoor"

            serialPortManager.startReadingSerialttyS1 { data ->
              CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
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
                          progress = "liftingDown"
                          val cmd = "# 1 1 1 -1 2"
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
                  when (response) {
                    "26,31,0d,0a,32,0d,0a,33,0d,0a,31,0d,0a,37,0d,0a" -> if (progress == "openingDoor") {
                      nextCommand = "# 1 1 5 10 17"; progress = "doorOpened"
                    }

                    "26,31,0d,0a,32,0d,0a,35,0d,0a,31,0d,0a,39,0d,0a" -> if (progress == "doorOpened") {
                      val floor = when {
                        position <= 10 -> 1400; position <= 20 -> 1210; position <= 30 -> 1010; position <= 40 -> 790; position <= 50 -> 580; position <= 60 -> 360; else -> 20
                      }
                      nextCommand = "# 1 1 1 $floor ${floor + 3}"
                      progress = "liftingUp"
                    }

                    "26,31,0d,0a,32,0d,0a,31,0d,0a,31,0d,0a,35,0d,0a" -> when (progress) {
                      "liftingUp" -> {
                        prepareTtyS1Command(positionsToDispense[currentDispenseIndex]); progress =
                          "waitingForPoll"
                      }

                      "liftingDown" -> {
                        nextCommand = "# 1 1 6 10 18"; progress = "closingDoor"
                      }
                    }

                    "26,31,0d,0a,32,0d,0a,36,0d,0a,31,0d,0a,31,30,0d,0a" -> if (progress == "closingDoor") {
                      nextCommand = "# 1 1 3 0 5"; progress = "finalizing"
                    }

                    "26,31,0d,0a,32,0d,0a,33,0d,0a,30,0d,0a,36,0d,0a" -> if (progress == "finalizing") {
                      delay(200)
                      cleanupAndComplete(itemsSuccessfullyDispensed == positionsToDispense.size)
                    }
                  }
                  if (nextCommand.isNotEmpty()) {
                    serialPortManager.writeSerialttyS2(nextCommand)
                    startCommandTimeout(nextCommand, "ttyS2")
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
}