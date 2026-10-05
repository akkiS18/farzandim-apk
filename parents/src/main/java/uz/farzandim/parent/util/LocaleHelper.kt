package uz.farzandim.parent.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    fun getLocale(languageCode: String): Locale {
        return when (languageCode.lowercase()) {
            "ru" -> Locale("ru", "RU")
            "en" -> Locale("en", "US")
            else -> Locale("uz", "UZ")
        }
    }

    fun getLocalizedConfiguration(baseConfig: Configuration, languageCode: String): Configuration {
        val locale = getLocale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(baseConfig)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        return config
    }

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = getLocale(languageCode)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(configuration)
        } else {
            context
        }
    }
}
