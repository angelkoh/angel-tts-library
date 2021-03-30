package angel.androidapps.ttslibrary.data.entities

import com.google.common.truth.Truth.assertThat
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PlaybackListTest : TestCase() {

    lateinit var data: PlaybackList

    @Before
    fun setup() {

        print("START TEST")
        data = PlaybackList()

        val list = listOf(
            PlaybackData("Ch1", "Ch1", PlaybackData.CHAPTER),
            PlaybackData("Ch1", "Text1", PlaybackData.TEXT),
            PlaybackData("Ch1", "Text2", PlaybackData.TEXT),
            PlaybackData("Ch1", "Text3", PlaybackData.TEXT),
            PlaybackData("Ch1", "Trans1", PlaybackData.TRANSLATION),
            PlaybackData("Ch1", "Trans2", PlaybackData.TRANSLATION),
            PlaybackData("Ch2", "Ch2", PlaybackData.CHAPTER),
            PlaybackData("Ch2", "Text1", PlaybackData.TEXT),
            PlaybackData("Ch2", "Text2", PlaybackData.TEXT),
            PlaybackData("Ch2", "Text3", PlaybackData.TEXT),
            PlaybackData("Ch2", "Trans1", PlaybackData.TRANSLATION),
            PlaybackData("Ch2", "Trans2", PlaybackData.TRANSLATION)
        )
        data.populateList(list)
        data.resetMetaData(false)
    }

    @Test
    fun testGet() {
        print("GET ${data.list.size}\n")
        var idx = 1
        println("$idx " + data.get() + ", hasNext = " + data.hasNext)
        while (data.hasNext) {
            data.currentLine++
            println("$idx " + data.currentLine + "  :  " + data.get() + ", hasNext = " + data.hasNext)
            idx++
        }
        assertThat(idx).isEqualTo(12)
    }

    @Test
    fun testGetTextOnly() {
        print("GET TEXT ${data.list.size}\n")
        data.metaData.typeMask = 0
        data.isPlayTranslation = false
        data.isPlayText = true
        var idx = 0
        if (data.shouldPlayCurrent()) {
            println(
                "$idx " + data.get() + ", hasNext = " + data.hasNext + " "
                        + data.metaData.typeMask.toString(2)
            )
            idx++
        }
        while (data.hasNext) {
            data.currentLine = data.getNextLine()
            println("$idx " + data.currentLine + "  :  " + data.get() + ", hasNext = " + data.hasNext)
            idx++
            if (idx > 100) break
        }
        assertThat(idx).isEqualTo(6)
    }

    @Test
    fun testGetTranslationOnly() {
        print("GET TEXT ${data.list.size}\n")
        data.metaData.typeMask = 0
        data.isPlayTranslation = true
        data.isPlayText = false
        var idx = 0
        if (data.shouldPlayCurrent()) {
            idx++
            println(
                "$idx " + data.get() + ", hasNext = " + data.hasNext + " "
                        + data.metaData.typeMask.toString(2)
            )
        }
        while (data.hasNext) {
            data.currentLine = data.getNextLine()
            idx++
            println("$idx " + data.currentLine + "  :  " + data.get() + ", hasNext = " + data.hasNext)
            if (idx > 100) break
        }
        assertThat(idx).isEqualTo(4)
    }

    @Test
    fun testGetNext() {
    }

    @Test
    fun testGetNextLineTranslationOnly() {

        print("GET TEXT ${data.list.size}\n")
        data.metaData.typeMask = 0
        data.isPlayTranslation = true
        data.isPlayText = false
        var idx = 0
        if (data.shouldPlayCurrent()) {
            idx++
            println(
                "$idx " + data.get() + ", hasNext = " + data.hasNext + " "
                        + data.metaData.typeMask.toString(2)
            )
        }
        while (data.hasNext) {
            data.currentLine = data.getNextLine()
            idx++
            println("$idx " + data.currentLine + "  :  " + data.get() + ", hasNext = " + data.hasNext)
            if (idx > 100) break
        }
        assertThat(idx).isEqualTo(4)
    }
}