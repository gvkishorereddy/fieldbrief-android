package com.gvkishore.fieldbrief

enum class Severity { HIGH, MEDIUM, LOW }
enum class IncidentStatus(val label: String) {
    NEW("New"), ACKNOWLEDGED("Acknowledged"), EN_ROUTE("En route"), RESOLVED("Resolved");
    fun next(): IncidentStatus = entries[(ordinal + 1).coerceAtMost(entries.lastIndex)]
}
data class Incident(val id: String, val title: String, val area: String, val severity: Severity, val brief: String, val status: IncidentStatus = IncidentStatus.NEW)
object IncidentBoard {
    fun visible(items: List<Incident>, severity: Severity? = null): List<Incident> =
        items.filter { severity == null || it.severity == severity }
            .sortedWith(compareBy<Incident> { it.status == IncidentStatus.RESOLVED }.thenBy { it.severity.ordinal }.thenBy { it.id })
    fun withStatuses(items: List<Incident>, statuses: Map<String, IncidentStatus>): List<Incident> =
        items.map { it.copy(status = statuses[it.id] ?: it.status) }
}
