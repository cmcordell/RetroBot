package com.retrobot.core.domain.service

import org.koin.core.KoinComponent

/**
 * Interface for a [Service] that performs long running operations.
 * [key] should be unique for each instance of the [Service].
 */
interface Service: KoinComponent {
    val key: String

    fun start()
    fun stop()
    fun isActive(): Boolean
}