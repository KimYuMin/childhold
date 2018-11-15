package com.example.yuminkim.childhold.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.model.Child;
import com.example.yuminkim.childhold.model.ChildListAdapter;
import com.example.yuminkim.childhold.model.LatLng;
import com.example.yuminkim.childhold.network.ApiService;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DriverActivity extends Activity implements OnMapReadyCallback {
    static int REQ_PERMISSION_MAP = 1000;
    MapFragment mapFr;
    GoogleMap map;
    double myLat;
    double myLng;
    Button drive_start_btn;
    ListView childlist_view;
    ChildListAdapter childListAdapter;
    ArrayList<Child> childArrayList;

    private Disposable disposable;
    private Disposable disposable2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        childArrayList = new ArrayList<Child>();
        setContentView(R.layout.activity_driver);
        getChildList(1);
        getDriverRoute(1);
        Log.d("driver", "driveractivity");
        initMap();


        drive_start_btn = (Button)findViewById(R.id.drive_start_btn);
        drive_start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout driver_default_linear = (LinearLayout)findViewById(R.id.driver_default);
                driver_default_linear.setVisibility(View.GONE);
                LinearLayout driver_drive_linear = (LinearLayout)findViewById(R.id.driver_drive);
                driver_drive_linear.setVisibility(View.VISIBLE);

                childlist_view = (ListView)findViewById(R.id.child_list);
                childListAdapter = new ChildListAdapter(DriverActivity.this, childArrayList);
                Log.d("child list :", childArrayList.get(1).getName());
                Log.d("child list :", childArrayList.get(1).getLatLng().toString());
                childlist_view.setAdapter(childListAdapter);

                //childlist_view.setAdapter(childListAdapter);
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
        mapFr = (MapFragment)getFragmentManager().findFragmentById(R.id.driver_map);
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


    private void getChildList(int driverId) {
        disposable = ApiService.getDRIVER_SERVCE().getChildList(driverId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<Child>>() {
                    @Override
                    public void accept(ArrayList<Child> children) throws Exception {
                        for (Child c: children) {
                            Log.d("child", "name : " + c.getLatLng().getLat());
                            childArrayList.add(c);
                        }
                    }
                });
    }

    private void getDriverRoute(int driverId) {
        disposable2 = ApiService.getDRIVER_SERVCE().getDriveRoute(driverId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<LatLng>>() {
                    @Override
                    public void accept(ArrayList<LatLng> latLngs) throws Exception {
                        for (LatLng l : latLngs) {
                            Log.d("latlng", "lat : " + l.getLat());
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        disposable.dispose();
        disposable2.dispose();
    }
}
