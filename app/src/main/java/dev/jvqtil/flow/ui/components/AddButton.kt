package dev.jvqtil.flow.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddButton(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(58.dp),
        shape = RoundedCornerShape(18.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add note"
        )
    }
}