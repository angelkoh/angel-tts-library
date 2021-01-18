/*
 * Created by Angel on 15/1/21 11:09 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 10:18 PM
 */

package angel.androidapps.ttslibrary.domain.playback

import android.content.Context
import android.util.Log
import angel.androidapps.ttslibrary.data.entities.PlaybackData
import angel.androidapps.ttslibrary.data.entities.PlaybackList
import angel.androidapps.ttslibrary.data.entities.PlaybackMetaData
import angel.androidapps.ttslibrary.domain.playback.tts.WrapperTts

class TtsPlaybackManager(
    private val onTtsStateChanged: (ttsReady: Boolean) -> Unit,
    private val onCurrentLineChanged: (metaData: PlaybackMetaData) -> Unit
) {

    private var data = PlaybackList()

    //=============
    //DATA
    //=============
    fun updateData(value: List<PlaybackData>) {
        data.populateList(value)
        print("resetting playback meta data (prev line: ${data.metaData.currentLine}")
        data.resetMetaData(true)
        print("- current line: ${data.metaData.currentLine})")
        onCurrentLineChanged.invoke(data.metaData)

    }

    //=============
    //TTS COMPONENT
    //=============
    private var tts = WrapperTts(::handleUtteranceDone, ::handleUtteranceError)

    fun isTtsReady() = tts.isReady()

    fun shutDown() = tts.shutDown()

    fun updateTtsEngine(context: Context, ttsEngine: String) {
        //Always re-initialise engine.
        print("Engine: $ttsEngine (${tts.ttsEngine})")
        tts.pause()
        tts.setupTts(context, ttsEngine) {
            onTtsStateChanged.invoke(tts.isReady())
            if (tts.isReady()) {
                if (data.isAutoPlay) doAutoPlay()
            } else {
                data.isAutoPlay = false
                onCurrentLineChanged.invoke(data.metaData)
            }
        }
    }

    fun updateTtsSettings(
        ttsLanguage: String,
        ttsVoice: String,
        ttsPitch: Float,
        ttsSpeechRate: Float,
        ttsVolume: Float
    ) {

        tts.updateLanguage(ttsLanguage).also {
            print("Language updated? $it ($ttsLanguage)")
        }
        tts.updateVoice(ttsVoice).also {
            print("Voice updated? $it ($ttsVoice)")
        }
        tts.updatePitch(ttsPitch).also {
            print("Pitch: (${format(ttsPitch)})")
        }
        tts.updateSpeechRate(ttsSpeechRate).also {
            print("SpeechRate: (${format(ttsSpeechRate)})")
        }
        tts.updateVolume(ttsVolume)
        print("Volume: ${format(ttsVolume)}")
    }

//    fun changeLanguage(value: String) = tts.updateLanguage(value)
//    fun changeVoice(value: String) = tts.updateVoice(value)


    //=============
    //PLAYBACK
    //=============
    fun doPause() {
        data.isAutoPlay = false
        tts.pause()
        onCurrentLineChanged(data.metaData)
    }

    fun doAutoPlay() {
        data.isAutoPlay = true
        doPlay(data.get())
    }

    fun doAutoPlayNext() {
        data.isAutoPlay = true
        data.currentLine++
        doPlay(data.get())
    }

    fun doPlay(lineNumber: Int) {
        data.currentLine = lineNumber
        doPlay(data.get())
    }

    fun jumpTo(lineNumber: Int) {
        if (data.isAutoPlay) {
            doPlay(lineNumber)
        } else {
            data.currentLine = lineNumber
            onCurrentLineChanged.invoke(data.metaData)
        }
    }

    private fun doPlay(value: PlaybackData?) {
        if (!isTtsReady()) {
            print("TTS IS NOT READY!!")
            data.isAutoPlay = false
            onCurrentLineChanged.invoke(data.metaData)
            onTtsStateChanged.invoke(false)
        } else if (value == null) {
            print("No more data to read. Line: ${data.currentLine}")
            doPause()
            //move back to first entry if data exists
            data.resetMetaData(false)
            print("Resetting Line: ${data.currentLine}")
        } else {
            data.subtitle.let {
                print(it)
                tts.speak(it)
            }
            onCurrentLineChanged.invoke(data.metaData)
        }
    }


    //=============
    //CALLBACKS
    //=============
    private fun handleUtteranceError(s: String) {
        print("Error! $s")
        doPause()
    }

    private fun handleUtteranceDone() {
        print("play next?  ${data.metaData}")
        if (data.isAutoPlay) {
            if (data.hasNext) {
                data.currentLine++
                print("Play next line")
                doAutoPlay()
            } else {
                print("last line reached.")
                data.currentLine = -1
                doPause()
            }
        }
    }

    //===================
    //DEBUG
    //===================
    companion object {

        private const val TAG = "Angel: PlayMgr"
        private fun print(s: String) = Log.d(TAG, s)

        private fun format(value: Float) = String.format("%.2f", value)
    }
}

