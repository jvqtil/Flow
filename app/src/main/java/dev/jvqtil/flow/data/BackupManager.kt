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
        lists: List<EntryList>
    ) {
        val root = JSONObject()

        root.put(
            "version",
            VERSION
        )

        val listsArray = JSONArray()

        lists.forEach { list ->
            val jsonList = JSONObject()

            jsonList.put(
                "id",
                list.id
            )

            jsonList.put(
                "name",
                list.name
            )

            jsonList.put(
                "position",
                list.position
            )

            listsArray.put(jsonList)
        }

        root.put(
            "lists",
            listsArray
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
                "listId",
                entry.listId
            )

            notesArray.put(jsonNote)
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
        val lists: List<EntryList>,
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

        val lists =
            if (version >= 5) {
                parseLists(
                    root.optJSONArray("lists")
                )
            } else {
                listOf(
                    EntryList(
                        id = ALL_LIST_ID,
                        name = "All",
                        position = 0L
                    )
                )
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

            val text =
                jsonNote.optString("text")

            if (id.isBlank()) {
                continue
            }

            entries += Entry(
                id = id,
                text = text,
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
                listId =
                    if (version >= 5) {
                        jsonNote.optString(
                            "listId",
                            ALL_LIST_ID
                        )
                    } else {
                        ALL_LIST_ID
                    }
            )
        }

        return BackupData(
            lists = lists,
            entries = entries
        )
    }

    private fun parseLists(
        listsArray: JSONArray?
    ): List<EntryList> {
        if (listsArray == null) {
            return listOf(
                EntryList(
                    id = ALL_LIST_ID,
                    name = "All",
                    position = 0L
                )
            )
        }

        val lists =
            mutableListOf<EntryList>()

        for (
        index in
        0 until listsArray.length()
        ) {
            val jsonList =
                listsArray.optJSONObject(index)
                    ?: continue

            val id =
                jsonList.optString("id")

            if (id.isBlank()) {
                continue
            }

            lists += EntryList(
                id = id,
                name =
                    jsonList.optString(
                        "name",
                        "List"
                    ),
                position =
                    jsonList.optLong(
                        "position",
                        index.toLong()
                    )
            )
        }

        if (
            lists.none {
                it.id == ALL_LIST_ID
            }
        ) {
            lists += EntryList(
                id = ALL_LIST_ID,
                name = "All",
                position = 0L
            )
        }

        return lists
            .sortedBy { it.position }
    }
}