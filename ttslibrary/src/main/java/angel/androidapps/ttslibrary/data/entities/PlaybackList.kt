/*
 * Created by Angel on 15/1/21 11:09 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 10:18 PM
 */

package angel.androidapps.ttslibrary.data.entities

data class PlaybackList(
    var list: List<PlaybackData> = emptyList(),
    var metaData: PlaybackMetaData = PlaybackMetaData()
) {

    fun populateList(list: List<PlaybackData>) {
        this.list = list
    }


    fun get(): PlaybackData? {
        return list.getOrNull(currentLine)
    }

    //=========
    //META DATA
    //=========
    // val title: String get() = metaData.title
    val subtitle: String get() = metaData.subtitle
    val hasNext: Boolean get() = metaData.hasNext

    //CURRENT LINE
    var currentLine: Int
        get() = metaData.currentLine
        set(value) {
            metaData.currentLine = value
            metaData.populate(get())
            metaData.hasNext = value < list.size
        }

    //AUTO PLAY
    var isAutoPlay: Boolean
        get() = metaData.isAutoPlay
        set(value) {
            metaData.isAutoPlay = value
        }


    override fun toString(): String {
        return "PlaybackList: ${list.size} items, currentLine=$currentLine"
    }

    fun resetMetaData(retainRow: Boolean) {
        val prevLine = currentLine
        metaData = PlaybackMetaData()
        currentLine = when {
            //setting currentLine will repopulate verse
            list.isEmpty() -> -1
            prevLine < 0 || !retainRow -> 0
            prevLine < list.size -> prevLine
            else -> list.size - 1

        }

    }
}