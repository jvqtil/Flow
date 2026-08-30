package dev.jvqtil.flow.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateModel(
    val version: String,
    val url: String
)

class UpdateChecker {
    suspend fun check(
        currentVersion: String
    ): UpdateModel? =
        withContext(Dispatchers.IO) {
            val connection =
                URL(
                    "https://api.github.com/repos/jvqtil/flow/releases/latest"
                ).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 5_000
            connection.readTimeout = 5_000
            connection.setRequestProperty(
                "Accept",
                "application/vnd.github+json"
            )
            connection.setRequestProperty(
                "User-Agent",
                "Flow"
            )

            try {
                if (connection.responseCode !in 200..299) {
                    throw IllegalStateException(
                        "GitHub returned HTTP ${connection.responseCode}"
                    )
                }

                val response =
                    connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                val json =
                    JSONObject(response)

                val latestVersion =
                    json.getString("tag_name")
                        .removePrefix("v")

                if (!isNewerVersion(
                        latest = latestVersion,
                        current = currentVersion
                    )
                ) {
                    return@withContext null
                }

                UpdateModel(
                    version = latestVersion,
                    url = json.getString("html_url")
                )
            } finally {
                connection.disconnect()
            }
        }
}

private fun isNewerVersion(
    latest: String,
    current: String
): Boolean {
    val latestParts = latest.split(".")
    val currentParts = current.split(".")

    val size = maxOf(
        latestParts.size,
        currentParts.size
    )

    for (i in 0 until size) {
        val latestPart =
            latestParts
                .getOrNull(i)
                ?.toIntOrNull()
                ?: return false

        val currentPart =
            currentParts
                .getOrNull(i)
                ?.toIntOrNull()
                ?: 0

        when {
            latestPart > currentPart -> return true
            latestPart < currentPart -> return false
        }
    }

    return false
}