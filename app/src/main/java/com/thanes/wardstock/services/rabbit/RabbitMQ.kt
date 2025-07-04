package com.thanes.wardstock.services.rabbit

import android.util.Log
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.thanes.wardstock.utils.RabbitHost
import com.thanes.wardstock.utils.RabbitPass
import com.thanes.wardstock.utils.RabbitPort
import com.thanes.wardstock.utils.RabbitUser

class RabbitMQService private constructor() {

  private var connection: Connection? = null
  private var channel: Channel? = null
  private var isListening = false
//  private var ackMessage: Envelope? = null
//  private var messageBody = ""

  private val tag = "RabbitMq"

  companion object {
    @Volatile
    private var instance: RabbitMQService? = null

    fun getInstance(): RabbitMQService {
      return instance ?: synchronized(this) {
        instance ?: RabbitMQService().also { instance = it }
      }
    }
  }

  fun connect() {
    if (connection?.isOpen == true && channel?.isOpen == true) {
      Log.d(tag, "Already connected.")
      return
    }

    synchronized(this) {
      if (connection?.isOpen == true && channel?.isOpen == true) {
        Log.d(tag, "Already connected (inside sync).")
        return
      }

      val factory = ConnectionFactory().apply {
        host = RabbitHost
        port = RabbitPort
        username = RabbitUser
        password = RabbitPass
        virtualHost = "/"
        isAutomaticRecoveryEnabled = true
      }

      var attempt = 0
      val maxRetries = 3

      while (attempt < maxRetries) {
        try {
          attempt++
          connection = factory.newConnection()
          channel = connection!!.createChannel()
          channel!!.basicQos(1)

          Log.d(tag, "New connection established on attempt $attempt.")
          break
        } catch (e: Exception) {
          Log.e(tag, "Failed to connect on attempt $attempt: ${e.message}", e)
          if (attempt >= maxRetries) {
            Log.e(tag, "All connection attempts failed.")
          } else {
            Thread.sleep(2000)
          }
        }
      }
    }
  }

  @Synchronized
  fun listenToQueue(
    queueName: String,
    onMessageReceived: (
      consumerTag: String?,
      envelope: Envelope,
      properties: AMQP.BasicProperties?,
      body: ByteArray,
      channel: Channel?
    ) -> Unit
  ) {
    if (isListening) {
      Log.d(tag, "Already listening to queue.")
      return
    }

    if (channel == null || !channel!!.isOpen) {
      Log.d(tag, "Channel is not open. Call connect() first.")
      return
    }

    try {
      channel!!.queueDeclare(queueName, true, false, false, null)

      val consumer = object : DefaultConsumer(channel) {
        override fun handleDelivery(
          consumerTag: String?,
          envelope: Envelope,
          properties: AMQP.BasicProperties?,
          body: ByteArray
        ) {
          onMessageReceived(consumerTag, envelope, properties, body, channel)
        }
      }

      channel!!.basicConsume(queueName, false, consumer)
      isListening = true
      Log.d(tag, "Started listening to $queueName")
    } catch (e: Exception) {
      Log.e(tag, "Failed to listening: ${e.message}", e)
    }
  }

  fun disconnect() {
    try {
      channel?.close()
      connection?.close()
    } catch (e: Exception) {
      Log.d(tag, "Error while disconnect ${e.message.toString()}")
    } finally {
      channel = null
      connection = null
      isListening = false
    }
  }
}
