package com.example.vibecloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListMusicAdapter extends BaseAdapter{

    //fields
    private Context context;
    private List<Music> listMusic;
    private LayoutInflater inflater;
    public volatile String url = null;
    public volatile int position=-1;

    public ListMusicAdapter(Context context, List<Music> listMusic){
        this.context=context;
        this.listMusic=listMusic;
        this.inflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listMusic.size();
    }

    @Override
    public Music getItem(int i) {
        return listMusic.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.adapter_song_test2, null);

        //get Item info
        Music currentMusic = getItem(i);
        String name = currentMusic.getName();
        String author = currentMusic.getAuthor();
        String image = currentMusic.getImage();

        //get layout item name view
        TextView songNameView = view.findViewById(R.id.song_name);
        TextView songAuthorView = view.findViewById(R.id.song_author);
        ImageView songImageView = view.findViewById(R.id.song_icon);

        //set layout item values
        songNameView.setText(name);
        songAuthorView.setText(author);
        Glide.with(context).load(image).into(songImageView);

        System.out.println(currentMusic.getId());
        songNameView.setTag((String) currentMusic.getId());
        songAuthorView.setTag((String) currentMusic.getId());
        songImageView.setTag((String) currentMusic.getId());

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String id = null;
                for (int i=0; i<listMusic.size(); i++){
                    if (songNameView.getText().equals(listMusic.get(i).getName()) && songAuthorView.getText().equals(listMusic.get(i).getAuthor())){
                        id=listMusic.get(i).getId();
                        position=i;
                    }
                }
                System.out.println("{\"video\": \"" + id + "\"}");

                String finalId = id;
                Thread t = new Thread() {
                    public void run() {
                        String u = MainActivity.sendRequest(MusicSelection.url_base+"get", "{\"video\": \"" + finalId + "\"}");
                        url = MusicSelection.url_base+u;

                        //construct song info and pass it between activities
                        if (position!=-1) {
                            String[] temp_song = new String[5];
                            temp_song[0]=(listMusic.get(position).getName());
                            temp_song[1]=(listMusic.get(position).getAuthor());
                            temp_song[2]=(listMusic.get(position).getImage());
                            temp_song[3]=url;
                            temp_song[4]="musicChosen";

                            String r = MainActivity.sendRequest(MusicSelection.url_base+"recommendation", "{\"video\": \"" + (listMusic.get(position).getId()) + "\"}");
                            ArrayList<Music> recommendation = new ArrayList<>();
                            try {
                                JSONArray ja = new JSONArray(r);

                                for (int i=0; i<15; i++){
                                    //MUSIC
                                    JSONObject j = ja.getJSONObject(i);
                                    String title = j.getString("title");
                                    String url_image = j.getString("thumbnail");
                                    String a = j.getString("artists");
                                    String id = j.getString("link");
                                    Music m = new Music(title, a, url_image);
                                    m.setId(id);
                                    recommendation.add(m);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Intent MusicPlayer = new Intent(context, test.class);

                            //MusicPlayer.putExtra("list", temp_song);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("recommendation", (Serializable)recommendation);
                            MusicPlayer.putExtra("bundle_recommendation", bundle);
                            MusicPlayer.putExtra("i", 0);
                            context.startActivity(MusicPlayer);
                            ((Activity)view.getContext()).finish();
                        }
                    }
                };
                t.start();

                /*try {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    System.out.println(url);
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    System.out.println("WTF");
                    e.printStackTrace();
                }*/
            }
        });

        return view;
    }
}
