package dev.jvqtil.flow.data.backup

import java.io.Closeable
import java.io.InputStream
import java.util.zip.ZipFile

class BackupArchive(
    private val zip: ZipFile,
    val backup: Backup
) : Closeable {

    fun openAttachment(
        attachment: Attachment
    ): InputStream {
        val path =
            "attachments/${attachment.id}"

        val entry =
            zip.getEntry(path)
                ?: throw IllegalArgumentException(
                    "Missing attachment file: " +
                            attachment.id
                )

        return zip.getInputStream(
            entry
        )
    }

    override fun close() {
        zip.close()
    }
}