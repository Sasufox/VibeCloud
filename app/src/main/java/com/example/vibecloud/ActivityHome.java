package com.example.vibecloud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActivityHome extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    Button search_song_btn, next;
    ImageView heart;
    public volatile String json_return;
    private MediaPlayer mediaPlayer;
    ArrayList<Music> listMusic = new ArrayList();
    ArrayList<Music> listTopMusic = new ArrayList();
    ArrayList<Artist> listArtist = new ArrayList();
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        faire();

        navigationView = findViewById(R.id.activity_main_bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.search:
                        Intent otherActivity = new Intent(getApplicationContext(), search_test.class);
                        startActivity(otherActivity);
                        finish();
                        return true;
                }
                return false;
            }
        });

        //ListSongs recommandés
        ExpandableHeightGridView mAppsGrid = (ExpandableHeightGridView) findViewById(R.id.grid_pop_songs);
        mAppsGrid.setExpanded(true);
        GridAdapter adapter = new GridAdapter(this, listMusic);
        mAppsGrid.setAdapter(adapter);
        mAppsGrid.setScrollbarFadingEnabled(true);

        //AUTHOR
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.popular_artists_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewAdapter artistAdapter = new RecyclerViewAdapter(this, listArtist);
        recyclerView.setAdapter(artistAdapter);

        //TOP SONGS
        RecyclerView songsRecyclerView = (RecyclerView) findViewById(R.id.popular_songs_list);

        LinearLayoutManager layoutTopSongs = new LinearLayoutManager(this);
        layoutTopSongs.setOrientation(LinearLayoutManager.HORIZONTAL);
        songsRecyclerView.setLayoutManager(layoutTopSongs);

        ListTopSongsAdapter topSongsAdapter = new ListTopSongsAdapter(this, listMusic);
        songsRecyclerView.setAdapter(topSongsAdapter);


        heart = findViewById(R.id.heart);

        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent otherActivity = new Intent(getApplicationContext(), MainActivity.class);
                SharedPreferences sp=getSharedPreferences("Login", MODE_PRIVATE);
                SharedPreferences.Editor Ed=sp.edit();
                Ed.putString("username", "");
                Ed.putString("password", "");
                Ed.commit();
                startActivity(otherActivity);
                finish();
            }
        });
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public void faire(){
        String search = "{\"country\": \"" + "JP" + "\"}";
        String url = MusicSelection.url_base + "homepage";
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
            JSONObject ja = new JSONObject(json_return);
            JSONArray ja_a = ja.getJSONArray("artists");
            JSONArray ja_m = ja.getJSONArray("musics");
            for (int i=0; i<6; i++){
                //MUSIC
                JSONObject j = ja_m.getJSONObject(i);
                String title = j.getString("title");
                String url_image = j.getString("thumbnail");
                String a = j.getString("artists");
                String id = j.getString("link");
                Music m = new Music(title, a, url_image);
                m.setId(id);
                listTopMusic.add(m);
                listMusic.add(m);

                //ARTISTS
                j = ja_a.getJSONObject(i);
                String name = j.getString("artist");
                String url_artist = j.getString("picture");
                Artist artist = new Artist(name, url_artist);
                listArtist.add(artist);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent otherActivity = new Intent(getApplicationContext(), search_test.class);
                startActivity(otherActivity);
                finish();
                return true;
        }
        return false;
    }
}