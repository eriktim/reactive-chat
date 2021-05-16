package io.timmers.reactivechat

import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Controller
class ChatController(private val timeService: TimeService) {
    private var sink: Sinks.Many<ChatMessage> = Sinks.many().multicast().onBackpressureBuffer()

    @MessageMapping("readMessages")
    fun readMessages(): Flux<ChatMessage> {
        logger.info("Received subscription")
        return sink.asFlux()
    }

    @MessageMapping("sendMessage")
    fun sendMessage(message: NewChatMessage): Mono<Void> {
        val result = sink.tryEmitNext(ChatMessage(timeService.now(), message.name, message.message))
        if (result.isSuccess) {
            logger.info("Received message: {}", message)
        } else {
            logger.error("Cannot process message: {}", message)
        }
        return Mono.empty()
    }
}

data class NewChatMessage(val name: String, val message: String)

data class ChatMessage(val timestamp: Instant, val name: String, val message: String)