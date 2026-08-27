package dev.jvqtil.flow.data

import android.net.Uri
import kotlinx.coroutines.flow.Flow

class FlowRepository(
    private val flowDao: FlowDao,
    private val attachmentDao: AttachmentDao,
    private val attachmentStorage: AttachmentStorage
) {

    fun observeEntries(): Flow<List<Entry>> {
        return flowDao.observeNotes()
    }

    suspend fun getAllEntries(): List<Entry> {
        return flowDao.getAllNotes()
    }

    suspend fun findById(id: String): Entry? {
        return flowDao.getById(id)
    }

    suspend fun insertAtTop(entry: Entry) {
        flowDao.shiftPositionsDown()

        flowDao.insert(
            entry.copy(position = 0L)
        )
    }

    suspend fun update(entry: Entry) {
        flowDao.update(entry)
    }

    suspend fun delete(entry: Entry) {
        flowDao.delete(entry)
    }

    suspend fun restore(entry: Entry) {
        flowDao.upsert(entry)
    }

    suspend fun restore(entries: List<Entry>) {
        entries.forEach { entry ->
            flowDao.upsert(entry)
        }
    }

    suspend fun updateEntriesPositions(
        entryIds: List<String>
    ) {
        entryIds.forEachIndexed { index, id ->
            flowDao.updatePosition(
                id = id,
                position = index.toLong()
            )
        }
    }

    fun observeAttachments(
        entryId: String
    ): Flow<List<Attachment>> {
        return attachmentDao.observeForEntry(entryId)
    }

    suspend fun insertAttachment(
        entryId: String,
        uri: Uri
    ): Attachment {
        val metadata =
            attachmentStorage.getMetadata(uri)

        val path =
            attachmentStorage.copyIntoStorage(
                uri = uri,
                fileName = metadata.fileName
            )

        val attachment =
            Attachment(
                entryId = entryId,
                path = path,
                fileName = metadata.fileName,
                mimeType = metadata.mimeType,
                size = metadata.size
            )

        try {
            attachmentDao.insert(attachment)
        } catch (error: Throwable) {
            attachmentStorage.delete(path)
            throw error
        }

        return attachment
    }

    suspend fun deleteAttachment(
        attachment: Attachment
    ) {
        attachmentStorage.delete(attachment.path)
        attachmentDao.delete(attachment)
    }

    suspend fun purgeAttachments(entryId: String) {
        val attachments =
            attachmentDao.getForEntry(entryId)

        attachments.forEach { attachment ->
            attachmentStorage.delete(attachment.path)
        }

        attachmentDao.deleteForEntry(entryId)
    }
}