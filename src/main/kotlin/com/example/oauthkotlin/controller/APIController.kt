package com.example.oauthkotlin.controller

import com.example.oauthkotlin.controller.consumers.Consumer
import com.example.oauthkotlin.model.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*
import java.util.concurrent.Executors
import javax.ws.rs.sse.Sse


@RestController
@RequestMapping(
    path = ["api"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
) // For simplicity of this sample, allow all origins. Real applications should configure CORS for their use case.
class APIController {



    @Autowired
    private lateinit var consumer: Consumer


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


    @GetMapping("/v1/public/sse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamSseMvc(): SseEmitter? {

        val emitter = SseEmitter(0)
        Thread {
            try {
                var offset = 0
                while(true) {
                    Thread.sleep(1000)
                    val event = SseEmitter.event()
                    val nextBatch = consumer.messages.slice(IntRange(offset, consumer.messages.count() - 1))
                    println("count: " + nextBatch.count() + " offset: " + offset.toString())
                    offset = offset + nextBatch.count()
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