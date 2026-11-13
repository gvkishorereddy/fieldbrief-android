package com.gvkishore.fieldbrief

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val ink = Color.rgb(22, 41, 55)
    private val teal = Color.rgb(0, 109, 119)
    private lateinit var repository: IncidentRepository
    private lateinit var cards: LinearLayout
    private lateinit var source: TextView
    private lateinit var count: TextView
    private lateinit var refresh: Button
    private var incidents = emptyList<Incident>()
    private var filter: Severity? = null
    private val executor = Executors.newSingleThreadExecutor()
    private var active = true
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun text(value: String, size: Float = 15f, color: Int = ink, bold: Boolean = false) = TextView(this).apply {
        text = value; textSize = size; setTextColor(color)
        if (bold) setTypeface(typeface, Typeface.BOLD)
        setPadding(0, dp(4), 0, dp(4))
    }
    private fun vertical() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = IncidentRepository(this)
        filter = savedInstanceState?.getString("filter")?.let { Severity.valueOf(it) }
        val root = vertical().apply { setBackgroundColor(Color.rgb(243,245,247)) }
        root.setOnApplyWindowInsetsListener { view, insets ->
            @Suppress("DEPRECATION")
            view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }
        val header = vertical().apply { setBackgroundColor(ink); setPadding(dp(22), dp(18), dp(22), dp(18)) }
        header.addView(text("FIELDBRIEF / TRAINING", 12f, Color.rgb(109,222,212), true))
        header.addView(text("A clearer field briefing.", 25f, Color.WHITE, true))
        header.addView(text("Fictional incidents. Local practice. No dispatch.", 13f, Color.LTGRAY))
        root.addView(header)
        val controls = vertical().apply { setPadding(dp(20), dp(12), dp(20), dp(8)) }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        count = text("", 18f, ink, true)
        row.addView(count, LinearLayout.LayoutParams(0, dp(52), 1f))
        refresh = Button(this).apply { text = "Refresh"; contentDescription = "Refresh demo feed"; setOnClickListener { loadRemote() } }
        row.addView(refresh)
        controls.addView(row)
        source = text("", 12f, teal)
        source.accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        controls.addView(source)
        val filters = LinearLayout(this)
        listOf("All" to null, "High" to Severity.HIGH, "Medium" to Severity.MEDIUM, "Low" to Severity.LOW).forEach { (label, severity) ->
            filters.addView(Button(this).apply {
                text = label; isAllCaps = false; textSize = 12f; tag = severity
                contentDescription = "Filter $label"
                setOnClickListener { filter = severity; render() }
            }, LinearLayout.LayoutParams(0, dp(48), 1f))
        }
        controls.addView(filters)
        root.addView(controls)
        cards = vertical().apply { setPadding(dp(20), 0, dp(20), dp(16)) }
        root.addView(ScrollView(this).apply { addView(cards) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        val snapshot = repository.local()
        incidents = snapshot.incidents; source.text = snapshot.source; render()
        if (!intent.getBooleanExtra("offlineDemo", false)) loadRemote()
    }
    private fun render() {
        cards.removeAllViews()
        val visible = IncidentBoard.visible(incidents, filter)
        count.text = "${visible.size} incidents · ${filter?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "All"}"
        visible.forEach { item ->
            val card = vertical().apply {
                setPadding(dp(16), dp(12), dp(16), dp(12))
                background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dp(14).toFloat(); setStroke(dp(1), Color.rgb(222,229,233)) }
            }
            val badgeColor = when (item.severity) { Severity.HIGH -> Color.rgb(161,57,45); Severity.MEDIUM -> Color.rgb(133,93,16); Severity.LOW -> teal }
            card.addView(text("${item.id}  /  ${item.severity.name}", 11f, badgeColor, true))
            card.addView(text(item.title, 19f, ink, true))
            card.addView(text(item.area, 12f, teal, true))
            card.addView(text(item.brief, 14f))
            card.addView(text("Status: ${item.status.label}", 13f, ink, true))
            if (item.status != IncidentStatus.RESOLVED) card.addView(Button(this).apply {
                text = "Mark ${item.status.next().label}"; isAllCaps = false
                contentDescription = "${item.id}: mark ${item.status.next().label}"
                setOnClickListener {
                    repository.saveStatus(item.id, item.status.next())
                    incidents = incidents.map { if (it.id == item.id) it.copy(status = item.status.next()) else it }
                    render()
                }
            })
            cards.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })
        }
        if (visible.isEmpty()) cards.addView(text("No incidents in this filter."))
    }
    private fun loadRemote() {
        refresh.isEnabled = false; source.text = "Refreshing HTTPS demo feed…"
        executor.execute {
            val result = runCatching { repository.refresh() }
            runOnUiThread {
                if (!active) return@runOnUiThread
                refresh.isEnabled = true
                result.onSuccess {
                    // Read status overrides again so a slow refresh cannot undo a local update.
                    incidents = IncidentBoard.withStatuses(it.incidents, repository.statuses()); source.text = it.source
                }.onFailure { source.text = "Offline · showing saved demo incidents" }
                render()
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) { outState.putString("filter", filter?.name); super.onSaveInstanceState(outState) }
    override fun onDestroy() { active = false; executor.shutdownNow(); super.onDestroy() }
}
