package com.example.oauthkotlin.controller.consumers

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.rest.lookups.v1.PhoneNumber
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
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
                            println(record.value())
                            messages.add(record.value())
                        }
                }
            }
        }.start()

    }

    private fun sendToTwilio (message: String ) {
        val accountSID = "123"
        val authToken = "234"
        Twilio.init(
            accountSID,
            authToken
        )
//        val message: Message = Message.creator(
//            PhoneNumber("+15558675309"),
//            PhoneNumber("+15017250604"),
//            "This is the ship that made the Kessel Run in fourteen parsecs?"
//        ).create()
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