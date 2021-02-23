package com.example.oauthkotlin.controller

import com.example.oauthkotlin.model.Message
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

import java.util.concurrent.Executors

import java.util.concurrent.ExecutorService








@RestController
@RequestMapping(
    path = ["api"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
) // For simplicity of this sample, allow all origins. Real applications should configure CORS for their use case.
class APIController {

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

    @GetMapping("/public/stream-sse-mvc")
    fun streamSseMvc(): SseEmitter? {
        val emitter = SseEmitter()
        val sseMvcExecutor = Executors.newSingleThreadExecutor()
        sseMvcExecutor.execute {
            try {
                var i = 0
                while (true) {
                    val event = SseEmitter.event()
                        .data("SSE MVC - " + LocalTime.now().toString())
                        .id(i.toString())
                        .name("sse event - mvc")
                    emitter.send(event)
                    Thread.sleep(1000)
                    i++
                }
            } catch (ex: Exception) {
                emitter.completeWithError(ex)
            }
        }
        return emitter
    }
}