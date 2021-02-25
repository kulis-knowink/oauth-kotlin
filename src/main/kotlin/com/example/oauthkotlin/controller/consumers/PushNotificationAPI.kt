package com.example.oauthkotlin.controller.consumers

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter


@RestController
@RequestMapping(
    path=["api/"],
    produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
)
class PushNotificationAPI(val consumer: Consumer) {

    @GetMapping("/v1/public/sse")
    fun streamSseMvc(): SseEmitter? {

        val emitter = SseEmitter(0)
        Thread {
            try {
                var offset = 0
                while(true) {
                    Thread.sleep(6000)
                    val event = SseEmitter.event()
                    val nextBatch = consumer.messages.slice(IntRange(offset, consumer.messages.count() - 1))
                    offset += nextBatch.count()
                    nextBatch.forEach { next ->
                        event.data(next)
                        event.name("issues")
                        emitter.send(event)
                    }
                }

            } catch (exception: Exception){
                emitter.completeWithError(exception)
            }
        }.start()


        return emitter
    }
}