package dev.jvqtil.flow.data

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [Note::class],
    version = 2,
    exportSchema = false
)
abstract class FlowDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
}