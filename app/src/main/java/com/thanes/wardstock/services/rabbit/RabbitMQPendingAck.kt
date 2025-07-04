package com.thanes.wardstock.services.rabbit

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Envelope

object RabbitMQPendingAck {
  var channel: Channel? = null
  var envelope: Envelope? = null

  fun reset() {
    channel = null
    envelope = null
  }
}
