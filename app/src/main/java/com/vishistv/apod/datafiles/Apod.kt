package com.vishistv.apod.datafiles

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Apod(
    @SerializedName("date")
    var date: String? = null,

    @SerializedName("explanation")
    var description: String? = null,

    @SerializedName("media_type")
    var mediaType: String,

    @SerializedName("title")
    var title: String? = null,

    @SerializedName("hdurl")
    var hdUrl: String? = null,

    @SerializedName("url")
    var url: String
) : Serializable