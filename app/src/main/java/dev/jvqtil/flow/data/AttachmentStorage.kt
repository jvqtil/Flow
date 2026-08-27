package dev.jvqtil.flow.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.util.UUID

data class AttachmentMetadata(
    val fileName: String,
    val mimeType: String?,
    val size: Long
)

class AttachmentStorage(
    context: Context
) {

    private val context = context.applicationContext

    private val attachmentsDir =
        File(
            this.context.filesDir,
            "attachments"
        ).apply {
            mkdirs()
        }

    fun copyIntoStorage(
        uri: Uri,
        fileName: String?
    ): String {
        val extension =
            fileName
                ?.substringAfterLast('.', "")
                ?.takeIf {
                    it.isNotBlank() &&
                            fileName.substringBeforeLast('.', "") != fileName
                }
                ?.let { ".$it" }
                ?: ""

        val file = File(
            attachmentsDir,
            "${UUID.randomUUID()}$extension"
        )

        val inputStream =
            context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException(
                    "Unable to open attachment"
                )

        try {
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (error: Throwable) {
            file.delete()
            throw error
        }

        return "attachments/${file.name}"
    }

    fun getFile(
        path: String
    ): File {
        val file =
            File(
                context.filesDir,
                path
            ).canonicalFile

        val root =
            attachmentsDir.canonicalFile

        if (file.relativeToOrNull(root) == null) {
            throw IllegalArgumentException(
                "Invalid attachment path"
            )
        }

        return file
    }

    fun getMetadata(
        uri: Uri
    ): AttachmentMetadata {
        val fileName =
            context.contentResolver
                .query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(0)
                    } else {
                        null
                    }
                }
                ?: "attachment"

        val mimeType =
            context.contentResolver.getType(uri)

        val size =
            context.contentResolver
                .query(
                    uri,
                    arrayOf(OpenableColumns.SIZE),
                    null,
                    null,
                    null
                )
                ?.use { cursor ->
                    if (
                        cursor.moveToFirst() &&
                        !cursor.isNull(0)
                    ) {
                        cursor.getLong(0)
                    } else {
                        -1L
                    }
                }
                ?: -1L

        return AttachmentMetadata(
            fileName = fileName,
            mimeType = mimeType,
            size = size
        )
    }

    fun delete(
        path: String
    ) {
        val file = getFile(path)

        if (file.exists() && !file.delete()) {
            throw IllegalStateException(
                "Unable to delete attachment"
            )
        }
    }
}