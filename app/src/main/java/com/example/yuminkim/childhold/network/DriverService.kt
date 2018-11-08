package com.example.yuminkim.childhold.network

import com.example.yuminkim.childhold.model.Child
import com.example.yuminkim.childhold.model.LatLng
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface DriverService {
    @GET("child_list.php")
    fun getChildList(@Query("driver_id") driverId: Int): Observable<ArrayList<Child>>

    @GET("drive_route.php")
    fun getDriveRoute(@Query("driver_id") driverId: Int): Observable<ArrayList<LatLng>>
}
