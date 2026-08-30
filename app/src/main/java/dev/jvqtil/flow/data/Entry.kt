package dev.jvqtil.flow.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.util.UUID

const val ENTRY_TYPE_NOTE = "note"
const val ENTRY_TYPE_TASK = "task"

@Entity(tableName = "notes")
data class Entry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val position: Long = 0L,
    val type: String = ENTRY_TYPE_NOTE,
    val completed: Boolean = false,
    val folderId: String
)