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
                "WHERE listId = :listId " +
                "ORDER BY position ASC, createdAt DESC"
    )
    fun observeNotes(
        listId: String
    ): Flow<List<Entry>>

    @Query(
        "SELECT * FROM notes " +
                "ORDER BY position ASC, createdAt DESC"
    )
    fun observeAllNotes(): Flow<List<Entry>>

    @Query(
        "SELECT * FROM notes " +
                "WHERE listId = :listId " +
                "ORDER BY position ASC, createdAt DESC"
    )
    suspend fun getNotes(
        listId: String
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
                "WHERE listId = :listId"
    )
    suspend fun shiftPositionsDown(
        listId: String
    )

    @Query(
        "UPDATE notes " +
                "SET position = position + 1 " +
                "WHERE listId = :listId " +
                "AND position >= :fromPosition"
    )
    suspend fun shiftPositionsDownFrom(
        listId: String,
        fromPosition: Long
    )

    @Query(
        "UPDATE notes " +
                "SET position = :position " +
                "WHERE id = :id " +
                "AND listId = :listId"
    )
    suspend fun updatePositionInList(
        id: String,
        listId: String,
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
                "WHERE listId = :listId"
    )
    suspend fun getNextPosition(
        listId: String
    ): Long

    @Query(
        "UPDATE notes " +
                "SET listId = :listId, " +
                "position = :position " +
                "WHERE id = :entryId"
    )
    suspend fun moveToList(
        entryId: String,
        listId: String,
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
                "WHERE listId = :listId"
    )
    suspend fun deleteByListId(
        listId: String
    )

    @Upsert
    suspend fun upsert(
        entry: Entry
    )
}