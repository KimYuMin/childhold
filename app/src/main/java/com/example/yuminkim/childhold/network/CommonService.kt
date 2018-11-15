package com.example.yuminkim.childhold.network

import com.example.yuminkim.childhold.network.model.BaseResponse
import io.reactivex.Observable
import retrofit2.http.*

interface CommonService {
    @FormUrlEncoded
    @POST("login.php")
    fun login(@Field("user_type") userType: String,
                           @Field("code") code: String): Observable<String>

    @FormUrlEncoded
    @POST("update_google_id.php")
    fun updateUserDeviceId(@Field("user_type") userType: String,
                           @Field("user_id") userId: String,
                           @Field("device_Id") deviceId: String): Observable<BaseResponse>
}
