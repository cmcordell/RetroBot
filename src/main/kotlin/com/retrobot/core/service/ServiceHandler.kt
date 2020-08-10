package com.retrobot.core.service

import com.retrobot.core.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Handles long running [Service]s like
 */
class ServiceHandler {
    private val cleanupDelay = 30 * Duration.MINUTE
    private val servicesMap = mutableMapOf<String, Service>()

    init {
        initCleanup()
    }


    fun addService(service: Service) {
        if (containsActiveService(service.key)) return

        service.start()
        servicesMap[service.key] = service
    }

    fun removeService(key: String) {
        servicesMap.remove(key)?.stop()
    }

    fun containsActiveService(key: String): Boolean {
        val service = servicesMap[key]
        return service != null && service.isActive()
    }

    private fun initCleanup() {
        GlobalScope.launch(Dispatchers.Default) {
            servicesMap.filter { it.value.isActive() }
            delay(cleanupDelay)
        }
    }
}