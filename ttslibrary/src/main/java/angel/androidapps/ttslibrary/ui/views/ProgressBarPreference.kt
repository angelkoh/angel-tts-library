/*
 * Created by Angel on 15/1/21 11:09 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 11:09 PM
 */

package angel.androidapps.ttslibrary.ui.views

//from https://gist.github.com/yuntan/637b7489bcb9d8e1b01026530dd1e1e9

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import angel.androidapps.ttslibrary.R

@Keep
class ProgressBarPreference(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) :
    Preference(context, attrs, defStyleAttr, defStyleRes) {

    private var isIndeterminate = true


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, R.attr.preferenceStyle)

    @Suppress("unused")
    constructor(context: Context) : this(context, null)

    init {
        widgetLayoutResource = R.layout.pref_progress_bar // カスタムレイアウトを指定する

        // XMLの値を取得
        val ta = context.obtainStyledAttributes(
            attrs, R.styleable.ProgressBarPreference, defStyleAttr, defStyleRes
        )
        isIndeterminate = ta.getBoolean(R.styleable.ProgressBarPreference_indeterminate, true)
        ta.recycle()
    }


    // onGetDefaultValue -> onSetInitialValue -> onBindViewHolder の順に呼ばれる
    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)

        if (holder != null) {
            holder.itemView.isClickable = false
        }
    }


}