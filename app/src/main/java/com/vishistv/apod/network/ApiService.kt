package com.vishistv.apod.network

import com.vishistv.apod.datafiles.Apod
import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ApiService {

    @GET("/planetary/apod")
    fun getApod(@QueryMap options: Map<String, String>): Single<Apod>?
}