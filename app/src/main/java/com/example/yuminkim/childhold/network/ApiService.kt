package com.example.yuminkim.childhold.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class ApiService {
    companion object {
        val BASE_URL = "http://cxcv92.dothome.co.kr/api/"

        //FIXME: For logging http request
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build();


        var retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        @JvmStatic
        val DRIVER_SERVICE = retrofit.create(DriverService::class.java)

        @JvmStatic
        val COMMON_SERVICE = retrofit.create(CommonService::class.java)

        @JvmStatic
        val PARENT_SERVICE = retrofit.create(ParentService::class.java)
    }
}
