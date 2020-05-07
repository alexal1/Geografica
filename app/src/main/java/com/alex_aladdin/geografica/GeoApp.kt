package com.alex_aladdin.geografica

import android.app.Application
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.alex_aladdin.geografica.di.ServiceLocator
import com.alex_aladdin.geografica.di.get
import com.alex_aladdin.geografica.di.inject
import com.alex_aladdin.geografica.di.register
import com.alex_aladdin.geografica.helpers.SharedPreferencesHelper
import com.alex_aladdin.geografica.helpers.SharedPreferencesHelper.Companion.PREFS_IS_FIRST_LAUNCH
import java.util.*

class GeoApp : Application(), LifecycleObserver {

    private val analytics: Analytics by inject()
    private val sharedPrefsHelper: SharedPreferencesHelper by inject()

    override fun onCreate() {
        super.onCreate()

        ServiceLocator.init(applicationContext)
        register { Analytics(applicationContext, get()) }

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        if (sharedPrefsHelper.getBoolAndUpdate(PREFS_IS_FIRST_LAUNCH, defaultValue = true, newValue = false)) {
            analytics.setInitialProperties(getLocale().toString(), BuildConfig.VERSION_NAME)
        }
    }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onAppForegrounded() {
        analytics.startSession()
    }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onAppBackgrounded() {
        analytics.finishSession()
    }

    private fun getLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale
        }
    }
}