package com.retrobot

import kotlin.reflect.KFunction
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.isAccessible


fun String.containsAny(items: List<String>, ignoreCase: Boolean = false): Boolean {
    for (item in items) {
        if (this.contains(item, ignoreCase)) {
            return true
        }
    }
    return false
}

fun String.containsEvery(items: List<String>, ignoreCase: Boolean = false): Boolean {
    for (item in items) {
        if (!this.contains(item, ignoreCase)) {
            return false
        }
    }
    return true
}

fun callPrivate(objectInstance: Any, methodName: String, vararg args: Any?): Any? {
    val privateMethod: KFunction<*>? =
            objectInstance::class.functions.find { t -> return@find t.name == methodName }

    val argList = args.toMutableList()
    (argList as ArrayList).add(0, objectInstance)
    val argArr = argList.toArray()

    privateMethod?.apply {
        isAccessible = true
        return call(*argArr)
    } ?: throw NoSuchMethodException("Method $methodName does not exist in ${objectInstance::class.qualifiedName}")

    return null
}