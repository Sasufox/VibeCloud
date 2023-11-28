package com.example.vibecloud;

import static com.example.vibecloud.ActivityHome.setWindowFlag;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
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
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Playlist_Layout extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{

    private ArrayList<Music> listMusic;
    private BottomNavigationView navigationView;

    private volatile String json_return;

    private ImageButton play_playlist;
    private ImageButton delete_playlist;

    //###################### Nav #################################
    BlurView blurView;
    TextView songName_Nav;
    TextView authorName;
    ImageView songImage;
    ImageView discord;
    ImageView previous;
    ImageView next;
    ImageView pause_play;
    public SeekBar seekbar;
    Timer timer;
    ListView listItems;

    //############################################################
    ServiceTest mService;
    static int index;
    public static boolean backToListening;
    public volatile int looping_trap;
    //############################################################

    Intent service;
    public volatile String id;
    public volatile Drawable d;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_layout);
        service=new Intent(this, ServiceTest.class);

        listMusic=new ArrayList();
        TextView playlistName = findViewById(R.id.playlist_name);

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

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("bundle_playlist");
        ArrayList<Music> playlist_songs = (ArrayList<Music>) args.getSerializable("playlist_songs");
        String playlist_name = args.getString("playlist_name");
        System.out.println("PlayList Name2 = " + playlist_name);
        playlistName.setText(playlist_name);

        for (int i=0; i<playlist_songs.size(); i++){
            Music m = playlist_songs.get(i);
            listMusic.add(m);
        }

        System.out.println("len list " + listMusic.size());
        System.out.println("len list2 " + playlist_songs.size());

        listItems = findViewById(R.id.songs_list);
        listItems.setAdapter(new listMusicPlaylistAdapter(this, listMusic, playlist_name));

        registerForContextMenu(listItems);

        //########################## Init song_nav #####################################
        if (ActivityHome.serviceOn) {
            setup_song_nav();
        }
        //##############################################################################

        navigationView = findViewById(R.id.activity_main_bottom_navigation);
        navigationView.setSelectedItemId(R.id.invisible);

        play_playlist = findViewById(R.id.play_playlist);
        delete_playlist = findViewById(R.id.delete_playlist);

        discord = findViewById(R.id.discord);

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
                    case R.id.library:
                        Intent otherActivity3 = new Intent(getApplicationContext(), Library.class);
                        startActivity(otherActivity3);
                        finish();
                        return true;
                }
                return false;
            }
        });

        play_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListMusicAdapter.list_d=new ArrayList<>();
                for (Music m : listMusic){
                    id = m.getId();
                    System.out.println("{\"video\": \"" + id + "\"}");
                    ListMusicAdapter.list_d.add(getImage(m.getImage()));

                    Thread t = new Thread() {
                        public void run() {
                            String u = null;
                            try {
                                u = MainActivity.sendRequest(MusicSelection.url_base + "get", "{\"video\": \"" + id + "\"}");
                                System.out.println("HELLO "+ u);
                            } catch (RequestException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    };
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                ActivityHome.backToListening=false;
                Intent MusicPlayer = new Intent(view.getContext(), Test.class);
                MusicPlayer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putSerializable("recommendation", (Serializable) listMusic);
                MusicPlayer.putExtra("bundle_recommendation", bundle);
                MusicPlayer.putExtra("i", 0);
                ((Activity) view.getContext()).startActivity(MusicPlayer);
                ((Activity) view.getContext()).finish();
            }
        });

        delete_playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destroy_playlist(playlist_name, view);
            }
        });

        discord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread() {
                    public void run() {
                        try {
                            requestRicardoMilos(listMusic, 0, 0);
                        } catch (JSONException | RequestException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                t.start();
            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    public void destroy_playlist(String playlist_name, View v){
        String search = MainActivity.token;
        String url = MusicSelection.url_base + "delete_playlist";
        String url2=url;

        String sub = search.replace("{", "").replace("}", "");
        sub = "{" + sub + ", \"playlist\" : \"" + playlist_name + "\"}";
        String s = sub;
        Thread t = new Thread() {
            public void run() {
                String r = null;
                try {
                    r = MainActivity.sendRequest(url2, s);
                } catch (RequestException e) {
                    e.printStackTrace();
                    return;
                }

                if (r!=null){
                    Intent MusicPlayer = new Intent(v.getContext(), Library.class);
                    MusicPlayer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(MusicPlayer);
                    ((Activity)v.getContext()).finish();
                }
            }
        };
        t.start();
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

        LinearLayout names = findViewById(R.id.names);
        //###############################################

        //######## init ############
        index = mService.index_playlist;
        Music song = mService.recommendation.get(mService.index_playlist);

        blurView.setVisibility(View.VISIBLE);
        listItems.getLayoutParams().height=550;
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

    @Override
    protected void onResume() {
        if (ActivityHome.serviceOn) {
            background();
            reset_FrontEnd();
            utilise();
        }
        super.onResume();
    }

    public Drawable getImage(String url){

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

    public void onBackPressed() {
        if (ActivityHome.serviceOn) {
            timer.cancel();
            ActivityHome.progressBar = seekbar.getProgress();
        }
        Intent otherActivity;
        otherActivity = new Intent(getApplicationContext(), Library.class);
        startActivity(otherActivity);
        finish();
    }

}
