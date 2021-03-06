package com.example.oauthkotlin.controller.eventSourcing

import com.example.oauthkotlin.model.Issue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

internal class RoverEventsTest {
    val producer: Producer = mock(Producer::class.java)
    val subject: RoverEvents = RoverEvents(producer)

    @Test
    fun roverNotifiedEventTest() {
        subject.roverNotifiedEvent(Issue(
            1,
            "foo",
            "bar",
            "baz",
            "quux",
            "quem",
            "high"
        ),
            1)

        verify(producer, times(1)).send(1, "rover-notified")
    }

}