package com.retrobot.core.service

import com.retrobot.core.Duration
import com.retrobot.utility.reaction.MultiMessageReactionListener
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Message

/**
 * A [Service] to periodically update a multi Discord message backed by a [MultiMessageReactionListener]
 */
abstract class MultiMessageUpdateService(
        initialMessageId: String,
        private val multiMessageReactionListener: MultiMessageReactionListener,
        private val updatePeriod: Long = 15 * Duration.MINUTE,
        duration: Long = Duration.DAY
) : Service {

    override val key = initialMessageId
    private val expiresAt: Long = System.currentTimeMillis() + duration
    private var job: Job? = null


    abstract suspend fun buildNewMessages(): List<Message>

    override fun start() {
        job?.cancel()
        job = GlobalScope.launch(Dispatchers.Default) {
            while (isActive()) {
                if (multiMessageReactionListener.isActive()) {
                    val newMessages = buildNewMessages()
                    if (newMessages.isNotEmpty()) {
                        multiMessageReactionListener.updateMessages(newMessages)
                    }
                } else {
                    stop()
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