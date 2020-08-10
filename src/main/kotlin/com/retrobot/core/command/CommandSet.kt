package com.retrobot.core.command

/**
 * Wrapper for a [Set] of [Command]s available for this [Bot]
 */
class CommandSet(vararg commandArgs: Command) : Set<Command> {

    private val commands = commandArgs.toSet()
    private val categoryMap = commandArgs.groupBy { it.category }.mapValues { it.value.toSet() }
    val categories = categoryMap.keys
    override val size: Int = commands.size

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

    override fun isEmpty(): Boolean = commands.isEmpty()
    override fun contains(element: Command): Boolean = commands.contains(element)
    override fun iterator(): Iterator<Command> = commands.iterator()
    override fun containsAll(elements: Collection<Command>): Boolean = commands.containsAll(elements)
}