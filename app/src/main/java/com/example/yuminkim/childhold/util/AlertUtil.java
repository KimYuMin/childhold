package com.example.yuminkim.childhold.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertUtil {
    public static AlertDialog showAlert(Context context, String title, String message,
                                        DialogInterface.OnClickListener okClick,
                                        DialogInterface.OnClickListener noClick) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("확인", okClick)
                .setNegativeButton("취소", noClick)
                .create();
        return alertDialog;
    }
}
