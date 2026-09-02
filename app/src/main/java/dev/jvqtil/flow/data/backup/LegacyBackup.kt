package dev.jvqtil.flow.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class LegacyBackup(
    val version: Int,
    val folders: List<LegacyFolder>,
    val notes: List<LegacyNote>
)

@Serializable
data class LegacyFolder(
    val id: String,
    val name: String,
    val position: Long
)

@Serializable
data class LegacyNote(
    val id: String,
    val text: String,
    val createdAt: Long,
    val position: Long,
    val type: String,
    val completed: Boolean,
    val folderId: String
)