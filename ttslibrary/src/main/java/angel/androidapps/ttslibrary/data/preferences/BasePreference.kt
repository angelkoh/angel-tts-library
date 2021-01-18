/*
 * Created by Angel on 15/1/21 11:09 PM
 * Originally created for project "BibleQuotesFinder"
 * Copyright (c) 2021  Angel. All rights reserved.
 * Last modified 15/1/21 11:09 PM
 */

package angel.androidapps.ttslibrary.data.preferences

import android.content.Context
import androidx.preference.PreferenceManager


open class BasePreference {


    // GETTERS
    //================
    protected fun prefFloat(ctx: Context, keyResId: Int, defValue: Float) =
        pref(ctx).getFloat(s(ctx, keyResId), defValue)

    protected fun prefString(ctx: Context, keyResId: Int, defValue: String = "") =
        pref(ctx).getString(s(ctx, keyResId), defValue) ?: ""

    private fun pref(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    private fun s(context: Context, resId: Int) = context.getString(resId)
}