package com.uuballgame.comicme;

import java.io.Serializable;

public class ComicFilter implements Serializable {
    public boolean registered;
    public int id;
    public String name;
    public String imageUrl;
    public String gifUrl;
    public String note;

    // constructor
    public ComicFilter(boolean registered, int id, String name, String imageUrl, String gifUrl, String note){
        this.registered = registered;
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.gifUrl = gifUrl;
        this.note = note;
    }

}
