package com.example.vibecloud;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class test extends AppCompatActivity {
    Button search_song_btn, next;
    ImageView heart;
    public volatile String json_return;
    private MediaPlayer mediaPlayer;
    ArrayList<Music> listMusic = new ArrayList();
    ArrayList<Artist> listArtist = new ArrayList();

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

        listMusic.add(new Music("Playground (from the series Arcane League of Legends)", "Bea Miller", "https://lh3.googleusercontent.com/J0ilK6Uhqjx9XlBA1q1uy3SrI4Q_5U-aSZ08xOgoQdRJI6kPRqrdLVzv-LDDcfbBD94Yig9IIZezX1x8=w120-h120-l90-rj"));
        listMusic.add(new Music("Rise (feat. Jack & Jack)", "Jonas Blue", "https://lh3.googleusercontent.com/358VKElmVdAaa_-nruzxHhz3bBE1GGtRM_EjfOeNOakV_s4u0ot2PvFxhPAzmQuD9-j66T7M8pevKaYC=w120-h120-l90-rj"));
        listMusic.add(new Music("RISE (feat. The Word Alive)", "League of Legends", "https://lh3.googleusercontent.com/9wphzJXb9UONKjd90QpXiyoqRSxg1nd60bjUVnEMN1-f8k1OyB_sDMk40_bEPtxHn2tPbofPRvDRbIpG=w120-h120-l90-rj"));
        listMusic.add(new Music("Rise (feat. Jack & Jack)", "Jonas Blue", "https://lh3.googleusercontent.com/358VKElmVdAaa_-nruzxHhz3bBE1GGtRM_EjfOeNOakV_s4u0ot2PvFxhPAzmQuD9-j66T7M8pevKaYC=w120-h120-l90-rj"));
        listMusic.add(new Music("Rise (feat. Jack & Jack)", "Jonas Blue", "https://lh3.googleusercontent.com/358VKElmVdAaa_-nruzxHhz3bBE1GGtRM_EjfOeNOakV_s4u0ot2PvFxhPAzmQuD9-j66T7M8pevKaYC=w120-h120-l90-rj"));
        listMusic.add(new Music("Rise (feat. Jack & Jack)", "Jonas Blue", "https://lh3.googleusercontent.com/358VKElmVdAaa_-nruzxHhz3bBE1GGtRM_EjfOeNOakV_s4u0ot2PvFxhPAzmQuD9-j66T7M8pevKaYC=w120-h120-l90-rj"));

        //GridView gridView = findViewById(R.id.grid_pop_songs);
        ExpandableHeightGridView mAppsGrid = (ExpandableHeightGridView) findViewById(R.id.grid_pop_songs);
        mAppsGrid.setExpanded(true);
        GridAdapter adapter = new GridAdapter(this, listMusic);
        mAppsGrid.setAdapter(adapter);
        mAppsGrid.setScrollbarFadingEnabled(true);

        listArtist.add(new Artist("BTS", "https://lh3.googleusercontent.com/kwdukSZuJEoHBNLDqNbHH1k2wexAndmTNZXu1J_Yy-0dkdxfwh4eWb2-JJ-lhMlHz0IrUqlOVIO3cgE=w120-h120-p-l90-rj"));
        listArtist.add(new Artist("TWICE", "https://lh3.googleusercontent.com/U4t11qqzjnSvqTaddAmJH1R_MQSODlc8XmmD2uyza4LHvZMfcMBgEDBqRG8z_66DhkVu0bUaGC36sOMN=w120-h120-p-l90-rj"));
        listArtist.add(new Artist("YOASOBI", "https://lh3.googleusercontent.com/1R7O2eSFP8Xv2bVPmyc8dB_HnLK723RizDuNT5x4MyLReA_7PFbI8OUnsuAYvo-VGdOabf_FqoBbFtY=w120-h120-p-l90-rj"));

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
                String search = "{\"query\": \"" + "League of legends" + "\"}";
                String url = MusicSelection.url_base + "search";
                System.out.println(search);
                System.out.println(url);

                json_return = null;

                Thread t = new Thread() {
                    public void run() {
                        json_return = MainActivity.sendRequest(url, search);
                    }
                };
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent otherActivity = new Intent(getApplicationContext(), test.class);
                if (json_return != null)
                    otherActivity.putExtra("key", json_return);
                startActivity(otherActivity);
                finish();
            }
        });


        /*this.search_song_btn = findViewById(R.id.search_song);
        search_song_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread() {
                    public void run() {
                        json_return = MainActivity.sendRequest("http://yoshibox.duckdns.org:8000/search", "{\"query\":\"playground\"}");
                    }
                };
                t.start();
            }
        });*/

        /*next=findViewById(R.id.autre_activite);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent otherActivity = new Intent(getApplicationContext(), MusicSelection.class);
                if (json_return!=null)
                    otherActivity.putExtra("key", json_return);
                startActivity(otherActivity);
                finish();
            }
        });*/
        /*im = findViewById(R.id.icon_song);
        Glide.with(this).load("http://i.imgur.com/DvpvklR.png").into(im);*/
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
}