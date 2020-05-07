package com.alex_aladdin.geografica

import android.content.Context
import android.util.Log
import androidx.core.os.bundleOf
import com.alex_aladdin.geografica.di.ServiceLocator
import com.alex_aladdin.geografica.helpers.SharedPreferencesHelper
import com.alex_aladdin.geografica.helpers.SharedPreferencesHelper.Companion.PREFS_ANALYTICS_SESSIONS_COUNT
import com.alex_aladdin.geografica.helpers.SharedPreferencesHelper.Companion.PREFS_ANALYTICS_TOTAL_TIME
import com.google.firebase.analytics.FirebaseAnalytics

@ServiceLocator.Service
class Analytics(context: Context, private val sharedPrefsHelper: SharedPreferencesHelper) {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    private var startSessionTime: Long? = null

    fun setInitialProperties(locale: String, startVersion: String) {
        Log.d(TAG, "setStaticProperties, locale: $locale, startVersion: $startVersion")
        firebaseAnalytics.setUserProperty("locale", locale)
        firebaseAnalytics.setUserProperty("start_version", startVersion)
    }

    fun zoomButtonClick(zoom: Zoom) {
        Log.d(TAG, "zoomButtonClick: ${zoom.name}")
        val bundle = bundleOf("direction" to zoom.name)
        firebaseAnalytics.logEvent("zoom_button_click", bundle)
    }

    fun addPieceButtonClick() {
        Log.d(TAG, "addPieceButtonClick")
        firebaseAnalytics.logEvent("add_piece_button_click", null)
    }

    fun mapInfoButtonClick() {
        Log.d(TAG, "mapInfoButtonClick")
        firebaseAnalytics.logEvent("map_info_button_click", null)
    }

    fun trainingStarted(mapName: String) {
        Log.d(TAG, "trainingStarted, mapName: $mapName")
        val bundle = bundleOf("map_name" to mapName)
        firebaseAnalytics.logEvent("training_started", bundle)
    }

    fun trainingFinished(mapName: String) {
        Log.d(TAG, "trainingFinished, mapName: $mapName")
        val bundle = bundleOf("map_name" to mapName)
        firebaseAnalytics.logEvent("training_finished", bundle)
    }

    fun championshipStarted() {
        Log.d(TAG, "championshipStarted")
        firebaseAnalytics.logEvent("championship_started", null)
    }

    fun championshipRoundFinished(roundNumber: Int) {
        Log.d(TAG, "championshipRoundFinished, roundNumber: $roundNumber")
        val bundle = bundleOf("round_number" to roundNumber)
        firebaseAnalytics.logEvent("championship_round_finished", bundle)
    }

    fun rateAppClick() {
        Log.d(TAG, "rateAppClick")
        firebaseAnalytics.logEvent("rate_app_click", null)
    }

    fun startSession() {
        startSessionTime = System.currentTimeMillis()
        val sessionsCount = sharedPrefsHelper.incrementCounter(PREFS_ANALYTICS_SESSIONS_COUNT)
        firebaseAnalytics.setUserProperty("sessions_count", sessionsCount.toString())
        Log.d(TAG, "startSession, sessionsCount: $sessionsCount")
    }

    fun finishSession() {
        val sessionLength = startSessionTime?.let { System.currentTimeMillis() - it } ?: 0L
        val totalLength = sharedPrefsHelper.incrementCounter(PREFS_ANALYTICS_TOTAL_TIME, sessionLength)
        firebaseAnalytics.setUserProperty("time_spent", totalLength.toString())
        Log.d(TAG, "finishSession, totalLength: $totalLength")
    }

    enum class Zoom { IN, OUT }

    companion object {
        private const val TAG = "GeoAnalytics"
    }
}