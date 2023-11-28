package com.example.vibecloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class listMusicPlaylistAdapter extends BaseAdapter{

    //fields
    private Context context;
    private List<Music> listMusic;
    private LayoutInflater inflater;
    public volatile String url = null;
    public volatile int position=-1;
    public volatile String json_return;
    public String playlist_token;
    search_test search_test;
    public volatile Drawable d;


    public listMusicPlaylistAdapter(Context context, List<Music> listMusic, String playlist_token){
        this.context=context;
        this.listMusic=listMusic;
        this.playlist_token=playlist_token;
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
        view = inflater.inflate(R.layout.notification_big, null);

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
                String id = listMusic.get(i).getId();
                position=i;
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
                                ListMusicAdapter.list_d=new ArrayList<>();

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
                                    ListMusicAdapter.list_d.add(getImage(url_image));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            ActivityHome.backToListening=false;
                            Intent MusicPlayer = new Intent(context, TestServiceShit.class);
                            MusicPlayer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int[] location = new int[2];
                String id = listMusic.get(i).getId();
                position=i;
                showPopup(view, id, position);
                return false;
            }
        });
        return view;
    }

    public void showPopup(View v, String id, int position) {
        PopupMenu popup = new PopupMenu(this.context, v, Gravity.RIGHT);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.playlist_elem_choice, popup.getMenu());

        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_song:
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
                                        ListMusicAdapter.list_d=new ArrayList<>();

                                        for (int i=0; i<1; i++){
                                            //MUSIC
                                            JSONObject j = ja.getJSONObject(i);
                                            String title = j.getString("title");
                                            String url_image = j.getString("thumbnail");
                                            String a = j.getString("artists");
                                            String id = j.getString("link");
                                            Music m = new Music(title, a, url_image);
                                            m.setId(id);
                                            recommendation.add(m);
                                            ListMusicAdapter.list_d.add(getImage(url_image));
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    ActivityHome.backToListening=false;
                                    Intent MusicPlayer = new Intent(context, Test.class);
                                    MusicPlayer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("recommendation", (Serializable)recommendation);
                                    MusicPlayer.putExtra("bundle_recommendation", bundle);
                                    MusicPlayer.putExtra("i", 0);
                                    context.startActivity(MusicPlayer);
                                    ((Activity)v.getContext()).finish();
                                }
                            }
                        };
                        t.start();
                        return true;
                    case R.id.discord_stream:
                        ArrayList<Music> tmp = new ArrayList<>();
                        tmp.add(listMusic.get(position));
                        Thread t2 = new Thread() {
                            public void run() {
                                try {
                                    Playlist_Layout.requestRicardoMilos(tmp, 0, 0);
                                } catch (JSONException | RequestException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        };
                        t2.start();
                        return false;
                    case R.id.action_delete_playlist:
                        delete_from_playlist(position, v);
                        return false;
                    case R.id.action_back:
                        System.out.println("BACK");
                        return false;
                    default:
                        return false;
                }
            }
        });
    }

    public void delete_from_playlist(int position, View v){
        String search = MainActivity.token;
        String url = MusicSelection.url_base + "delete_from_playlist";
        String url2=url;

        String sub = search.replace("{", "").replace("}", "");
        sub = "{" + sub + ", \"playlist\" : \"" + playlist_token + "\", \"position\" : " + position + "}";
        String s = sub;
        System.out.println(url + s);
        Thread t = new Thread() {
            public void run() {
                String r = null;
                try {
                    r = MainActivity.sendRequest(url2, s);
                    listMusic.remove(position);
                } catch (RequestException e) {
                    e.printStackTrace();
                    System.out.println("Il s'est passé de la merde frerot, ça a pas supprimé");
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
        notifyDataSetChanged();
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
}
