package com.gvkishore.fieldbrief

import org.junit.Assert.*
import org.junit.Test
class IncidentBoardTest {
    private fun item(id: String, severity: Severity, status: IncidentStatus = IncidentStatus.NEW) = Incident(id, "Training", "Demo", severity, "Fictional", status)
    @Test fun highPriorityFirst() { assertEquals("H", IncidentBoard.visible(listOf(item("L", Severity.LOW), item("H", Severity.HIGH))).first().id) }
    @Test fun filtersSeverity() { assertEquals(listOf("H"), IncidentBoard.visible(listOf(item("L", Severity.LOW), item("H", Severity.HIGH)), Severity.HIGH).map { it.id }) }
    @Test fun resolvedGoesLast() { assertEquals("L", IncidentBoard.visible(listOf(item("H", Severity.HIGH, IncidentStatus.RESOLVED), item("L", Severity.LOW))).first().id) }
    @Test fun emptyIsValid() { assertTrue(IncidentBoard.visible(emptyList()).isEmpty()) }
    @Test fun tieOrderIsDeterministic() { assertEquals(listOf("A", "B"), IncidentBoard.visible(listOf(item("B", Severity.HIGH), item("A", Severity.HIGH))).map { it.id }) }
    @Test fun statusOverrideApplied() { assertEquals(IncidentStatus.EN_ROUTE, IncidentBoard.withStatuses(listOf(item("A", Severity.HIGH)), mapOf("A" to IncidentStatus.EN_ROUTE)).single().status) }
    @Test fun unknownOverrideDoesNotCreateIncident() { assertEquals(1, IncidentBoard.withStatuses(listOf(item("A", Severity.HIGH)), mapOf("B" to IncidentStatus.RESOLVED)).size) }
    @Test fun missingOverrideRetainsStatus() { assertEquals(IncidentStatus.ACKNOWLEDGED, IncidentBoard.withStatuses(listOf(item("A", Severity.LOW, IncidentStatus.ACKNOWLEDGED)), emptyMap()).single().status) }
    @Test fun advancesStatus() { assertEquals(IncidentStatus.EN_ROUTE, IncidentStatus.ACKNOWLEDGED.next()) }
    @Test fun resolvedIsTerminal() { assertEquals(IncidentStatus.RESOLVED, IncidentStatus.RESOLVED.next()) }
    private val valid = """[{"id":"TR-1","title":"Drill","area":"Training","severity":"HIGH","brief":"Fictional"}]"""
    @Test fun parsesValidFeed() { assertEquals("TR-1", IncidentJson.parse(valid).single().id) }
    @Test(expected = IllegalArgumentException::class) fun rejectsDuplicateIds() { IncidentJson.parse(valid.dropLast(1) + "," + valid.drop(1)) }
    @Test(expected = IllegalArgumentException::class) fun rejectsUnknownSeverity() { IncidentJson.parse(valid.replace("HIGH", "URGENT")) }
    @Test(expected = IllegalArgumentException::class) fun rejectsBlankTitle() { IncidentJson.parse(valid.replace("Drill", " ")) }
    @Test(expected = IllegalArgumentException::class) fun rejectsEmptyFeed() { IncidentJson.parse("[]") }
    @Test(expected = IllegalArgumentException::class) fun rejectsOversizePayload() { IncidentJson.parse(" ".repeat(262145)) }
}
