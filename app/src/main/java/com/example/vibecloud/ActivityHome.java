package com.example.vibecloud;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class ActivityHome extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

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
    ScrollView scrollView;

    //############################################################
    ServiceTest mService;
    static int index;
    public volatile int looping_trap;
    //############################################################

    Button search_song_btn;
    ImageView heart;
    public volatile String json_return;
    private MediaPlayer mediaPlayer;
    ArrayList<Music> listMusic = new ArrayList();
    ArrayList<Music> listTopMusic = new ArrayList();
    ArrayList<Artist> listArtist = new ArrayList();
    BottomNavigationView navigationView;
    static Intent service;
    static boolean serviceOn=false;
    static boolean backToListening=false;
    static int progressBar=0;

    static ArrayList<Music> tmp_listMusic;
    static ArrayList<Music> tmp_listTopMusic;
    static ArrayList<Artist> tmp_listArtist;

    // Handle server request exceptions
    private volatile boolean requestException = false;

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

        faire();

        navigationView = findViewById(R.id.activity_main_bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (timer!=null)
                    timer.cancel();
                switch (item.getItemId()) {
                    case R.id.search:
                        Intent otherActivity = new Intent(getApplicationContext(), search_test.class);
                        startActivity(otherActivity);
                        finish();
                        return true;
                    case R.id.library:
                        Intent otherActivity2 = new Intent(getApplicationContext(), Library.class);
                        startActivity(otherActivity2);
                        finish();
                        return true;
                }
                return false;
            }
        });

        int nightModeFlags = this.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        System.out.println("Darkmode : " + nightModeFlags);

        //ListSongs recommandés
        ExpandableHeightGridView mAppsGrid = (ExpandableHeightGridView) findViewById(R.id.grid_pop_songs);
        mAppsGrid.setExpanded(true);
        GridAdapter adapter = new GridAdapter(this, listMusic);
        mAppsGrid.setAdapter(adapter);
        mAppsGrid.setScrollbarFadingEnabled(true);

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
                Intent otherActivity = new Intent(getApplicationContext(), MainActivity.class);
                SharedPreferences sp=getSharedPreferences("Login", MODE_PRIVATE);
                SharedPreferences.Editor Ed=sp.edit();
                Ed.putString("username", "");
                Ed.putString("password", "");
                Ed.commit();
                startActivity(otherActivity);
                finish();
            }
        });

        //########################## Init song_nav #####################################
        if (serviceOn) {
            setup_song_nav();
        }
        //##############################################################################
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

    public void faire() {
        if (tmp_listMusic==null || tmp_listMusic.size()==0) {
            String search = "{\"country\": \"" + "JP" + "\"}";
            String url = MusicSelection.url_base + "homepage";
            System.out.println(search);
            System.out.println(url);
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

            if (json_return == null)
                return;

            try {
                JSONObject ja = new JSONObject(json_return);
                JSONArray ja_a = ja.getJSONArray("artists");
                JSONArray ja_m = ja.getJSONArray("musics");
                for (int i = 0; i < 6; i++) {
                    //MUSIC
                    JSONObject j = ja_m.getJSONObject(i);
                    String title = j.getString("title");
                    String url_image = j.getString("thumbnail");
                    String a = j.getString("artists");
                    String id = j.getString("link");
                    Music m = new Music(title, a, url_image);
                    m.setId(id);
                    listTopMusic.add(m);
                    listMusic.add(m);

                    //ARTISTS
                    j = ja_a.getJSONObject(i);
                    String name = j.getString("artist");
                    String url_artist = j.getString("picture");
                    Artist artist = new Artist(name, url_artist);
                    listArtist.add(artist);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            tmp_listMusic = listMusic;
            tmp_listTopMusic = listTopMusic;
            tmp_listArtist = listArtist;
        }
        else{
            System.out.println("I like cookies");
            listMusic=tmp_listMusic;
            listTopMusic=tmp_listTopMusic;
            listArtist=tmp_listArtist;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent otherActivity = new Intent(getApplicationContext(), search_test.class);
                startActivity(otherActivity);
                finish();
                return true;
        }
        return false;
    }


    //############################ NAV NAV #############################################
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
        scrollView = findViewById(R.id.scrollview);
        //###############################################

        //######## init ############
        index = mService.index_playlist;
        Music song = mService.recommendation.get(mService.index_playlist);

        blurView.setVisibility(View.VISIBLE);
        scrollView.getLayoutParams().height=1700;
        background();
        reset_FrontEnd();
        utilise();

        blurView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.cancel();
                serviceOn = true;
                backToListening=true;
                progressBar=seekbar.getProgress();
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
                    if (serviceOn) {
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
        songName_Nav.setText(mService.recommendation.get(mService.index_playlist).getName());
        authorName.setText(mService.recommendation.get(mService.index_playlist).getAuthor());
        Drawable myDrawable = getResources().getDrawable(R.drawable.pause);
        pause_play.setImageDrawable(myDrawable);
        Glide.with(this).load(mService.recommendation.get(mService.index_playlist).getImage()).into(songImage);
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
        if (serviceOn) {
            background();
            reset_FrontEnd();
            utilise();
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onPause();
    }
}