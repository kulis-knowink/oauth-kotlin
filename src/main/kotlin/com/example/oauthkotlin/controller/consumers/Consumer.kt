package com.example.oauthkotlin.controller.consumers

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*

@Component
class Consumer {

    val messages = mutableListOf<String>()

    private var instance: KafkaConsumer<String, String > = createConsumer()

    fun consumeKafka(){
        Thread {
            var i = 0
            instance.subscribe(listOf("issues"))
            instance.use {
                while (true) {
                    instance
                        .poll(Duration.ofSeconds(1))
                        .iterator().forEach { record ->
                            contentBasedRouter(record.value())
                        }
                }
            }
        }.start()

    }

    fun contentBasedRouter(message: String) {
        when (message) {
            "issue-resolved", "issue-opened", "rover-arrived", "rover-notified", "rover-unassigned" -> {
                messages.add(message)
                sendToTwilio(message)
            }
            "rover-assigned"  -> println(message)
            "issue-prioritized"-> messages.add(message)

        }
    }

    private fun sendToTwilio (message: String ) {
        val accountSID = "ACa62659104c1748771f71a13c143dab8f"
        val authToken = "6733ba4b60e9be29674c13868883e5f7"
        val myPhoneNumber = "+15182558938"

        Twilio.init(
            accountSID,
            authToken
        )

        val toPhone = PhoneNumber("+16189230696")
        val fromPhone = PhoneNumber(myPhoneNumber)

        val message: Message = Message.creator(
            toPhone,
            fromPhone,
            message
        ).create()
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
        props["auto.offset.reset"] = "latest"
        return KafkaConsumer<String, String>(props)
    }


    init {
        consumeKafka()
    }

}