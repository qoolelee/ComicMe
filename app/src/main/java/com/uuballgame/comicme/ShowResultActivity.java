package com.uuballgame.comicme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

public class ShowResultActivity extends AppCompatActivity {
    private ComicFilter comicFilter;
    private ComicSourceImage comicSourceImage;
    private Bitmap waterMarkBitmap = null;
    private Bitmap result_bitmap, org_bitmap;
    private ImageButton buttonShare;
    private ImageButton buttonToggle;

    protected static boolean ORIGINAL_PIC = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);

        // get comic filter
        Intent from = getIntent();
        String url = from.getStringExtra("url");
        comicFilter = (ComicFilter) from.getSerializableExtra("ComicFilter");
        comicSourceImage = (ComicSourceImage) from.getSerializableExtra("ComicSourceImage");

        // back arrow but since previous activity is closed, this will not work
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(comicFilter.name);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // for avoiding share error
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }

        // show bitmap in imageview
        ProgressBar progressBar = findViewById(R.id.show_detail_progressbar);
        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        progressBar.setVisibility(View.GONE);

                        org_bitmap = resource;
                        muskBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        progressBar.setVisibility(View.GONE);
                    }
                });

        // button share
        buttonShare = findViewById(R.id.show_detail_button_share);
        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareCall(result_bitmap);
            }
        });

        // button Toggle
        buttonToggle = findViewById(R.id.show_detail_button_toggle_original);
        buttonToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ORIGINAL_PIC = !ORIGINAL_PIC;
                muskBitmap();
                if(ORIGINAL_PIC){
                    buttonToggle.setBackground(getResources().getDrawable(R.drawable.toggle_switch_on));
                }
                else{
                    buttonToggle.setBackground(getResources().getDrawable(R.drawable.toggle_switch_off));
                }
            }
        });

    }

    private void muskBitmap() {
        Bitmap bitmap = addOriginal(ORIGINAL_PIC, comicSourceImage, org_bitmap);
        if(!Constants.PRO_USER){
            waterMarked(bitmap);
            return;
        }
        else{
            setView(bitmap);
        }
    }


    private void shareCall(Bitmap bitmap) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        Uri uriToImage = Constants.bitmapToUri(this, bitmap);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
        shareIntent.setType("image/png");
        startActivity(Intent.createChooser(shareIntent, null));
    }

    private Bitmap addOriginal(boolean originalPic, ComicSourceImage comicSourceImage, Bitmap src) {
        if(!originalPic)return src;

        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Bitmap bitmapOrg = Constants.convert(comicSourceImage.thumbnailBitmapBase64);
        bitmapOrg = Constants.rotateBitmap(bitmapOrg, -90);
        bitmapOrg = Constants.scaleBitmap(bitmapOrg, 1.2f);
        bitmapOrg = Constants.getCircularCroppedBitmap(bitmapOrg);
        canvas.drawBitmap(bitmapOrg, 60, 10, null);

        return result;
    }

    public void waterMarked(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);


        Glide.with(this)
                .asBitmap()
                .load(R.drawable.watermark_musk)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        //Save the bitmap to your global bitmap
                        waterMarkBitmap = resource;
                        canvas.drawBitmap(waterMarkBitmap, 0, 0, null);
                        setView(result);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private void setView(Bitmap bitmap) {
        ImageView imageView = findViewById(R.id.show_detail_imageview_result);
        imageView.setImageBitmap(bitmap);

        result_bitmap = bitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_detail_ac_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // over ride this method to finish current activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.show_detail_action_home:
                PictureCollectionActivity.Back_Home = true;
                finish();
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

}