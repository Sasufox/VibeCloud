package com.example.vibecloud;

import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service implements MediaPlayer.OnCompletionListener {

    private int index_playlist=0;
    private Music song;
    private String web_song_url;
    private MediaPlayer mediaPlayer;
    ArrayList<Music> recommendation;
    public volatile String json_return;
    private int loop=0;

    // Server request exception handling
    private volatile RequestException requestException = null;

    public class ServiceBinder extends Binder{
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String userID = intent.getStringExtra("UserID");
        //extracting infos from previously Activity
        Bundle args = intent.getBundleExtra("bundle_recommendation");
        recommendation = (ArrayList<Music>) args.getSerializable("recommendation");
        System.out.println("BIIIIIIIIIIIIIIIIIIIIIIIIIIIIITEEEEEEEEEEEEEEEEEEEEEEE");

        for (int i=0; i<recommendation.size(); i++){
            System.out.println(recommendation.get(i).getName());
        }
        song = recommendation.get(index_playlist);

        //Starting song
        mediaPlayer = new MediaPlayer();

        web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();

        try {
            mediaPlayer.setDataSource(web_song_url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                start_music();
                mp.start();
            }
        });

        mediaPlayer.setOnCompletionListener(this);


        if (index_playlist==recommendation.size()-1){
            Thread t = new Thread() {
                public void run() {
                    continueRecommendation(recommendation.get(index_playlist).getId());
                }
            };
            t.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new ServiceBinder();
    }

    public void continueRecommendation(String song){
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

        try {
            JSONArray ja = new JSONArray(json_return);

            for (int i=0; i<15; i++){
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

    }

    public void setIndex_playlist(int i){
        this.index_playlist=i;
    }

    public void start_music(){
        //Starting song
        System.out.println(web_song_url);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (loop==0){
            if (!(index_playlist==recommendation.size()-1)) {
                mediaPlayer.reset();
                index_playlist++;

                song=recommendation.get(index_playlist);

                web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();
                try {
                    mediaPlayer.setDataSource(web_song_url);
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
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
            }
        }
        else{
            System.out.println("HERE1");
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        }
    }

    public Music getSong(){
        return this.song;
    }
}
