/*
 * Created by Angel on 15/1/21 11:39 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 11:39 PM
 */

package angel.androidapps.ttslibrary.ui

import angel.androidapps.ttslibrary.TtsService
import android.content.Context
import android.content.Intent
import android.util.Log
import angel.androidapps.ttslibrary.data.entities.PlaybackData
import angel.androidapps.ttslibrary.data.entities.PlaybackMetaData

@Suppress("unused")
class TtsServiceHandler(
    onConnectedCallback: (ttsReady: Boolean) -> Unit,
    onDisconnectedCallback: () -> Unit,
    onTtsStateChanged: (ttsReady: Boolean) -> Unit,
    onCurrentLineChanged: (metaData: PlaybackMetaData) -> Unit
) {

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
            context.bindService(
                intent, ttsConnection, Context.BIND_AUTO_CREATE
            )
        }
    }

    fun unbind(context: Context) {
        print("Unbinding service")
        context.unbindService(ttsConnection)
        try {
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

    //PREFERENCES AND SETTINGS
    fun updateTts() = ttsConnection.updateTts()

//    fun updateLanguageAndVoice(language: String, voice: String) =
//        ttsConnection.updateTtsLanguageAndVoice(language, voice)

    //PLAYBACK CONTROLS
    fun autoPlay() = ttsConnection.autoPlay()
    fun autoPlayNext() = ttsConnection.autoPlayNext()
    fun pause() = ttsConnection.pause()
    fun play(lineNumber: Int) = ttsConnection.play(lineNumber)
    fun jumpTo(lineNumber: Int) =ttsConnection.jumpTo(lineNumber)

    companion object {

        private const val TAG = "Angel: TtsSH"
        private fun print(s: String) {
            Log.d(TAG, s)
        }
    }

}