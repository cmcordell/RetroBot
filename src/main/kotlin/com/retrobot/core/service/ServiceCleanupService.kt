package com.retrobot.core.service

import com.retrobot.core.Duration
import kotlinx.coroutines.*

/**
 * A [Service] that will periodically cleanup old [Service]s from a [ServiceHandler]
 *
 * @param serviceHandler The [ServiceHandler] to clean.
 * @param cleanupPeriod How often to clean the [ServiceHandler] in milliseconds.
 */
class ServiceCleanupService(
        private val serviceHandler: ServiceHandler,
        private val cleanupPeriod: Long = Duration.DAY
) : Service {
    override val key = "ServiceCleanupService"

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    override fun start() {
        scope.launch {
            while (isActive()) {
                serviceHandler.cleanCache()
                delay(cleanupPeriod)
            }
        }
    }

    override fun stop() {
        scope.cancel()
    }

    override fun isActive() = scope.isActive
}