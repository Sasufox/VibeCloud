package com.example.vibecloud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.RemoteViews;
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
    static int loop=0;
    public static volatile String json_return;
    private static String web_song_url;
    static public ArrayList<Music> recommendation = new ArrayList();
    static public volatile boolean isInRecommendation;

    static int index_playlist=0;

    TestServiceShit mClient;

    static MediaPlayer mediaPlayer;
    static Music song;
    String image_url, name, author;
    static MutableLiveData<Boolean> isreco;
    static boolean mediaPrepared;
    Notification notification;

    public static void playlist(){
        //Starting song
        mediaPrepared = false;
        System.out.println("PREPARED???? " + mediaPrepared);
        isreco.setValue(false);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPrepared=true;
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        web_song_url = MusicSelection.url_base + "static/youtube/" + recommendation.get(index_playlist).getId();

        try {
            mediaPlayer.setDataSource(web_song_url);
            mediaPlayer.prepareAsync();
            mediaPrepared=true;
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Bundle b = intent.getBundleExtra("bundle_song");
//        String song = b.getString("song");
        System.out.println("lol2");
        final String CHANNELID = "Foreground Service ID";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else {
            System.out.println("NIGGAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            startForeground(1, new Notification());
        }

        //System.out.println(song);
        return START_NOT_STICKY;
    }

    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.vibecloud2";
        String channelName = "My Background Service";
        NotificationChannel chan = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            System.out.println("21");
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan.setLightColor(Color.BLUE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        }
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(chan);
        }

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small);
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_big);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Niggers")
                .setContentText("I hate niggers")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .build();
        startForeground(2, notification);
    }

    public static void continueRecommendation(String song){
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
        stopForeground(true);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}