/*
 * Created by Angel on 15/1/21 11:09 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 11:09 PM
 */

package angel.androidapps.ttslibrary.domain.playback.tts

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.*

/**
 * wrapper class around baseTts
 * this class handles the actual utterance (play/pause) functionality
 *
 * @property ttsEngine String
 * @property language Locale?
 * @property voice Voice?
 * @property pitch Float
 * @property volume Float
 * @property speechRate Float
 * @property progressListener ProgressListener
 * @constructor
 */
class WrapperTts(
    onUtteranceDone: () -> Unit,
    onUtteranceError: (String) -> Unit
) : BaseTts() {

    var ttsEngine: String = ""
    private var language: Locale? = null
    private var voice: Voice? = null
    private var pitch: Float = 1.0f
    private var volume: Float = 1.0f
    private var speechRate: Float = 1.0f

    private val progressListener = ProgressListener(onUtteranceDone, onUtteranceError)

    //PLAYBACK TTS
    //============
    @SuppressLint("ObsoleteSdkInt")
    fun speak(text: String): Boolean {
        if (isReady()) {

            tts?.apply {
                val utteranceId = text.hashCode().toString()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (isSpeaking) {
                        // print("Stop current")
                        stop()
                    }
                    ttsGreater21(text, utteranceId)
                } else {
                    if (isSpeaking) {
                        // print("Stop current")
                        progressListener.clearUtteranceId()
                        stop()
                    }
                    ttsUnder20(text, utteranceId)
                }
            }
            return true
        } else {
            print("TTS not ready")
            return false
        }
    }

    fun pause() {
        tts?.apply {
            if (isSpeaking) {
                stop()
            }
        }
    }


    @Suppress("DEPRECATION")
    private fun ttsUnder20(text: String, utteranceId: String) {
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
        map[TextToSpeech.Engine.KEY_PARAM_VOLUME] = String.format("%.2f", volume)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, map)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun ttsGreater21(text: String, utteranceId: String) {

        val params = Bundle()
        if (volume < 1.0f) {
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }


    //SETUP TTS
    //=========
    override fun setupTts(context: Context, engineName: String, callback: (Int) -> Unit) {
        ttsEngine = engineName
        super.setupTts(context, engineName, callback)
        tts?.setOnUtteranceProgressListener(progressListener)
    }


    /**
     * change the language locale
     * @param newLocale String
     * @return Boolean true if language is changed, false otherwise
     */
    fun updateLanguage(newLocale: String): Boolean {
        if (!isReady()) return false

        language = toLocale(newLocale)
        print("Language = $language ('$newLocale')")
        if (language != null) tts?.language = language

        return (language != null)
    }

    fun updateVoice(newVoice: String): Boolean {
        if (!isReady()) return false
        if (language == null) {
            print("language not set. (no voice)"); return false
        }

        parseVoice(language) { list ->
            voice = list.firstOrNull { it.name == newVoice }

            if (voice != null) tts?.voice = voice
        }
        return (voice != null)
    }

    fun updatePitch(newPitch: Float) {
        if (isReady()) tts?.setPitch(newPitch)
        if (pitch != newPitch) {
            pitch = newPitch
        }
    }

    fun updateSpeechRate(newSpeechRate: Float) {
        if (isReady()) tts?.setSpeechRate(newSpeechRate)
        if (speechRate != newSpeechRate) {
            speechRate = newSpeechRate
        }
    }

    fun updateVolume(volume: Float) {
        this.volume = volume
    }


    //UTTERANCE PROGRESS LISTENER
    //============================
    inner class ProgressListener(
        private val onDone: () -> Unit,
        private val onError: (errorType: String) -> Unit
    ) : UtteranceProgressListener() {

        private var currentId = ""

        fun clearUtteranceId() {
            currentId = ""
        }

        override fun onDone(utteranceId: String) {
            if (currentId == utteranceId) {
                //print("done progress listener: $utteranceId")
                onDone.invoke()
            }
            TextToSpeech.ERROR
        }

        override fun onStart(utteranceId: String) {
            //print("start progress listener $utteranceId")
            currentId = utteranceId
        }

        @Suppress("OverridingDeprecatedMember")
        override fun onError(utteranceId: String) {
            if (currentId == utteranceId) {
                onError.invoke("tts error")
            }
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            //errorCode one of the ERROR_* codes from {@link TextToSpeech}
            if (currentId == utteranceId) {

                val error = getErrorString(errorCode)

                onError.invoke(error)
            }
        }
    }


    companion object {
        private const val TAG = "Angel: TtsWrap"
        private fun print(s: String) = Log.d(TAG, s)
    }
}