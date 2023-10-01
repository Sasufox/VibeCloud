package com.example.vibecloud;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.MutableLiveData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class ServiceTest extends Service {

    //fields
    static int loop = 0;
    public static volatile String json_return;
    private static String web_song_url;
    static public ArrayList<Music> recommendation = new ArrayList();
    static public volatile boolean isInRecommendation;
    public static volatile Bitmap image;
    public static volatile Drawable d;

    public static volatile Drawable p_b;
    public static volatile Drawable pause_b;

    static NotificationManagerCompat managerCompat;

    static int index_playlist = 0;

    TestServiceShit mClient;

    static MediaPlayer mediaPlayer=null;
    static Music song;
    String image_url, name, author;
    static MutableLiveData<Boolean> isreco;
    static boolean mediaPrepared;
    static boolean song_unique;
    static RemoteViews notificationLayout;
    static boolean isPlaying;
    Notification notification;

    static NotificationCompat.Builder notif;

    static NotificationCompat.Builder builder;

    public static void playlist() {
        //Starting song
        mediaPrepared = false;
        System.out.println("PREPARED???? " + mediaPrepared);
        isreco.setValue(false);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        web_song_url = MusicSelection.url_base + "static/youtube/" + recommendation.get(index_playlist).getId();

        try {
            mediaPlayer.setDataSource(web_song_url);
            mediaPlayer.prepareAsync();
            mediaPrepared = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                isPlaying = true;
                updateNotification();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                System.out.println("HEHO ICI ON PASSE");
                if (loop == 0) {
                    if (song_unique) {
                        mediaPlayer.stop();
                        mediaPlayer.seekTo(0);
                    } else if (!(index_playlist == recommendation.size() - 1) && !isInRecommendation) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();

                        if ((index_playlist == recommendation.size() - 2)) {
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

                        song = recommendation.get(index_playlist);
                        web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();
                        try {
                            mediaPlayer.setDataSource(web_song_url);
                            mediaPlayer.prepareAsync();
                            //updateNotification();
                            if (!isreco.getValue() == true) {
                                isreco.setValue(true);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
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
                } else {
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

        p_b = getResources().getDrawable(R.drawable.play_botton);
        pause_b = getResources().getDrawable(R.drawable.pause_botton);

        System.out.println("lol2");
        final String CHANNELID = "Foreground Service ID";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            testNotif();
        else {
            System.out.println("NIGGAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            startForeground(1, new Notification());
        }

        //System.out.println(song);
        return START_NOT_STICKY;
    }

    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.example.vibecloud2";
        String channelName = "My Background Service";
        NotificationChannel chan = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            System.out.println("21");
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        }

        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notification", "notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }*/

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
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_small);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notification = notificationBuilder
                .setSmallIcon(R.drawable.app_icon)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutExpanded)
                .build();
        startForeground(2, notification);
        System.out.println("Niggers");
    }

    @SuppressLint("MissingPermission")
    public void testNotif() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notification", "notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        notificationLayout = new RemoteViews(getPackageName(), R.layout.test_search);

        Intent nextbutton = new Intent(this, nextReceiver.class);
        nextbutton.setAction("next");
        nextbutton.putExtra("notification", 0);
        nextbutton.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent nextIntent = PendingIntent.getBroadcast(this, 0, nextbutton, 0);

        Intent previousbutton = new Intent(this, nextReceiver.class);
        previousbutton.setAction("previous");
        previousbutton.putExtra("notification", 0);
        previousbutton.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent previousIntent = PendingIntent.getBroadcast(this, 0, previousbutton, 0);

        Intent pause_playbutton = new Intent(this, nextReceiver.class);
        pause_playbutton.setAction("pause_play");
        pause_playbutton.putExtra("notification", 0);
        pause_playbutton.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pause_playIntent = PendingIntent.getBroadcast(this, 0, pause_playbutton, 0);

        Intent quitbutton = new Intent(this, nextReceiver.class);
        quitbutton.setAction("quit");
        quitbutton.putExtra("notification", 0);
        quitbutton.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent quitIntent = PendingIntent.getBroadcast(this, 0, quitbutton, 0);


        builder = new NotificationCompat.Builder(this, "notification");
        builder.setSmallIcon(R.drawable.app_icon);
        builder.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
        builder.setCustomContentView(notificationLayout);
        builder.setAutoCancel(true);
        notificationLayout.setOnClickPendingIntent(R.id.next, nextIntent);
        notificationLayout.setOnClickPendingIntent(R.id.previous, previousIntent);
        notificationLayout.setOnClickPendingIntent(R.id.pause_play, pause_playIntent);
        notificationLayout.setOnClickPendingIntent(R.id.quit, quitIntent);

        managerCompat = NotificationManagerCompat.from(ServiceTest.this);
        managerCompat.notify(1, builder.build());
        startForeground(1, builder.getNotification());
    }

    public static class nextReceiver extends BroadcastReceiver {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == "next") {
                if (loop == 0) {
                    if (song_unique) {
                        mediaPlayer.stop();
                        mediaPlayer.seekTo(0);
                    } else if (!(index_playlist == recommendation.size() - 1) && !isInRecommendation) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();

                        if ((index_playlist == recommendation.size() - 2)) {
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

                        song = recommendation.get(index_playlist);
                        web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();
                        try {
                            mediaPlayer.setDataSource(web_song_url);
                            mediaPlayer.prepareAsync();
                            //updateNotification();
                            if (!isreco.getValue() == true) {
                                isreco.setValue(true);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
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
                } else {
                    System.out.println("HERE1");
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
            }
            if (intent.getAction() == "previous") {
                if (index_playlist != 0 && loop != 1) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    index_playlist--;

                    song = recommendation.get(index_playlist);
                    web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();
                    try {
                        mediaPlayer.setDataSource(web_song_url);
                        mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println(index_playlist);
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
            }
            if (intent.getAction() == "pause_play") {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    Bitmap bitmap = ((BitmapDrawable) p_b).getBitmap();
                    notificationLayout.setImageViewBitmap(R.id.pause_play, bitmap);
                } else {
                    mediaPlayer.start();
                    Bitmap bitmap = ((BitmapDrawable) pause_b).getBitmap();
                    notificationLayout.setImageViewBitmap(R.id.pause_play, bitmap);
                }
                managerCompat.notify(1, builder.build());
            }
            if (intent.getAction()=="quit"){
                context.stopService(new Intent(context, ServiceTest.class));
            }
        }
    }

    @SuppressLint("MissingPermission")
    public static void updateNotification() {
        System.out.println("I hate niggers");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Thread t = new Thread() {
                public void run() {
                    InputStream is = null;
                    try {
                        URL url = new URL(recommendation.get(index_playlist).getImage());
                        image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();

            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // update the icon
            notificationLayout.setImageViewBitmap(R.id.songImage, image);
        }

        int api = Build.VERSION.SDK_INT;
        // update the title
        notificationLayout.setTextViewText(R.id.songName, recommendation.get(index_playlist).getName());
        // update the content
        notificationLayout.setTextViewText(R.id.artistName, recommendation.get(index_playlist).getAuthor());
        managerCompat.notify(1, builder.build());
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
                    if (!recommendation.contains(m)) {
                        recommendation.add(m);
                        ListMusicAdapter.list_d.add(getImage(url_image));
                    }
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

    public static Drawable getImage(String url){

        Thread t = new Thread() {
            public void run() {
                InputStream is = null;
                try {
                    is = (InputStream) new URL(url).getContent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                d = Drawable.createFromStream(is, "blurry image");
            }
        };
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return d;
    }
}