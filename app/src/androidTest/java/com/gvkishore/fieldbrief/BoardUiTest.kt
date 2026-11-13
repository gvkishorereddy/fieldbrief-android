package com.gvkishore.fieldbrief

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class BoardUiTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    @Before fun reset() { context.getSharedPreferences("fieldbrief", Context.MODE_PRIVATE).edit().clear().commit() }
    private fun launch() = ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java).putExtra("offlineDemo", true))
    @Test fun bundledDataWorksOffline() { launch().use {
        onView(withText("4 incidents · All")).check(matches(isDisplayed()))
        onView(withText("Bundled demo · ready offline")).check(matches(isDisplayed()))
    } }
    @Test fun severityFilterSurvivesRecreation() { launch().use {
        onView(withContentDescription("Filter High")).perform(click()); it.recreate()
        onView(withText("1 incidents · High")).check(matches(isDisplayed()))
        onView(withText("Training center alarm drill")).check(matches(isDisplayed()))
    } }
    @Test fun statusSurvivesRecreation() { launch().use {
        onView(withContentDescription("TR-101: mark Acknowledged")).perform(click()); it.recreate()
        onView(withText("Status: Acknowledged")).check(matches(isDisplayed()))
    } }
}
