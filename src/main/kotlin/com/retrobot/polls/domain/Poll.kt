package com.retrobot.polls.domain

data class Poll(
        val title: String,
        val options: Map<Int, Option>
) {
    var votes = countVotes()

    fun addVote(optionNumber: Int) {
        options[optionNumber]?.apply {
            votes++
        }
        votes = countVotes()
    }

    fun removeVote(optionNumber: Int) {
        options[optionNumber]?.apply {
            votes--
        }
        votes = countVotes()
    }

    private fun countVotes() : Int {
        var votes = 0
        for (option in options.values) {
            votes += option.votes
        }
        return votes
    }

    data class Option(
            val number: Int,
            val title: String,
            var votes: Int = 0
    )
}