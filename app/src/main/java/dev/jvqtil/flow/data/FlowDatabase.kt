package dev.jvqtil.flow.data

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [
        Entry::class,
        Folder::class,
        Attachment::class
    ],
    version = 5,
    exportSchema = false
)
abstract class FlowDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao

    abstract fun folderDao(): FolderDao

    abstract fun attachmentDao(): AttachmentDao
}