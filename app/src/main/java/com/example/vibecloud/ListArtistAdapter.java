package com.example.vibecloud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListArtistAdapter extends BaseAdapter {
    //fields
    private Context context;
    private List<Artist> listArtist;
    private LayoutInflater inflater;
    public volatile String url = null;
    public volatile int position=-1;

    public ListArtistAdapter(Context context, List<Artist> listArtist){
        this.context=context;
        this.listArtist=listArtist;
        this.inflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listArtist.size();
    }

    @Override
    public Artist getItem(int i) {
        return listArtist.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.adapter_artists_home_panel, null);

        //get Item info
        Artist currentArtist = getItem(i);
        String name = currentArtist.getName();
        String image = currentArtist.getImage();

        //get layout item name view
        TextView authorNameView = view.findViewById(R.id.author);
        ImageView authorImageView = view.findViewById(R.id.author_icon);

        //set layout item values
        authorNameView.setText(name);
        Glide.with(context).load(image).into(authorImageView);

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
            }
        });

        return view;
    }
}
