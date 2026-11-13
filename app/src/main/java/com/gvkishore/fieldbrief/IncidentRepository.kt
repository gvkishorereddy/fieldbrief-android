package com.gvkishore.fieldbrief

import android.content.Context
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class BoardSnapshot(val incidents: List<Incident>, val source: String)
class IncidentRepository(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("fieldbrief", Context.MODE_PRIVATE)
    fun statuses(): Map<String, IncidentStatus> {
        val saved = runCatching { JSONObject(prefs.getString("statuses", "{}")!!) }.getOrDefault(JSONObject())
        return saved.keys().asSequence().mapNotNull { id ->
            runCatching { id to IncidentStatus.valueOf(saved.getString(id)) }.getOrNull()
        }.toMap()
    }
    fun saveStatus(id: String, status: IncidentStatus) {
        val updated = statuses().toMutableMap().apply { put(id, status) }
        val json = JSONObject().apply { updated.forEach { (key, value) -> put(key, value.name) } }
        prefs.edit().putString("statuses", json.toString()).apply()
    }
    fun local(): BoardSnapshot {
        val cached = prefs.getString("feed", null)
        if (cached != null) runCatching { IncidentJson.parse(cached) }.getOrNull()?.let {
            return BoardSnapshot(IncidentBoard.withStatuses(it, statuses()), "Offline cache · saved feed")
        }
        val seed = app.assets.open("incidents.json").bufferedReader().use { it.readText() }
        return BoardSnapshot(IncidentBoard.withStatuses(IncidentJson.parse(seed), statuses()), "Bundled demo · ready offline")
    }
    fun refresh(): BoardSnapshot {
        val connection = URL(FEED).openConnection() as HttpsURLConnection
        try {
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.instanceFollowRedirects = false
            require(connection.responseCode == 200) { "Feed unavailable" }
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    require(output.size() + count <= 262144) { "Feed is too large" }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            require(bytes.size <= 262144) { "Feed is too large" }
            val text = bytes.toString(Charsets.UTF_8)
            val incidents = IncidentJson.parse(text)
            prefs.edit().putString("feed", text).apply()
            return BoardSnapshot(IncidentBoard.withStatuses(incidents, statuses()), "HTTPS demo feed · just refreshed")
        } finally { connection.disconnect() }
    }
    companion object {
        const val FEED = "https://raw.githubusercontent.com/gvkishorereddy/fieldbrief-android/main/sample-data/incidents.json"
    }
}
