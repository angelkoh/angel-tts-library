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

}