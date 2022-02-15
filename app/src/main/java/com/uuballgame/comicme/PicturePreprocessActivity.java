package com.uuballgame.comicme;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
    private Bitmap originalBitmap;

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
        originalBitmap = BitmapFactory.decodeFile(comicSourceImage.photoPath);
        originalBitmap = Constants.rotateBmap(originalBitmap, -90);

        // enlarge 2 times the bitmap
        float bScale = 0.5f;
        originalBitmap = Constants.enlargeBmap(originalBitmap, bScale);

        GestureImageView pictureView = findViewById(R.id.image_detailed_picture_view);
        pictureView.setImageBitmap(originalBitmap);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //int height = displayMetrics.heightPixels;
        int vWidth = displayMetrics.widthPixels;
        //float vWidth = pictureView.getWidth();
        float iWidth = pictureView.getImageWidth();
        float sScale = (vWidth / bScale) / iWidth;
        pictureView.setStartingScale(sScale);

        // bitmap check and crop
        Button okButton = findViewById(R.id.image_detailed_confirm_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap cropedBitmap = getFramedBitmap(pictureView);

                String checkResult = checkIfBitmapValid(cropedBitmap);
                /*
                if(!checkResult.equals("ok")){
                    switch (checkResult){
                        case "no_face":
                            break;
                        case "face_too_small":
                            break;
                        case "face_too_large":
                            break;
                        default: // "face_direction_wrong

                    }
                }
                else{
                    // upload to server ...
                }
                 */
            }
        });

    }

    private String checkIfBitmapValid(Bitmap cropedBitmap) {
        return null;
    }

    private Bitmap getFramedBitmap(GestureImageView pictureView) {
        float imageWidth = pictureView.getImageWidth();
        float imageHeight = pictureView.getImageHeight();
        float viewWidth = pictureView.getWidth();
        float viewHeight = pictureView.getHeight();
        float imageX = pictureView.getImageX();
        float imageY = pictureView.getImageY();
        float scale = pictureView.getScale();

        float width = 300.0f / 380.0f * imageWidth * scale;
        float height = width;
        float centerX = imageX * imageWidth / viewWidth;
        float centerY = imageY * imageHeight / viewHeight;

        int left = (int)(centerX - width / 2.0f);
        int top = (int)(centerY - height / 2.0f);

        Bitmap resultBitmap = Bitmap.createBitmap(originalBitmap, left, top, (int)width, (int)height);

        return resultBitmap;
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