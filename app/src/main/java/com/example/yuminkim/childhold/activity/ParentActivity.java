package com.example.yuminkim.childhold.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.model.LatLng;
import com.example.yuminkim.childhold.network.ApiService;
import com.example.yuminkim.childhold.network.model.AbsentResponse;
import com.example.yuminkim.childhold.util.Constants;
import com.example.yuminkim.childhold.util.PushMessageUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ParentActivity extends BaseActivity {
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
        setContentView(R.layout.activity_parent);
        super.onCreate(savedInstanceState);
        idx = getIntent().getStringExtra(Constants.KEY_IDX);
        parent_locaion_btn = findViewById(R.id.parent_location_btn);
        parent_absent_btn = findViewById(R.id.parent_absent_btn);
        layout_cover_absent = findViewById(R.id.layout_cover_absent);
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
                callAbsentDialog();
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
                        isAbsent = !isAbsent;
                        layout_cover_absent.setVisibility(isAbsent ? View.VISIBLE : View.GONE);
                        disposable2 = ApiService.getPARENT_SERVICE().updateChildAbsent(Integer.parseInt(idx), isAbsent ? 1 : 0)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<AbsentResponse>() {
                                    @Override
                                    public void accept(AbsentResponse absentResponse) {
                                        //PushMessageUtil.sendAbsentPushNotification(absentResponse.driverId, idx, childName, isAbsent);
                                    }
                                });
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create();
        alertDialog.show();
    }
}
