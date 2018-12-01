package com.example.yuminkim.childhold.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.model.ChildLocationResponse;
import com.example.yuminkim.childhold.model.LatLng;
import com.example.yuminkim.childhold.network.ApiService;
import com.example.yuminkim.childhold.network.model.AbsentResponse;
import com.example.yuminkim.childhold.network.model.AbsentStatusResponse;
import com.example.yuminkim.childhold.util.Constants;
import com.example.yuminkim.childhold.util.PushMessageUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ParentActivity extends BaseActivity {
    RelativeLayout childLocationButton, parent_absent_btn;
    private com.google.android.gms.maps.model.LatLng center;
    Disposable disposable;
    Disposable disposable2;
    Disposable disposable3;
    Disposable disposableChild;
    String idx;
    boolean isAbsent = false;
    View layout_cover_absent;
    String childName;
    double childLat, childLng, busLat, busLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_parent);
        super.onCreate(savedInstanceState);
        idx = getIntent().getStringExtra(Constants.KEY_IDX);
        childLocationButton = findViewById(R.id.child_location_btn);
        parent_absent_btn = findViewById(R.id.parent_absent_btn);
        layout_cover_absent = findViewById(R.id.layout_cover_absent);

        disposable3 = ApiService.getPARENT_SERVICE().getChildAbsentState(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<AbsentStatusResponse>() {
                    @Override
                    public void accept(AbsentStatusResponse absentStatusResponse) {
                        processChildAbsentState(absentStatusResponse);
                    }
                });

        childLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               getChildLocation();
            }
        });

        parent_absent_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callAbsentDialog();
            }
        });

        findViewById(R.id.update_bus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAbsent) return;
                disposable = ApiService.getPARENT_SERVICE().getDriverLocation(Integer.parseInt(idx))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<LatLng>() {
                            @Override
                            public void accept(LatLng latLng) {
                                busLat = latLng.getLat();
                                busLng = latLng.getLng();
                                updateMap();
                            }
                        });
            }
        });
    }

    private void processChildAbsentState(AbsentStatusResponse absentStatusResponse) {
        if(absentStatusResponse.isAbsent())
            isAbsent = true;
        else
            isAbsent = false;
        childName = absentStatusResponse.name;
        layout_cover_absent.setVisibility(isAbsent ? View.VISIBLE : View.GONE);
    }

    private void processUpdateDriverLocation() {
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.mini_bus);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 110, 110, false);
        map.addMarker( new MarkerOptions().position(
                new com.google.android.gms.maps.model.LatLng(busLat, busLng)
                ).title(String.format("기사위치")).icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
        );
    }

    private void processUpdateChildLocation() {
        map.addMarker( new MarkerOptions().position(
                new com.google.android.gms.maps.model.LatLng(childLat, childLng)
                ).title(String.format("아이위치"))
        );
    }

    private void updateMap() {
        map.clear();
        processUpdateDriverLocation();
        processUpdateChildLocation();
        if (busLat != 0 && childLat != 0 && childLat != 0 && childLng != 0) {
            center = new com.google.android.gms.maps.model.LatLng((busLat + childLat) / (double) 2,
                    (busLng + childLng) / (double) 2);
        } else {
            center = new com.google.android.gms.maps.model.LatLng(busLat + childLat, busLng + childLng);
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 14));
    }

    private void getChildLocation() {
        childLat = 0;
        childLng = 0;
        disposableChild = ApiService.getPARENT_SERVICE().getChildLocation(Integer.parseInt(idx))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ChildLocationResponse>() {
                    @Override
                    public void accept(ChildLocationResponse childLocationResponse) {
                        if (childLocationResponse.who.equals("bus")) {
                            busLat = childLocationResponse.lat;
                            busLng = childLocationResponse.lng;
                        } else {
                            childLat = childLocationResponse.lat;
                            childLng = childLocationResponse.lng;
                        }
                        updateMap();
                    }
                });
    }

    private void callAbsentDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(ParentActivity.this)
                .setTitle("부재 알림")
                .setMessage("아이의 상태를 (" + (isAbsent ? "승차함" : "승차하지않음") + ") 으로 변경하시겠습니까?")
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
                                        PushMessageUtil.sendAbsentPushNotification(absentResponse.driverId, idx, childName, isAbsent);
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
