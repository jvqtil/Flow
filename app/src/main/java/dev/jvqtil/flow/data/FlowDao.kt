package dev.jvqtil.flow.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FlowDao {

    @Query(
        "SELECT * FROM notes " +
                "ORDER BY position ASC, createdAt DESC"
    )
    fun observeNotes(): Flow<List<Entry>>

    @Query(
        "SELECT * FROM notes " +
                "ORDER BY position ASC, createdAt DESC"
    )
    suspend fun getAllNotes(): List<Entry>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Entry?

    @Query(
        "UPDATE notes " +
                "SET position = position + 1"
    )
    suspend fun shiftPositionsDown()

    @Query(
        "UPDATE notes SET position = :position " +
                "WHERE id = :id"
    )
    suspend fun updatePosition(
        id: String,
        position: Long
    )

    @Insert
    suspend fun insert(entry: Entry)

    @Update
    suspend fun update(entry: Entry)

    @Delete
    suspend fun delete(entry: Entry)

    @Upsert
    suspend fun upsert(entry: Entry)
}