package com.retrobot.commands.utils

import com.retrobot.core.Bot
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.domain.command.Command
import com.retrobot.core.domain.command.CommandHandler
import com.retrobot.utility.HelpCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
internal class HelpCommandTest {

    private val bot = mockk<Bot>(relaxed = true)
    private val event = mockk<GuildMessageReceivedEvent>(relaxed = true)
    private val channel = mockk<TextChannel>(relaxed = true)
    private val messageAction = mockk<MessageAction>(relaxed = true)
    private val command = mockk<Command>(relaxed = true)
    private val commandHandler = mockk<CommandHandler>(relaxed = true)
    private val guildSettings = mockk<GuildSettings>(relaxed = true)

    private val messagesSent = slot<Message>()

    private val helpInfo = "This is a help info test"

    private val help = HelpCommand()

    @BeforeEach
    fun setup() {
        every { bot.commandHandler } returns commandHandler
        every { commandHandler.getCommandsByCategory(any()) } returns listOf(command)
        every { command.usage } returns helpInfo
        every { event.channel } returns channel
        every { channel.sendMessage(capture(messagesSent)) } returns messageAction
    }

    @Test
    @DisplayName("!help - sends message to channel")
    fun runIsSuccessful() = runBlockingTest {
        help.run(bot, event, "", guildSettings)
        verify(exactly = 1) { messageAction.queue() }
    }
}