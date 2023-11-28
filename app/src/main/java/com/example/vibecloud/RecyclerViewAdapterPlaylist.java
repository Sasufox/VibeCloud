package com.example.vibecloud;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

public class RecyclerViewAdapterPlaylist extends RecyclerView.Adapter<RecyclerViewAdapterPlaylist.ViewHolder>{

    private ArrayList<Playlist> listPlaylist = new ArrayList();
    Context context;
    public volatile String json_return;

    public String url;

    public RecyclerViewAdapterPlaylist(Context context, ArrayList<Playlist> listPlaylist){
        this.context=context;
        this.listPlaylist=listPlaylist;
    }

    @NonNull
    @Override
    public RecyclerViewAdapterPlaylist.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_artists_home_panel, parent, false);
        return new RecyclerViewAdapterPlaylist.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterPlaylist.ViewHolder view, int i) {
        Playlist playlist = listPlaylist.get(i);
        String name = playlist.getName();
        view.name.setText(name);
        if (listPlaylist.get(i).getImage()=="Add"){
            view.image.setImageDrawable(context.getResources().getDrawable(R.drawable.plus));
        }
    }

    @Override
    public int getItemCount() {
        return listPlaylist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView image;
        TextView name;

        private String m_Text = "";

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.author_icon);
            name = itemView.findViewById(R.id.author);

            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println(getAdapterPosition());
                    if (getAdapterPosition()==0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Title");

                        final EditText input = new EditText(context);
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        builder.setView(input);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                m_Text = input.getText().toString();
                                add_playlist(m_Text);

                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                    else{
                        load_playlist_activity(getAdapterPosition(), view);
                    }
                }
            });
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void add_playlist(String m_Text) {
        String search = MainActivity.token;
        String url = MusicSelection.url_base + "create_playlist";

        String sub = search.replace("{", "").replace("}", "");
        sub = "{" + sub + ", \"playlist\" : \"" + m_Text + "\"}";

        System.out.println(search);
        System.out.println(url);

        json_return = null;


        String finalSub = sub;
        Thread t = new Thread() {
            public void run() {
                try {
                    json_return = MainActivity.sendRequest(url, finalSub);
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

        Playlist p = new Playlist(m_Text, "");
        listPlaylist.add(p);
    }

    public void load_playlist_activity(int position, View v){
        String search = MainActivity.token;
        String url = MusicSelection.url_base + "get_playlist";
        String url2=url;
        String playlist_name=listPlaylist.get(position).getName();
        System.out.println("PlayList Name = " + playlist_name);

        String sub = search.replace("{", "").replace("}", "");
        sub = "{" + sub + ", \"playlist\" : \"" + listPlaylist.get(position).getName() + "\"}";
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

                //construct song info and pass it between activities
                if (position!=-1) {
                    ArrayList<Music> playlist_songs = new ArrayList<>();
                    try {
                        JSONArray ja = new JSONArray(r);
                        if (ja.length()!=0){
                            for (int i=0; i<ja.length(); i++){
                                //MUSIC
                                JSONObject j = ja.getJSONObject(i);
                                String title = j.getString("title");
                                String url_image = j.getString("thumbnail");
                                String a = j.getString("artists");
                                String id = j.getString("link");
                                Music m = new Music(title, a, url_image);
                                m.setId(id);
                                playlist_songs.add(m);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent MusicPlayer = new Intent(context, Playlist_Layout.class);
                    MusicPlayer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //MusicPlayer.putExtra("list", temp_song);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("playlist_songs", (Serializable)playlist_songs);
                    bundle.putString("playlist_name", playlist_name);
                    MusicPlayer.putExtra("bundle_playlist", bundle);
                    context.startActivity(MusicPlayer);
                    ((Activity)v.getContext()).finish();
                }
            }
        };
        t.start();
    }
}
