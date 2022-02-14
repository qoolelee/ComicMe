package com.uuballgame.comicme;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.cuneytayyildiz.gestureimageview.GestureImageView;

public class PicturePreprocessActivity extends AppCompatActivity {
    private ComicFilter comicFilter;
    private ComicSourceImage comicSourceImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preprocess);

        // get comic filter
        Intent from = getIntent();
        comicFilter = (ComicFilter) from.getSerializableExtra("ComicFilter");
        comicSourceImage = (ComicSourceImage) from.getSerializableExtra("ComicSourceImage");

        // back arrow but since previous activity is closed, this will not work
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.image_detailed_title);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Get the dimensions of the bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(comicSourceImage.photoPath);
        bitmap = Constants.rotateBmap(bitmap, -90);

        // enlarge 2 times the bitmap
        bitmap = Constants.enlargeBmap(bitmap, 0.7f);

        GestureImageView pictureView = findViewById(R.id.image_detailed_picture_view);
        pictureView.setImageBitmap(bitmap);

        // bitmap check and crop
        Button okButton = findViewById(R.id.image_detailed_confirm_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }


    // over ride this method to finish current activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}