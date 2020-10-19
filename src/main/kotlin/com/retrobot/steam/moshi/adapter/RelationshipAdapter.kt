package com.retrobot.steam.moshi.adapter

import com.retrobot.steam.moshi.entity.Relationship
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class RelationshipAdapter {
    @FromJson
    fun fromJson(value: String): Relationship {
        Relationship.values().forEach { relationship ->
            if (value == relationship.name) return relationship
        }
        return Relationship.ALL
    }

    @ToJson
    fun toJson(relationship: Relationship): String {
        return relationship.name
    }
}