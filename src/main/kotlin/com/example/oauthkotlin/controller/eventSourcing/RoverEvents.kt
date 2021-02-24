package com.example.oauthkotlin.controller.eventSourcing

import com.example.oauthkotlin.model.Issue
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    private lateinit var producer: com.example.oauthkotlin.controller.eventSourcing.Producer

    @PostMapping("/{id}/rover-notified-event")
    fun roverNotifiedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Int): Issue {
        assert(issue.id == id)
        producer.send(id, "rover-notified")
        return issue
    }

    @PostMapping("/{id}/rover-unassigned-event")
    fun roverUnassignedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Int): Issue {
        assert(issue.id == id)
        producer.send(id, "rover-unassigned")
        return issue
    }

    @PostMapping("/{id}/rover-accepted-event")
    fun roverAcceptedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Int): Issue {
        assert(issue.id == id)
        producer.send(id, "rover-accepted")
        return issue
    }

    @PostMapping("/{id}/rover-arrived-event")
    fun roverArrivedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Int): Issue {
        assert(issue.id == id)
        producer.send(id, "rover-arrived")
        return issue
    }

    @PostMapping("/{id}/issue-resolved-event")
    fun issueResolvedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Int): Issue {
        assert(issue.id == id)
        issue.status = "resolved"
        producer.send(id, "issue-resolved")
        return issue
    }
}