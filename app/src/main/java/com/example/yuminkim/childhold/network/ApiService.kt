package com.example.yuminkim.childhold.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ApiService {
    val BASE_URL = "http://cxcv92.dothome.co.kr/api";

    var retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

}