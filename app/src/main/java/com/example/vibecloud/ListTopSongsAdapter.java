package com.example.vibecloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListTopSongsAdapter extends RecyclerView.Adapter<ListTopSongsAdapter.ViewHolder> {

    private ArrayList<Music> listMusic = new ArrayList();
    Context context;

    public ListTopSongsAdapter(Context context, ArrayList<Music> listMusic) {
        this.context = context;
        this.listMusic = listMusic;
    }

    @NonNull
    @Override
    public ListTopSongsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_top_song_home, parent, false);
        return new ListTopSongsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder view, int i) {
        Music music = listMusic.get(i);
        String name = music.getName();
        String author_name = music.getAuthor();
        String image_url = music.getImage();

        Glide.with(this.context).load(image_url).into(view.image);
        view.name.setText(name);
        view.author.setText(author_name);

    }

    @Override
    public int getItemCount() {
        return listMusic.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView image;
        TextView name;
        TextView author;
        private LayoutInflater inflater;
        public volatile String url = null;
        public volatile int position=-1;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.song_icon);
            name = itemView.findViewById(R.id.song_name);
            author = itemView.findViewById(R.id.song_author);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = null;
                    for (int i = 0; i < listMusic.size(); i++) {
                        if (name.getText().equals(listMusic.get(i).getName()) && author.getText().equals(listMusic.get(i).getAuthor())) {
                            id = listMusic.get(i).getId();
                            position = i;
                        }
                    }
                    System.out.println("{\"video\": \"" + id + "\"}");

                    String finalId = id;
                    Thread t = new Thread() {
                        public void run() {
                            String u = null;
                            try {
                                u = MainActivity.sendRequest(MusicSelection.url_base+"get", "{\"video\": \"" + finalId + "\"}");
                            } catch (RequestException e) {
                                e.printStackTrace();
                                return;
                            }
                            url = MusicSelection.url_base+u;

                            //construct song info and pass it between activities
                            if (position!=-1) {
                                String[] temp_song = new String[5];
                                temp_song[0]=(listMusic.get(position).getName());
                                temp_song[1]=(listMusic.get(position).getAuthor());
                                temp_song[2]=(listMusic.get(position).getImage());
                                temp_song[3]=url;
                                temp_song[4]="musicChosen";

                                String r = null;
                                try {
                                    r = MainActivity.sendRequest(MusicSelection.url_base+"recommendation", "{\"video\": \"" + (listMusic.get(position).getId()) + "\"}");
                                } catch (RequestException e) {
                                    e.printStackTrace();
                                    return;
                                }
                                ArrayList<Music> recommendation = new ArrayList<>();
                                try {
                                    JSONArray ja = new JSONArray(r);

                                    for (int i=0; i<ja.length(); i++){
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

                                Intent MusicPlayer = new Intent(context, test_service.class);
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

            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = null;
                    for (int i = 0; i < listMusic.size(); i++) {
                        if (name.getText().equals(listMusic.get(i).getName()) && author.getText().equals(listMusic.get(i).getAuthor())) {
                            id = listMusic.get(i).getId();
                            position = i;
                        }
                    }
                    System.out.println("{\"video\": \"" + id + "\"}");

                    String finalId = id;
                    Thread t = new Thread() {
                        public void run() {
                            String u = null;
                            try {
                                u = MainActivity.sendRequest(MusicSelection.url_base + "get", "{\"video\": \"" + finalId + "\"}");
                            } catch (RequestException e) {
                                e.printStackTrace();
                                return;
                            }
                            url = MusicSelection.url_base + u;

                            //construct song info and pass it between activities
                            if (position != -1) {
                                String[] temp_song = new String[4];
                                temp_song[0] = (listMusic.get(position).getName());
                                temp_song[1] = (listMusic.get(position).getAuthor());
                                temp_song[2] = (listMusic.get(position).getImage());
                                temp_song[3] = url;

                                Intent MusicPlayer = new Intent(context, MusicListening.class);

                                MusicPlayer.putExtra("list", temp_song);
                                context.startActivity(MusicPlayer);
                                ((Activity) view.getContext()).finish();
                            }
                        }
                    };
                    t.start();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

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

        }

        @Override
        public void onClick (View view){

        }
    }
}
