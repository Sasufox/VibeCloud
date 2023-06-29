package com.example.vibecloud;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;

import java.io.IOException;
import java.util.Random;

public class LocalService extends Service {
    // Binder given to clients.
    boolean nig=false;
    MediaPlayer mediaPlayer;

    private final IBinder binder = new LocalBinder();
    // Random number generator.
    private final Random mGenerator = new Random();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */


    public class LocalBinder extends Binder {
        LocalService getService() {
            System.out.println("lol");
            // Return this instance of LocalService so clients can call public methods.
            return LocalService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Bundle b = intent.getBundleExtra("bundle_song");
//        String song = b.getString("song");
        System.out.println("lol2");
        //System.out.println(song);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        System.out.println("lol2222222222222");
        nig=true;
        return binder;
    }

    /** Method for clients. */
    public int getSongTime() {
        return mGenerator.nextInt(100);
    }

    public int setSongTime() {
        return mGenerator.nextInt(100);
    }

    public void playSong(String url){
        mediaPlayer=new MediaPlayer();
        //mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            System.out.println("ICI DATASOURCE ......");
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            System.out.println("NIGGERS");
            throw new RuntimeException(e);
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                System.out.println("HATE NIGGERS");
                SystemClock.sleep(200);
                mp.start();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
    }
}
