package com.uuballgame.comicme;

import android.graphics.Bitmap;
import android.net.Uri;

public class ComicSourceImage {
    public String thumbnailBitmapBase64;
    public Uri bitmap;

    public ComicSourceImage(String thumbnailBitmapStr, Uri bitmap){
        this.thumbnailBitmapBase64 = thumbnailBitmapStr;
        this.bitmap = bitmap;
    }
}
