package dev.jvqtil.flow.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import java.util.UUID

const val MASTER_FOLDER_ID = "master"

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val position: Long = 0L
)