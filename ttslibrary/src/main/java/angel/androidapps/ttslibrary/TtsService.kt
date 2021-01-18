/*
 * Created by Angel on 17/1/21 12:03 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 11:43 PM
 */

package angel.androidapps.ttslibrary/*
 * Created by Angel on 15/1/21 11:09 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 10:59 PM
 */

import android.app.*
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import angel.androidapps.ttslibrary.data.entities.PlaybackData
import angel.androidapps.ttslibrary.data.entities.PlaybackMetaData
import angel.androidapps.ttslibrary.data.preferences.TtsPreference
import angel.androidapps.ttslibrary.domain.playback.TtsPlaybackManager


class TtsService : Service() {

    init {
        print("TTS Service created.")
    }

    private val playbackManager = TtsPlaybackManager(
        { handleTtsStateChanged(it) },
        { handleCurrentLineChanged(it) })

    private var ttsChangedCallback: ((ttsReady: Boolean) -> Unit)? = null
    private var lineChangedCallback: ((metaData: PlaybackMetaData) -> Unit)? = null

    private fun setupService(
        onTtsStateChanged: (ttsReady: Boolean) -> Unit,
        onCurrentLineChanged: (metaData: PlaybackMetaData) -> Unit
    ): Boolean {
        updateTts()
        ttsChangedCallback = onTtsStateChanged
        lineChangedCallback = onCurrentLineChanged

        return playbackManager.isTtsReady().also {
            handleTtsStateChanged(it)
        }

    }

    private fun populate(list: List<PlaybackData>) {
        playbackManager.updateData(list)
    }

    //==================
    //PLAYBACK
    //==================
    private fun pause() = playbackManager.doPause()
    private fun autoPlay() {
        if (playbackManager.isTtsReady()) {
            playbackManager.doAutoPlay()
        } else {
            //try to recover TTS
            print(">>>TTS not ready... try to recover.<<<")
            updateTts()
        }
    }

    private fun autoPlayNext() {
        if (playbackManager.isTtsReady()) {
            playbackManager.doAutoPlayNext()
        } else {
            //try to recover TTS
            print(">>>TTS not ready... try to recover.<<<")
            updateTts()
        }
    }

    private fun play(lineNumber: Int) {
        if (playbackManager.isTtsReady()) {
            playbackManager.doPlay(lineNumber)
        } else {
            //try to recover TTS
            print(">>>TTS not ready... try to recover.<<<")
            updateTts()
        }
    }

    private fun jumpTo(lineNumber: Int) {
        playbackManager.jumpTo(lineNumber)
    }

    //==================================================
    //INIT AND UPDATES TO TTS SETTINGS
    //==================================================

    private fun handleCurrentLineChanged(metaData: PlaybackMetaData) {
        lineChangedCallback?.invoke(metaData) ?: print("CALLBACK IS NULL (lineChangedCallback)")
    }

    private fun updateTts() {
        print("Update TTS...")
        playbackManager.updateTtsEngine(
            applicationContext,
            TtsPreference.ttsEngine(applicationContext)
        )
    }

    private fun handleTtsStateChanged(ttsReady: Boolean) {
        if (ttsReady) {
            changeTtsParamsIfNeeded()
        } else {
            print("Failed to init TTS")

        }
        //notify upstream.
        ttsChangedCallback?.invoke(ttsReady) ?: print("CALLBACK IS NULL (ttsChangedCallback)")
    }

    private fun changeTtsParamsIfNeeded() {
        playbackManager.updateTtsSettings(
            TtsPreference.ttsLanguage(applicationContext),
            TtsPreference.ttsVoice(applicationContext),
            TtsPreference.ttsPitch(applicationContext),
            TtsPreference.ttsSpeechRate(applicationContext),
            TtsPreference.ttsVolume(applicationContext)
        )
    }


//    private fun updateTtsLanguageAndVoice(language: String, voice: String) {
//        val pLanguage = TtsPreference.ttsLanguage(applicationContext)
//        val pVoice = TtsPreference.ttsVoice(applicationContext)
//
//        if (language.isNotBlank() && pLanguage != language) {
//            TtsPreference.ttsLanguage(applicationContext, language)
//            val languageChanged = playbackManager.changeLanguage(language)
//            print("Language changed? $languageChanged ($language)")
//        }
//
//        if (voice.isNotBlank() && pVoice != voice) {
//            TtsPreference.ttsVoice(applicationContext, voice)
//            playbackManager.changeVoice(voice)
//        }
//    }

    //==================================================
    //LIFECYCLE (DESTROY)
    //==================================================


    override fun onDestroy() {
        print("destroying service")
        playbackManager.shutDown()
        super.onDestroy()
    }

    //==================================================
    //BINDER
    //==================================================
    private var binder = TtsBinder()
    override fun onBind(intent: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        print("Start sticky")
        return START_STICKY
    }


    inner class TtsBinder : Binder() {
        fun getService(): TtsService = this@TtsService
    }

    //==================================================
    //TTS BOUND
    //==================================================
    class TtsConnection(
        //TSS RELATED
        private val onConnectedCallback: (ttsReady: Boolean) -> Unit,
        private val onDisconnectedCallback: () -> Unit,
        private val onTtsStateChanged: (ttsReady: Boolean) -> Unit,
        private val onCurrentLineChanged: (metaData: PlaybackMetaData) -> Unit
    ) : ServiceConnection {

        private lateinit var ttsService: TtsService

        companion object {
            var bound: Boolean = false
        }

        //CONNECTIONS
        //===================
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            bound = true
            ttsService = (service as TtsBinder).getService()
            ttsService.setupService(onTtsStateChanged, onCurrentLineChanged)
                .also { isReady ->
                    onConnectedCallback.invoke(isReady)
                }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
            onDisconnectedCallback.invoke()
        }


        //LOADER
        //===================
        fun populate(
            list: List<PlaybackData>,
            onError: (String) -> Unit
        ) {
            if (bound) {
                ttsService.populate(list)
            } else {
                onError.invoke("Error! Tts Service not bounded!")
            }
        }

        fun autoPlay() = ttsService.autoPlay()
        fun autoPlayNext() = ttsService.autoPlayNext()
        fun pause() = ttsService.pause()
        fun play(lineNumber: Int) = ttsService.play(lineNumber)
        fun jumpTo(lineNumber: Int) = ttsService.jumpTo(lineNumber)

        //UPDATE PREF (TTS)
        //===================
        fun updateTts() {
            if (bound) ttsService.updateTts()
        }

//        fun updateTtsLanguageAndVoice(language: String, voice: String) {
//            if (bound) ttsService.updateTtsLanguageAndVoice(language, voice)
//        }
    }

    companion object {
        private const val TAG = "Angel: TtsS"
        private fun print(s: String) = Log.d(TAG, s)
    }
}