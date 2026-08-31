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
    fun parseVersion(version: String): Pair<List<Int>, Int> {
        val parts = version.split("-", limit = 2)

        val versionParts =
            parts[0]
                .split(".")
                .map { it.toIntOrNull() ?: 0 }

        val revision =
            parts
                .getOrNull(1)
                ?.toIntOrNull()
                ?: 0

        return versionParts to revision
    }

    val (latestVersion, latestRevision) =
        parseVersion(latest)

    val (currentVersion, currentRevision) =
        parseVersion(current)

    val size =
        maxOf(
            latestVersion.size,
            currentVersion.size
        )

    for (i in 0 until size) {
        val latestPart =
            latestVersion.getOrNull(i) ?: 0

        val currentPart =
            currentVersion.getOrNull(i) ?: 0

        when {
            latestPart > currentPart -> return true
            latestPart < currentPart -> return false
        }
    }

    return latestRevision > currentRevision
}