package com.example.vibecloud;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Music implements Serializable{

    //fields
    private String name;
    private String author;
    private String image;
    private String id;

    //constructor
    public Music(String name, String author, String image){
        this.name=name;
        this.author=author;
        this.image=image;
    }

    public String getName(){
        return this.name;
    }

    public String getAuthor(){
        return this.author;
    }

    public String getImage(){
        return this.image;
    }

    public void setId(String id){
        this.id=id;
    }

    public String getId(){
        return this.id;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Music) {
            return this.id.equals(((Music) o).id);
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return getName();
    }
}
