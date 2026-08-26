package dev.jvqtil.flow.data

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [Entry::class],
    version = 3,
    exportSchema = false
)
abstract class FlowDatabase : RoomDatabase() {

    abstract fun entryDao(): FlowDao
}