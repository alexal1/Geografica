package com.alex_aladdin.geografica.di

import android.content.Context

inline fun <reified T> get(): T {
    return ServiceLocator.get(T::class.java)
}

inline fun <reified T> inject() = lazy {
    ServiceLocator.get(T::class.java)
}

inline fun <reified T> register(crossinline creator: (context: Context) -> T) {
    ServiceLocator.registerServiceCreator(T::class.java) { context -> creator(context) }
}