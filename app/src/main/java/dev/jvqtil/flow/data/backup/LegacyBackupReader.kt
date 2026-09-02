package dev.jvqtil.flow.data.backup

import kotlinx.serialization.json.Json
import dev.jvqtil.flow.data.Entry as DbEntry
import dev.jvqtil.flow.data.Folder as DbFolder

object LegacyBackupReader {

    private const val SUPPORTED_VERSION = 5

    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    fun read(
        value: String
    ): LegacyBackupData {
        val backup =
            try {
                json.decodeFromString(
                    LegacyBackup.serializer(),
                    value
                )
            } catch (
                error: Throwable
            ) {
                throw IllegalArgumentException(
                    "Invalid legacy backup",
                    error
                )
            }

        if (
            backup.version !=
            SUPPORTED_VERSION
        ) {
            throw IllegalArgumentException(
                "Unsupported legacy backup version: " +
                        backup.version
            )
        }

        validate(
            backup
        )

        return LegacyBackupData(
            folders =
                backup.folders.map { folder ->
                    DbFolder(
                        id = folder.id,
                        name = folder.name,
                        position = folder.position
                    )
                },
            entries =
                backup.notes.map { note ->
                    DbEntry(
                        id = note.id,
                        text = note.text,
                        createdAt = note.createdAt,
                        position = note.position,
                        type = note.type,
                        completed = note.completed,
                        folderId = note.folderId
                    )
                }
        )
    }

    private fun validate(
        backup: LegacyBackup
    ) {
        validateUniqueIds(
            ids =
                backup.folders.map {
                    it.id
                },
            type = "folder"
        )

        validateUniqueIds(
            ids =
                backup.notes.map {
                    it.id
                },
            type = "note"
        )

        val folderIds =
            backup.folders
                .map {
                    it.id
                }
                .toSet()

        backup.notes.forEach { note ->
            if (
                note.folderId !in
                folderIds
            ) {
                throw IllegalArgumentException(
                    "Note references missing folder: " +
                            note.id
                )
            }
        }
    }

    private fun validateUniqueIds(
        ids: List<String>,
        type: String
    ) {
        if (
            ids.any {
                it.isBlank()
            }
        ) {
            throw IllegalArgumentException(
                "Blank $type id"
            )
        }

        if (
            ids.size !=
            ids.toSet().size
        ) {
            throw IllegalArgumentException(
                "Duplicate $type id"
            )
        }
    }
}

data class LegacyBackupData(
    val folders: List<DbFolder>,
    val entries: List<DbEntry>
)