package com.retrobot.core.domain.service

import com.retrobot.core.Duration
import com.retrobot.core.util.launchContinue
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
        scope.launchContinue {
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