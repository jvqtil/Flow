package dev.jvqtil.flow.data

import android.content.Context
import androidx.room3.Room
import androidx.room3.migration.Migration
import androidx.sqlite.execSQL

private val MIGRATION_1_2 = object : Migration(1, 2) {

    override suspend fun migrate(
        connection: androidx.sqlite.SQLiteConnection
    ) {
        connection.execSQL(
            """
            ALTER TABLE notes
            ADD COLUMN position INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        connection.execSQL(
            """
            UPDATE notes
            SET position = (
                SELECT COUNT(*)
                FROM notes AS newer
                WHERE newer.createdAt > notes.createdAt
                   OR (
                       newer.createdAt = notes.createdAt
                       AND newer.id > notes.id
                   )
            )
            """.trimIndent()
        )
    }
}

object DatabaseProvider {

    @Volatile
    private var instance: FlowDatabase? = null

    fun get(context: Context): FlowDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                FlowDatabase::class.java,
                "flow.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also {
                    instance = it
                }
        }
    }
}