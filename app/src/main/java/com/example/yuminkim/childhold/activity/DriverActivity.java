package com.example.yuminkim.childhold.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
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

import com.example.yuminkim.childhold.network.model.BaseResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DriverActivity extends Activity implements OnMapReadyCallback {
    static int REQ_PERMISSION_MAP = 1000;
    MapFragment mapFr;
    GoogleMap map;
    Button drive_start_btn;
    ListView childlist_view;
    ChildListAdapter childListAdapter;
    ArrayList<Child> childArrayList;

    private Disposable disposable;
    private Disposable disposable2;
    private Disposable disposable3;

    private com.google.android.gms.maps.model.LatLng center;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        childArrayList = new ArrayList<>();
        setContentView(R.layout.activity_driver);
        getChildList(1);
        getDriverRoute(1);
        initMap();


        drive_start_btn = findViewById(R.id.drive_start_btn);
        drive_start_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout driver_default_linear = findViewById(R.id.driver_default);
                driver_default_linear.setVisibility(View.GONE);
                LinearLayout driver_drive_linear = findViewById(R.id.driver_drive);
                driver_drive_linear.setVisibility(View.VISIBLE);

                childlist_view = findViewById(R.id.child_list);
                childListAdapter = new ChildListAdapter(DriverActivity.this, childArrayList);
                childlist_view.setAdapter(childListAdapter);
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
        disposable = ApiService.getDRIVER_SERVICE().getChildList(driverId)
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
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //TODO: handle error
                    }
                });
    }

    private void getDriverRoute(int driverId) {
        disposable2 = ApiService.getDRIVER_SERVICE().getDriveRoute(driverId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<LatLng>>() {
                    @Override
                    public void accept(ArrayList<LatLng> latLngs) {
                        for (LatLng l : latLngs) {
                            Log.d("latlng", "lat : " + l.getLat());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //TODO: handle error
                    }
                });
    }

    private void sendPushNotification() {
        //FIXME: Push from me modify send to other
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String userId = status.getSubscriptionStatus().getUserId();
        updateDeviceId(userId);
        boolean isSubscribed = status.getSubscriptionStatus().getSubscribed();

        if (!isSubscribed)
            return;

        try {
            JSONObject notificationContent = new JSONObject("{'contents': {'en': 'The notification message or body'}," +
                    "'include_player_ids': ['" + userId + "'], " +
                    "'headings': {'en': 'Notification Title'}, " +
                    "'big_picture': 'http://i.imgur.com/DKw1J2F.gif'}");
            OneSignal.postNotification(notificationContent, null);
            Log.d("userid", "usr-id : " + userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateDeviceId(String deviceId) {
        disposable3 = ApiService.getCOMMON_SERVICE().updateUserDeviceId("1","parent", deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<BaseResponse>() {
                    @Override
                    public void accept(BaseResponse baseResponse) {
                        Log.d("status", "status : " + baseResponse.status);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.d("fail","fail");
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        disposable.dispose();
        disposable2.dispose();
        //disposable3.dispose();
    }
}
