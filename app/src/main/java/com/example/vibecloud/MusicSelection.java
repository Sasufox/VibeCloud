package com.example.vibecloud;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MusicSelection extends AppCompatActivity {

    //fields
    public static String url_base = "https://vibecloud.yoshibox.duckdns.org/";
    List<Music> listMusic = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_selection);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        Bundle bundle = getIntent().getExtras();
        String jsonString = bundle.getString("key");
        try {
            JSONArray ja = new JSONArray(jsonString);
            for (int i=0; i<15; i++){
                JSONObject j = ja.getJSONObject(i);
                String t = j.getString("title");
                String url_image = j.getString("thumbnail");
                String a = j.getString("artists");
                String id = j.getString("link");
                Music m = new Music(t, a, url_image);
                m.setId(id);
                listMusic.add(m);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //list
        /*listMusic.add(new Music( "Playground (from the series Arcane League of Legends)", "Bea Miller", "https://lh3.googleusercontent.com/J0ilK6Uhqjx9XlBA1q1uy3SrI4Q_5U-aSZ08xOgoQdRJI6kPRqrdLVzv-LDDcfbBD94Yig9IIZezX1x8=w120-h120-l90-rj"));
        listMusic.add(new Music( "Rise (feat. Jack & Jack)", "Jonas Blue", "https://lh3.googleusercontent.com/358VKElmVdAaa_-nruzxHhz3bBE1GGtRM_EjfOeNOakV_s4u0ot2PvFxhPAzmQuD9-j66T7M8pevKaYC=w120-h120-l90-rj"));
        listMusic.add(new Music( "RISE (feat. The Word Alive)", "League of Legends", "https://lh3.googleusercontent.com/9wphzJXb9UONKjd90QpXiyoqRSxg1nd60bjUVnEMN1-f8k1OyB_sDMk40_bEPtxHn2tPbofPRvDRbIpG=w120-h120-l90-rj"));*/

        //get list view
        ListView listItems = findViewById(R.id.songs_list);
        listItems.setAdapter(new ListMusicAdapter(this, listMusic));

    }

    public void playSongChosen(View view){

    }
}