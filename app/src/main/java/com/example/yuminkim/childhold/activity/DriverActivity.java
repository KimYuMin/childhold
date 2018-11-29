package com.example.yuminkim.childhold.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.model.Child;
import com.example.yuminkim.childhold.model.ChildListAdapter;
import com.example.yuminkim.childhold.model.LatLng;
import com.example.yuminkim.childhold.network.ApiService;

import com.example.yuminkim.childhold.sensor.CHBluetoothManager;
import com.example.yuminkim.childhold.util.Constants;
import com.example.yuminkim.childhold.util.PushMessageUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DriverActivity extends BaseActivity{
    private Button drive_start_btn;
    private ListView childlist_view;
    private ChildListAdapter childListAdapter;
    private ArrayList<Child> childArrayList;
    private ArrayList<Child> childListForEndDrive;
    private ArrayList<Child> childListForExit;
    private Disposable disposable;
    private com.google.android.gms.maps.model.LatLng center;
    private String idx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        idx = getIntent().getStringExtra(Constants.KEY_IDX);
        childArrayList = new ArrayList<>();
        childListForEndDrive = new ArrayList<>();
        childListForExit = new ArrayList<>();
        setContentView(R.layout.activity_driver);
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
                childlist_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        LatLng src = childArrayList.get(position).getLatLng();
                        com.google.android.gms.maps.model.LatLng latLng =
                                new com.google.android.gms.maps.model.LatLng(src.getLat(), src.getLng());
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
                    }
                });
                startBeaconScan();
            }
        });
    }

    //TODO: Check Bluetooth is ON?
    private void startBeaconScan() { // 여기가 비콘스캔인데...
        CHBluetoothManager.getInstance(this).scanLeDevice(true, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (!childArrayList.isEmpty()) {
                    String curDeviceId = result.getDevice().getAddress();
                    if (curDeviceId != null) {
                        Child curChild = null;
                        for (Child child : childArrayList) {
                            if (child.getBeaconId().equals(curDeviceId)) {
                                curChild = child;
                                break;
                            }
                        }
                        if (curChild != null) {
                            PushMessageUtil.sendPushNotification(curChild.getDeviceId(), curChild.getName(), true);
                            //FIXME: 왜 유민이가 탔는데 세훈이가 사라질까 ㅅㅂ라멎댈ㅈㄷ
                            childArrayList.remove(curChild);
                            childListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) { }

            @Override
            public void onScanFailed(int errorCode) { }
        });
    }

    private void BeaconScanForHome() { // 여기가 비콘스캔인데...
        final ArrayList<Child> childListForExit_copy = new ArrayList<>(childListForExit);
        CHBluetoothManager.getInstance(this).scanLeDeviceForExit(true, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                String curDeviceId = result.getDevice().getAddress();
                if (curDeviceId != null) {
                    for (Child child : childListForExit) {
                        if (child.getBeaconId().equals(curDeviceId)) {
                            childListForExit_copy.remove(child);
                        }
                    }
                }
            }
        }, new CHBluetoothManager.DriveEndScanCallback() {
            @Override
            public void driveEnd(boolean status) {
                // nothing
            }

            @Override
            public void scanEnd() {
                for(Child child : childListForExit_copy){
                  PushMessageUtil.sendPushNotification(child.getDeviceId(), child.getName(), false);
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        getChildList(Integer.parseInt(idx));
    }


    private void getChildList(int driverId) {
        disposable = ApiService.getDRIVER_SERVICE().getChildList(driverId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<Child>>() {
                    @Override
                    public void accept(ArrayList<Child> children) throws Exception {
                        double lat = 0, lng = 0;
                        for (Child c: children) {
                            lat += c.getLatLng().getLat();
                            lng += c.getLatLng().getLng();
                            childArrayList.add(c);
                            childListForEndDrive.add(c);
                            childListForExit.add(c);
                            map.addMarker( new MarkerOptions().position(
                                    new com.google.android.gms.maps.model.LatLng(
                                            c.getLatLng().getLat(),
                                            c.getLatLng().getLng()
                                    )
                                ).title(String.format("%d", c.getIdx()))
                            );
                        }
                        center = new com.google.android.gms.maps.model.LatLng(
                                lat / (double) children.size(),
                                lng / (double) children.size()
                        );
                        if (map != null) {
                            postMapProcess();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //TODO: handle error
                    }
                });
    }

    private void postMapProcess(){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 14));
    }

    private void driveEnd() {
        CHBluetoothManager.getInstance(this)
                .driveEndScan(childListForEndDrive,
                        new CHBluetoothManager.DriveEndScanCallback() {
            @Override
            public void driveEnd(boolean status) {
                if (status) {
                    //모든 탑승한 아이의 부모에게 안전하차 push알림 보내기
                    String ids = "";
                    for (Child c : childListForEndDrive) {
                        if (ids.equals("")) {
                            ids += c.getDeviceId();
                        } else {
                            ids += ", " + c.getDeviceId();
                        }
                    }
                    PushMessageUtil.sendPushNotificationForSafetyEnd(ids);
                    //TODO: 아이가 모두 내렸음 끝 !
                } else {
                    //TODO: 아이가 아직 남아있음 다시 스캔을 돌리도록 유도 !
                }
            }

            @Override
            public void scanEnd() {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (disposable != null) {
            disposable.dispose();
        }
    }
}
