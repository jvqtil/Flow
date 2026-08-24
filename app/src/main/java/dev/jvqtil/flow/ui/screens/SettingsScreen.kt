package dev.jvqtil.flow.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.jvqtil.flow.BuildConfig
import dev.jvqtil.flow.data.AppPreferences
import kotlinx.coroutines.launch

@SuppressLint("UseKtx")
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    var amoled by remember {
        mutableStateOf(false)
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        AppPreferences
            .observeAmoled(context)
            .collect { enabled ->
                amoled = enabled
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        Text(
            text = "Appearance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "AMOLED",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Use pure black background color",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = amoled,
                enabled = isDark,
                onCheckedChange = { enabled ->
                    amoled = enabled

                    scope.launch {
                        AppPreferences.setAmoled(
                            context = context,
                            enabled = enabled
                        )
                    }
                }
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Flow ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/jvqtil/Flow".toUri()
                        )
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "GitHub",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Icon(
                            imageVector = Icons.Default.NorthEast,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}