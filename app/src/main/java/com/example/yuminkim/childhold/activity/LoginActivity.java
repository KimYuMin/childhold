package com.example.yuminkim.childhold.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListPopupWindow;

import com.example.yuminkim.childhold.R;

public class LoginActivity extends Activity {

    private Button loginButton;
    private EditText userTypeEditText;
    private EditText passwordEditText;

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
                //TODO do login
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
        ListPopupWindow popupWindow = new ListPopupWindow(this);
        popupWindow.setAdapter(
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        new String[]{"운전자", "보호자"}
                )
        );
        popupWindow.setHeight(480);
        popupWindow.setWidth(userTypeEditText.getMeasuredWidth());
        popupWindow.setAnchorView(userTypeEditText);

        popupWindow.show();
    }
}
