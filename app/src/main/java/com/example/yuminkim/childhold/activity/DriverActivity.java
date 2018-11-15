package com.example.yuminkim.childhold.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.model.Child;
import com.example.yuminkim.childhold.model.LatLng;
import com.example.yuminkim.childhold.network.ApiService;
import com.example.yuminkim.childhold.network.model.BaseResponse;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DriverActivity extends Activity {

    private Disposable disposable;
    private Disposable disposable2;
    private Disposable disposable3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        getChildList(1);
        getDriverRoute(1);

        findViewById(R.id.button_push).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPushNotification();
            }
        });
    }

    private void getChildList(int driverId) {
        disposable = ApiService.getDRIVER_SERVICE().getChildList(driverId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<Child>>() {
                    @Override
                    public void accept(ArrayList<Child> children) {
                        for (Child c : children) {
                            Log.d("child", "name : " + c.getName());
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
