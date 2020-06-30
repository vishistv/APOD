package com.vishistv.apod.util

import android.net.Uri
import java.util.regex.Matcher
import java.util.regex.Pattern

object YoutubeUtil {

    fun getThumbnail() {
        val s = "http://img.youtube.com/vi/GDFUdMvacI0/0.jpg"
    }

    fun getThumbnail(videoUrl: String): String? {
        return "https://img.youtube.com/vi/" + getYoutubeVideoIdFromUrl(videoUrl) + "/0.jpg"
    }

    private fun getYoutubeVideoIdFromUrl(inUrl: String): String? {
        var inUrl = inUrl
        inUrl = inUrl.replace("&feature=youtu.be", "")
        if (inUrl.toLowerCase().contains("youtu.be")) {
            return inUrl.substring(inUrl.lastIndexOf("/") + 1)
        }
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*"
        val compiledPattern: Pattern = Pattern.compile(pattern)
        val matcher: Matcher = compiledPattern.matcher(inUrl)
        return if (matcher.find()) {
            matcher.group()
        } else null
    }
}