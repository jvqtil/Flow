package dev.jvqtil.flow.data

import android.net.Uri
import androidx.room3.withWriteTransaction
import kotlinx.coroutines.flow.Flow

data class DeletedListSnapshot(
    val list: EntryList,
    val entries: List<Entry>
)

class FlowRepository(
    private val database: FlowDatabase,
    private val entryDao: EntryDao,
    private val entryListDao: EntryListDao,
    private val attachmentDao: AttachmentDao,
    private val attachmentStorage: AttachmentStorage
) {

    fun observeLists(): Flow<List<EntryList>> {
        return entryListDao.observeLists()
    }

    suspend fun getAllLists(): List<EntryList> {
        return entryListDao.getAllLists()
    }

    suspend fun findListById(
        id: String
    ): EntryList? {
        return entryListDao.getById(id)
    }

    suspend fun insertList(
        list: EntryList
    ) {
        entryListDao.insert(list)
    }

    suspend fun updateList(
        list: EntryList
    ) {
        entryListDao.update(list)
    }

    suspend fun deleteList(
        list: EntryList
    ) {
        entryListDao.delete(list)
    }

    suspend fun deleteListWithEntries(
        list: EntryList
    ) {
        database.withWriteTransaction {
            entryDao.deleteByListId(list.id)
            entryListDao.delete(list)
        }
    }

    suspend fun restoreList(
        list: EntryList
    ) {
        entryListDao.upsert(list)
    }

    suspend fun restoreListWithEntries(
        snapshot: DeletedListSnapshot
    ) {
        database.withWriteTransaction {
            entryListDao.insert(
                snapshot.list
            )

            entryDao.insertAll(
                snapshot.entries
            )
        }
    }

    fun observeEntries(): Flow<List<Entry>> {
        return entryDao.observeAllNotes()
    }

    fun observeEntries(
        listId: String
    ): Flow<List<Entry>> {
        return entryDao.observeNotes(listId)
    }

    suspend fun getAllEntries(): List<Entry> {
        return entryDao.getAllNotes()
    }

    suspend fun getEntries(
        listId: String
    ): List<Entry> {
        return entryDao.getNotes(listId)
    }

    suspend fun findById(
        id: String
    ): Entry? {
        return entryDao.getById(id)
    }

    suspend fun insertAtTop(
        entry: Entry
    ) {
        entryDao.shiftPositionsDown(
            listId = entry.listId
        )

        entryDao.insert(
            entry.copy(
                position = 0L
            )
        )
    }

    suspend fun insertAtTop(
        entry: Entry,
        listId: String
    ) {
        entryDao.shiftPositionsDown(
            listId = listId
        )

        entryDao.insert(
            entry.copy(
                listId = listId,
                position = 0L
            )
        )
    }

    suspend fun update(
        entry: Entry
    ) {
        entryDao.update(entry)
    }

    suspend fun delete(
        entry: Entry
    ) {
        entryDao.delete(entry)
    }

    suspend fun restore(
        entry: Entry
    ) {
        entryDao.upsert(entry)
    }

    suspend fun restore(
        entries: List<Entry>
    ) {
        entries.forEach { entry ->
            entryDao.upsert(entry)
        }
    }

    suspend fun updateEntriesPositions(
        listId: String,
        entryIds: List<String>
    ) {
        entryIds.forEachIndexed { index, id ->
            entryDao.updatePositionInList(
                id = id,
                listId = listId,
                position = index.toLong()
            )
        }
    }

    suspend fun moveEntryToList(
        entry: Entry,
        targetListId: String
    ) {
        val nextPosition =
            entryDao.getNextPosition(
                listId = targetListId
            )

        entryDao.moveToList(
            entryId = entry.id,
            listId = targetListId,
            position = nextPosition
        )
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

    suspend fun purgeAttachments(
        entryId: String
    ) {
        val attachments =
            attachmentDao.getForEntry(entryId)

        attachments.forEach { attachment ->
            attachmentStorage.delete(attachment.path)
        }

        attachmentDao.deleteForEntry(entryId)
    }

    suspend fun purgeListAttachments(
        entries: List<Entry>
    ) {
        entries.forEach { entry ->
            purgeAttachments(
                entry.id
            )
        }
    }
}