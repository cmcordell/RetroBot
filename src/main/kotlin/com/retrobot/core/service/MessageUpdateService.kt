package com.retrobot.core.service

import com.retrobot.core.Duration
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Message

/**
 * A [Service] to periodically update a Discord message.
 *
 * @param message This must be a message returned from JDA
 */
abstract class MessageUpdateService(
        private val initialMessage: Message,
        private val updatePeriod: Long = 15 * Duration.MINUTE,
        duration: Long = Duration.DAY
) : Service {

    override val key = initialMessage.id
    private val expiresAt: Long = System.currentTimeMillis() + duration
    private var job: Job? = null


    abstract suspend fun buildNewMessage(): Message?

    override fun start() {
        job?.cancel()
        job = GlobalScope.launch(Dispatchers.Default) {
            while (isActive()) {
                buildNewMessage()?.let { newMessage ->
                    initialMessage.textChannel.editMessageById(initialMessage.id, newMessage).queue()
                }
                delay(updatePeriod)
            }
        }
    }

    override fun stop() {
        job?.cancel()
    }

    override fun isActive(): Boolean {
        return System.currentTimeMillis() < expiresAt
    }
}