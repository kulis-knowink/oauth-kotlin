package com.example.oauthkotlin.controller.consumers

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Duration
import java.util.*

@Component
class Consumer {

    private val emitters = mutableListOf<SseEmitter>()
    private var consuming = false;
    private var toBeRemoved = mutableSetOf<Int>()

    fun registerEmitter(emitter: SseEmitter): Int {
        if(!consuming){
            consuming = true
            consume();
        }
        toBeRemoved.iterator().forEach { key ->
            emitters.removeAt(key)
        }


        emitters.add(emitter)
        return emitters.count()
    }

    fun unregisterEmitter(id: Int){
        emitters.removeAt(id)
    }

    fun consume(){
        try {
            Thread {
                var i = 0
                instance.subscribe(listOf("issues"))
                instance.use {
                    while (true) {
                        instance
                            .poll(Duration.ofSeconds(1))
                            .iterator().forEach { record ->
                                val event = SseEmitter.event()
                                    .data(record.value())
                                    .name("issues")
                                emitters.forEach { emitter ->

                                    println(emitter.toString())
                                    event.id(i.toString())
                                    i++
                                    try {
                                        emitter.send(event)
                                    } catch (exception: Exception) {
//                                        toBeRemoved.add(emitters.indexOf(emitter))
                                    }
                                }
                            }
                    }
                }
            }.start()
        } catch(exception: Exception) {
            consuming = false
        }
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

    private var instance: KafkaConsumer<String, String > = createConsumer()
}