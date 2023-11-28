package com.example.vibecloud;


import static com.example.vibecloud.ActivityHome.setWindowFlag;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class Library extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    BottomNavigationView navigationView;
    public volatile String json_return;

    TextView username;

    ArrayList<Playlist> listPlaylist;

    //###################### Nav #################################
    BlurView blurView;
    TextView songName_Nav;
    TextView authorName;
    ImageView songImage;
    ImageView previous;
    ImageView next;
    ImageView pause_play;
    public SeekBar seekbar;
    Timer timer;
    ListView listItems;

    //############################################################

    ServiceTest mService;
    Intent service;
    static int index;
    public static boolean backToListening;
    public volatile int looping_trap;

    //############################################################
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent otherActivity = new Intent(getApplicationContext(), search_test.class);
                startActivity(otherActivity);
                finish();
                return true;
            case R.id.home:
                Intent otherActivity2 = new Intent(getApplicationContext(), ActivityHome.class);
                startActivity(otherActivity2);
                finish();
                return true;
        }
        return false;
    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_layout);

        listPlaylist=new ArrayList();

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

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.playlist_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        RecyclerViewAdapterPlaylist playlistAdapter = new RecyclerViewAdapterPlaylist(this, listPlaylist);
        recyclerView.setAdapter(playlistAdapter);

        navigationView = findViewById(R.id.activity_main_bottom_navigation);
        navigationView.setSelectedItemId(R.id.library);

        username = findViewById(R.id.username);
        username.setText(MainActivity.name);

        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.search:
                        Intent otherActivity = new Intent(getApplicationContext(), search_test.class);
                        startActivity(otherActivity);
                        finish();
                        return true;
                    case R.id.home:
                        Intent otherActivity2 = new Intent(getApplicationContext(), ActivityHome.class);
                        startActivity(otherActivity2);
                        finish();
                        return true;
                }
                return false;
            }
        });

        //########################## Init song_nav #####################################
        if (ActivityHome.serviceOn) {
            setup_song_nav();
        }
        //##############################################################################
        service=new Intent(this, ServiceTest.class);
        faire();
    }

    public void faire() {
        String m_Text = "Playlist 1";
        String search = MainActivity.token;
        String url = MusicSelection.url_base + "get_user_playlists";

        System.out.println(search);
        System.out.println(url);
        //System.out.println(sub);

        json_return = null;


        Thread t = new Thread() {
            public void run() {
                try {
                    json_return = MainActivity.sendRequest(url, search);
                } catch (RequestException e) {
                    e.printStackTrace();
                    return;
                }
                System.out.println("JSON RETURN = " + json_return);
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
            Playlist p = new Playlist("Add new playlist", "Add");
            listPlaylist.add(p);
            for (int i=0; i<ja.length(); i++){
                JSONObject j = ja.getJSONObject(i);
                String title = j.getString("playlist");
                p = new Playlist(title, "");
                listPlaylist.add(p);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setup_song_nav(){
        //###############################################
        authorName = findViewById(R.id.artistName);
        songName_Nav = findViewById(R.id.songName);
        songImage = findViewById(R.id.songImage);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        pause_play = findViewById(R.id.pause_play_image);
        seekbar = findViewById(R.id.seekbar);
        blurView = findViewById(R.id.activity_main_blur);
        //###############################################

        //######## init ############
        index = mService.index_playlist;
        Music song = mService.recommendation.get(mService.index_playlist);

        blurView.setVisibility(View.VISIBLE);
        background();
        reset_FrontEnd();
        utilise();

        blurView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                ActivityHome.service = service;
                ActivityHome.serviceOn = true;
                ActivityHome.backToListening=true;
                ActivityHome.progressBar=seekbar.getProgress();
                Intent otherActivity;
                otherActivity = new Intent(getApplicationContext(), Test.class);
                startActivity(otherActivity);
                finish();
            }
        });

        //######################### Listeners #########################################

        pause_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mService.mediaPlayer.isPlaying()){
                    seekbar.setProgress(mService.mediaPlayer.getCurrentPosition()/1000);
                    mService.mediaPlayer.pause();
                    Drawable myDrawable = getResources().getDrawable(R.drawable.play);
                    pause_play.setImageDrawable(myDrawable);
                }
                else{
                    mService.mediaPlayer.start();
                    Drawable myDrawable = getResources().getDrawable(R.drawable.pause);
                    pause_play.setImageDrawable(myDrawable);
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(mService.index_playlist==mService.recommendation.size()-1) && !mService.isInRecommendation) {
                    mService.mediaPlayer.stop();
                    mService.mediaPlayer.reset();
                    if ((mService.index_playlist==mService.recommendation.size()-2)) {
                        Thread t2 = new Thread() {
                            public void run() {
                                mService.continueRecommendation(mService.recommendation.get(mService.index_playlist).getId());
                            }
                        };
                        t2.start();
                    }
                    index++;
                    mService.index_playlist++;

                    Music song=mService.recommendation.get(mService.index_playlist);
                    String image_url=song.getImage();

                    background();
                    reset_FrontEnd();

                    String web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();
                    try {
                        mService.mediaPlayer.setDataSource(web_song_url);
                        mService.mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Thread t = new Thread() {
                        public void run() {
                            mService.continueRecommendation(mService.recommendation.get(mService.index_playlist).getId());
                        }
                    };
                    t.start();

                    mService.mediaPlayer.seekTo(0);
                    mService.mediaPlayer.start();
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index!=0) {
                    mService.mediaPlayer.stop();
                    mService.mediaPlayer.reset();
                    index--;
                    mService.index_playlist--;

                    Music song=mService.recommendation.get(mService.index_playlist);
                    String image_url=song.getImage();

                    background();
                    reset_FrontEnd();
                    String web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();
                    try {
                        mService.mediaPlayer.setDataSource(web_song_url);
                        mService.mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    System.out.println(index);
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
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (ActivityHome.serviceOn) {
                        if (mService.mediaPlayer != null) {
                            if (mService.mediaPlayer.isPlaying()) {
                                looping_trap = 0;
                                int max = mService.mediaPlayer.getDuration() / 1000;
                                seekbar.setMax(max);
                                seekbar.setProgress(mService.mediaPlayer.getCurrentPosition() / 1000);
                            }
                        } else {
                            looping_trap++;
                            if (looping_trap > 5) {
                                Intent otherActivity;
                                otherActivity = new Intent(getApplicationContext(), ActivityHome.class);
                                startActivity(otherActivity);
                                finish();
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, 1000, 1000);

    }

    //################################ FRONTEND CHANGES ################################

    public void background(){
        Thread t = new Thread() {
            public void run() {
                Drawable d = ListMusicAdapter.list_d.get(mService.index_playlist);
                d.setAlpha(60);
                LinearLayout linearLayout = findViewById(R.id.main_blur_layout);
                linearLayout.setBackgroundDrawable(d);
                blurBackground();
            }
        };
        t.start();
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

    public void reset_FrontEnd(){
        songName_Nav.setText(mService.recommendation.get(index).getName());
        authorName.setText(mService.recommendation.get(index).getAuthor());
        Drawable myDrawable = getResources().getDrawable(R.drawable.pause);
        pause_play.setImageDrawable(myDrawable);
        Glide.with(this).load(mService.recommendation.get(index).getImage()).into(songImage);
    }

    //#####################################################################

    public void utilise(){
        mService.isreco2 = new MutableLiveData<>();
        mService.isreco2.setValue(false);
        mService.isreco2.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(final Boolean newBooleanValue) {
                if (mService.isreco2.getValue()==true) {
                    index+=1;
                    Music song = mService.recommendation.get(mService.index_playlist);
                    String image_url = song.getImage();
                    background();
                    blurBackground();
                    reset_FrontEnd();
                    mService.isreco2.setValue(false);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        if (ActivityHome.serviceOn) {
            background();
            reset_FrontEnd();
            utilise();
        }
        super.onResume();
    }

    public void onBackPressed() {
        if (ActivityHome.serviceOn) {
            timer.cancel();
            ActivityHome.progressBar = seekbar.getProgress();
        }
        Intent otherActivity;
        otherActivity = new Intent(getApplicationContext(), ActivityHome.class);
        startActivity(otherActivity);
        finish();
    }
}
