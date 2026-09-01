package dev.jvqtil.flow.data.backup

import java.io.File
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object BackupReader {

    private const val MANIFEST_PATH =
        "manifest.json"

    private const val FOLDERS_PATH =
        "data/folders.json"

    private const val ENTRIES_PATH =
        "data/entries.json"

    private const val ATTACHMENTS_PATH =
        "data/attachments.json"

    private const val ATTACHMENTS_DIRECTORY =
        "attachments/"

    fun open(
        file: File
    ): BackupArchive {
        val zip =
            try {
                ZipFile(file)
            } catch (
                error: Throwable
            ) {
                throw IllegalArgumentException(
                    "Invalid backup archive",
                    error
                )
            }

        try {
            val manifest =
                readManifest(zip)

            validateManifest(
                manifest
            )

            val folders =
                readFolders(zip)

            val entries =
                readEntries(zip)

            val attachments =
                readAttachments(zip)

            validateData(
                zip = zip,
                folders = folders,
                entries = entries,
                attachments = attachments
            )

            return BackupArchive(
                zip = zip,
                backup =
                    Backup(
                        manifest = manifest,
                        folders = folders,
                        entries = entries,
                        attachments = attachments
                    )
            )
        } catch (
            error: Throwable
        ) {
            zip.close()
            throw error
        }
    }

    private fun readManifest(
        zip: ZipFile
    ): Manifest {
        return try {
            BackupJson.decodeManifest(
                zip
                    .requireEntry(
                        MANIFEST_PATH
                    )
                    .readText(zip)
            )
        } catch (
            error: Throwable
        ) {
            throw IllegalArgumentException(
                "Invalid backup manifest",
                error
            )
        }
    }

    private fun readFolders(
        zip: ZipFile
    ): List<Folder> {
        return BackupJson.decodeFolders(
            zip
                .requireEntry(
                    FOLDERS_PATH
                )
                .readText(zip)
        )
    }

    private fun readEntries(
        zip: ZipFile
    ): List<Entry> {
        return BackupJson.decodeEntries(
            zip
                .requireEntry(
                    ENTRIES_PATH
                )
                .readText(zip)
        )
    }

    private fun readAttachments(
        zip: ZipFile
    ): List<Attachment> {
        return BackupJson.decodeAttachments(
            zip
                .requireEntry(
                    ATTACHMENTS_PATH
                )
                .readText(zip)
        )
    }

    private fun validateManifest(
        manifest: Manifest
    ) {
        if (
            manifest.format !=
            Manifest.FORMAT
        ) {
            throw IllegalArgumentException(
                "Invalid backup format"
            )
        }

        if (
            manifest.formatVersion !=
            Manifest.FORMAT_VERSION
        ) {
            throw IllegalArgumentException(
                "Unsupported backup version: " +
                        manifest.formatVersion
            )
        }
    }

    private fun validateData(
        zip: ZipFile,
        folders: List<Folder>,
        entries: List<Entry>,
        attachments: List<Attachment>
    ) {
        validateUniqueIds(
            folders.map {
                it.id
            },
            "folder"
        )

        validateUniqueIds(
            entries.map {
                it.id
            },
            "entry"
        )

        validateUniqueIds(
            attachments.map {
                it.id
            },
            "attachment"
        )

        val folderIds =
            folders
                .map {
                    it.id
                }
                .toSet()

        val entryIds =
            entries
                .map {
                    it.id
                }
                .toSet()

        entries.forEach { entry ->
            if (
                entry.folderId !in
                folderIds
            ) {
                throw IllegalArgumentException(
                    "Entry references missing folder: " +
                            entry.id
                )
            }
        }

        attachments.forEach { attachment ->
            if (
                attachment.entryId !in
                entryIds
            ) {
                throw IllegalArgumentException(
                    "Attachment references missing entry: " +
                            attachment.id
                )
            }

            val zipEntry =
                zip.getEntry(
                    ATTACHMENTS_DIRECTORY +
                            attachment.id
                )
                    ?: throw IllegalArgumentException(
                        "Missing attachment file: " +
                                attachment.id
                    )

            if (
                attachment.size >= 0 &&
                zipEntry.size >= 0 &&
                zipEntry.size != attachment.size
            ) {
                throw IllegalArgumentException(
                    "Attachment size mismatch: " +
                            attachment.id
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

    private fun ZipFile.requireEntry(
        path: String
    ): ZipEntry {
        return getEntry(path)
            ?: throw IllegalArgumentException(
                "Missing backup file: $path"
            )
    }

    private fun ZipEntry.readText(
        zip: ZipFile
    ): String {
        return zip
            .getInputStream(this)
            .use { input ->
                input
                    .readBytes()
                    .toString(
                        StandardCharsets.UTF_8
                    )
            }
    }
}