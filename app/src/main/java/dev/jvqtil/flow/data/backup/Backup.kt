package dev.jvqtil.flow.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class Backup(
    val manifest: Manifest,
    val folders: List<Folder>,
    val entries: List<Entry>,
    val attachments: List<Attachment>
)

@Serializable
data class Manifest(
    val format: String,
    val formatVersion: Int,
    val flowVersion: String,
    val createdAt: String
) {
    companion object {
        const val FORMAT = "flow"
        const val FORMAT_VERSION = 1
    }
}

@Serializable
data class Folder(
    val id: String,
    val name: String,
    val position: Long
)

@Serializable
data class Entry(
    val id: String,
    val text: String,
    val createdAt: String,
    val position: Long,
    val type: String,
    val completed: Boolean,
    val folderId: String
)

@Serializable
data class Attachment(
    val id: String,
    val entryId: String,
    val fileName: String,
    val mimeType: String?,
    val size: Long,
    val createdAt: String
)