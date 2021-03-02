package com.example.oauthkotlin.controller.consumers

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import com.eatthepath.pushy.apns.ApnsClientBuilder

import com.eatthepath.pushy.apns.ApnsClient
import com.eatthepath.pushy.apns.auth.ApnsSigningKey
import java.io.File
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification

import com.eatthepath.pushy.apns.util.TokenUtil

import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder

import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder
import com.eatthepath.pushy.apns.PushNotificationResponse

import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture
import org.springframework.core.io.ClassPathResource
import java.time.Instant
import java.util.concurrent.ExecutionException
import java.util.concurrent.CompletableFuture


@Component
class Consumer() {

    val messages = mutableListOf<String>()

    private var instance: KafkaConsumer<String, String > = createConsumer()


    fun consumeKafka(){
        Thread {
            instance.subscribe(listOf("issues"))
            instance.use {
                while (true) {
                    instance
                        .poll(Duration.ofSeconds(3))
                        .iterator().forEach { record ->
                            println("Found $record")
                            contentBasedRouter(record.value())
                        }
                }
            }
        }.start()

    }

    fun contentBasedRouter(message: String) {
        /**
         * Caller based issue flow
         *
         * Caller calls
         * Agent opens ticket -> no notications
         * Agent reads kb -> no event sourced in poc
         * Agent resolves issue -> events to but filtered so no one gets messages (not in poc)
         * Agent prioritizes issue if not resolved -> evented  no messages
         * ::: Agent assigns rover -> evented message sent to rover
         * skipping caller registration for texting
         * ::: rover views notification -> agent notified
         * ::: rover rejects ticket -> agent notified
         * ::: rover accepts ticket -> evented twillio message
         * ::: rover resolves issue -> agent notified, caller notified
         */

        when(message) {
            "rover-assigned" -> sendToAPN(message)
            "rover-notified" -> sendToSSE(message)
            "rover-unassigned" -> sendToSSE(message)
            "rover-accepted" -> sendToTwilio(message)
            "issue-resolved" -> {
                sendToSSE(message)
                sendToTwilio(message)
            }
            else -> println("Event:: $message")
        }
    }

    private fun sendToAPN(message: String){
        println("REAL send to APN: $message")

        val apnsClient = ApnsClientBuilder()
            .setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
            .setSigningKey(
                ApnsSigningKey.loadFromPkcs8File(
                    ClassPathResource("AuthKey_NF5S6P93TQ.p8").file,
                    "E787C92P66", "NF5S6P93TQ"
                )
            )
            .build()

        var pushNotification: SimpleApnsPushNotification = getPushNotificationStuff(message)

        val sendNotificationFuture = apnsClient.sendNotification(pushNotification)

        try {
            val pushNotificationResponse = sendNotificationFuture.get()
            if (pushNotificationResponse.isAccepted) {
                println("Push notification accepted by APNs gateway.")
            } else {
                println(
                    "Notification rejected by the APNs gateway: " +
                            pushNotificationResponse.rejectionReason
                )
                pushNotificationResponse.tokenInvalidationTimestamp.ifPresent { timestamp: Instant ->
                    println(
                        "\t…and the token is invalid as of $timestamp"
                    )
                }
            }
        } catch (e: ExecutionException) {
            System.err.println("Failed to send push notification.")
            e.printStackTrace()
        }

        sendNotificationFuture.whenComplete { response: Any?, _: Any ->
            if (response != null) {
                // Handle the push notification response as before from here.
            } else {
                // Something went wrong when trying to send the notification to the
                // APNs server. Note that this is distinct from a rejection from
                // the server, and indicates that something went wrong when actually
                // sending the notification or waiting for a reply.
                println("oh noes!")
            }
        }
    }
    private fun sendToSSE(message: String){
        println("Sending to SSE $message")
        messages.add(message)
    }

    private fun getPushNotificationStuff(message: String): SimpleApnsPushNotification {
        val payloadBuilder: ApnsPayloadBuilder = SimpleApnsPayloadBuilder()
        payloadBuilder.setAlertBody(message)
        val payload = payloadBuilder.build()
        val token = TokenUtil.sanitizeTokenString("f08ae4bc009b4c2632406b569d0be1c2e0b55b698d78311a71dbe54840b95cc2")
        return SimpleApnsPushNotification(token, "test.TestAuthApp", payload)
    }

    private fun sendToTwilio (message: String ) {
        println("Sending to twilio $message")
        val accountSID = "ACa62659104c1748771f71a13c143dab8f"
        val authToken = "d022e9d5dfc64d86ca1cc4dd03321321"
//        val myPhoneNumber = "+15182558938"

        Twilio.init(
            accountSID,
            authToken
        )
//        val toPhone = PhoneNumber("+16189230696")
//        val fromPhone = PhoneNumber(myPhoneNumber)
    }

    private fun createConsumer(): KafkaConsumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = "pkc-ep9mm.us-east-2.aws.confluent.cloud:9092"
        props["client.dns.lookup"] = "use_all_dns_ips"
        props["security.protocol"] = "SASL_SSL"
        props["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule   required username='WIP7HWXCLFECWGCP'   password='sSY4JZ6sLq2QorkY/6ZVHxZUjehKaJ6Z9dsg5vWn87VmJTEtRTQB2HsCBnO6JWOK';"
        props["sasl.mechanism"] = "PLAIN"
        props["acks"] = "all"
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = StringDeserializer::class.java
        props["group.id"] = "issue-processor"
        props["auto.offset.reset"] = "earliest"
        return KafkaConsumer<String, String>(props)
    }


    init {
        consumeKafka()
    }

}