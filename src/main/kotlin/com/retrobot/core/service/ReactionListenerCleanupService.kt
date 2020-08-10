package com.retrobot.core.service

import com.retrobot.core.Duration
import com.retrobot.core.reactionhandler.ReactionHandler
import kotlinx.coroutines.*

/**
 * Service to periodically cleanup old ReactionListeners from [reactionHandler]
 */
class ReactionListenerCleanupService(
        private val reactionHandler: ReactionHandler,
        private val cleanupPeriod: Long = Duration.DAY
) : Service {
    override val key = "ReactionListenerCleanupService"

    private var job: Job? = null

    override fun start() {
        job?.cancel()
        job = GlobalScope.launch(Dispatchers.Default) {
            while (true) {
                reactionHandler.cleanCache()
                delay(cleanupPeriod)
            }
        }
    }

    override fun stop() {
        job?.cancel()
    }

    override fun isActive() = (job != null && (job?.isActive ?: false))
}