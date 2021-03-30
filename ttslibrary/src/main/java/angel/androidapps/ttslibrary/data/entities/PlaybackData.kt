/*
 * Created by Angel on 15/1/21 11:09 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 11:00 AM
 */

package angel.androidapps.ttslibrary.data.entities

open class PlaybackData(
    val title: String,
    val subtitle: String,
    val type: Int = TEXT
) {

    companion object {
        const val CHAPTER = 0b0001
        const val TEXT = 0b0010
        const val TRANSLATION = 0b0100
        const val OTHERS = 0b1000

        const val NONE = 0b0000
        const val ALL = 0b1111
    }

    override fun toString(): String {
        return "PlaybackData(title='$title', subtitle='$subtitle', type=$type)"
    }
}