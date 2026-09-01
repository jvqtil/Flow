package dev.jvqtil.flow.data.backup

import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object BackupWriter {

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

    fun write(
        outputStream: OutputStream,
        backup: Backup,
        attachmentProvider: (
            Attachment
        ) -> InputStream
    ) {
        ZipOutputStream(
            outputStream
        ).use { zip ->
            writeText(
                zip = zip,
                path = MANIFEST_PATH,
                value =
                    BackupJson.encodeManifest(
                        backup.manifest
                    )
            )

            writeText(
                zip = zip,
                path = FOLDERS_PATH,
                value =
                    BackupJson.encodeFolders(
                        backup.folders
                    )
            )

            writeText(
                zip = zip,
                path = ENTRIES_PATH,
                value =
                    BackupJson.encodeEntries(
                        backup.entries
                    )
            )

            writeText(
                zip = zip,
                path = ATTACHMENTS_PATH,
                value =
                    BackupJson.encodeAttachments(
                        backup.attachments
                    )
            )

            backup.attachments.forEach { attachment ->
                attachmentProvider(
                    attachment
                ).use { inputStream ->
                    writeStream(
                        zip = zip,
                        path =
                            ATTACHMENTS_DIRECTORY +
                                    attachment.id,
                        inputStream = inputStream
                    )
                }
            }
        }
    }

    private fun writeText(
        zip: ZipOutputStream,
        path: String,
        value: String
    ) {
        value
            .byteInputStream(
                Charsets.UTF_8
            )
            .use { inputStream ->
                writeStream(
                    zip = zip,
                    path = path,
                    inputStream = inputStream
                )
            }
    }

    private fun writeStream(
        zip: ZipOutputStream,
        path: String,
        inputStream: InputStream
    ) {
        zip.putNextEntry(
            ZipEntry(path)
        )

        val buffer =
            ByteArray(
                DEFAULT_BUFFER_SIZE
            )

        while (true) {
            val count =
                inputStream.read(
                    buffer
                )

            if (count == -1) {
                break
            }

            zip.write(
                buffer,
                0,
                count
            )
        }

        zip.closeEntry()
    }
}