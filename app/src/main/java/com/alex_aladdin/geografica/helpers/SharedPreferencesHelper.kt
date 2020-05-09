package com.alex_aladdin.geografica.helpers

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.alex_aladdin.geografica.di.ServiceLocator

@ServiceLocator.Service
class SharedPreferencesHelper(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(GEO_PREFERENCES, MODE_PRIVATE)

    fun getBool(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun setBool(key: String, value: Boolean) {
        sharedPreferences.edit {
            putBoolean(key, value)
        }
    }

    fun getBoolAndUpdate(key: String, defaultValue: Boolean, newValue: Boolean): Boolean {
        val value = sharedPreferences.getBoolean(key, defaultValue)
        sharedPreferences.edit {
            putBoolean(key, newValue)
        }
        return value
    }

    fun incrementCounter(key: String, add: Long = 1): Long {
        var value = sharedPreferences.getLong(key, 0)
        value += add
        sharedPreferences.edit {
            putLong(key, value)
        }
        return value
    }

    companion object {
        private const val GEO_PREFERENCES = "com.alex_aladdin.geografica.GEO_PREFERENCES"
        const val PREFS_IS_FIRST_LAUNCH = "is_first_launch"
        const val PREFS_ANALYTICS_SESSIONS_COUNT = "analytics_sessions_count"
        const val PREFS_ANALYTICS_TOTAL_TIME = "analytics_total_time"
        const val PREFS_CONTACT_FORM_WAS_CLOSED = "contact_form_was_closed"
        const val PREFS_CONTACT_FORM_WAS_SENT = "contact_form_was_sent"
    }
}