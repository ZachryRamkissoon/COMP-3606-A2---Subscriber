package com.example.maplab

import android.content.Context
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class Subscriber(context: Context, updater: UpdateLocation) {
    private val update: UpdateLocation = updater
    private val client: Mqtt5AsyncClient = MqttClient.builder()
        .useMqttVersion5()
        .identifier("816000000")
        .serverHost("broker.sundaebytestt.com")
        .serverPort(1883)
        .buildAsync()

    private val dbHelper = DatabaseHelper(context, null)

    init {
        subscribe()
    }

    private fun subscribe() {
        client.connect().whenComplete { _, throwable ->
            if (throwable != null) {
                println("Failed to connect to broker: ${throwable.message}")
            } else {
                println("Connected to broker!")
                client.subscribeWith()
                    .topicFilter("assignment/location")
                    .callback { publish: Mqtt5Publish ->
                        val message = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
                        val json = JSONObject(message)
                        dbHelper.createLocation(json.getDouble("latitude"), json.getDouble("longitude"), json.getString("id"), json.getInt("timestamp"), json.getDouble("speed"))
                        update.onNewLocationReceived(json.getString("id"))
                    }
                    .send()
                    .whenComplete { _, subThrowable ->
                        if (subThrowable != null) {
                            println("Failed to subscribe: ${subThrowable.message}")
                        } else {
                            println("Subscribed successfully to: assignment/location")
                        }
                    }
            }
        }
    }
}