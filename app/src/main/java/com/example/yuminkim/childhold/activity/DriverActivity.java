package com.example.yuminkim.childhold.activity;

import android.app.ProgressDialog;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.model.Child;
import com.example.yuminkim.childhold.model.ChildListAdapter;
import com.example.yuminkim.childhold.model.LatLng;
import com.example.yuminkim.childhold.network.ApiService;

import com.example.yuminkim.childhold.network.model.BaseResponse;
import com.example.yuminkim.childhold.sensor.CHBluetoothManager;
import com.example.yuminkim.childhold.sensor.LocationTracker;
import com.example.yuminkim.childhold.util.AlertUtil;
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
    private Button driveForGoToSchool;
    private Button driveForGoToHome;
    private ListView childlist_view;
    private ChildListAdapter childListAdapter;
    private ArrayList<Child> childArrayList;
    private ArrayList<Child> childListForEndDrive;
    private ArrayList<Child> childListForExit;
    private Disposable disposable;
    private Disposable childLocationUpdateDisposable;
    private com.google.android.gms.maps.model.LatLng center;
    private String idx;
    LocationTracker locationTracker;
    public Handler mHandler;


    private LinearLayout driveDefaultContainer;
    private LinearLayout driveDriveContainer;
    private LinearLayout driveEndContainer;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_driver);
        super.onCreate(savedInstanceState);
        idx = getIntent().getStringExtra(Constants.KEY_IDX);
        childArrayList = new ArrayList<>();
        childListForEndDrive = new ArrayList<>();
        childListForExit = new ArrayList<>();

        driveDefaultContainer = findViewById(R.id.driver_default);
        driveDriveContainer = findViewById(R.id.driver_drive);
        driveEndContainer = findViewById(R.id.drive_end_container);

        mHandler = new Handler();
        driveForGoToSchool = findViewById(R.id.drive_go_to_school_btn);
        driveForGoToHome = findViewById(R.id.drive_go_to_home_btn);
        driveForGoToSchool.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                driveDefaultContainer.setVisibility(View.GONE);
                driveDriveContainer.setVisibility(View.VISIBLE);
                driveEndContainer.setVisibility(View.GONE);

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
                startBeaconScanForGoToSchool();
                locationTracker = new LocationTracker(DriverActivity.this, mHandler, idx);
                double lat = locationTracker.getLat();
                double lng = locationTracker.getLng();
                Toast.makeText(getApplicationContext(), "LOCATION : " + lat + " " + lng, Toast.LENGTH_SHORT).show();
            }
        });

        driveForGoToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driveDefaultContainer.setVisibility(View.GONE);
                driveDriveContainer.setVisibility(View.VISIBLE);
                driveEndContainer.setVisibility(View.GONE);

                childlist_view = findViewById(R.id.child_list);
                childListAdapter = new ChildListAdapter(DriverActivity.this, childListForExit);
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
                startBeaconScanForGoToSchool();
                locationTracker = new LocationTracker(DriverActivity.this, mHandler, idx);
                startBeaconScanForHome();
            }
        });

        findViewById(R.id.drive_end_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(DriverActivity.this);
                progressDialog.setMessage("남아있는 아이를 확인중입니다.");
                progressDialog.create();
                progressDialog.show();
                driveEnd();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                messageReceiver,
                new IntentFilter(Constants.KEY_NOTI_FOR_DRIVER)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String alert = intent.getStringExtra("alert");
            AlertUtil.showAlert(DriverActivity.this, title, alert, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getChildList(Integer.parseInt(idx));
                    dialog.dismiss();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    };


    //TODO: Check Bluetooth is ON?
    private void startBeaconScanForGoToSchool() { // 여기가 비콘스캔인데...
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
                            childArrayList.remove(curChild);
                            childListAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    driveDefaultContainer.setVisibility(View.GONE);
                    driveDriveContainer.setVisibility(View.GONE);
                    driveEndContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) { }

            @Override
            public void onScanFailed(int errorCode) { }
        });
    }

    private void startBeaconScanForHome() { // 여기가 비콘스캔인데...
        locationTracker.getLocation();
        final ArrayList<Child> childListForExit_copy = new ArrayList<>(childListForExit);
        CHBluetoothManager.getInstance(this).scanLeDeviceForExit(true, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                String curDeviceId = result.getDevice().getAddress();
                if (curDeviceId != null) {
                    for (Child child : childListForExit) {
                        Log.d("push", "curDeviceId : " + curDeviceId + " , c d : " + child.getDeviceId());
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
                for (Child child : childListForExit_copy){
                    PushMessageUtil.sendPushNotification(child.getDeviceId(), child.getName(), false);
                    Log.d("push", child.getDeviceId());
                    childLocationUpdateDisposable = ApiService.getPARENT_SERVICE().updateChildLocation(child.getIdx(),
                            locationTracker.getLat(), locationTracker.getLng())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<BaseResponse>() {
                                @Override
                                public void accept(BaseResponse baseResponse) {
                                    Log.d("update", baseResponse.status);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) {

                                }
                            });
                    for (Child child1 : childListForExit_copy) {
                        childListForExit.remove(child1);
                    }
                    childListAdapter.notifyDataSetChanged();
                    if (childListForExit.isEmpty()) {
                        driveDefaultContainer.setVisibility(View.GONE);
                        driveDriveContainer.setVisibility(View.GONE);
                        driveEndContainer.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap);
        getChildList(Integer.parseInt(idx));
    }


    private void getChildList(int driverId) {
        disposable = ApiService.getDRIVER_SERVICE().getChildList(driverId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<Child>>() {
                    @Override
                    public void accept(ArrayList<Child> children) {
                        childArrayList.clear();
                        childListForEndDrive.clear();
                        childListForExit.clear();
                        map.clear();
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
                        childListAdapter.notifyDataSetChanged();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
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
                    progressDialog.dismiss();
                    Toast.makeText(DriverActivity.this, "남아있는 아이가 없습니다.", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            driveDefaultContainer.setVisibility(View.VISIBLE);
                            driveDriveContainer.setVisibility(View.GONE);
                            driveEndContainer.setVisibility(View.GONE);
                        }
                    }, 1000);
                } else {
                    Toast.makeText(DriverActivity.this, "아이가 남아있습니다. 다시 한번 스캔해보세요.", Toast.LENGTH_SHORT).show();
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

        if (childLocationUpdateDisposable != null) {
            childLocationUpdateDisposable.dispose();
        }
    }
}
