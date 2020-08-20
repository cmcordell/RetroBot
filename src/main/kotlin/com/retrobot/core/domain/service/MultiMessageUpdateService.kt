package com.retrobot.core.domain.service

import com.retrobot.core.Duration
import com.retrobot.core.domain.reaction.MultiMessageReactionListener
import com.retrobot.core.util.launchContinue
import kotlinx.coroutines.*
import net.dv8tion.jda.api.entities.Message
import kotlin.reflect.KFunction

/**
 * A [Service] to periodically update a multi Discord [Message] backed by a [MultiMessageReactionListener]
 *
 * @param initialMessageId This must be the id of the original [Message] returned from JDA.
 * @param multiMessageReactionListener The [MultiMessageReactionListener] backing the multi Discord [Message].
 * @param updatePeriod How often to update the [Message] in milliseconds.
 * @param duration How long to keep this [Service] alive.
*/
abstract class MultiMessageUpdateService(
    initialMessageId: String,
    private val multiMessageReactionListener: MultiMessageReactionListener,
    private val updatePeriod: Long = 15 * Duration.MINUTE,
    duration: Long = Duration.DAY
) : Service {

    override val key = initialMessageId
    private val expiresAt: Long = System.currentTimeMillis() + duration
    private val scope = CoroutineScope(Job() + Dispatchers.Default)


    abstract suspend fun buildNewMessages(): List<Message>

    override fun start() {
        scope.launchContinue {
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
        scope.cancel()
    }

    override fun isActive(): Boolean {
        return scope.isActive && (System.currentTimeMillis() < expiresAt)
    }

    companion object {
        @JvmStatic
        fun build(
            initialMessageId: String,
            multiMessageReactionListener: MultiMessageReactionListener,
            updatePeriod: Long = 15 * Duration.MINUTE,
            duration: Long = Duration.DAY,
            buildNewMessagesFunc: suspend () -> List<Message>
        ): MultiMessageUpdateService {
            return object :
                MultiMessageUpdateService(initialMessageId, multiMessageReactionListener, updatePeriod, duration) {
                override suspend fun buildNewMessages() = buildNewMessagesFunc()
            }
        }
    }
}