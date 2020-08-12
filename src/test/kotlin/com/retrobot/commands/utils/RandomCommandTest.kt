package com.retrobot.commands.utils

import com.retrobot.containsAny
import com.retrobot.core.Bot
import com.retrobot.core.Commands.Utils.Random.USAGE
import com.retrobot.core.util.Messages.generateDelimitedString
import com.retrobot.utility.RandomCommand
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
internal class RandomCommandTest {

    private val random = RandomCommand()

    private val bot = mockk<Bot>(relaxed = true)
    private val event = mockk<GuildMessageReceivedEvent>(relaxed = true)
    private val channel = mockk<TextChannel>(relaxed = true)
    private val message = mockk<Message>(relaxed = true)
    private val messageAction= mockk<MessageAction>(relaxed = true)

    private val messagesSent = slot<String>()

    @BeforeEach
    fun setup() {
        every { event.message } returns message
        every { event.channel } returns channel
        every { channel.sendMessage(capture(messagesSent)) } returns messageAction
    }

    @Test
    @DisplayName("!random - sends help info message")
    fun baseCommand_sendsCorrectMessage() = runBlockingTest {
        testingBaseCommand()
        random.run(bot, event)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.contains(USAGE, true))
    }

    @Test
    @DisplayName("!random number <number> - sends correct message to channel")
    fun numberCommand_sendsCorrectMessage() = runBlockingTest {
        val number = 3
        testingNumberCommand(number)
        random.run(bot, event)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny((0..number).toList().map { it.toString() }, true))
    }

    @Test
    @DisplayName("!random number <range> - send correct message to channel")
    fun numberRangeCommand_sendsCorrectMessage() = runBlockingTest {
        val from = 12
        val to = 15
        testingNumberRangeCommand(from, to)
        random.run(bot, event)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny((from..to).toList().map { it.toString() }, true))
    }

    @Test
    @DisplayName("!random list <items> - send correct message to channel")
    fun listCommand_sendsCorrectMessage() = runBlockingTest {
        val items = listOf("Red", "Blue", "Green")
        testingListCommand(items)
        random.run(bot, event)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(items, true))
    }

    @Test
    @DisplayName("!random day - send correct message to channel")
    fun dayCommand_sendsCorrectMessage() = runBlockingTest {
        val daysOfTheWeek = RandomCommand.DayOfTheWeek.values().map { it.value }
        testingDayCommand()
        random.run(bot, event)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(daysOfTheWeek, true))
    }

    @Test
    @DisplayName("!random weekday - send correct message to channel")
    fun weekdayCommand_sendsCorrectMessage() = runBlockingTest {
        val weekdays = RandomCommand.DayOfTheWeek.values().toList().subList(1, 6).map { it.value }
        testingWeekdayCommand()
        random.run(bot, event)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(weekdays, true))
    }

    @Test
    @DisplayName("!random month - send correct message to channel")
    fun monthCommand_sendsCorrectMessage() = runBlockingTest {
        val months = RandomCommand.Month.values().map { it.value }
        testingMonthCommand()
        random.run(bot, event)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(months, true))
    }

    private fun testingBaseCommand() {
        every { message.contentRaw } returns "!random"
    }

    private fun testingNumberCommand(number: Int) {
        every { message.contentRaw } returns "!random number $number"
    }

    private fun testingNumberRangeCommand(from: Int, to: Int) {
        every { message.contentRaw } returns "!random number $from-$to"
    }

    private fun testingListCommand(list: List<String>) {
        every { message.contentRaw } returns "!random list ${generateDelimitedString(list)}"
    }

    private fun testingDayCommand() {
        every { message.contentRaw } returns "!random day"
    }

    private fun testingWeekdayCommand() {
        every { message.contentRaw } returns "!random weekday"
    }

    private fun testingMonthCommand() {
        every { message.contentRaw } returns "!random month"
    }
}