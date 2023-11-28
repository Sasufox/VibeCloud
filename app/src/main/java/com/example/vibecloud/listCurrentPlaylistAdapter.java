package com.example.vibecloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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

public class listCurrentPlaylistAdapter extends BaseAdapter{

    //fields
    private Context context;
    private List<Music> listMusic;
    private LayoutInflater inflater;
    public volatile String url = null;
    public volatile int position=-1;

    public volatile String json_return;
    public String playlist_token;
    CurrentPlaylist_Discord cpd;

    search_test search_test;
    ServiceTest mService;

    public listCurrentPlaylistAdapter(Context context, List<Music> listMusic, CurrentPlaylist_Discord cpd){
        this.context=context;
        this.listMusic=listMusic;
        this.inflater=LayoutInflater.from(context);
        this.cpd=cpd;
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

        //########### Color? ############
        if (cpd.index==i){
            view.setBackgroundColor(Color.parseColor("#708DF7"));
        }

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String id = listMusic.get(i).getId();
                position = i;
                if (!(mService.index_playlist == mService.recommendation.size() - 1) && !mService.isInRecommendation) {
                    mService.mediaPlayer.stop();
                    mService.mediaPlayer.reset();
                    if ((position == mService.recommendation.size() - 1)) {
                        Thread t2 = new Thread() {
                            public void run() {
                                mService.continueRecommendation(mService.recommendation.get(mService.index_playlist).getId());
                            }
                        };
                        t2.start();
                    }
                    cpd.index = position;
                    mService.index_playlist = position;

                    Music song = mService.recommendation.get(mService.index_playlist);
                    String image_url = song.getImage();

                    cpd.reset_FrontEnd();
                    cpd.background();

                    String web_song_url = MusicSelection.url_base + "static/youtube/" + song.getId();
                    try {
                        mService.mediaPlayer.setDataSource(web_song_url);
                        mService.mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Thread t = new Thread() {
                        public void run() {
                            mService.continueRecommendation(mService.recommendation.get(mService.index_playlist).getId());
                        }
                    };
                    t.start();

                    mService.mediaPlayer.seekTo(0);
                    mService.mediaPlayer.start();
                }
                notifyDataSetChanged();
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
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    Intent MusicPlayer = new Intent(context, TestServiceShit.class);
                                    MusicPlayer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    //MusicPlayer.putExtra("list", temp_song);
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
                        if (mService.index_playlist!=position){
                            delete_from_playlist(position, v);
                            mService.recommendation=(ArrayList<Music>)listMusic;
                            ListMusicAdapter.list_d.remove(position);
                            if (position<mService.index_playlist)
                                mService.index_playlist--;
                            cpd.index=mService.index_playlist;
                            notifyDataSetChanged();
                        }
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

}

