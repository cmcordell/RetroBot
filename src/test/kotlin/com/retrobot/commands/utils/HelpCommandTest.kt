package com.retrobot.commands.utils

import com.retrobot.core.Bot
import com.retrobot.core.Commands.Utils.Help.TITLE
import com.retrobot.core.domain.command.Command
import com.retrobot.utility.HelpCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class HelpCommandTest {
    private val help = HelpCommand()

    private val bot = mockk<Bot>(relaxed = true)
    private val event = mockk<GuildMessageReceivedEvent>(relaxed = true)
    private val channel = mockk<TextChannel>(relaxed = true)
    private val message = mockk<Message>(relaxed = true)
    private val messageAction = mockk<MessageAction>(relaxed = true)
    private val command = mockk<Command>(relaxed = true)

    private val messagesSent = slot<Message>()

    private val commands = listOf(command)
    private val helpInfo = "This is a help info test"

    @BeforeEach
    fun setup() {
        every { bot.commands } returns commands
        every { command.usage } returns helpInfo
        every { event.message } returns message
        every { message.contentRaw } returns "!help"
        every { event.channel } returns channel
        every { channel.sendMessage(capture(messagesSent)) } returns messageAction
    }

    @Test
    @DisplayName("!help - sends message to channel")
    fun runIsSuccessful() = runBlockingTest {
        help.run(bot, event)
        verify(exactly = 1) { channel.sendMessage(any<MessageEmbed>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.embeds.size == 1)
        assert(messagesSent.captured.embeds[0].title!!.contains(TITLE, true))
        assert(messagesSent.captured.embeds[0].description!!.contains(helpInfo, true))
    }
}