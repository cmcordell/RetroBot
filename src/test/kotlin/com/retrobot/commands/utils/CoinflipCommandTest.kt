package com.retrobot.commands.utils

import com.retrobot.containsAny
import com.retrobot.containsEvery
import com.retrobot.core.Bot
import com.retrobot.utility.command.CoinflipCommand
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
internal class CoinflipCommandTest {

    private val coinflip = CoinflipCommand()

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
    @DisplayName("!coinflip - sends correct message to channel")
    fun baseCommand_sendsCorrectMessage() = runBlockingTest {
        testingBaseCommand()
        coinflip.run(bot, event)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(listOf("heads", "tails"), true))
    }

    @Test
    @DisplayName("!coinflip <prompt> - send correct message to channel")
    fun actionCommand_sendsCorrectMessage() = runBlockingTest {
        val prompt = "go for a run"
        testingActionCommand(prompt)
        coinflip.run(bot, event)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(listOf("heads", "tails"), true)
                && messagesSent.captured.contains(prompt, true))
    }

    @Test
    @DisplayName("!coinflip heads <prompt> tails <prompt> - send correct message to channel")
    fun decisionCommand_sendsCorrectMessage() = runBlockingTest {
        val headsPrompt = "east"
        val tailsPrompt = "west"
        testingDecisionCommand(headsPrompt, tailsPrompt)
        coinflip.run(bot, event)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsEvery(listOf("heads", headsPrompt), true)
                || messagesSent.captured.containsEvery(listOf("tails", tailsPrompt), true))
    }

    @Test
    @DisplayName("!coinflip heads tails - send correct message to channel")
    fun decisionCommand_incorrectFormat_sendsCorrectMessage() = runBlockingTest {
        testingDecisionCommand("", "")
        coinflip.run(bot, event)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(listOf("heads", "tails"), true))
    }

    private fun testingBaseCommand() {
        every { message.contentRaw } returns "!coinflip"
    }

    private fun testingActionCommand(prompt: String) {
        every { message.contentRaw } returns "!coinflip $prompt"
    }

    private fun testingDecisionCommand(headsPrompt: String, tailsPrompt: String) {
        every { message.contentRaw } returns "!coinflip heads $headsPrompt tails $tailsPrompt"
    }
}