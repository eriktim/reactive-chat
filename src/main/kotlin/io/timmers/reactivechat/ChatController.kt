package io.timmers.reactivechat

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Instant

@Controller
class ChatController(private val timeService: TimeService) {
    private var sink: Sinks.Many<ChatMessage> = Sinks.many().multicast().onBackpressureBuffer()

    @MessageMapping("readMessages")
    fun readMessages(): Flux<ChatMessage> {
        return sink.asFlux()
    }

    @MessageMapping("sendMessage")
    fun sendMessage(message: NewChatMessage): Mono<Boolean> {
        val result = sink.tryEmitNext(ChatMessage(timeService.now(), message.name, message.message))
        return Mono.just(result.isSuccess)
    }
}

data class NewChatMessage(val name: String, val message: String)

data class ChatMessage(val timestamp: Instant, val name: String, val message: String)