/*
 * Created by Angel on 15/1/21 11:09 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 11:09 PM
 */

package angel.androidapps.ttslibrary.domain.playback.tts

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import angel.androidapps.ttslibrary.R
import java.util.*

/**
 * this class provide the base TTS setup
 * (changing of engines, language and voices, etc)
 * used by TTS Settings, and WrapperTts
 *
 * @property tts TextToSpeech?
 * @property isTtsReady Int
 * @property ttsEngines List<EngineInfo>
 * @property languages List<Locale>
 * @property allVoices List<Voice>
 */
open class BaseTts {

    protected var tts: TextToSpeech? = null
    private var isTtsReady: Int = TTS_STATUS_UNINITIALISED

    fun isReady(): Boolean {
        return isTtsReady == TTS_STATUS_SUCCESS
    }

    private var ttsEngines: List<TextToSpeech.EngineInfo> = emptyList()
    private var languages: List<Locale> = emptyList()
    private var allVoices: List<Voice> = emptyList()

    //INIT
    open fun setupTts(context: Context, engineName: String, callback: (Int) -> Unit) {

        val filterLanguage = context.resources.getStringArray(R.array.included_language)
            .map { Locale(it) }

        setupTts(context, engineName, filterLanguage, callback)
    }

    private fun setupTts(
        context: Context,
        engineName: String,
        filter: List<Locale>,
        callback: (Int) -> Unit
    ) {
        tts?.shutdown()
        if (engineName.isNotBlank()) {
            this.tts = TextToSpeech(context, { onTtsInit(it, filter, callback) }, engineName)
        } else {
            //empty tts engine name
            this.tts = TextToSpeech(context) { onTtsInit(it, filter, callback) }
        }
    }

    private fun onTtsInit(status: Int, filter: List<Locale>, callback: (Int) -> Unit) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = TTS_STATUS_SUCCESS
            ttsEngines = tts?.engines.orEmpty()


            languages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    tts?.availableLanguages?.sortedBy { it.language }.orEmpty()
                } catch (e: java.lang.Exception) {
                    //TTS may crash if language returns empty collection
                    Locale.getAvailableLocales().filter { locale -> isLocaleSupported(locale) }
                        .sortedBy { it.language }
                }
            } else {
                Locale.getAvailableLocales().filter { locale -> isLocaleSupported(locale) }
                    .sortedBy { it.language }
            }
            if (filter.isNotEmpty()) {
                val filterLanguage = filter.map { it.language }
                languages = languages.filter { filterLanguage.contains(it.language) }
            }

            allVoices = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                tts?.voices?.sortedBy { it.name }.orEmpty()
            } else {
                emptyList()
            }

        } else {
            val error = getErrorString(status)
            print(">>> ERROR TTS <<<")
            print(">>> ERROR TTS <<<")
            print("-- error $error--")
            print(">>> ERROR TTS <<<")
            print(">>> ERROR TTS <<<")

            isTtsReady = TTS_STATUS_ERROR
            ttsEngines = emptyList()
            languages = emptyList()
            allVoices = emptyList()
        }
        callback.invoke(status)
    }


    //ENGINE
    fun parseEngine(onEngineListReady: (engines: List<TextToSpeech.EngineInfo>) -> Unit) {
        onEngineListReady.invoke(ttsEngines)
    }

    //LANGUAGE
    fun toLocale(strLocale: String): Locale? {
        return if (strLocale.isNotBlank()) {
            languages.firstOrNull { it.toString() == strLocale }
        } else {
            //use default locale's language on first load (i.e. when pref is empty)
            val defaultLocale = Locale.getDefault()
            languages.firstOrNull { it.language == defaultLocale.language }
        }
    }

    //check if TTS supports Locale
    private fun isLocaleSupported(locale: Locale): Boolean {
        val res = tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED

        val hasVariant = locale.variant.isNotEmpty()
        val hasCountry = locale.country.isNotEmpty()

        //options:
        // no variant, no country, lang avail
        // no variant, has country, lang&country avail
        // lang&var&country avail
        return !hasVariant && !hasCountry && res == TextToSpeech.LANG_AVAILABLE ||
                !hasVariant && hasCountry && res == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                res == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
    }

    fun parseLanguage(onLanguageListReady: (List<Locale>) -> Unit) {
        onLanguageListReady.invoke(languages)
    }

    //VOICE
    fun parseVoice(locale: Locale?, onVoiceReady: (List<Voice>) -> Unit) {
        onVoiceReady.invoke(
            allVoices.filter { it.locale == locale }
                .filter { !it.name.contains("network") }
        )
    }

    //CLEAN UP
    //=========
    fun shutDown() {
        try {
            tts?.apply {
                stop()
                shutdown()
            }
            tts = null
            isTtsReady = TTS_STATUS_UNINITIALISED
        } catch (e: Exception) {
            print("Shut down TTS issue: $e")
        }
    }

    //DEBUG ERROR
    protected fun getErrorString(errorCode: Int): String {
        return when (errorCode) {
            TextToSpeech.ERROR_SYNTHESIS -> "synthesis error"
            TextToSpeech.ERROR_SERVICE -> "service error"
            TextToSpeech.ERROR_OUTPUT -> "output error"
            TextToSpeech.ERROR_NETWORK -> "network error"
            TextToSpeech.ERROR_NETWORK_TIMEOUT -> "network timeout error"
            TextToSpeech.ERROR_INVALID_REQUEST -> "invalid request error"
            TextToSpeech.ERROR_NOT_INSTALLED_YET -> "tts not installed error"
            else -> "Unknown error"
        }
    }


    companion object {
        const val TTS_STATUS_UNINITIALISED = -4
        const val TTS_STATUS_ERROR = -1
        const val TTS_STATUS_SUCCESS = 0

        private const val TAG = "Angel: TTS HBase"
        private fun print(s: String) = Log.d(TAG, s)
    }

}