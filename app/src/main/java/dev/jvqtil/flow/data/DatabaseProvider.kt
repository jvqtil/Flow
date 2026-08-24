package dev.jvqtil.flow.data

import android.content.Context
import androidx.room3.Room

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
                .build()
                .also {
                    instance = it
                }
        }
    }
}