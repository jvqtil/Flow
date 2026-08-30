package dev.jvqtil.flow.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Query(
        "SELECT * FROM notes " +
                "WHERE folderId = :folderId " +
                "ORDER BY position ASC, createdAt DESC"
    )
    fun observeNotes(
        folderId: String
    ): Flow<List<Entry>>

    @Query(
        "SELECT * FROM notes " +
                "ORDER BY position ASC, createdAt DESC"
    )
    fun observeAllNotes(): Flow<List<Entry>>

    @Query(
        "SELECT * FROM notes " +
                "WHERE folderId = :folderId " +
                "ORDER BY position ASC, createdAt DESC"
    )
    suspend fun getNotes(
        folderId: String
    ): List<Entry>

    @Query(
        "SELECT * FROM notes " +
                "ORDER BY position ASC, createdAt DESC"
    )
    suspend fun getAllNotes(): List<Entry>

    @Query(
        "SELECT * FROM notes " +
                "WHERE id = :id " +
                "LIMIT 1"
    )
    suspend fun getById(
        id: String
    ): Entry?

    @Query(
        "UPDATE notes " +
                "SET position = position + 1 " +
                "WHERE folderId = :folderId"
    )
    suspend fun shiftPositionsDown(
        folderId: String
    )

    @Query(
        "UPDATE notes " +
                "SET position = position + 1 " +
                "WHERE folderId = :folderId " +
                "AND position >= :fromPosition"
    )
    suspend fun shiftPositionsDownFrom(
        folderId: String,
        fromPosition: Long
    )

    @Query(
        "UPDATE notes " +
                "SET position = :position " +
                "WHERE id = :id " +
                "AND folderId = :folderId"
    )
    suspend fun updatePositionInFolder(
        id: String,
        folderId: String,
        position: Long
    )

    @Query(
        "UPDATE notes " +
                "SET position = :position " +
                "WHERE id = :id"
    )
    suspend fun updatePosition(
        id: String,
        position: Long
    )

    @Query(
        "SELECT COALESCE(MAX(position), -1) + 1 " +
                "FROM notes " +
                "WHERE folderId = :folderId"
    )
    suspend fun getNextPosition(
        folderId: String
    ): Long

    @Query(
        "UPDATE notes " +
                "SET folderId = :folderId, " +
                "position = :position " +
                "WHERE id = :entryId"
    )
    suspend fun moveToFolder(
        entryId: String,
        folderId: String,
        position: Long
    )

    @Insert
    suspend fun insert(
        entry: Entry
    )

    @Insert
    suspend fun insertAll(
        entries: List<Entry>
    )

    @Update
    suspend fun update(
        entry: Entry
    )

    @Delete
    suspend fun delete(
        entry: Entry
    )

    @Query(
        "DELETE FROM notes " +
                "WHERE folderId = :folderId"
    )
    suspend fun deleteByFolderId(
        folderId: String
    )

    @Upsert
    suspend fun upsert(
        entry: Entry
    )
}