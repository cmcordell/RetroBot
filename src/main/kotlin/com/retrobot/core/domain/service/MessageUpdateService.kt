package com.retrobot.core.domain.service

import com.retrobot.core.Duration
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Message

/**
 * A [Service] to periodically update a Discord message.
 *
 * @param initialMessage This must be a message returned from JDA.
 * @param updatePeriod How often to update the [Message] in milliseconds.
 * @param duration How long to keep this [Service] alive.
 */
abstract class MessageUpdateService(
        private val initialMessage: Message,
        private val updatePeriod: Long = 15 * Duration.MINUTE,
        duration: Long = Duration.DAY
) : Service {
    override val key = initialMessage.id
    private val expiresAt: Long = System.currentTimeMillis() + duration
    private val scope = CoroutineScope(Job() + Dispatchers.Default)


    abstract suspend fun buildNewMessage(): Message?

    override fun start() {
        scope.launch {
            while (isActive()) {
                buildNewMessage()?.let { newMessage ->
                    initialMessage.textChannel.editMessageById(initialMessage.id, newMessage).queue()
                }
                delay(updatePeriod)
            }
        }
    }

    override fun stop() {
        scope.cancel()
    }

    override fun isActive(): Boolean {
        return scope.isActive && (System.currentTimeMillis() < expiresAt)
    }
}