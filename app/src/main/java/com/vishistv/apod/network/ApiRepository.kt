package com.vishistv.apod.network

import com.google.gson.GsonBuilder
import com.vishistv.apod.datafiles.Apod
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


object ApiRepository {

    private val apiService: ApiService

    init {
        val okHttpClient = OkHttpClient.Builder()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(Env.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient.build())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    fun getApod(options: Map<String, String>): Single<Apod>? {
        return apiService.getApod(options)
    }
}