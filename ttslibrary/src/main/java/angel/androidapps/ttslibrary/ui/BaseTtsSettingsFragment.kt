/*
 * Created by Angel on 15/1/21 11:09 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 14/1/21 3:42 PM
 */

package angel.androidapps.ttslibrary.ui

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import angel.androidapps.ttslibrary.R
import angel.androidapps.ttslibrary.domain.playback.tts.BaseTts
import java.util.*


@Suppress("unused")
open class BaseTtsSettingsFragment : PreferenceFragmentCompat() {

    private val baseTts = BaseTts()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        print("OnCreateView")
        return super.onCreateView(inflater, container, savedInstanceState)?.also { root ->

            val toolbar = root.findViewById<Toolbar>(R.id.toolbar_top)
            (activity as AppCompatActivity?)?.let {
                it.setSupportActionBar(toolbar)
                it.supportActionBar?.let { actionBar ->
                    actionBar.setDisplayHomeAsUpEnabled(true)
                    actionBar.setTitle(R.string.label_settings)
                }
            }
            setHasOptionsMenu(true)
        }
    }


    override fun onDestroy() {
        print("onDestroy")
        baseTts.shutDown()

        super.onDestroy()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        print("onCreatePreferences")
        setPreferencesFromResource(R.xml.speech_preferences, rootKey)
        setupTts()

        //setClickListeners
        getPref(R.string.key_no_tts_language)?.setOnPreferenceClickListener {
            print("clicked")
            //try install directly
            // missing data, install it
            try {
                val intent = Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)

                startActivity(intent)

            } catch (e: Exception) {
                print("no activity found 1: $e")
                //open TTS page instead.
                try {
                    startActivity(Intent(android.provider.Settings.ACTION_VOICE_INPUT_SETTINGS))
                } catch (e: Exception) {
                    print("no activity found 2: $e")
                }
            }

            true
        }
    }


    //===================
    //TTS RELATED
    //===================
    private fun setupTts() {
        print("Check TTS availability")

        setVisibility(R.string.key_check_tts_availability, true)

        try {
            print("extracting tts information...")
            val prefEngine: DropDownPreference? =
                getPref(R.string.key_tts_engine) as DropDownPreference?


            baseTts.setupTts(
                requireContext(),
                prefEngine?.value ?: "",
                ::onTtsReady
            )

        } catch (e: Exception) {
            print("Cannot check TTS availability: $e")
            onTtsReady(TextToSpeech.ERROR)
        }
    }

    private fun onTtsReady(status: Int) {
        setVisibility(R.string.key_check_tts_availability, false)
        if (status != TextToSpeech.SUCCESS) {
            print("Error getting TTS.")
            setVisibility(R.string.key_no_tts_engine, true)
        } else {
            //get TTS ENGINE
            parseTtsEngine()
        }
    }

    private fun parseTtsEngine() {
        baseTts.parseEngine { engines ->
            (getPref(R.string.key_tts_engine) as DropDownPreference?)?.let {
                val names = engines.map { engine -> engine.name }
                val labels = engines.map { engine -> engine.label }
                //  val icons = engines.map { engine -> engine.icon }

                it.entries = labels.toTypedArray()
                it.entryValues = names.toTypedArray()

                val engineSize = engines.size
                if (engineSize == 0) {
                    print(
                        "No engines found!"
                    )
                    setVisibility(R.string.key_no_tts_engine, true)
                    setSummary(
                        it,
                        R.string.error_no_tts
                    )
                    it.isEnabled = false
                } else {
                    print(
                        "$engineSize engines found. ($engines)"
                    )
                    //DEBUG...
                    setVisibility(R.string.key_no_tts_engine, false)
                    it.isEnabled = true

                    val prev = it.value
                    val idx = it.findIndexOfValue(prev)
                    if (prev.isNullOrEmpty() || idx == -1) {
                        it.value = names[0]
                    }

                    val engineUsed = "Engine ${it.entry} (${it.value}) / prev: $prev"
                    print(engineUsed)
                    parseTtsLanguage(engineUsed)



                    it.setOnPreferenceChangeListener { _, newValue ->
                        print("Engine changed: $newValue")


                        baseTts.setupTts(
                            requireContext(), newValue.toString(),
                            ::onTtsReady
                        )
                        true
                    }
                }
            }
        }
    }

    private fun parseTtsLanguage(engineText: String) {
        baseTts.parseLanguage { list ->

            (getPref(R.string.key_language) as DropDownPreference?)?.let {
                it.entries = list.map { locale -> locale.getDisplayName(locale) }.toTypedArray()
                it.entryValues = list.map { locale -> locale.toString() }.toTypedArray()

                val languageSize = list.size
                if (languageSize == 0) {
                    print("No languages found!")
                    setVisibility(R.string.key_no_tts_engine, false)
                    setVisibility(R.string.key_no_tts_language, true)
                    setSummary(
                        it,
                        getString(R.string.error_no_language) + ". ENGINE: $engineText"
                    )
                    getPref(R.string.key_tts_engine)?.isEnabled = true
                    parseVoice(null)
                } else {
                    setVisibility(R.string.key_no_tts_language, false)


                    print("Languages found: ${list.size}")
//                    print("")
//                    list.forEach { print(it.toString()) }
//                    print("")

                    val prev = it.value
                    val index = it.findIndexOfValue(prev)
                    if (prev.isNullOrEmpty() || index == -1) {
                        //previously was empty.
                        //use the default locale
                        val defaultLocale = Locale.getDefault()
                        val locale =
                            list.firstOrNull { ll -> ll.language == defaultLocale.language }
                                ?: list[0]

                        it.value = locale.toString()
                        parseVoice(locale)
                    } else {
                        parseVoice(list[index])
                    }
                    print("Language ${it.entry} (${it.value}) / prev: $prev")

                    it.setOnPreferenceChangeListener { _, newValue ->
                        val idx = it.findIndexOfValue(newValue.toString())
                        print(
                            "Language changed $newValue ($idx)"
                        )

                        if (idx >= 0) {
                            parseVoice(list[idx])
                        } else {
                            parseVoice(null)
                        }
                        true
                    }

                }
            }
        }
    }

    private fun parseVoice(locale: Locale?) {
        (getPref(R.string.key_voice) as DropDownPreference?)?.let {

            baseTts.parseVoice(locale) { list ->

                val voiceSize = list.size

                list.map { v -> v.name }.toTypedArray().let { v ->
                    print("Voices found: ${v.size}")
                    it.entryValues = v
                    it.entries = v

                    it.isVisible = voiceSize > 1

                    if (voiceSize > 0) {
                        val prev = it.value
                        val index = it.findIndexOfValue(prev)
                        if (prev.isNullOrEmpty() || index == -1) {
                            it.value = v[0]
                        }
                        print(
                            "Voice (${it.value}) / prev: $prev"
                        )
                    }
                }
            }
        }
    }

    //===================
    //ON BACK ARROW PRESSED
    //===================
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            requireActivity().onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    //===================
    //COMMON METHODS
    //===================
    private fun getPref(keyResId: Int): Preference? {
        return findPreference(getString(keyResId))
    }

    private fun setVisibility(keyResId: Int, isVisible: Boolean) {
        findPreference<Preference>(getString(keyResId))?.isVisible = isVisible
    }

    private fun setSummary(preference: Preference, textResId: Int) {
        preference.summaryProvider = Preference.SummaryProvider<Preference> {
            getString(textResId)
        }
    }

    private fun setSummary(preference: Preference, text: String) {
        preference.summaryProvider = Preference.SummaryProvider<Preference> {
            text
        }
    }

    @Suppress("unused")
    private fun setHint(keyResId: Int, hintResId: Int) {

        getPref(keyResId)?.summaryProvider =
            Preference.SummaryProvider<EditTextPreference> { pref ->
                if (pref.text.isNullOrBlank()) {
                    getString(hintResId)
                } else {
                    pref.text
                }
            }
    }

    companion object {
        private const val TAG = "Angel: PrefF"
        private fun print(s: String) = Log.d(TAG, s)

        const val PAGE_NAME = "TTS SETTINGS"
    }

}