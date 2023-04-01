package com.example.vibecloud;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class RequestException extends Exception {
    private String message = new String();

    public RequestException() {

    }

    public RequestException(String message) {
        this.message = message;
    }

    public void showExceptionBar(View view) {

        View rootView = view.getRootView();

        Snackbar bar = Snackbar.make(rootView, this.message, 5000);

        bar.setAnchorView(view);

        bar.show();
    }

    public void showExceptionToast(Context context) {

        Toast toast = Toast.makeText(context, this.message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
