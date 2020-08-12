package com.retrobot.core.service


/**
 * Handles long running [Service]s.
 */
class ServiceHandler {
    // TODO Persist Services so we can restart them after process death
    private val servicesMap = mutableMapOf<String, Service>()

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

    @Synchronized
    fun cleanCache() {
        servicesMap.filter { it.value.isActive() }
    }
}