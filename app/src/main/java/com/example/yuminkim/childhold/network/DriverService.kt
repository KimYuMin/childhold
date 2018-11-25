package com.example.yuminkim.childhold.network

import com.example.yuminkim.childhold.model.Child
import com.example.yuminkim.childhold.model.LatLng
import com.example.yuminkim.childhold.network.model.BaseResponse
import io.reactivex.Observable
import retrofit2.http.*

interface DriverService {
    @GET("child_list.php")
    fun getChildList(@Query("driver_id") driverId: Int): Observable<ArrayList<Child>>
}
