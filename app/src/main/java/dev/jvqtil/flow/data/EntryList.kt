package dev.jvqtil.flow.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.util.UUID

const val ALL_LIST_ID = "all"

@Entity(tableName = "lists")
data class EntryList(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val position: Long = 0L
)