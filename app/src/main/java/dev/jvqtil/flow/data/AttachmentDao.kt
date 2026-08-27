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

    @Insert
    suspend fun insert(
        attachment: Attachment
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
}