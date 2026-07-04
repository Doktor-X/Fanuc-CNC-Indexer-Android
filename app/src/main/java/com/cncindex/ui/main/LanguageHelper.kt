package com.cncindex.ui.main

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageHelper {

    private const val PREF_KEY = "app_language"
    private const val PREF_NAME = "cnc_prefs"

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_KEY, "hr") ?: "hr"
    }

    fun saveLanguage(context: Context, lang: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(PREF_KEY, lang).apply()
    }

    fun applyLanguage(context: Context, lang: String): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun wrap(context: Context): Context {
        val lang = getSavedLanguage(context)
        return applyLanguage(context, lang)
    }
}
