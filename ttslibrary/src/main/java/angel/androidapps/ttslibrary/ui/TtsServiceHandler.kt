/*
 * Created by Angel on 15/1/21 11:39 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 11:39 PM
 */

package angel.androidapps.ttslibrary.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import angel.androidapps.ttslibrary.TtsService
import angel.androidapps.ttslibrary.data.entities.PlaybackData
import angel.androidapps.ttslibrary.data.entities.PlaybackMetaData

@Suppress("unused")
class TtsServiceHandler(
    onConnectedCallback: (ttsReady: Boolean) -> Unit,
    onDisconnectedCallback: () -> Unit,
    onTtsStateChanged: (ttsReady: Boolean) -> Unit,
    onCurrentLineChanged: (metaData: PlaybackMetaData) -> Unit
) {

    private var shouldUnbind = false
    private val ttsConnection =
        TtsService.TtsConnection(
            onConnectedCallback,
            onDisconnectedCallback,
            onTtsStateChanged,
            onCurrentLineChanged
        )

    private var serviceIntent: Intent? = null

    //BINDING AND UNBINDING
    fun bind(context: Context) {
        serviceIntent = Intent(context, TtsService::class.java).also { intent ->
            print("Starting service")
            context.startService(intent)

            print("Binding service")
            shouldUnbind = context.bindService(
                intent, ttsConnection, Context.BIND_AUTO_CREATE
            )
        }
    }

    fun unbind(context: Context) {
        print("Unbinding service")
        try {
            if(shouldUnbind) {
                context.unbindService(ttsConnection)
                shouldUnbind = false
            }
            print("Stopping service")
            serviceIntent?.let { intent ->
                context.stopService(intent)
                serviceIntent = null
            }
        } catch (e: Exception) {
            print("error: $e")
        }
    }

    fun populate(
        list: List<PlaybackData>, onError: (String) -> Unit
    ) = ttsConnection.populate(list, onError)

    fun setPlayChapter(isPlay: Boolean) = ttsConnection.setPlayChapter(isPlay)
    fun setPlayText(isPlay: Boolean) = ttsConnection.setPlayText(isPlay)
    fun setPlayTranslation(isPlay: Boolean) = ttsConnection.setPlayTranslation(isPlay)
    fun setPlayOthers(isPlay: Boolean) = ttsConnection.setPlayOthers(isPlay)

    //PREFERENCES AND SETTINGS
    fun updateTts() = ttsConnection.updateTts()

//    fun updateLanguageAndVoice(language: String, voice: String) =
//        ttsConnection.updateTtsLanguageAndVoice(language, voice)

    //PLAYBACK CONTROLS
    fun autoPlay() = ttsConnection.autoPlay()
    fun autoPlayNext() = ttsConnection.autoPlayNext()
    fun autoPlayPrev() = ttsConnection.autoPlayPrev()
    fun pause() = ttsConnection.pause()
    fun play(lineNumber: Int) = ttsConnection.play(lineNumber)
    fun jumpTo(lineNumber: Int) = ttsConnection.jumpTo(lineNumber)

    companion object {

        private const val TAG = "Angel: TtsSH"
        private fun print(s: String) {
            Log.d(TAG, s)
        }
    }

}