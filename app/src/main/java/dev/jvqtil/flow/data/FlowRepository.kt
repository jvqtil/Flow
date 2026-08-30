package dev.jvqtil.flow.data

import android.net.Uri
import androidx.room3.withWriteTransaction
import kotlinx.coroutines.flow.Flow

data class DeletedFolderSnapshot(
    val folder: Folder,
    val entries: List<Entry>,
    val attachments: List<Attachment>
)

class FlowRepository(
    private val database: FlowDatabase,
    private val entryDao: EntryDao,
    private val folderDao: FolderDao,
    private val attachmentDao: AttachmentDao,
    private val attachmentStorage: AttachmentStorage
) {

    fun observeFolders(): Flow<List<Folder>> {
        return folderDao.observeFolders()
    }

    suspend fun getAllFolders(): List<Folder> {
        return folderDao.getAllFolders()
    }

    suspend fun findFolderById(
        id: String
    ): Folder? {
        return folderDao.getById(id)
    }

    suspend fun insertFolder(
        folder: Folder
    ) {
        folderDao.insert(folder)
    }

    suspend fun updateFolder(
        folder: Folder
    ) {
        folderDao.update(folder)
    }

    suspend fun deleteFolderWithEntries(
        folder: Folder
    ): DeletedFolderSnapshot {
        val entries =
            entryDao.getNotes(
                folderId = folder.id
            )

        val attachments =
            entries.flatMap { entry ->
                attachmentDao.getForEntry(
                    entry.id
                )
            }

        database.withWriteTransaction {
            attachmentDao.deleteForFolder(
                folder.id
            )

            entryDao.deleteByFolderId(
                folder.id
            )

            folderDao.delete(
                folder
            )
        }

        return DeletedFolderSnapshot(
            folder = folder,
            entries = entries,
            attachments = attachments
        )
    }

    suspend fun restoreFolder(
        folder: Folder
    ) {
        folderDao.upsert(folder)
    }

    suspend fun restoreFolderWithEntries(
        snapshot: DeletedFolderSnapshot
    ) {
        database.withWriteTransaction {
            folderDao.insert(
                snapshot.folder
            )

            entryDao.insertAll(
                snapshot.entries
            )

            attachmentDao.insertAll(
                snapshot.attachments
            )
        }
    }

    fun observeEntries(): Flow<List<Entry>> {
        return entryDao.observeAllNotes()
    }

    suspend fun getAllEntries(): List<Entry> {
        return entryDao.getAllNotes()
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
            folderId = entry.folderId
        )

        entryDao.insert(
            entry.copy(
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
        folderId: String,
        entryIds: List<String>
    ) {
        entryIds.forEachIndexed { index, id ->
            entryDao.updatePositionInFolder(
                id = id,
                folderId = folderId,
                position = index.toLong()
            )
        }
    }

    suspend fun moveEntryToFolder(
        entry: Entry,
        targetFolderId: String
    ) {
        val nextPosition =
            entryDao.getNextPosition(
                folderId = targetFolderId
            )

        entryDao.moveToFolder(
            entryId = entry.id,
            folderId = targetFolderId,
            position = nextPosition
        )
    }

    fun observeAttachments(
        entryId: String
    ): Flow<List<Attachment>> {
        return attachmentDao.observeForEntry(
            entryId
        )
    }

    suspend fun insertAttachment(
        entryId: String,
        uri: Uri
    ): Attachment {
        val metadata =
            attachmentStorage.getMetadata(
                uri
            )

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
            attachmentDao.insert(
                attachment
            )
        } catch (error: Throwable) {
            attachmentStorage.delete(
                path
            )

            throw error
        }

        return attachment
    }

    suspend fun deleteAttachment(
        attachment: Attachment
    ) {
        attachmentDao.delete(
            attachment
        )
    }

    suspend fun restoreAttachment(
        attachment: Attachment
    ) {
        attachmentDao.insert(
            attachment
        )
    }

    fun permanentlyDeleteAttachment(
        attachment: Attachment
    ) {
        attachmentStorage.delete(
            attachment.path
        )
    }

    suspend fun purgeAttachments(
        entryId: String
    ) {
        val attachments =
            attachmentDao.getForEntry(
                entryId
            )

        attachments.forEach { attachment ->
            attachmentStorage.delete(
                attachment.path
            )
        }

        attachmentDao.deleteForEntry(
            entryId
        )
    }

    fun permanentlyDeleteFolderSnapshot(
        snapshot: DeletedFolderSnapshot
    ) {
        snapshot.attachments.forEach { attachment ->
            attachmentStorage.delete(
                attachment.path
            )
        }
    }

    fun observeEntryIdsWithAttachments(): Flow<List<String>> {
        return attachmentDao.observeEntryIdsWithAttachments()
    }
}