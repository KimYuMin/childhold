package com.example.yuminkim.childhold.network

import com.example.yuminkim.childhold.model.Child
import com.example.yuminkim.childhold.model.LatLng
import com.example.yuminkim.childhold.network.model.BaseResponse
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface DriverService {
    @GET("child_list.php")
    fun getChildList(@Query("driver_id") driverId: Int): Observable<ArrayList<Child>>

    @GET("drive_route.php")
    fun getDriveRoute(@Query("driver_id") driverId: Int): Observable<ArrayList<LatLng>>

    //FIXME: User service를 만들어서 옮기자 (운전자, 부모 둘다 사용해야함)
    @POST("update_google_id.php")
    fun updateUserDeviceId(@Field("user_type") userType: String,
                           @Field("user_id") userId: String,
                           @Field("device_Id") deviceId: String): Observable<BaseResponse>
}
