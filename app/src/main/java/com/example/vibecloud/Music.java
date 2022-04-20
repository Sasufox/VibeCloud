package com.example.vibecloud;

import android.os.Parcelable;

public class Music{

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
}
