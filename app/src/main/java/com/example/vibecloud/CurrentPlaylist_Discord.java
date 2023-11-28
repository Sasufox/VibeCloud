package com.example.vibecloud;

import static com.example.vibecloud.ActivityHome.setWindowFlag;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CurrentPlaylist_Discord extends AppCompatActivity {
    ServiceTest mService;
    static int index;

    // VIEWS ###############
    BlurView blurView;
    TextView songName;
    TextView songName_Nav;
    TextView authorName;
    ImageView songImage;
    ImageView discord;
    ImageView previous;
    ImageView next;
    ImageView pause_play;
    ImageView mainImage;
    public SeekBar seekbar;
    Timer timer;

    public static boolean backToListening;

    public volatile int looping_trap;

    public ArrayList<Music> listMusic;

    listCurrentPlaylistAdapter adapter;

    Intent service;
    //######################


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.currentplaylistdiscord);

        listMusic = mService.recommendation;
        index=mService.index_playlist;

        //########################### id --> Views ####################################
        songName = findViewById(R.id.playlist_name);
        discord = findViewById(R.id.discord);
        authorName = findViewById(R.id.artistName);
        songName_Nav = findViewById(R.id.songName);
        songImage = findViewById(R.id.songImage);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        pause_play = findViewById(R.id.pause_play_image);
        seekbar = findViewById(R.id.seekbar);
        blurView = findViewById(R.id.activity_main_bottom_navigation);
        mainImage = findViewById(R.id.main_Image);

        service=new Intent(this, ServiceTest.class);
        // ############################################################################

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

        songName.setText(listMusic.get(index).getName());

        ListView listItems = findViewById(R.id.songs_list);
        adapter = new listCurrentPlaylistAdapter(this, listMusic, this);
        listItems.setAdapter(adapter);
        listItems.getAdapter().getView(index, null, listItems).setBackgroundColor(Color.BLUE);

        registerForContextMenu(listItems);

        //######## init ############
        index = mService.index_playlist;
        Music song = mService.recommendation.get(mService.index_playlist);
        background();
        reset_FrontEnd();
        utilise();

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
                adapter.notifyDataSetChanged();
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
                adapter.notifyDataSetChanged();
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
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, 1000, 1000);

        discord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread() {
                    public void run() {
                        try {
                            requestRicardoMilos(listMusic, mService.mediaPlayer.getDuration(), index);
                        } catch (JSONException | RequestException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                t.start();
            }
        });

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

        //#############################################################################



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
        index=mService.index_playlist;
        songName.setText(mService.recommendation.get(mService.index_playlist).getName());
        songName_Nav.setText(mService.recommendation.get(mService.index_playlist).getName());
        authorName.setText(mService.recommendation.get(mService.index_playlist).getAuthor());
        Glide.with(this).load(mService.recommendation.get(mService.index_playlist).getImage()).into(mainImage);
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
                    if ((listMusic.size() != mService.recommendation.size())) {
                        listMusic = mService.recommendation;
                    }
                    index+=1;
                    Music song = mService.recommendation.get(mService.index_playlist);
                    String image_url = song.getImage();
                    background();
                    blurBackground();
                    reset_FrontEnd();
                    mService.isreco2.setValue(false);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    public static void requestRicardoMilos(ArrayList<Music> listMusic, int timer, int index) throws JSONException, RequestException {
        final MediaType JSON = MediaType.parse("application/json");

        OkHttpClient client = new OkHttpClient();
        JSONObject jo = new JSONObject();
        jo.put("list", musicToJSON(listMusic));
        jo.put("index", index);
        jo.put("timer", timer);

        RequestBody body = RequestBody.create(jo.toString(), JSON);
        String url = "http://144.24.203.145:6969";
        Request request = new Request.Builder().url(url).header("Content-Type", "application/json").post(body).build();

        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return;
    }

    public static JSONArray musicToJSON(ArrayList<Music> listMusic){
        JSONArray ja = new JSONArray();
        for (Music m : listMusic){
            JSONObject jo = new JSONObject();
            try {
                jo.put("id", m.getId());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            ja.put(jo);
        }
        return ja;
    }

    //###################################################################

    public void onBackPressed() {
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

    @Override
    protected void onResume() {
        System.out.println("Resume HERE");
        listMusic = mService.recommendation;
        index = mService.index_playlist;
        Music song = mService.recommendation.get(mService.index_playlist);
        String image_url = song.getImage();
        background();
        reset_FrontEnd();
        adapter.notifyDataSetChanged();
        super.onResume();
    }
}
