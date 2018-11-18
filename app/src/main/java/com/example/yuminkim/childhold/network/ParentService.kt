package com.example.yuminkim.childhold.network

import com.example.yuminkim.childhold.model.LatLng
import com.example.yuminkim.childhold.network.model.BaseResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ParentService {
    @GET("get_driver_location.php")
    fun getDriverLocation(@Query("parent_id") parentId: Int): Observable<LatLng>

    @GET("update_child_absent.php")
    fun updateChildAbsent(@Query("parent_id") parentId: Int, @Query("absent") absent: Int): Observable<BaseResponse>
}