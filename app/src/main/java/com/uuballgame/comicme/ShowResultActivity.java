package com.uuballgame.comicme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShowResultActivity extends AppCompatActivity {
    private ComicFilter comicFilter;
    private ComicSourceImage comicSourceImage;
    private Bitmap waterMarkBitmap = null;
    private Bitmap result_bitmap, org_bitmap;
    private ImageButton buttonShare;
    private ImageButton buttonSave;
    private ImageButton buttonToggle;
    protected Context context;

    protected static boolean ORIGINAL_PIC = true;
    public static String IMAGES_FOLDER_NAME = "ComicMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);

        context = this;

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

        // button save
        buttonSave = findViewById(R.id.show_detail_button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToLocalGallery();
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

    private void saveToLocalGallery() {
        // Save Alert dialog
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(R.string.show_result_save_to_disk);
        alertDialog.setPositiveButton(R.string.show_result_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Create an image file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "ComicMe_" + timeStamp;

                try {
                    saveImage(result_bitmap, imageFileName);

                    Toast toast = Toast.makeText(context, R.string.show_result_pic_saved, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER,0,0);
                    //toast.setGravity(Gravity.BOTTOM|Gravity.RIGHT,0,0);   //靠右下用法
                    toast.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                dialog.dismiss();
            }
        });
        alertDialog.setNegativeButton(R.string.show_result_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }

    private void muskBitmap() {
        Bitmap bitmap = addOriginal(ORIGINAL_PIC, org_bitmap);
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

    private Bitmap addOriginal(boolean originalPic, Bitmap src) {
        if(!originalPic)return src;

        float xShift = 60.0f;
        float yShift = 25.0f;
        float circleRadius = 60.0f;
        float circleMargin = 4.0f;

        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        canvas.drawCircle((xShift+(xShift+2*circleMargin+2*circleRadius))/2.0f, (yShift+(yShift+2*circleMargin+2*circleRadius))/2.0f, circleMargin+circleRadius, paint);

        Bitmap bitmapOrg = Constants.scaleBitmap(PictureCollectionActivity.LASTBitmap, (int)circleRadius*2, (int)circleRadius*2);
        bitmapOrg = Constants.getCircularCroppedBitmap(bitmapOrg);
        canvas.drawBitmap(bitmapOrg, xShift+circleMargin, yShift+circleMargin, null);

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

    private void saveImage(Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        OutputStream fos;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + IMAGES_FOLDER_NAME);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + IMAGES_FOLDER_NAME;

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }

            File image = new File(imagesDir, name + ".png");
            fos = new FileOutputStream(image);

        }

        saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.flush();
        fos.close();
    }

}