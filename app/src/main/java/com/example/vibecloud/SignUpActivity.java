package com.example.vibecloud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SignUpActivity extends AppCompatActivity {

    Button login, cancel;
    EditText username_insert, password_insert;
    TextView signIn;
    public volatile String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_create_account);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //find id for vars
        login = findViewById(R.id.register);
        cancel = findViewById(R.id.cancel_login);
        username_insert = findViewById(R.id.username_entry);
        password_insert = findViewById(R.id.password_entry);
        signIn = findViewById(R.id.signIn);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!username_insert.getText().toString().equals("")){
                    if (!password_insert.getText().toString().equals("")){
                        String inscription = "{\"username\": \"" + username_insert.getText().toString() + "\", \"password\": \"" + password_insert.getText().toString() + "\"}";
                        String url = MusicSelection.url_base + "register";
                        System.out.println(inscription);
                        System.out.println(url);

                        token = null;

                        Thread t = new Thread() {
                            public void run() {
                                token = MainActivity.sendRequest(url, inscription);
                            }
                        };
                        t.start();
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (token!=null) {
                            Intent activityHome = new Intent(getApplicationContext(), ActivityHome.class);
                            startActivity(activityHome);
                            finish();
                        }
                        else{
                            Toast toast = Toast.makeText(SignUpActivity.this, "User already exists", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signIn = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(signIn);
                finish();
            }
        });
    }
}