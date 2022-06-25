package com.example.vibecloud;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class author_activity extends AppCompatActivity {

    //fields
    TextView nameView, authorView;
    ImageView imageView, play_pause, nextSong, previousSong, repeat;
    SeekBar seekbar;
    private int loop=0;
    public volatile String json_return;
    public ArrayList<Music> listMusicNext = new ArrayList();
    private String web_song_url;
    Context context = this;
    private ArrayList<Music> listMusic = new ArrayList();

    private int index_playlist;

    BlurView blurView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.author_panel);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //extracting infos from previously Activity
        Intent intent = getIntent();
        /*Bundle bundle = intent.getExtras();
        String[] song_info = bundle.getStringArray("list");*/

        Bundle args = intent.getBundleExtra("bundle_recommendation");
        ArrayList<Music> recommendation = (ArrayList<Music>) args.getSerializable("recommendation");

        for (int i = 0; i < recommendation.size(); i++) {
            System.out.println(recommendation.get(i).getName());
        }
        this.index_playlist = getIntent().getExtras().getInt("i");
        System.out.println(this.index_playlist);


        Music song = recommendation.get(index_playlist);
        String name = song.getName();
        String author = song.getAuthor();
        String image_url = song.getImage();
        web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();


        //Setting variables uptoDate
        nameView = findViewById(R.id.song_name);
        authorView = findViewById(R.id.author);
        imageView = findViewById(R.id.music_image);
        play_pause = findViewById(R.id.pause_play_image);

        nameView.setText(name);
        authorView.setText(author);
        Glide.with(this).load(image_url).into(imageView);


        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Thread t = new Thread() {
            public void run() {
                InputStream is = null;
                try {
                    is = (InputStream) new URL(image_url).getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Drawable d = Drawable.createFromStream(is, "blurry image");
                LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                linearLayout.setBackgroundDrawable(d);
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        blurView = findViewById(R.id.blur_layout);
        blurBackground();

        listMusic.add(new Music("Rise (feat. Jack & Jack)", "Jonas Blue", "https://lh3.googleusercontent.com/358VKElmVdAaa_-nruzxHhz3bBE1GGtRM_EjfOeNOakV_s4u0ot2PvFxhPAzmQuD9-j66T7M8pevKaYC=w120-h120-l90-rj"));
        listMusic.add(new Music("RISE (feat. The Word Alive)", "League of Legends", "https://lh3.googleusercontent.com/9wphzJXb9UONKjd90QpXiyoqRSxg1nd60bjUVnEMN1-f8k1OyB_sDMk40_bEPtxHn2tPbofPRvDRbIpG=w120-h120-l90-rj"));
        listMusic.add(new Music("Rise (feat. Jack & Jack)", "Jonas Blue", "https://lh3.googleusercontent.com/358VKElmVdAaa_-nruzxHhz3bBE1GGtRM_EjfOeNOakV_s4u0ot2PvFxhPAzmQuD9-j66T7M8pevKaYC=w120-h120-l90-rj"));

        ListView listItems = findViewById(R.id.songs_list);
        listItems.setAdapter(new ListMusicAdapter(this, listMusic));
    }

    private void blurBackground() {
        float radius = 5f;

        View decorView = getWindow().getDecorView();
        //ViewGroup you want to start blur from. Choose root as close to BlurView in hierarchy as possible.
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();

        blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(this))
                .setBlurRadius(radius)
                .setBlurAutoUpdate(true);

        //blurView.setAlpha(0.2f);

    }
}