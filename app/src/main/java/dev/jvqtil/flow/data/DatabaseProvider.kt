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

private val MIGRATION_2_3 = object : Migration(2, 3) {

    override suspend fun migrate(
        connection: androidx.sqlite.SQLiteConnection
    ) {
        connection.execSQL(
            """
            ALTER TABLE notes
            ADD COLUMN type TEXT NOT NULL DEFAULT 'note'
            """.trimIndent()
        )

        connection.execSQL(
            """
            ALTER TABLE notes
            ADD COLUMN completed INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {

    override suspend fun migrate(
        connection: androidx.sqlite.SQLiteConnection
    ) {
        connection.execSQL(
            """
            CREATE TABLE attachments (
                id TEXT NOT NULL,
                entryId TEXT NOT NULL,
                path TEXT NOT NULL,
                fileName TEXT NOT NULL,
                mimeType TEXT,
                size INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            CREATE INDEX index_attachments_entryId
            ON attachments(entryId)
            """.trimIndent()
        )
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {

    override suspend fun migrate(
        connection: androidx.sqlite.SQLiteConnection
    ) {
        connection.execSQL(
            """
            CREATE TABLE lists (
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                position INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            INSERT INTO lists (
                id,
                name,
                position
            )
            VALUES (
                '$ALL_LIST_ID',
                'All',
                0
            )
            """.trimIndent()
        )

        connection.execSQL(
            """
            ALTER TABLE notes
            ADD COLUMN listId TEXT NOT NULL DEFAULT '$ALL_LIST_ID'
            """.trimIndent()
        )

        connection.execSQL(
            """
            UPDATE notes
            SET listId = '$ALL_LIST_ID'
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
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5
                )
                .build()
                .also {
                    instance = it
                }
        }
    }
}