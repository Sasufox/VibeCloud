package com.example.vibecloud;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ListMusicAdapter extends BaseAdapter{

    //fields
    private Context context;
    private List<Music> listMusic;
    private LayoutInflater inflater;
    public volatile String url = null;

    public volatile String json_return = null;
    public volatile int position=-1;
    public volatile Drawable d;

    public static volatile ArrayList<Drawable> list_d=new ArrayList<>();;

    search_test search_test;

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
                                list_d=new ArrayList<>();

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
                                    list_d.add(getImage(url_image));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            Intent MusicPlayer = new Intent(context, Test.class);
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
        inflater.inflate(R.menu.song_choice, popup.getMenu());
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.setGravity(Gravity.TOP);
        }*/
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
                    case R.id.action_add_playlist:
                        System.out.println("Add to Playlist");
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());
                        alertDialog.setTitle("Add to playlist ...");
                        String[] items = get_all_playlists();
                        boolean[] checkedItems = new boolean[items.length];
                        for (int i=0; i<items.length; i++){
                            checkedItems[i]=false;
                        }
                        alertDialog.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                switch (which) {
                                    case 0:
                                        if(isChecked) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                            builder.setTitle("Name of playlist ...");

                                            final EditText input = new EditText(context);
                                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                                            builder.setView(input);

                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    String m_Text = input.getText().toString();
                                                    add_playlist(m_Text);
                                                    add_to_playlist(position, v, items, m_Text);

                                                }
                                            });
                                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            });
                                            builder.show();
                                            dialog.cancel();
                                            break;
                                        }
                                    default:
                                        if(isChecked) {
                                            add_to_playlist(position, v, items, which);
                                            dialog.cancel();
                                        }
                                }
                            }
                        });
                        AlertDialog alert = alertDialog.create();
                        alert.setCanceledOnTouchOutside(false);
                        alert.show();
                    case R.id.action_back:
                        System.out.println("BACK");
                        return false;
                    default:
                        return false;
                }
            }
        });
    }

    public String[] get_all_playlists(){
        String[] res = null;
        String search = MainActivity.token;
        String url = MusicSelection.url_base + "get_user_playlists";

        System.out.println(search);
        System.out.println(url);
        //System.out.println(sub);

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

        if(json_return != null) {
            try {
                JSONArray ja = new JSONArray(json_return);
                res=new String[ja.length()+1];
                res[0]="Add new playlist";
                if (ja.length()!=0) {
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject j = ja.getJSONObject(i);
                        String title = j.getString("playlist");
                        res[i + 1] = title;
                    }
                }
                return res;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return res;
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
    }

    public void add_to_playlist(int position, View v, String[] res, int which){
        String search = MainActivity.token;
        String url = MusicSelection.url_base + "add_to_playlist";
        String url2=url;
        String playlist_name=res[which];

        String sub = search.replace("{", "").replace("}", "");
        sub = "{" + sub + ", \"playlist\" : \"" + res[which] + "\", \"position\" : -1, \"link\" : \"" + listMusic.get(position).getId() + "\"}";
        String s = sub;
        System.out.println(s);
        Thread t = new Thread() {
            public void run() {
                String r = null;
                try {
                    r = MainActivity.sendRequest(url2, s);
                } catch (RequestException e) {
                    e.printStackTrace();
                    return;
                }
            }
        };
        t.start();
    }

    public void add_to_playlist(int position, View v, String[] res, String playlist_name){
        String search = MainActivity.token;
        String url = MusicSelection.url_base + "add_to_playlist";
        String url2=url;

        String sub = search.replace("{", "").replace("}", "");
        sub = "{" + sub + ", \"playlist\" : \"" + playlist_name + "\", \"position\" : -1, \"link\" : \"" + listMusic.get(position).getId() + "\"}";
        String s = sub;
        System.out.println(s);
        Thread t = new Thread() {
            public void run() {
                String r = null;
                try {
                    r = MainActivity.sendRequest(url2, s);
                } catch (RequestException e) {
                    e.printStackTrace();
                    return;
                }
            }
        };
        t.start();
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
