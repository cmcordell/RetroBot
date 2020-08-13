package com.retrobot.core.domain.command

/**
 * Wrapper for a [Set] of [Command]s available.
 */
class CommandSet(vararg commandArgs: Command) : Collection<Command> {
    private val commands = commandArgs.toSet()
    private val categoryMap = commandArgs.groupBy { it.category }.mapValues { it.value.toSet() }
    val categories = categoryMap.keys

    fun getCategoryByAlias(alias: String) : CommandCategory? {
        categoryMap.keys.forEach { category ->
            category.aliases.forEach {  categoryAlias ->
                if (alias.equals(categoryAlias, true)) {
                    return category
                }
            }
        }
        return null
    }
    fun getCommandsByCategory(category: CommandCategory) : Set<Command> = categoryMap[category] ?: error("Category not in map.")

    // MutableSet delegated functions
    override val size: Int = commands.size
    override fun isEmpty(): Boolean = commands.isEmpty()
    override fun contains(element: Command): Boolean = commands.contains(element)
    override fun iterator(): Iterator<Command> = commands.iterator()
    override fun containsAll(elements: Collection<Command>): Boolean = commands.containsAll(elements)
}

class MutableCommandSet(vararg commandArgs: Command) : Set<Command>, MutableCollection<Command> {
    private val commands = commandArgs.toMutableSet()
    private val categoryMap = commandArgs.groupBy { it.category }.mapValues { it.value.toMutableSet() }.toMutableMap()
    val categories get() = categoryMap.keys

    fun getCategoryByAlias(alias: String) : CommandCategory? {
        categoryMap.keys.forEach { category ->
            category.aliases.forEach {  categoryAlias ->
                if (alias.equals(categoryAlias, true)) {
                    return category
                }
            }
        }
        return null
    }
    fun getCommandsByCategory(category: CommandCategory) : Set<Command> = categoryMap[category] ?: error("Category not in map.")

    // MutableSet delegated functions
    override val size: Int = commands.size
    override fun isEmpty(): Boolean = commands.isEmpty()
    override fun contains(element: Command): Boolean = commands.contains(element)
    override fun iterator(): MutableIterator<Command> = commands.iterator()
    override fun containsAll(elements: Collection<Command>): Boolean = elements.containsAll(elements)

    override fun add(element: Command): Boolean {
        val commandAdded = commands.add(element)
        if (commandAdded) {
            categoryMap.getOrPut(element.category) { mutableSetOf() }.add(element)
        }
        return commandAdded
    }

    override fun addAll(elements: Collection<Command>): Boolean {
        var commandsAdded = false
        elements.forEach { command ->
            if (add(command)) commandsAdded = true
        }
        return commandsAdded
    }

    override fun clear() {
        commands.clear()
        categoryMap.clear()
    }

    override fun remove(element: Command): Boolean {
        val commandRemoved = commands.remove(element)
        if (commandRemoved) {
            categoryMap[element.category]?.let{
                remove(element)
                if (size == 0) categoryMap.remove(element.category)
            }
        }
        return commandRemoved
    }

    override fun removeAll(elements: Collection<Command>): Boolean {
        var commandsRemoved = false
        elements.forEach { command ->
            if (remove(command)) commandsRemoved = true
        }
        return commandsRemoved
    }

    override fun retainAll(elements: Collection<Command>): Boolean {
        val commandsRemoved = commands.retainAll(elements)
        if (commandsRemoved) {
            categoryMap.forEach { entries ->
                entries.value.forEach {  command ->
                    if (!elements.contains(command)) entries.value.remove(command)
                }
                if (entries.value.isEmpty()) categoryMap.remove(entries.key)
            }
        }
        return commandsRemoved
    }
}

fun CommandSet.toMutableCommandSet(): MutableCommandSet = MutableCommandSet(*toTypedArray())
fun MutableCommandSet.toCommandSet(): CommandSet = CommandSet(*toTypedArray())

