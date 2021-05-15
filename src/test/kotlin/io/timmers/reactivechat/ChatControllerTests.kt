package io.timmers.reactivechat

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import java.time.Instant

val INSTANT = Instant.EPOCH

class ChatControllerTests {
    private lateinit var controller: ChatController

    @BeforeEach
    fun setUp() {
        controller = ChatController(FakeTimeService())
    }

    @Test
    fun testSubscribe() {
        val name1 = "Alice"
        val name2 = "Bob"
        val message = "Hello, world!"
        StepVerifier.create(controller.readMessages())
            .then {
                controller.sendMessage(NewChatMessage(name1, message)).block()
                controller.sendMessage(NewChatMessage(name2, message)).block()
            }
            .expectNext(ChatMessage(INSTANT, name1, message))
            .expectNext(ChatMessage(INSTANT, name2, message))
            .thenCancel()
            .verify()
    }
}

class FakeTimeService : TimeService {
    override fun now(): Instant {
        return INSTANT
    }
}