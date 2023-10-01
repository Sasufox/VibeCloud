package com.example.vibecloud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class TestServiceShit extends AppCompatActivity{

    //fields
    public TextView nameView, authorView;
    public ImageView imageView, play_pause, nextSong, previousSong, repeat;
    public SeekBar seekbar;
    private int loop=0;
    public volatile String json_return;
    private String web_song_url;
    Context context = this;
    public ArrayList<Music> recommendation = new ArrayList();
    public volatile Drawable d;
    public volatile boolean isInRecommendation;
    public boolean song_unique=false;

    public int index_playlist, max;

    TextView current, missed;

    BlurView blurView;
    Music song;
    String image_url, name, author;
    Timer timer;
    ServiceTest mService;
    boolean mBound;
    Intent service;

    boolean isOnResume;
    boolean demarremtn=true;


    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_listening);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        //extracting infos from previously Activity
        System.out.println("ActivityHome " + ActivityHome.serviceOn);
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("bundle_recommendation");
        recommendation = (ArrayList<Music>) args.getSerializable("recommendation");

        for (int i=0; i<recommendation.size(); i++){
            System.out.println(recommendation.get(i).getName());
        }

        if (recommendation.size()<=1){
            song_unique=true;
            mService.song_unique=true;
        }

        System.out.println("Song_unique = " + song_unique);

        current = findViewById(R.id.song_current);
        missed = findViewById(R.id.song_missed);

        //Setting variables uptoDate
        nameView = findViewById(R.id.music_name);
        authorView = findViewById(R.id.music_author);
        imageView = findViewById(R.id.music_image);
        play_pause = findViewById(R.id.pause_play_image);
        seekbar = findViewById(R.id.seek_bar);

        nextSong = findViewById(R.id.next);
        previousSong = findViewById(R.id.previous);
        blurView = findViewById(R.id.blur_layout);

        nameView.setText(name);
        authorView.setText(author);

        repeat = findViewById(R.id.repeat);

        service=new Intent(this, ServiceTest.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ActivityHome.serviceOn) {
                mService.mediaPlayer.stop();
                mService.mediaPlayer.reset();
                mService.mediaPlayer.release();
                mService.index_playlist=0;
            }
            else{
                startForegroundService(service);
            }
        }

        if (demarremtn) isOnResume=false;
        demarremtn=false;
        System.out.println(isOnResume);
        if (!isOnResume) {
            mService.recommendation = recommendation;
            activity_action();
        }
        else {
            recommendation = mService.recommendation;
            index_playlist = mService.index_playlist;
            song = recommendation.get(index_playlist);
            image_url = song.getImage();

            Thread t = new Thread() {
                public void run() {
                    InputStream is = null;
                    try {
                        is = (InputStream) new URL(image_url).getContent();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    d = Drawable.createFromStream(is, "blurry image");
                    d.setAlpha(180);
                }
            };
            t.start();
            start_music();
            try {
                t.join();
                LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                linearLayout.setBackgroundDrawable(d);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void blurBackground() {
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

    public void setIndex_playlist(int i){
        this.index_playlist=i;
    }

    public void onBackPressed() {
        timer.cancel();
        if (!mService.mediaPlayer.isPlaying()){
            stopService(service);
        }
        else {
            ActivityHome.service = service;
            ActivityHome.serviceOn = true;
        }
        Intent otherActivity;
        otherActivity = new Intent(getApplicationContext(), ActivityHome.class);
        startActivity(otherActivity);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        recommendation = mService.recommendation;
        index_playlist = mService.index_playlist;
        song = recommendation.get(index_playlist);
        image_url = song.getImage();
        Thread t = new Thread() {
            public void run() {
                InputStream is = null;
                try {
                    is = (InputStream) new URL(image_url).getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                d = Drawable.createFromStream(is, "blurry image");
                d.setAlpha(180);
            }
        };
        t.start();
        start_music();
        try {
            t.join();
            LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
            linearLayout.setBackgroundDrawable(d);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isOnResume = true;
        super.onResume();
    }

    public void activity_action(){
        mService.recommendation = recommendation;
        Thread t = new Thread() {
            public void run() {
                InputStream is = null;
                try {
                    is = (InputStream) new URL(image_url).getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                d = Drawable.createFromStream(is, "blurry image");
                d.setAlpha(180);
                LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                linearLayout.setBackgroundDrawable(d);
                blurBackground();
            }
        };
        t.start();
        song=recommendation.get(index_playlist);
        start_music();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        utilise();
        mService.playlist();
        while (!mService.mediaPrepared){}
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mService.mediaPlayer.isPlaying()) {
                    max = mService.mediaPlayer.getDuration() / 1000;
                    seekbar.setMax(max);
                    seekbar.setProgress(mService.mediaPlayer.getCurrentPosition() / 1000);

                    int decimal = max - (max / 60) * 60;
                    String d = String.valueOf(max - (max / 60) * 60);
                    if (decimal < 10) {
                        d = 0 + String.valueOf(decimal);
                    }
                    String c = String.valueOf(max / 60) + ":" + d;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            missed.setText(c);
                        }
                    });
                }
            }
        }, 1000, 1000);

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loop==0){
                    loop=1;
                    mService.loop=1;
                    Drawable myDrawable = getResources().getDrawable(R.drawable.repeat_loop);
                    repeat.setImageDrawable(myDrawable);
                }
                else{
                    loop=0;
                    mService.loop=0;
                    Drawable myDrawable = getResources().getDrawable(R.drawable.repeat);
                    repeat.setImageDrawable(myDrawable);
                }
            }
        });

        play_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mService.mediaPlayer.isPlaying()){
                    seekbar.setProgress(mService.mediaPlayer.getCurrentPosition()/1000);
                    mService.mediaPlayer.pause();
                    Drawable myDrawable = getResources().getDrawable(R.drawable.play);
                    play_pause.setImageDrawable(myDrawable);
                }
                else{
                    mService.mediaPlayer.start();
                    Drawable myDrawable = getResources().getDrawable(R.drawable.pause);
                    play_pause.setImageDrawable(myDrawable);
                }
            }
        });

        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loop==0) {
                    if (song_unique){
                        mService.mediaPlayer.pause();
                        mService.mediaPlayer.seekTo(0);
                        seekbar.setProgress(0);
                        Drawable myDrawable = getResources().getDrawable(R.drawable.play);
                        play_pause.setImageDrawable(myDrawable);
                    }
                    else if (!(index_playlist==recommendation.size()-1) && !isInRecommendation) {
                        mService.mediaPlayer.stop();
                        mService.mediaPlayer.reset();
                        if ((index_playlist==recommendation.size()-2)) {
                            Thread t2 = new Thread() {
                                public void run() {
                                    System.out.println(index_playlist + " " + recommendation.toString());
                                    continueRecommendation(recommendation.get(index_playlist).getId());
                                }
                            };
                            t2.start();
                        }
                        index_playlist++;
                        mService.index_playlist++;

                        song=recommendation.get(index_playlist);
                        image_url=song.getImage();

                        Thread t = new Thread() {
                            public void run() {
                                InputStream is = null;
                                try {
                                    is = (InputStream) new URL(image_url).getContent();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                d = Drawable.createFromStream(is, "blurry image");
                                d.setAlpha(180);
                            }
                        };
                        t.start();
                        start_music();

                        web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();
                        try {
                            mService.mediaPlayer.setDataSource(web_song_url);
                            mService.mediaPlayer.prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            t.join();
                            LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                            linearLayout.setBackgroundDrawable(d);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Thread t = new Thread() {
                            public void run() {
                                continueRecommendation(recommendation.get(index_playlist).getId());
                            }
                        };
                        t.start();

                        mService.mediaPlayer.seekTo(0);
                        mService.mediaPlayer.start();
                    }
                }
                else{
                    mService.mediaPlayer.seekTo(0);
                    mService.mediaPlayer.start();
                }
            }
        });

        previousSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index_playlist!=0 && loop!=1) {
                    mService.mediaPlayer.stop();
                    mService.mediaPlayer.reset();
                    index_playlist--;
                    mService.index_playlist--;

                    song=recommendation.get(index_playlist);
                    image_url=song.getImage();

                    Thread t = new Thread() {
                        public void run() {
                            InputStream is = null;
                            try {
                                is = (InputStream) new URL(image_url).getContent();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            d = Drawable.createFromStream(is, "blurry image");
                            d.setAlpha(180);
                            LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                            linearLayout.setBackgroundDrawable(d);
                        }
                    };
                    t.start();
                    start_music();
                    web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();
                    try {
                        mService.mediaPlayer.setDataSource(web_song_url);
                        mService.mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        t.join();
                        LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                        linearLayout.setBackgroundDrawable(d);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    System.out.println(index_playlist);
                    mService.mediaPlayer.seekTo(0);
                    mService.mediaPlayer.start();
                }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if(fromUser) {
                    mService.mediaPlayer.seekTo(seekbar.getProgress() * 1000);
                }
                int time = seekBar.getProgress();
                int decimal = time-(time/60)*60;
                String d = String.valueOf(time-(time/60)*60);
                if (decimal<10){
                    d=0+String.valueOf(decimal);
                }
                String c = String.valueOf(time/60) + ":" + d;
                current.setText(c);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    // Fonctions actions
    public void continueRecommendation(String song){
        if (!isInRecommendation) {
            isInRecommendation=true;
            String search = "{\"video\": \"" + song + "\"}";
            String url = MusicSelection.url_base + "recommendation";
            System.out.println(search);
            System.out.println(url);

            json_return = null;

            Thread t = new Thread() {
                public void run() {
                    try {
                        json_return = MainActivity.sendRequest(url, search);
                    } catch (RequestException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(json_return == null)
                return;

            try {
                JSONArray ja = new JSONArray(json_return);

                for (int i = 0; i < 15; i++) {
                    //MUSIC
                    JSONObject j = ja.getJSONObject(i);
                    String title = j.getString("title");
                    String url_image = j.getString("thumbnail");
                    String a = j.getString("artists");
                    String id = j.getString("link");
                    Music m = new Music(title, a, url_image);
                    m.setId(id);
                    if (!recommendation.contains(m)) {
                        recommendation.add(m);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            isInRecommendation=false;
        }
    }

    public void start_music(){
        //Starting song
        name = song.getName();
        author = song.getAuthor();
        image_url = song.getImage();

        nameView.setText(name);
        authorView.setText(author);

        Glide.with(this).load(image_url).into(imageView);
        System.out.println(song.getId());
    }

    public void utilise(){
        mService.isreco = new MutableLiveData<>();
        mService.isreco.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(final Boolean newBooleanValue) {
                if (mService.isreco.getValue()==true) {
                    System.out.println("HEHO LES GAYS");
                    if ((index_playlist == recommendation.size() - 1)) {
                        mService.recommendation = recommendation;
                    }
                    if (loop==0) {
                        index_playlist += 1;
                        song = recommendation.get(index_playlist);
                        image_url = song.getImage();
                        Thread t = new Thread() {
                            public void run() {
                                InputStream is = null;
                                try {
                                    is = (InputStream) new URL(image_url).getContent();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                d = Drawable.createFromStream(is, "blurry image");
                                d.setAlpha(180);
                                LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                                linearLayout.setBackgroundDrawable(d);
                                blurBackground();
                            }
                        };
                        t.start();
                        start_music();
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mService.isreco.setValue(false);
                    }
                }
            }
        });
    }

}