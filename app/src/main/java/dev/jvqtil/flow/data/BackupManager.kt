package dev.jvqtil.flow.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    private const val VERSION = 5

    fun exportNotes(
        context: Context,
        uri: Uri,
        entries: List<Entry>,
        folders: List<Folder>
    ) {
        val root = JSONObject()

        root.put(
            "version",
            VERSION
        )

        val foldersArray = JSONArray()

        folders.forEach { folder ->
            val jsonFolder = JSONObject()

            jsonFolder.put(
                "id",
                folder.id
            )

            jsonFolder.put(
                "name",
                folder.name
            )

            jsonFolder.put(
                "position",
                folder.position
            )

            foldersArray.put(
                jsonFolder
            )
        }

        root.put(
            "folders",
            foldersArray
        )

        val notesArray = JSONArray()

        entries.forEach { entry ->
            val jsonNote = JSONObject()

            jsonNote.put(
                "id",
                entry.id
            )

            jsonNote.put(
                "text",
                entry.text
            )

            jsonNote.put(
                "createdAt",
                entry.createdAt
            )

            jsonNote.put(
                "position",
                entry.position
            )

            jsonNote.put(
                "type",
                entry.type
            )

            jsonNote.put(
                "completed",
                entry.completed
            )

            jsonNote.put(
                "folderId",
                entry.folderId
            )

            notesArray.put(
                jsonNote
            )
        }

        root.put(
            "notes",
            notesArray
        )

        context.contentResolver
            .openOutputStream(uri)
            ?.use { outputStream ->
                outputStream.writer(Charsets.UTF_8).use { writer ->
                    writer.write(
                        root.toString(2)
                    )
                }
            }
            ?: throw IllegalStateException(
                "Unable to open output file"
            )
    }

    data class BackupData(
        val folders: List<Folder>,
        val entries: List<Entry>
    )

    fun importNotes(
        context: Context,
        uri: Uri
    ): BackupData {
        val json =
            context.contentResolver
                .openInputStream(uri)
                ?.use { inputStream ->
                    inputStream.reader(Charsets.UTF_8).use { reader ->
                        reader.readText()
                    }
                }
                ?: throw IllegalStateException(
                    "Unable to open backup file"
                )

        val root = JSONObject(json)

        val version =
            root.optInt(
                "version",
                -1
            )

        if (
            version != 1 &&
            version != 2 &&
            version != 5
        ) {
            throw IllegalArgumentException(
                "Unsupported backup version"
            )
        }

        val folders =
            when (version) {
                1, 2 -> {
                    listOf(
                        Folder(
                            id = MASTER_FOLDER_ID,
                            name = "Master",
                            position = 0L
                        )
                    )
                }

                5 -> {
                    parseFolders(
                        root.optJSONArray("folders")
                    )
                }

                else -> {
                    emptyList()
                }
            }

        val notesArray =
            root.optJSONArray("notes")
                ?: throw IllegalArgumentException(
                    "Invalid backup file"
                )

        val entries =
            mutableListOf<Entry>()

        for (
        index in
        0 until notesArray.length()
        ) {
            val jsonNote =
                notesArray.optJSONObject(index)
                    ?: continue

            val id =
                jsonNote.optString("id")

            if (id.isBlank()) {
                continue
            }

            val folderId =
                when (version) {
                    1, 2 -> {
                        MASTER_FOLDER_ID
                    }

                    5 -> {
                        jsonNote.optString(
                            "folderId",
                            MASTER_FOLDER_ID
                        )
                    }

                    else -> {
                        MASTER_FOLDER_ID
                    }
                }

            entries += Entry(
                id = id,
                text =
                    jsonNote.optString(
                        "text"
                    ),
                createdAt =
                    jsonNote.optLong(
                        "createdAt",
                        System.currentTimeMillis()
                    ),
                position =
                    jsonNote.optLong(
                        "position",
                        index.toLong()
                    ),
                type =
                    if (version >= 2) {
                        jsonNote.optString(
                            "type",
                            ENTRY_TYPE_NOTE
                        )
                    } else {
                        ENTRY_TYPE_NOTE
                    },
                completed =
                    if (version >= 2) {
                        jsonNote.optBoolean(
                            "completed",
                            false
                        )
                    } else {
                        false
                    },
                folderId = folderId
            )
        }

        return BackupData(
            folders = folders,
            entries = entries
        )
    }

    private fun parseFolders(
        foldersArray: JSONArray?
    ): List<Folder> {
        if (foldersArray == null) {
            return listOf(
                Folder(
                    id = MASTER_FOLDER_ID,
                    name = "Master",
                    position = 0L
                )
            )
        }

        val folders =
            mutableListOf<Folder>()

        for (
        index in
        0 until foldersArray.length()
        ) {
            val jsonFolder =
                foldersArray.optJSONObject(index)
                    ?: continue

            val id =
                jsonFolder.optString("id")

            if (id.isBlank()) {
                continue
            }

            folders += Folder(
                id = id,
                name =
                    jsonFolder.optString(
                        "name",
                        "Folder"
                    ),
                position =
                    jsonFolder.optLong(
                        "position",
                        index.toLong()
                    )
            )
        }

        return normalizeFolders(
            folders
        )
    }

    private fun normalizeFolders(
        folders: List<Folder>
    ): List<Folder> {
        val normalized =
            folders.filter {
                it.id.isNotBlank()
            }

        if (
            normalized.none {
                it.id == MASTER_FOLDER_ID
            }
        ) {
            return (
                    normalized +
                            Folder(
                                id = MASTER_FOLDER_ID,
                                name = "Master",
                                position =
                                    (
                                            normalized.maxOfOrNull {
                                                it.position
                                            } ?: -1L
                                            ) + 1L
                            )
                    ).sortedBy {
                    it.position
                }
        }

        return normalized.sortedBy {
            it.position
        }
    }
}