package com.example.yuminkim.childhold.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.network.ApiService;
import com.example.yuminkim.childhold.network.model.BaseResponse;
import com.example.yuminkim.childhold.network.model.LoginResponse;
import com.example.yuminkim.childhold.util.Constants;
import com.example.yuminkim.childhold.util.PrefsUtil;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends Activity {
    private Button loginButton;
    private EditText userTypeEditText;
    private EditText passwordEditText;
    private String[] USER_TYPES_TEXT = {"운전자", "보호자"};
    private String userType;
    private Disposable disposable;
    private Disposable loginDisposable;

    private LinearLayout inputContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    private void initView() {
        loginButton = findViewById(R.id.button_login);
        userTypeEditText = findViewById(R.id.edit_text_user_type);
        passwordEditText = findViewById(R.id.edit_text_password);
        inputContainer = findViewById(R.id.container_input);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogin();
            }
        });

        userTypeEditText.setInputType(InputType.TYPE_NULL);
        userTypeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserTypeWindow();
            }
        });
        checkAutoLogin();
    }

    private void checkAutoLogin() {
        String idx = PrefsUtil.getFromPrefs(this, PrefsUtil.KEY_IDX, "");
        if (idx.equals("")) {
            inputContainer.setVisibility(View.VISIBLE);
        } else {
            updateDeviceId(idx);
        }
    }

    private void showUserTypeWindow() {
        final ListPopupWindow popupWindow = new ListPopupWindow(this);
        popupWindow.setAdapter(
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        USER_TYPES_TEXT
                )
        );
        popupWindow.setHeight(480);
        popupWindow.setWidth(userTypeEditText.getMeasuredWidth());
        popupWindow.setAnchorView(userTypeEditText);
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userTypeEditText.setText(USER_TYPES_TEXT[position]);
                if (position == 0) {
                    userType = "driver";
                } else {
                    userType = "parent";
                }
                popupWindow.dismiss();
            }
        });
        popupWindow.show();
    }

    private void doLogin() {
        String code = passwordEditText.getText().toString();
        disposable = ApiService.getCOMMON_SERVICE().login(userType, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<LoginResponse>() {
                    @Override
                    public void accept(LoginResponse loginResponse) {
                        PrefsUtil.saveToPrefs(LoginActivity.this,
                                PrefsUtil.KEY_IDX,
                                loginResponse.idx
                        );
                        updateDeviceId(loginResponse.idx);
                     }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Log.d("fail","fail");
                    }
                });
    }

    private void updateDeviceId(final String idx) {
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String userId = status.getSubscriptionStatus().getUserId();
        if (userId != null) {
            loginDisposable = ApiService.getCOMMON_SERVICE().updateUserDeviceId(userType, idx, userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<BaseResponse>() {
                        @Override
                        public void accept(BaseResponse baseResponse) {
                            Log.d("status", "status : " + baseResponse.status);
                            nextStep(idx);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            Log.d("fail", "fail");
                        }
                    });
        } else {
            nextStep(idx);
        }
    }

    private void nextStep(String idx) {
        if (userType == "parent") {
            Intent intent = new Intent(LoginActivity.this, ParentActivity.class);
            intent.putExtra(Constants.KEY_IDX, idx);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(LoginActivity.this, DriverActivity.class);
            intent.putExtra(Constants.KEY_IDX, idx);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (disposable != null) {
            disposable.dispose();
        }

        if (loginDisposable != null) {
            loginDisposable.dispose();
        }
    }
}
