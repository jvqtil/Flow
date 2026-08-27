package dev.jvqtil.flow.data

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "attachments",
    indices = [
        Index(value = ["entryId"])
    ]
)
data class Attachment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val entryId: String,

    val path: String,

    val fileName: String,

    val mimeType: String?,

    val size: Long,

    val createdAt: Long = System.currentTimeMillis()
)