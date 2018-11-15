package com.example.yuminkim.childhold.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.yuminkim.childhold.R;

public class LoginActivity extends Activity {
    Button parent_btn, driver_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        parent_btn = (Button)findViewById(R.id.parent_button);
        parent_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ParentActivity.class);
                startActivity(intent);
                finish();
            }
        });

        driver_btn = (Button)findViewById(R.id.driver_button);
        driver_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, DriverActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
