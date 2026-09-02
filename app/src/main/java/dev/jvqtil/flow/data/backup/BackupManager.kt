package dev.jvqtil.flow.data.backup

import android.content.Context
import android.net.Uri
import dev.jvqtil.flow.BuildConfig
import dev.jvqtil.flow.data.AttachmentStorage
import dev.jvqtil.flow.data.FlowRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant

class BackupManager(
    private val context: Context,
    private val repository: FlowRepository,
    private val attachmentStorage: AttachmentStorage
) {

    suspend fun export(
        uri: Uri
    ) = withContext(Dispatchers.IO) {

        val folders =
            repository.getAllFolders()

        val entries =
            repository.getAllEntries()

        val attachments =
            repository.getAllAttachments()

        val backup =
            Backup(
                manifest =
                    Manifest(
                        format =
                            Manifest.FORMAT,
                        formatVersion =
                            Manifest.FORMAT_VERSION,
                        flowVersion =
                            BuildConfig.VERSION_NAME,
                        createdAt =
                            Instant.now().toString()
                    ),
                folders =
                    folders.map(
                        BackupMapper::folder
                    ),
                entries =
                    entries.map(
                        BackupMapper::entry
                    ),
                attachments =
                    attachments.map(
                        BackupMapper::attachment
                    )
            )

        val outputStream =
            context.contentResolver
                .openOutputStream(uri)
                ?: throw IllegalStateException(
                    "Unable to open backup file"
                )

        outputStream.use { output ->
            val attachmentsById =
                attachments.associateBy { it.id }

            BackupWriter.write(
                outputStream = output,
                backup = backup,
                attachmentProvider = { attachment ->
                    val databaseAttachment =
                        attachmentsById[attachment.id]
                            ?: error(
                                "Attachment not found: ${attachment.id}"
                            )

                    attachmentStorage.open(
                        databaseAttachment.path
                    )
                }
            )
        }
    }

    suspend fun inspect(
        uri: Uri
    ): BackupPreview =
        withContext(Dispatchers.IO) {

            val file =
                copyToTemporaryFile(uri)

            try {

                if (
                    isLegacyBackup(
                        file
                    )
                ) {
                    inspectLegacy(
                        file
                    )
                } else {
                    BackupReader
                        .open(file)
                        .use { archive ->

                            val backup =
                                archive.backup

                            BackupPreview(
                                notes =
                                    backup.entries.count {
                                        it.type == "note"
                                    },

                                tasks =
                                    backup.entries.count {
                                        it.type == "task"
                                    },

                                folders =
                                    backup.folders.size,

                                attachments =
                                    backup.attachments.size,

                                createdAt =
                                    backup.manifest.createdAt,

                                version =
                                    backup.manifest.formatVersion
                            )
                        }
                }

            } finally {
                file.delete()
            }
        }

    suspend fun restore(
        uri: Uri
    ) = withContext(Dispatchers.IO) {

        val file =
            copyToTemporaryFile(uri)

        try {

            if (
                isLegacyBackup(
                    file
                )
            ) {
                restoreLegacy(
                    file
                )
            } else {
                BackupReader
                    .open(file)
                    .use { archive ->

                        restore(
                            archive
                        )
                    }
            }

        } finally {
            file.delete()
        }
    }

    private suspend fun restoreLegacy(
        file: File
    ) {
        val value =
            file
                .readText(
                    Charsets.UTF_8
                )

        val legacy =
            LegacyBackupReader.read(
                value
            )

        repository.replaceDatabase(
            folders =
                legacy.folders,

            entries =
                legacy.entries,

            attachments =
                emptyList()
        )
    }

    private fun inspectLegacy(
        file: File
    ): BackupPreview {
        val value =
            file
                .readText(
                    Charsets.UTF_8
                )

        val legacy =
            LegacyBackupReader.read(
                value
            )

        return BackupPreview(
            notes =
                legacy.entries.count {
                    it.type == "note"
                },

            tasks =
                legacy.entries.count {
                    it.type == "task"
                },

            folders =
                legacy.folders.size,

            attachments = 0,

            createdAt =
                Instant
                    .ofEpochMilli(
                        file.lastModified()
                    )
                    .toString(),

            version =
                5
        )
    }

    // yea i know this isnt the best way to do the thing but it works
    private fun isLegacyBackup(
        file: File
    ): Boolean {
        return try {
            file.inputStream().use { input ->

                val firstByte =
                    input.read()

                firstByte ==
                        '{'.code
            }
        } catch (
            _: Throwable
        ) {
            false
        }
    }

    private suspend fun restore(
        archive: BackupArchive
    ) {

        val backup =
            archive.backup

        val oldAttachments =
            repository.getAllAttachments()

        val restoredAttachments =
            mutableListOf<
                    dev.jvqtil.flow.data.Attachment
                    >()

        try {

            backup.attachments.forEach { attachment ->

                archive
                    .openAttachment(
                        attachment
                    )
                    .use { input ->

                        val path =
                            attachmentStorage.restore(
                                inputStream = input,
                                fileName = attachment.fileName
                            )

                        restoredAttachments +=
                            BackupMapper.attachment(
                                attachment = attachment,
                                path = path
                            )
                    }
            }

            repository.replaceDatabase(
                folders =
                    backup.folders.map(
                        BackupMapper::folder
                    ),

                entries =
                    backup.entries.map(
                        BackupMapper::entry
                    ),

                attachments =
                    restoredAttachments
            )

            oldAttachments.forEach { attachment ->

                try {
                    attachmentStorage.delete(
                        attachment.path
                    )
                } catch (_: Throwable) {
                }
            }

        } catch (error: Throwable) {

            restoredAttachments.forEach { attachment ->

                try {
                    attachmentStorage.delete(
                        attachment.path
                    )
                } catch (_: Throwable) {
                }
            }

            throw error
        }
    }

    private fun copyToTemporaryFile(
        uri: Uri
    ): File {

        val file =
            File.createTempFile(
                "flow-backup-",
                ".flow",
                context.cacheDir
            )

        try {

            val input =
                context.contentResolver
                    .openInputStream(uri)
                    ?: throw IllegalStateException(
                        "Unable to open backup file"
                    )

            input.use { source ->

                file.outputStream().use { target ->

                    source.copyTo(
                        target
                    )
                }
            }

            return file

        } catch (error: Throwable) {

            file.delete()

            throw error
        }
    }
}