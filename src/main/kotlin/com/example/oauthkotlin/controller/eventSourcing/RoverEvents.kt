package com.example.oauthkotlin.controller.eventSourcing

import com.example.oauthkotlin.model.Issue
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(
    path=["api/v1/rover-events/issue"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)

class RoverEvents {
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

    private fun send(issue: Issue, event: String) {
        val producer = createProducer()
        producer.send(ProducerRecord(TOPIC, issue.id.toString(), event))
    }

    private fun createProducer(): KafkaProducer<String, String> {
        val props = Properties()

        props["bootstrap.servers"] = brokers
        props["client.dns.lookup"] = dns
        props["security.protocol"] = secproto
        props["sasl.jaas.config"] = jaasconfig
        props["sasl.mechanism"] = saslmechanism
        props["acks"] = acks
        props["key.serializer"] = org.apache.kafka.common.serialization.StringSerializer::class.java.canonicalName
        props["value.serializer"] = org.apache.kafka.common.serialization.StringSerializer::class.java.canonicalName
        return KafkaProducer<String, String>(props)
    }

    @PostMapping("/{id}/rover-notified-event")
    fun roverNotifiedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Number): Issue {
        assert(issue.id == id)
        send(issue, "rover-notified")
        return issue
    }

    @PostMapping("/{id}/rover-unassigned-event")
    fun roverUnassignedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Number): Issue {
        assert(issue.id == id)
        send(issue, "rover-unassigned")
        return issue
    }

    @PostMapping("/{id}/rover-accepted-event")
    fun roverAcceptedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Number): Issue {
        assert(issue.id == id)
        send(issue, "rover-accepted")
        return issue
    }

    @PostMapping("/{id}/rover-arrived-event")
    fun roverArrivedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Number): Issue {
        assert(issue.id == id)
        send(issue, "rover-arrived")
        return issue
    }

    @PostMapping("/{id}/issue-resolved-event")
    fun issueResolvedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Number): Issue {
        assert(issue.id == id)
        issue.status = "resolved"
        send(issue, "issue-resolved")
        return issue
    }
}