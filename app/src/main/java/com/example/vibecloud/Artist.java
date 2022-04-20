package com.example.vibecloud;

public class Artist {
    private String name;
    private String image;

    public Artist(String name, String image){
        this.name=name;
        this.image=image;
    }

    public String getName(){
        return this.name;
    }

    public String getImage(){
        return this.image;
    }

}
