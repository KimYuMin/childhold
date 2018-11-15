package com.example.yuminkim.childhold.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.example.yuminkim.childhold.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;

public class MapsActivity extends Activity implements OnMapReadyCallback {
            static int REQ_PERMISSION_MAP = 1000;

            MapFragment mapFr;
            GoogleMap map;
            double myLat;
            double myLng;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_maps);

                initMap();
    }

    private static String[] permission_map = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private boolean checkPermission(String[] requestPermission){
        boolean[] requestResult = new boolean[requestPermission.length];
        for(int i = 0; i < requestPermission.length; i++){
            requestResult[i] = (ContextCompat.checkSelfPermission(this, requestPermission[i]) == PackageManager.PERMISSION_GRANTED);
            if(!requestResult[i])
                return false;
        }
        return true;
    }

    private void askPermission(String[] requestPermission, int requestCode){
        ActivityCompat.requestPermissions(
                this,
                requestPermission,
                requestCode
        );
    }

    public void initMap(){
        mapFr = (MapFragment)getFragmentManager().findFragmentById(R.id.map);
        mapFr.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQ_PERMISSION_MAP){
            if(grantResults.length > 0){
                if(checkPermission(permission_map))
                    map.setMyLocationEnabled(true);
                else
                    askPermission(permission_map, REQ_PERMISSION_MAP);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if(checkPermission(permission_map))
            map.setMyLocationEnabled(true);
        else
            askPermission(permission_map, REQ_PERMISSION_MAP);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
    }
}
