package dev.jvqtil.flow.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    private const val VERSION = 2

    fun exportNotes(
        context: Context,
        uri: Uri,
        notes: List<Note>
    ) {
        val root = JSONObject()

        root.put("version", VERSION)

        val notesArray = JSONArray()

        notes.forEach { note ->
            val jsonNote = JSONObject()

            jsonNote.put("id", note.id)
            jsonNote.put("text", note.text)
            jsonNote.put("createdAt", note.createdAt)
            jsonNote.put("position", note.position)
            jsonNote.put("type", note.type)
            jsonNote.put("completed", note.completed)

            notesArray.put(jsonNote)
        }

        root.put("notes", notesArray)

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

    fun importNotes(
        context: Context,
        uri: Uri
    ): List<Note> {
        val json = context.contentResolver
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

        val version = root.optInt(
            "version",
            -1
        )

        if (version != VERSION && version != 1) {
            throw IllegalArgumentException(
                "Unsupported backup version"
            )
        }

        val notesArray = root.optJSONArray(
            "notes"
        )
            ?: throw IllegalArgumentException(
                "Invalid backup file"
            )

        val notes = mutableListOf<Note>()

        for (index in 0 until notesArray.length()) {
            val jsonNote = notesArray.optJSONObject(index)
                ?: continue

            val id = jsonNote.optString("id")
            val text = jsonNote.optString("text")

            if (id.isBlank()) {
                continue
            }

            notes += Note(
                id = id,
                text = text,
                createdAt = jsonNote.optLong(
                    "createdAt",
                    System.currentTimeMillis()
                ),
                position = jsonNote.optLong(
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
            )
        }

        return notes
    }
}