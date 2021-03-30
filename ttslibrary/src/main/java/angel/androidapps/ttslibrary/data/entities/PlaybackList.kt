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

    private fun getPrev(): PlaybackData? {
        if (currentLine <= 0 || currentLine >= list.size) return null
        return list.subList(0, currentLine - 1)
            .lastOrNull { metaData.isAnySet(it.type) }

    }

    private fun getNext(): PlaybackData? {
        if (currentLine < 0 || currentLine >= list.size - 1) return null
        return list.subList(currentLine + 1, list.size)
            .firstOrNull { metaData.isAnySet(it.type) }
    }

    fun getPrevLine(): Int {
        if (currentLine <= 0 || currentLine >= list.size) return -1
        return list.subList(0, currentLine - 1).indexOfLast { metaData.isAnySet(it.type) }
    }

    fun getNextLine(): Int {
        if (currentLine < 0 || currentLine >= list.size) return -1
        val idx = list.subList(currentLine + 1, list.size)
            .indexOfFirst { metaData.isAnySet(it.type) }
        return if (idx == -1) -1 else idx + currentLine + 1
    }

    fun shouldPlayCurrent(): Boolean {
        return get()?.let {
            metaData.isAnySet(it.type)
        } ?: false
    }

    //=========l
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
            updateHasNextAndPrev()
            metaData.populate(get())
        }

    //AUTO PLAY
    var isAutoPlay: Boolean
        get() = metaData.isAutoPlay
        set(value) {
            metaData.isAutoPlay = value
        }

    var isPlayChapter: Boolean
        get() = metaData.isAllSet(PlaybackData.CHAPTER)
        set(value) {
            if (value) metaData.setMask(PlaybackData.CHAPTER)
            else metaData.clearMask(PlaybackData.CHAPTER)
            updateHasNextAndPrev()
        }

    var isPlayText: Boolean
        get() = metaData.isAllSet(PlaybackData.TEXT)
        set(value) {
            if (value) metaData.setMask(PlaybackData.TEXT)
            else metaData.clearMask(PlaybackData.TEXT)
            updateHasNextAndPrev()
        }

    var isPlayTranslation: Boolean
        get() = metaData.isAllSet(PlaybackData.TRANSLATION)
        set(value) {
            if (value) metaData.setMask(PlaybackData.TRANSLATION)
            else metaData.clearMask(PlaybackData.TRANSLATION)
            updateHasNextAndPrev()
        }

    var isPlayOthers: Boolean
        get() = metaData.isAllSet(PlaybackData.OTHERS)
        set(value) {
            if (value) metaData.setMask(PlaybackData.OTHERS)
            else metaData.clearMask(PlaybackData.OTHERS)
            updateHasNextAndPrev()
        }

    private fun updateHasNextAndPrev() {
        metaData.hasNext = getNext() != null
        metaData.hasPrev = getPrev() != null
    }

    fun resetMetaData(retainRow: Boolean) {
        val prevLine = currentLine
        metaData = PlaybackMetaData()
        currentLine = when {
            //setting currentLine will repopulate verse
            list.isEmpty() -> -1
            !retainRow -> 0
            prevLine > 0 && prevLine < list.size -> prevLine
            else -> 0
        }
    }

    override fun toString(): String {
        return "PlaybackList: ${list.size} items, currentLine=$currentLine, $metaData"
    }
}