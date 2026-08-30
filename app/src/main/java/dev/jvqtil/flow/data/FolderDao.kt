package dev.jvqtil.flow.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query(
        "SELECT * FROM folders " +
                "ORDER BY position ASC"
    )
    fun observeFolders(): Flow<List<Folder>>

    @Query(
        "SELECT * FROM folders " +
                "ORDER BY position ASC"
    )
    suspend fun getAllFolders(): List<Folder>

    @Query(
        "SELECT * FROM folders " +
                "WHERE id = :id " +
                "LIMIT 1"
    )
    suspend fun getById(
        id: String
    ): Folder?

    @Query(
        "SELECT * FROM folders " +
                "WHERE id = :id " +
                "LIMIT 1"
    )
    fun observeById(
        id: String
    ): Flow<Folder?>

    @Insert
    suspend fun insert(
        folder: Folder
    )

    @Update
    suspend fun update(
        folder: Folder
    )

    @Delete
    suspend fun delete(
        folder: Folder
    )

    @Upsert
    suspend fun upsert(
        folder: Folder
    )
}