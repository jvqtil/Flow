package dev.jvqtil.flow.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Query(
        "SELECT * FROM attachments " +
                "WHERE entryId = :entryId " +
                "ORDER BY createdAt ASC"
    )
    fun observeForEntry(
        entryId: String
    ): Flow<List<Attachment>>

    @Query(
        "SELECT * FROM attachments " +
                "WHERE entryId = :entryId " +
                "ORDER BY createdAt ASC"
    )
    suspend fun getForEntry(
        entryId: String
    ): List<Attachment>

    @Query(
        "SELECT * FROM attachments"
    )
    suspend fun getAll(): List<Attachment>

    @Insert
    suspend fun insert(
        attachment: Attachment
    )

    @Insert
    suspend fun insertAll(
        attachments: List<Attachment>
    )

    @Delete
    suspend fun delete(
        attachment: Attachment
    )

    @Query(
        "DELETE FROM attachments " +
                "WHERE entryId = :entryId"
    )
    suspend fun deleteForEntry(
        entryId: String
    )

    @Query(
        "DELETE FROM attachments " +
                "WHERE entryId IN (" +
                "SELECT id FROM notes " +
                "WHERE folderId = :folderId" +
                ")"
    )
    suspend fun deleteForFolder(
        folderId: String
    )

    @Query(
        "SELECT DISTINCT entryId FROM attachments"
    )
    fun observeEntryIdsWithAttachments(): Flow<List<String>>

    @Query(
        "DELETE FROM attachments"
    )
    suspend fun deleteAll()
}