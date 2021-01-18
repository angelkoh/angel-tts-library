/*
 * Created by Angel on 15/1/21 11:09 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 11:09 PM
 */

package angel.androidapps.ttslibrary.data.preferences

import android.content.Context
import angel.androidapps.ttslibrary.R

object TtsPreference : BasePreference() {

    fun ttsEngine(ctx: Context) = prefString(ctx, R.string.key_tts_engine, "")
    fun ttsVolume(ctx: Context) = prefFloat(ctx, R.string.key_volume, 1f)
    fun ttsPitch(ctx: Context) = prefFloat(ctx, R.string.key_pitch, 1f)
    fun ttsSpeechRate(ctx: Context) = prefFloat(ctx, R.string.key_speech_rate, 1f)
    fun ttsVoice(ctx: Context) = prefString(ctx, R.string.key_voice, "")
    fun ttsLanguage(ctx: Context) = prefString(ctx, R.string.key_language, "")


}