package com.example.vibecloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GridAdapter extends ArrayAdapter<Music> {

    private Context context;
    private List<Music> listMusic;
    private LayoutInflater inflater;
    public volatile String url = null;
    public volatile int position=-1;

    public GridAdapter(@NonNull Context context, ArrayList<Music> listMusic) {
        super(context, 0, listMusic);
        this.listMusic=listMusic;
        this.context=context;
    }

    @NonNull
    @Override
    public View getView(int i, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listitemView = convertView;
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
        }
        Music currentMusic = getItem(i);
        String author = currentMusic.getAuthor();
        TextView musicName = listitemView.findViewById(R.id.song_name);
        ImageView musicIcon = listitemView.findViewById(R.id.song_icon);
        musicName.setText(currentMusic.getName());
        Glide.with(getContext()).load(currentMusic.getImage()).into(musicIcon);

        //listener on musics
        listitemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String id = null;
                for (int i=0; i<listMusic.size(); i++){
                    if (musicName.getText().equals(listMusic.get(i).getName()) && author.equals(listMusic.get(i).getAuthor())){
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
            }
        });


        return listitemView;
    }
}
