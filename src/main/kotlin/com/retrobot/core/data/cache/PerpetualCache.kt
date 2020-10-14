package com.retrobot.core.data.cache

class PerpetualCache<K, V> : Cache<K, V> {
    private val cache = HashMap<K, V>()

    override val size: Int
        get() = cache.size

    override fun set(key: K, value: V) {
        cache[key] = value
    }

    override fun remove(key: K) = cache.remove(key)

    override fun get(key: K) = cache[key]

    override fun clear() = cache.clear()
}