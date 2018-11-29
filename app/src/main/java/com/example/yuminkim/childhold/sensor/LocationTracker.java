package com.example.yuminkim.childhold.sensor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;


public class LocationTracker extends Service implements LocationListener {

    private final Context mContext;
    private Handler mHandler;

    boolean isGPSEnabled = false;
    boolean canGetLocation = false;

    Location location;
    double lat, lng;

    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10;

    protected LocationManager locationManager;

    public LocationTracker(Context context, Handler handler){
        this.mContext = context;
        this.mHandler = handler;
        getLocation();
    }
    public void Update(){
        getLocation();
    }
    public Location getLocation(){
        if(Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED)
            return null;
        try{
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSEnabled){
                if(location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if(locationManager != null){
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(location != null){
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return location;
    }

    public double getLat(){
        if(location != null)
            lat = location.getLatitude();
        return lat;
    }
    public double getLng(){
        if(location != null)
            lng = location.getLongitude();
        return lng;
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            // 서버로넘기기 
        }
    }
    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

}
