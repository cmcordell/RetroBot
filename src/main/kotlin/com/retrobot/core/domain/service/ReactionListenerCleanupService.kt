package com.retrobot.core.domain.service

import com.retrobot.core.Duration
import com.retrobot.core.domain.reaction.ReactionHandler
import com.retrobot.core.domain.reaction.ReactionListener
import kotlinx.coroutines.*

/**
 * A [Service] that will periodically cleanup old [ReactionListener]s from [reactionHandler]
 *
 * @param reactionHandler The [ReactionHandler] to clean.
 * @param cleanupPeriod How often to clean the [ReactionHandler] in milliseconds.
 */
class ReactionListenerCleanupService(
        private val reactionHandler: ReactionHandler,
        private val cleanupPeriod: Long = Duration.DAY
) : Service {
    override val key = "ReactionListenerCleanupService"

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    override fun start() {
        scope.launch {
            while (isActive()) {
                reactionHandler.cleanCache()
                delay(cleanupPeriod)
            }
        }
    }

    override fun stop() {
        scope.cancel()
    }

    override fun isActive() = scope.isActive
}