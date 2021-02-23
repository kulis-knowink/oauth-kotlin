package com.example.oauthkotlin.controller.eventSourcing

import com.example.oauthkotlin.model.Issue
import com.example.oauthkotlin.model.Message
import com.fasterxml.jackson.databind.ser.std.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Producer
import org.springframework.http.MediaType
import java.util.*
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.errors.TopicExistsException
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import javax.ws.rs.Path


@RestController
@RequestMapping(
    path=["api/v1/issues"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class AgentEvents {

    var id = 1

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

    private fun send(issue: Issue, event: String) {
        val producer = createProducer()
        producer.send(ProducerRecord(TOPIC, issue.id.toString(), event))
    }

    @PostMapping("/new-issue-event")
    fun newIssueEvent(): Issue {
        val issue = Issue(id = id, status = "open")
        send(issue, "issue-opened")
        id++
        return issue
    }

    @PostMapping("/{id}/issue-resolved-event")
    fun issueResolvedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Number): Issue {
        assert(issue.id == id)
        issue.status = "resolved"
        send(issue, "issue-resolved")
        return issue
    }

    @PostMapping("/{id}/issue-prioritized-event")
    fun prioritizedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Number): Issue {
        assert(issue.id == id)
        issue.priority = "urgent"
        send(issue, "issue-prioritized")
        return issue
    }

    @PostMapping("/{id}/rover-assigned-event")
    fun roverAssignedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Number): Issue {
        assert(issue.id == id)
        issue.assignedRover = "foobar"
        send(issue, "rover-assigned")
        return issue
    }

    @PostMapping("/{id}/caller-subscribed-event")
    fun callerSubscribedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Number): Issue {
        assert(issue.id == id)
        send(issue, "caller-subscribed")
        return issue
    }
}

