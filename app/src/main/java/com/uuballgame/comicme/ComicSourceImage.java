package com.uuballgame.comicme;

import android.graphics.Bitmap;
import android.net.Uri;

public class ComicSourceImage {
    public Bitmap thumbnailBitmap;
    public Uri bitmap;

    public ComicSourceImage(Bitmap thumbnailBitmap, Uri bitmap){
        this.thumbnailBitmap = thumbnailBitmap;
        this.bitmap = bitmap;
    }
}
