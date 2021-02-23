package com.example.oauthkotlin.controller

import com.example.oauthkotlin.model.Message
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping

import org.springframework.web.bind.annotation.CrossOrigin

import org.springframework.web.bind.annotation.RequestMapping

import org.springframework.web.bind.annotation.RestController
import java.time.LocalTime

import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder
import java.lang.Exception
import java.time.Duration
import java.util.*

import java.util.concurrent.Executors

import java.util.concurrent.ExecutorService








@RestController
@RequestMapping(
    path = ["api"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
) // For simplicity of this sample, allow all origins. Real applications should configure CORS for their use case.
class APIController {

    @Value("\${kafka.bootstrap.servers}")
    val brokers = ""

    @Value("\${kafka.client.dns.lookup}")
    val dns = ""

    @Value("\${kafka.security.protocol}")
    val secproto = ""

    @Value("\${kafka.sasl.jaas.config}")
    val jaasconfig = ""

    @Value("\${kafka.sasl.mechanism}")
    val saslmechanism = ""

    @Value("\${kafka.acks}")
    val acks = "all"

    val TOPIC = "issues"


    private fun createConsumer(brokers: String): Consumer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["client.dns.lookup"] = dns
        props["security.protocol"] = secproto
        props["sasl.jaas.config"] = jaasconfig
        props["sasl.mechanism"] = saslmechanism
        props["group.id"] = "issue-processor"
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = StringDeserializer::class.java
        props["auto.offset.reset"] = "earliest"
        return KafkaConsumer<String, String>(props)
    }

    @GetMapping(value = ["/public"])
    fun publicEndpoint(): Message {
        return Message("All good. You DO NOT need to be authenticated to call /api/public.")
    }

    @GetMapping(value = ["/private"])
    fun privateEndpoint(): Message {
        return Message("All good. You can see this because you are Authenticated.")
    }

    @GetMapping(value = ["/private-scoped"])
    fun privateScopedEndpoint(): Message {
        return Message("All good. You can see this because you are Authenticated with a Token granted the 'read:messages' scope")
    }



    @GetMapping("/public/stream-sse-mvc", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamSseMvc(): SseEmitter? {

        val emitter = SseEmitter(0)
        val sseMvcExecutor = Executors.newSingleThreadExecutor()
        sseMvcExecutor.execute {
            try {

                val consumer = createConsumer(brokers)
                consumer.subscribe(listOf(TOPIC))

                var i = 0
                while(true) {
                    val records = consumer.poll(Duration.ofSeconds(1)).records("issues")
                    records.iterator().forEach {
                        val issue = it.value()
                        val key = it.key()
                        if(key.toInt() % 2 > 0){
                            println(key)
                            println(issue)
                            val event = SseEmitter.event()
                                .id(i.toString())
                                .data(issue)
                                .name("issues")
                            emitter.send(event)
                            i++
                        }


                    }
                }

            } catch (ex: Exception) {
                emitter.completeWithError(ex)
            }
        }
        return emitter
    }
}