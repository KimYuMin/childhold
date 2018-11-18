package com.example.yuminkim.childhold.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.model.LatLng;
import com.example.yuminkim.childhold.network.ApiService;
import com.example.yuminkim.childhold.network.model.BaseResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.MarkerOptions;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ParentActivity extends Activity implements OnMapReadyCallback {
    static int REQ_PERMISSION_MAP = 1000;

    MapFragment mapFr;
    GoogleMap map;
    double myLat;
    double myLng;

    Button parent_locaion_btn, parent_absent_btn;
    private com.google.android.gms.maps.model.LatLng center;
    Disposable disposable;
    Disposable disposable2;
    LatLng driver_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        parent_locaion_btn = findViewById(R.id.parent_location_btn);
        parent_absent_btn = findViewById(R.id.parent_absent_btn);
        initMap();
        parent_locaion_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disposable = ApiService.getPARENT_SERVICE().getDriverLocation(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<LatLng>() {
                            @Override
                            public void accept(LatLng latLng) {
                                double lat = 0, lng = 0;
                                lat += latLng.getLat();
                                lng += latLng.getLng();
                                map.addMarker( new MarkerOptions().position(
                                        new com.google.android.gms.maps.model.LatLng(
                                                latLng.getLat(),
                                                latLng.getLng()
                                        )
                                        ).title(String.format("기사위치"))
                                );
                                center = new com.google.android.gms.maps.model.LatLng(lat, lng);
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 14));
                            }
                        });
            }
        });

        parent_absent_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disposable2 = ApiService.getPARENT_SERVICE().updateChildAbsent(1, 1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<BaseResponse>() {
                            @Override
                            public void accept(BaseResponse baseResponse) {
                                Toast.makeText(ParentActivity.this, baseResponse.status, Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
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
        mapFr = (MapFragment)getFragmentManager().findFragmentById(R.id.parent_map);
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
