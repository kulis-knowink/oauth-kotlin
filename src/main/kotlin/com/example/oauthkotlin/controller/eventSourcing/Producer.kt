package com.example.oauthkotlin.controller.eventSourcing

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.*

@Component
class Producer {

    @Value("\${kafka.bootstrap.servers}")
    lateinit var brokers: String

    @Value("\${kafka.client.dns.lookup}")
    lateinit var dns: String

    @Value("\${kafka.security.protocol}")
    lateinit var secproto: String

    @Value("\${kafka.sasl.jaas.config}")
    lateinit var jaasconfig: String

    @Value("\${kafka.sasl.mechanism}")
    lateinit var saslmechanism: String

    @Value("\${kafka.acks}")
    lateinit var acks: String

    val TOPIC = "issues"

    private fun createProducer(): KafkaProducer<String, String> {
        val props = Properties()

        props["bootstrap.servers"] = "pkc-ep9mm.us-east-2.aws.confluent.cloud:9092"
        props["client.dns.lookup"] = "use_all_dns_ips"
        props["security.protocol"] = "SASL_SSL"
        props["sasl.jaas.config"] = "org.apache.kafka.common.security.plain.PlainLoginModule   required username='WIP7HWXCLFECWGCP'   password='sSY4JZ6sLq2QorkY/6ZVHxZUjehKaJ6Z9dsg5vWn87VmJTEtRTQB2HsCBnO6JWOK';"
        props["sasl.mechanism"] = "PLAIN"
        props["acks"] = "all"
        props["key.serializer"] = org.apache.kafka.common.serialization.StringSerializer::class.java.canonicalName
        props["value.serializer"] = org.apache.kafka.common.serialization.StringSerializer::class.java.canonicalName
        return KafkaProducer<String, String>(props)
    }

    private var instance: KafkaProducer<String, String> = createProducer()

    fun send(id: Int, event: String) {
        instance.send(ProducerRecord(TOPIC, id.toString(), event))
    }
}