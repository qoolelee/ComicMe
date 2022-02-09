package com.uuballgame.comicme;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;

public class ComicSourceImage implements Serializable {
    public String thumbnailBitmapBase64;
    public String photoPath;

    public ComicSourceImage(String thumbnailBitmapStr, String photoPath){
        this.thumbnailBitmapBase64 = thumbnailBitmapStr;
        this.photoPath = photoPath;
    }
}
