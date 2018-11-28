package com.example.yuminkim.childhold.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.model.LatLng;
import com.example.yuminkim.childhold.network.ApiService;
import com.example.yuminkim.childhold.network.model.AbsentResponse;
import com.example.yuminkim.childhold.network.model.BaseResponse;
import com.example.yuminkim.childhold.util.Constants;
import com.example.yuminkim.childhold.util.PushMessageUtil;
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

    RelativeLayout parent_locaion_btn, parent_absent_btn;
    private com.google.android.gms.maps.model.LatLng center;
    Disposable disposable;
    Disposable disposable2;
    LatLng driver_location;
    String idx;
    boolean isAbsent = false;
    View layout_cover_absent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        idx = getIntent().getStringExtra(Constants.KEY_IDX);
        parent_locaion_btn = findViewById(R.id.parent_location_btn);
        parent_absent_btn = findViewById(R.id.parent_absent_btn);
        layout_cover_absent = findViewById(R.id.layout_cover_absent);
        initMap();
        parent_locaion_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAbsent) return;
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
                disposable2 = ApiService.getPARENT_SERVICE().updateChildAbsent(Integer.parseInt(idx), 1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<AbsentResponse>() {
                            @Override
                            public void accept(AbsentResponse absentResponse) {
                                PushMessageUtil.sendAbsentPushNotification(absentResponse.driverId, idx);
                                isAbsent = !isAbsent;
                                callAbsentDialog();
                            }
                        });
            }
        });
    }

    private void callAbsentDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(ParentActivity.this)
                .setTitle("부재 알림")
                .setMessage("아이의 상태를 (" + (isAbsent ? "승차하지않음" : "승차함") + ") 으로 변경하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        layout_cover_absent.setVisibility(isAbsent ? View.VISIBLE : View.GONE);
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
        alertDialog.show();
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
