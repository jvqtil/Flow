package dev.jvqtil.flow.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryListDao {

    @Query(
        "SELECT * FROM lists " +
                "ORDER BY position ASC"
    )
    fun observeLists(): Flow<List<EntryList>>

    @Query(
        "SELECT * FROM lists " +
                "ORDER BY position ASC"
    )
    suspend fun getAllLists(): List<EntryList>

    @Query(
        "SELECT * FROM lists " +
                "WHERE id = :id " +
                "LIMIT 1"
    )
    suspend fun getById(
        id: String
    ): EntryList?

    @Query(
        "SELECT * FROM lists " +
                "WHERE id = :id " +
                "LIMIT 1"
    )
    fun observeById(
        id: String
    ): Flow<EntryList?>

    @Insert
    suspend fun insert(
        list: EntryList
    )

    @Update
    suspend fun update(
        list: EntryList
    )

    @Delete
    suspend fun delete(
        list: EntryList
    )

    @Upsert
    suspend fun upsert(
        list: EntryList
    )
}