package com.example.vibecloud;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
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

public class ServiceTest extends Service {

    //fields
    int loop=0;
    public volatile String json_return;
    private String web_song_url;
    public ArrayList<Music> recommendation = new ArrayList();
    public volatile boolean isInRecommendation;

    int index_playlist=0;

    TestServiceShit mClient;

    MediaPlayer mediaPlayer;
    Music song;
    String image_url, name, author;
    MutableLiveData<Boolean> isreco;
    private final IBinder binder = new ServiceTest.LocalBinder();

    public void playlist(){
        //Starting song
        isreco.setValue(false);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        web_song_url = MusicSelection.url_base + "static/youtube/" + recommendation.get(index_playlist).getId();

        try {
            mediaPlayer.setDataSource(web_song_url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                System.out.println("HEHO ICI ON PASSE");
                if (loop==0){
                    if (!(index_playlist==recommendation.size()-1) && !isInRecommendation) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();

                        if ((index_playlist==recommendation.size()-2)) {
                            Thread t2 = new Thread() {
                                public void run() {
                                    System.out.println(index_playlist + " " + recommendation.toString());
                                    continueRecommendation(recommendation.get(index_playlist).getId());
                                    isreco.postValue(true);
                                }
                            };
                            t2.start();
                        }
                        index_playlist++;

                        song=recommendation.get(index_playlist);
                        web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();
                        try {
                            mediaPlayer.setDataSource(web_song_url);
                            mediaPlayer.prepareAsync();
                            if (!isreco.getValue()==true){
                                isreco.setValue(true);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Thread t = new Thread() {
                            public void run() {
                                continueRecommendation(recommendation.get(index_playlist).getId());
                                isreco.setValue(true);
                            }
                        };
                        t.start();

                        mediaPlayer.seekTo(0);
                        mediaPlayer.start();
                    }
                }
                else{
                    System.out.println("HERE1");
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
            }
        });
    }

    public class LocalBinder extends Binder {
        ServiceTest getService() {
            System.out.println("lol");
            // Return this instance of LocalService so clients can call public methods.
            return ServiceTest.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Bundle b = intent.getBundleExtra("bundle_song");
//        String song = b.getString("song");
        System.out.println("lol2");
        //System.out.println(song);
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("lol2222222222222");

        return binder;
    }


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
                    if (!recommendation.contains(m))
                        recommendation.add(m);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            isInRecommendation=false;
        }
    }

    public void setIndex_playlist(int i){
        this.index_playlist=i;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        super.onDestroy();
    }


}