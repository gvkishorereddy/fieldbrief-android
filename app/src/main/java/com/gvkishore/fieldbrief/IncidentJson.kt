package com.gvkishore.fieldbrief

import org.json.JSONArray
object IncidentJson {
    fun parse(text: String): List<Incident> {
        require(text.length <= 262144) { "Feed is too large" }
        val array = JSONArray(text)
        require(array.length() in 1..100) { "Feed must contain 1–100 incidents" }
        val ids = mutableSetOf<String>()
        fun String.checked(max: Int): String = trim().also { require(it.isNotEmpty() && it.length <= max) { "Invalid field length" } }
        return (0 until array.length()).map { index ->
            val row = array.getJSONObject(index)
            val id = row.getString("id").checked(24)
            require(id.matches(Regex("[A-Z0-9-]+")) && ids.add(id)) { "Invalid or duplicate incident ID" }
            Incident(id, row.getString("title").checked(100), row.getString("area").checked(100),
                Severity.valueOf(row.getString("severity")), row.getString("brief").checked(500))
        }
    }
}
