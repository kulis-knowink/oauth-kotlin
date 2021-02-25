package com.example.oauthkotlin.controller.eventSourcing

import com.example.oauthkotlin.model.Issue
import org.springframework.http.MediaType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*



@RestController
@RequestMapping(
    path=["api/v1/agent-events/issue"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class AgentEvents(val producer: Producer) {

    var id: Int = 1

    @PostMapping("/new-issue-event")
    fun newIssueEvent(): Issue {
        val issue = Issue(id = id, status = "open")
        producer.send(id, "issue-opened")
        id++
        return issue
    }

    @PostMapping("/{id}/issue-resolved-event")
    fun issueResolvedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Int): Issue {
        assert(issue.id == id)
        issue.status = "resolved"
        producer.send(id, "issue-resolved")
        return issue
    }

    @PostMapping("/{id}/issue-prioritized-event")
    fun prioritizedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Int): Issue {
        assert(issue.id == id)
        issue.priority = "urgent"
        producer.send(id, "issue-prioritized")
        return issue
    }

    @PostMapping("/{id}/rover-assigned-event")
    fun roverAssignedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Int): Issue {
        assert(issue.id == id)
        issue.assignedRover = "foobar"
        producer.send(id, "rover-assigned")
        return issue
    }

    @PostMapping("/{id}/caller-subscribed-event")
    fun callerSubscribedEvent(@RequestBody issue: Issue, @PathVariable("id") id: Int): Issue {
        assert(issue.id == id)
        producer.send(id, "caller-subscribed")
        return issue
    }
}

