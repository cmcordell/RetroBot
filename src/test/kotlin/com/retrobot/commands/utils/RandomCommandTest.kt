package com.retrobot.commands.utils

import com.retrobot.containsAny
import com.retrobot.core.Bot
import com.retrobot.core.domain.GuildSettings
import com.retrobot.core.util.Messages.generateDelimitedString
import com.retrobot.utility.RandomCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.requests.restaction.MessageAction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class RandomCommandTest {

    private val random = RandomCommand()

    private val bot = mockk<Bot>(relaxed = true)
    private val event = mockk<GuildMessageReceivedEvent>(relaxed = true)
    private val channel = mockk<TextChannel>(relaxed = true)
    private val messageAction = mockk<MessageAction>(relaxed = true)
    private val guildSettings = mockk<GuildSettings>(relaxed = true)

    private val messagesSent = slot<String>()

    @BeforeEach
    fun setup() {
        every { event.channel } returns channel
        every { channel.sendMessage(capture(messagesSent)) } returns messageAction
    }

    @Test
    @DisplayName("!random - sends help info message")
    fun baseCommand_sendsCorrectMessage() = runBlockingTest {
        random.run(bot, event, "", guildSettings)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
    }

    @Test
    @DisplayName("!random number <number> - sends correct message to channel")
    fun numberCommand_sendsCorrectMessage() = runBlockingTest {
        val number = 3
        random.run(bot, event, "$number", guildSettings)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny((0..number).toList().map { it.toString() }, true))
    }

    @Test
    @DisplayName("!random number <range> - send correct message to channel")
    fun numberRangeCommand_sendsCorrectMessage() = runBlockingTest {
        val from = 12
        val to = 15
        random.run(bot, event, "$from-$to", guildSettings)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny((from..to).toList().map { it.toString() }, true))
    }

    @Test
    @DisplayName("!random list <items> - send correct message to channel")
    fun listCommand_sendsCorrectMessage() = runBlockingTest {
        val items = listOf("Red", "Blue", "Green")
        random.run(bot, event, generateDelimitedString(items, ","), guildSettings)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(items, true))
    }

    @Test
    @DisplayName("!random day - send correct message to channel")
    fun dayCommand_sendsCorrectMessage() = runBlockingTest {
        val daysOfTheWeek = RandomCommand.DayOfTheWeek.values().map { it.value }
        random.run(bot, event, "day", guildSettings)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(daysOfTheWeek, true))
    }

    @Test
    @DisplayName("!random weekday - send correct message to channel")
    fun weekdayCommand_sendsCorrectMessage() = runBlockingTest {
        val weekdays = RandomCommand.DayOfTheWeek.values().toList().subList(1, 6).map { it.value }
        random.run(bot, event, "weekday", guildSettings)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(weekdays, true))
    }

    @Test
    @DisplayName("!random month - send correct message to channel")
    fun monthCommand_sendsCorrectMessage() = runBlockingTest {
        val months = RandomCommand.Month.values().map { it.value }
        random.run(bot, event, "month", guildSettings)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(months, true))
    }
}