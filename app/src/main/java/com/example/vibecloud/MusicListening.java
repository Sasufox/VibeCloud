package com.example.vibecloud;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class MusicListening extends AppCompatActivity {

    //fields
    TextView nameView, authorView;
    ImageView imageView, play_pause;
    SeekBar seekbar;
    private int loop=0;

    BlurView blurView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_blur);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //extracting infos from previously Activity
        Bundle bundle = getIntent().getExtras();
        String[] song_info = bundle.getStringArray("list");

        for (int i=0; i<4;i++){
            System.out.println(song_info[i]);
        }
        System.out.println("ICI : " + song_info[0]);
        String name = song_info[0];
        String author = song_info[1];
        String image_url = song_info[2];
        String web_song_url = song_info[3];

        //Setting variables uptoDate
        nameView = findViewById(R.id.music_name);
        authorView = findViewById(R.id.music_author);
        imageView = findViewById(R.id.music_image);
        play_pause = findViewById(R.id.pause_play_image);
        seekbar = findViewById(R.id.seek_bar);
        
        nameView.setText(name);
        authorView.setText(author);
        Glide.with(this).load(image_url).into(imageView);

        //Starting song
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            Timer timer = new Timer();
            System.out.println(web_song_url);
            mediaPlayer.setDataSource(web_song_url);
            mediaPlayer.prepare();
            mediaPlayer.start();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    seekbar.setMax(mediaPlayer.getDuration()/1000);
                    seekbar.setProgress(mediaPlayer.getCurrentPosition()/1000);
                }
            }, 1000, 1000);
        } catch (IOException e) {
            System.out.println("WTF");
            e.printStackTrace();
        }

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    Drawable myDrawable = getResources().getDrawable(R.drawable.play);
                    play_pause.setImageDrawable(myDrawable);
                }
                else{
                    mediaPlayer.start();
                    Drawable myDrawable = getResources().getDrawable(R.drawable.pause);
                    play_pause.setImageDrawable(myDrawable);
                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if(fromUser) {
                    mediaPlayer.seekTo(seekbar.getProgress() * 1000);
                }
                if (mediaPlayer!=null && !mediaPlayer.isPlaying()){
                    if (loop==0) {
                        seekbar.setProgress(0);
                        Drawable myDrawable = getResources().getDrawable(R.drawable.play);
                        play_pause.setImageDrawable(myDrawable);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
    }
}