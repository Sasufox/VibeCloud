package com.example.vibecloud;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //fields

    private MediaPlayer mediaPlayer;
    //private Button search_song_btn;
    //private ImageView im;
    //private Button next;
    //private String json_return;
    Button login, cancel;
    EditText username_insert, password_insert;
    TextView createAccount;
    public static volatile String token;

    public static volatile String name;

    // Handle server request exception inside a thread
    private volatile RequestException requestException = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_login_activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //find id for vars
        login = findViewById(R.id.login);
        cancel = findViewById(R.id.cancel_login);
        username_insert = findViewById(R.id.username_entry);
        password_insert = findViewById(R.id.password_entry);
        createAccount = findViewById(R.id.create_account);

        SharedPreferences sp1=this.getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences sp_adr=this.getSharedPreferences("IP", MODE_PRIVATE);

        String username = sp1.getString("username", null);
        String password = sp1.getString("password", null);
        String address = sp_adr.getString("address", null);
        System.out.println("xdddd " + address);

        System.out.println(username + " " + password);

        if (username!=null && password!=null){
            if (address!=null && address!=""){
                MusicSelection.url_base=address;
            }
            String inscription = "{\"username\": \"" + username + "\", \"password\": \"" + password + "\"}";
            String url = MusicSelection.url_base + "connect";
            System.out.println(inscription);
            System.out.println(url);

            token = null;

            RequestException exception = null;

            Thread t = new Thread() {
                public void run() {
                    try {
                        token = MainActivity.sendRequest(url, inscription);
                    } catch (RequestException e) {
                        e.printStackTrace();
                        requestException = e;
                    }
                }
            };
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(requestException != null) {
                requestException.showExceptionToast(getApplicationContext());
                requestException = null;
            }

            if (token!=null) {
                SharedPreferences sp=getSharedPreferences("Login", MODE_PRIVATE);
                SharedPreferences.Editor Ed_adr = sp_adr.edit();
                SharedPreferences.Editor Ed=sp.edit();
                Ed.putString("username", username);
                Ed.putString("password", password);
                Ed_adr.putString("address", address);
                Ed.commit();
                Ed_adr.commit();
                name=username;
                System.out.println(name);

                Intent activityHome = new Intent(getApplicationContext(), ActivityHome.class);
                startActivity(activityHome);
                finish();
            }
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(username_insert.getText().toString().contains("http"));
                if (username_insert.getText().toString().contains("http")){
                    System.out.println("IP ENREGISTREE");
                    SharedPreferences sp_adr=getSharedPreferences("IP", MODE_PRIVATE);
                    SharedPreferences.Editor Ed_adr=sp_adr.edit();
                    Ed_adr.putString("address", String.valueOf(username_insert.getText()));
                    System.out.println("HERE COMES THE ADDRESS " + String.valueOf(username_insert.getText()));
                    Ed_adr.commit();
                }
                if (!username_insert.getText().toString().equals("")){
                    if (!password_insert.getText().toString().equals("")){
                        if (address!=null)
                            MusicSelection.url_base=address;
                        String inscription = "{\"username\": \"" + username_insert.getText().toString() + "\", \"password\": \"" + password_insert.getText().toString() + "\"}";
                        String url = MusicSelection.url_base + "connect";
                        System.out.println(inscription);
                        System.out.println(url);

                        token = null;

                        Thread t = new Thread() {
                            public void run() {
                                try {
                                    token = MainActivity.sendRequest(url, inscription);
                                } catch (RequestException e) {
                                    e.printStackTrace();
                                    requestException = e;
                                }
                            }
                        };
                        t.start();
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(requestException != null) {
                            requestException.showExceptionToast(getApplicationContext());
                            requestException = null;
                            return;
                        }

                        if (token!=null) {
                            SharedPreferences sp=getSharedPreferences("Login", MODE_PRIVATE);
                            SharedPreferences.Editor Ed=sp.edit();
                            Ed.putString("username", String.valueOf(username_insert.getText()));
                            Ed.putString("password", String.valueOf(password_insert.getText()));
                            Ed.commit();

                            Intent activityHome = new Intent(getApplicationContext(), ActivityHome.class);
                            startActivity(activityHome);
                            finish();
                        }
                        else{
                            Toast toast = Toast.makeText(MainActivity.this, "User or password wrong", Toast.LENGTH_LONG);
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

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUp = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(signUp);
                finish();
            }
        });
    }

    public static String sendRequest(String url, String song_title) throws RequestException {
        final MediaType JSON = MediaType.parse("application/json");

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(song_title, JSON);

        Request request = new Request.Builder().url(url).header("Content-Type", "application/json").post(body).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.code()>=200 && response.code()<=299)
                if (response.body() != null) {
                    return response.body().string();
                }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RequestException("Can't send the request to the server");
        }
        return null;
    }
}