package dev.jvqtil.flow.data

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [
        Entry::class,
        Attachment::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FlowDatabase : RoomDatabase() {

    abstract fun entryDao(): FlowDao

    abstract fun attachmentDao(): AttachmentDao
}