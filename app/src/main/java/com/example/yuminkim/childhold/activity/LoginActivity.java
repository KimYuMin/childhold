package com.example.yuminkim.childhold.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListPopupWindow;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.network.ApiService;
import com.example.yuminkim.childhold.network.model.BaseResponse;

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
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String idx) {
                        //TODO: idx를 받아왔으니 userType으로 나눠서 activity 이동시키기
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
    }
}
