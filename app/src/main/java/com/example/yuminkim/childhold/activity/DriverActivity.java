package com.example.yuminkim.childhold.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.model.Child;
import com.example.yuminkim.childhold.network.ApiService;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DriverActivity extends Activity {

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        getChildList(1);
    }

    private void getChildList(int driverId) {
        disposable = ApiService.getDRIVER_SERVCE().getChildList(driverId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<Child>>() {
                    @Override
                    public void accept(ArrayList<Child> children) throws Exception {
                        for (Child c: children) {
                            Log.d("child", "name : " + c.getName());
                        }
                    }
                });

    }

    @Override
    protected void onPause() {
        super.onPause();
        disposable.dispose();
    }
}
