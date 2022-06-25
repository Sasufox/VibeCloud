package com.example.vibecloud;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.SearchView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class search_test extends AppCompatActivity {

    SearchView searchView;
    public volatile String json_return;
    private MediaPlayer mediaPlayer;
    ArrayList<Music> listMusic = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_panel);

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            ActivityHome.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            ActivityHome.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        searchView = findViewById(R.id.simpleSearchView);

        ListView listItems = findViewById(R.id.songs_list);
        listItems.setAdapter(new ListMusicAdapter(this, listMusic));

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });
        searchView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode==KeyEvent.KEYCODE_ENTER) { //Whenever you got user click enter. Get text in edittext and check it equal test1. If it's true do your code in listenerevent of button3
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return false;

            }
         });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //listMusic=new ArrayList<Music>();
                listMusic.clear();

                String search = "{\"query\": \"" + s + "\"}";
                String url = MusicSelection.url_base + "search";
                System.out.println(search);
                System.out.println(url);

                json_return = null;

                Thread t = new Thread() {
                    public void run() {
                        json_return = MainActivity.sendRequest(url, search);
                        System.out.println("JSON RETURN = " + json_return);
                    }
                };
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                try {

                    JSONArray ja = new JSONArray(json_return);
                    for (int i=0; i<ja.length(); i++){
                        //MUSIC
                        JSONObject j = ja.getJSONObject(i);
                        String title = j.getString("title");
                        String url_image = j.getString("thumbnail");
                        String a = j.getString("artists");
                        String id = j.getString("link");
                        Music m = new Music(title, a, url_image);
                        m.setId(id);
                        listMusic.add(m);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                searchView.clearFocus();
                listItems.invalidateViews();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    public void onBackPressed() {
        Intent otherActivity;
        if (listMusic.size()>0) {
            otherActivity = new Intent(getApplicationContext(), search_test.class);
        }
        else {
            otherActivity = new Intent(getApplicationContext(), ActivityHome.class);
        }
        startActivity(otherActivity);
        finish();
    }
}