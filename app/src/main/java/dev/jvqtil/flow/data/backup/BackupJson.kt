package dev.jvqtil.flow.data.backup

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object BackupJson {

    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    fun encodeManifest(
        manifest: Manifest
    ): String {
        return json.encodeToString(
            Manifest.serializer(),
            manifest
        )
    }

    fun decodeManifest(
        value: String
    ): Manifest {
        return json.decodeFromString(
            Manifest.serializer(),
            value
        )
    }

    fun encodeFolders(
        folders: List<Folder>
    ): String {
        return json.encodeToString(
            ListSerializer(
                Folder.serializer()
            ),
            folders
        )
    }

    fun decodeFolders(
        value: String
    ): List<Folder> {
        return json.decodeFromString(
            ListSerializer(
                Folder.serializer()
            ),
            value
        )
    }

    fun encodeEntries(
        entries: List<Entry>
    ): String {
        return json.encodeToString(
            ListSerializer(
                Entry.serializer()
            ),
            entries
        )
    }

    fun decodeEntries(
        value: String
    ): List<Entry> {
        return json.decodeFromString(
            ListSerializer(
                Entry.serializer()
            ),
            value
        )
    }

    fun encodeAttachments(
        attachments: List<Attachment>
    ): String {
        return json.encodeToString(
            ListSerializer(
                Attachment.serializer()
            ),
            attachments
        )
    }

    fun decodeAttachments(
        value: String
    ): List<Attachment> {
        return json.decodeFromString(
            ListSerializer(
                Attachment.serializer()
            ),
            value
        )
    }
}