package com.example.yuminkim.childhold.network

import com.example.yuminkim.childhold.model.ChildLocationResponse
import com.example.yuminkim.childhold.model.LatLng
import com.example.yuminkim.childhold.network.model.AbsentResponse
import com.example.yuminkim.childhold.network.model.AbsentStatusResponse
import com.example.yuminkim.childhold.network.model.BaseResponse
import io.reactivex.Observable
import retrofit2.http.*

interface ParentService {
    @GET("get_driver_location.php")
    fun getDriverLocation(@Query("parent_id") parentId: Int): Observable<LatLng>

    @GET("update_child_absent.php")
    fun updateChildAbsent(@Query("parent_id") parentId: Int, @Query("absent") absent: Int): Observable<AbsentResponse>

    @FormUrlEncoded
    @POST("update_child_location.php")
    fun updateChildLocation(@Field("child_id") childId: Int,
                            @Field("lat") lat: Double,
                            @Field("lng") lng: Double): Observable<BaseResponse>

    @GET("get_absent_state.php")
    fun getChildAbsentState(@Query("parent_id") parentId: Int): Observable<AbsentStatusResponse>

    @GET("get_child_location.php")
    fun getChildLocation(@Query("parent_id") parentId: Int): Observable<ChildLocationResponse>
}