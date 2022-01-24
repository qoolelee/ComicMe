package com.uuballgame.comicme;

public class ComicFilter {
    public boolean registered;
    public int id;
    public String name;
    public String imageUrl;
    public String note;

    // constructor
    public ComicFilter(boolean registered, int id, String name, String imageUrl, String note){
        this.registered = registered;
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.note = note;
    }

}
