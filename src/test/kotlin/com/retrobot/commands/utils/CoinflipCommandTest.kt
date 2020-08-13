package com.retrobot.commands.utils

import com.retrobot.containsAny
import com.retrobot.containsEvery
import com.retrobot.core.Bot
import com.retrobot.core.domain.GuildSettings
import com.retrobot.utility.CoinflipCommand
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
internal class CoinflipCommandTest {

    private val bot = mockk<Bot>(relaxed = true)
    private val event = mockk<GuildMessageReceivedEvent>(relaxed = true)
    private val channel = mockk<TextChannel>(relaxed = true)
    private val guildSettings = mockk<GuildSettings>(relaxed = true)
    private val messageAction = mockk<MessageAction>(relaxed = true)

    private val messagesSent = slot<String>()

    private val HEADS = "Heads"
    private val TAILS = "Tails"
    private val POSSIPLE_FLIP_RESULTS = listOf(HEADS, TAILS)

    private val coinflip = CoinflipCommand()


    @BeforeEach
    fun setup() {
        every { event.channel } returns channel
        every { channel.sendMessage(capture(messagesSent)) } returns messageAction
    }

    @Test
    @DisplayName("!coinflip - sends correct message to channel")
    fun baseCommand_sendsCorrectMessage() = runBlockingTest {
        coinflip.run(bot, event, "", guildSettings)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(POSSIPLE_FLIP_RESULTS, true))
    }

    @Test
    @DisplayName("!coinflip <prompt> - send correct message to channel")
    fun actionCommand_sendsCorrectMessage() = runBlockingTest {
        val prompt = "go for a run"
        coinflip.run(bot, event, prompt, guildSettings)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(POSSIPLE_FLIP_RESULTS, true)
                && messagesSent.captured.contains(prompt, true))
    }

    @Test
    @DisplayName("!coinflip heads <prompt> tails <prompt> - send correct message to channel")
    fun decisionCommand_sendsCorrectMessage() = runBlockingTest {
        val headsCondition = "east"
        val tailsCondition = "west"
        coinflip.run(bot, event, "$HEADS $headsCondition $TAILS $tailsCondition", guildSettings)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsEvery(listOf(HEADS, headsCondition), true)
                || messagesSent.captured.containsEvery(listOf(TAILS, tailsCondition), true))
    }

    @Test
    @DisplayName("!coinflip heads tails - send correct message to channel")
    fun decisionCommand_incorrectFormat_sendsCorrectMessage() = runBlockingTest {
        coinflip.run(bot, event, "$HEADS  $TAILS ", guildSettings)

        verify(exactly = 1) { channel.sendMessage(any<String>()) }
        verify(exactly = 1) { messageAction.queue() }
        assert(messagesSent.captured.containsAny(POSSIPLE_FLIP_RESULTS, true))
    }
}