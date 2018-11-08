package com.example.yuminkim.childhold.network

import com.example.yuminkim.childhold.model.Child
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface DriverService {
    @GET("child_list.php")
    fun getChildList(@Query("driver_id") driverId: Int): Observable<ArrayList<Child>>
}
