package dev.jvqtil.flow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.jvqtil.flow.data.DatabaseProvider
import dev.jvqtil.flow.data.NoteRepository
import dev.jvqtil.flow.navigation.FlowNavHost
import dev.jvqtil.flow.ui.theme.FlowTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val database = DatabaseProvider.get(applicationContext)
        val repository = NoteRepository(database.noteDao())

        setContent {
            FlowTheme {
                FlowNavHost(
                    repository = repository
                )
            }
        }
    }
}