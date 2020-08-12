package com.retrobot.commands.utils

import com.retrobot.core.Bot
import com.retrobot.utility.PingCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class PingCommandTest {

    private val ping = PingCommand()

    private val bot = mockk<Bot>(relaxed = true)
    private val event = mockk<GuildMessageReceivedEvent>(relaxed = true)
    private val channel = mockk<TextChannel>(relaxed = true)
    private val message = mockk<Message>(relaxed = true)
    private val messageAction= mockk<MessageAction>(relaxed = true)

    @BeforeEach
    fun setup() {
        every { event.message } returns message
        every { message.contentRaw } returns "!ping"
        every { event.channel } returns channel
        every { channel.sendMessage(any<String>()) } returns messageAction
    }

    @Test
    @DisplayName("!ping - sends message to channel")
    fun runIsSuccessful() = runBlockingTest {
        ping.run(bot, event)
        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
    }
}