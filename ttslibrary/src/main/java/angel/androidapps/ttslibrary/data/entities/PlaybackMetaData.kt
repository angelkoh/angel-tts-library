/*
 * Created by Angel on 15/1/21 11:09 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 11:09 PM
 */

package angel.androidapps.ttslibrary.data.entities


/**
 * used be MainFragment to monitor current TTS playback data.
 * used to populate Foreground notification and
 * post loading autoNarrate logic
 */

data class PlaybackMetaData(
    var currentLine: Int = -1,
    var typeMask: Int = PlaybackData.ALL,
    //STATE
    var isAutoPlay: Boolean = false,
    var hasNext: Boolean = false,
    //PLAYBACK
    var title: String = "",
    var subtitle: String = ""
) {

    fun populate(data: PlaybackData?) {
        if (data != null) {
            title = data.title
            subtitle = data.subtitle
        } else {
            title = ""
            subtitle = ""
        }
    }

    fun setMask(bit: Int) {
        typeMask = typeMask or bit
    }

    fun clearMask(bit: Int) {
        typeMask = typeMask and bit.inv()
    }

    fun isAllSet(bit: Int): Boolean {
        return (bit and typeMask) == bit
    }

    fun isAnySet(bit: Int): Boolean {
        return (bit and typeMask) > 0
    }

}