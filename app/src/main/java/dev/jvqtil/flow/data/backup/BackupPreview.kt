package dev.jvqtil.flow.data.backup

data class BackupPreview(
    val notes: Int,
    val tasks: Int,
    val folders: Int,
    val attachments: Int,
    val createdAt: String,
    val version: Int
)