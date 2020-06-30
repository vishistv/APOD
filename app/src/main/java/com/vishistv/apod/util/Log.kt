package com.vishistv.apod.util

import android.util.Log
import com.vishistv.apod.BuildConfig

object Log {
    private const val LOG: Boolean = BuildConfig.isLogEnabled
    fun i(tag: String?, string: String?) { if (LOG && string != null) Log.i(tag, string) }

    fun e(tag: String?, string: String?) { if (LOG && string != null) Log.e(tag, string) }

    fun d(tag: String?, string: String?) { if (LOG && string != null) Log.d(tag, string) }

    fun v(tag: String?, string: String?) { if (LOG && string != null) Log.v(tag, string) }

    fun w(tag: String?, string: String?) { if (LOG && string != null) Log.w(tag, string) }
}
