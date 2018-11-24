package com.example.yuminkim.childhold.network

import com.example.yuminkim.childhold.network.model.BaseResponse
import com.example.yuminkim.childhold.network.model.LoginResponse
import io.reactivex.Observable
import retrofit2.http.*

interface CommonService {
    @FormUrlEncoded
    @POST("login.php")
    fun login(@Field("user_type") userType: String,
                           @Field("code") code: String): Observable<LoginResponse>

    @FormUrlEncoded
    @POST("update_google_id.php")
    fun updateUserDeviceId(@Field("user_type") userType: String,
                           @Field("user_id") userId: String,
                           @Field("device_id") deviceId: String): Observable<BaseResponse>
}
