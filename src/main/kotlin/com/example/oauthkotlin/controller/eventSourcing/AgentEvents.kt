package com.example.oauthkotlin.controller.eventSourcing

import com.example.oauthkotlin.model.Issue
import com.example.oauthkotlin.model.Message
import com.fasterxml.jackson.databind.ser.std.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Producer
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.errors.TopicExistsException
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestBody


@RestController
@RequestMapping(
    path=["api/v1/agent-events"],
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

    @PostMapping("/new-issue-event")
    fun newIssueEvent(): Issue {
        var producer = createProducer()
        var issue = Issue(id = id, status = "open")
        producer.send(ProducerRecord(TOPIC, issue.id.toString(), "issue-created"))
        id++
        return issue
    }

    @PostMapping("/issue-resolved-event")
    fun issueResolvedEvent(@RequestBody issue: Issue): Issue {
        var producer = createProducer()
        issue.status = "resolved"
        producer.send(ProducerRecord(TOPIC, issue.id.toString(), "issue-resolved"))
        return issue
    }

    @PostMapping("/issue-prioritized-event")
    fun prioritizedEvent(@RequestBody issue: Issue): Issue {
        var producer = createProducer()
        issue.priority = "urgent"
        producer.send(ProducerRecord(TOPIC, issue.id.toString(), "issue-prioritized"))
        return issue
    }

    @PostMapping("/rover-assigned-event")
    fun roverAssignedEvent(@RequestBody issue: Issue): Issue {
        val producer = createProducer()
        issue.assignedRover = "foobar"
        producer.send(ProducerRecord(TOPIC, issue.id.toString(), "rover-assigned"))
        return issue
    }

    @PostMapping("/caller-subscribed-event")
    fun callerSubscribedEvent(@RequestBody issue: Issue): Issue {
        val producer = createProducer()
        producer.send(ProducerRecord(TOPIC, issue.id.toString(), "caller-subscribed"))
        return issue
    }
}

