package io.timmers.reactivechat

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.*
import java.time.Instant

@Controller
class ChatController(private val timeService: TimeService) {
    private lateinit var processor: FluxProcessor<ChatMessage, ChatMessage>
    private lateinit var sink: FluxSink<ChatMessage>

    init {
        processor = DirectProcessor.create<ChatMessage?>().serialize();
        sink = processor.sink();
    }

    @MessageMapping("readMessages")
    fun readMessages(): Flux<ChatMessage> {
        return processor.map { x -> x }
    }

    @MessageMapping("sendMessage")
    fun sendMessage(message: NewChatMessage): Mono<Void> {
        sink.next(ChatMessage(timeService.now(), message.name, message.message))
        return Mono.empty()
    }
}

data class NewChatMessage(val name: String, val message: String)

data class ChatMessage(val timestamp: Instant, val name: String, val message: String)