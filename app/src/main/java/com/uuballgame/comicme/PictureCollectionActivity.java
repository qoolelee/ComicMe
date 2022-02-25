package com.uuballgame.comicme;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PictureCollectionActivity extends AppCompatActivity {
    public static final int REQUEST_IMAGE_CAPTURE = 1888;
    public static final int REQUEST_IMAGE_PROCESS = 1889;
    public static final int SHOW_RESULT_ACTIVITY = 1890;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    public static final int NUMBER_OF_COLUMNS = 4;

    private ComicFilter comicFilter;
    String currentPhotoPath;
    List<ComicSourceImage> comicSourceImages;
    PictureCollectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_collection);

        // get comic filter
        Intent from = getIntent();
        comicFilter = (ComicFilter) from.getSerializableExtra("ComicFilter");

        // back arrow but since previous activity is closed, this will not work
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.picture_album);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // read in data rom preference
        comicSourceImages = getSavedComicSourceImages();

        // find recycler view in fragment
        RecyclerView recyclerView = findViewById(R.id.picture_collection_recyclerview);

        // prepare data for adapter
        //int numberOfColumns = Constants.calculateNoOfColumns(this, 80);
        recyclerView.setLayoutManager(new GridLayoutManager(this, NUMBER_OF_COLUMNS));

        // Create the View holder adapter
        adapter = new PictureCollectionAdapter(this, comicSourceImages, comicFilter);
        // attach adapter to recycler view
        recyclerView.setAdapter(adapter);


        // camera button
        Button buttonCamera = findViewById(R.id.picture_collection_button_camera);
        buttonCamera.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v)
            {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    //dispatchTakePictureIntent();
                    startCustomCameraIntent();
                }
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

        // unhidden musk
        findViewById(R.id.picture_collection_top_view).setVisibility(View.GONE);
        findViewById(R.id.picture_collection_progressbar).setVisibility(View.GONE);
        findViewById(R.id.picture_collection_button_camera).setClickable(true);
        findViewById(R.id.picture_collection_button_gallery).setClickable(true);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("ERROR!", ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.uuballgame.comicme",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void startCustomCameraIntent() {
        Intent cameraIntent = new Intent(this, CameraActivity.class);
        // Ensure that there's a camera activity to handle the intent
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("ERROR!", ex.toString());
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.uuballgame.comicme",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoPath);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // read back the image file and scale it to thumbnail
            Bitmap scaledBitmap = Constants.getScaledBitmap(currentPhotoPath, 100, 100);
            ComicSourceImage comicSourceImage = new ComicSourceImage(Constants.convert(scaledBitmap), currentPhotoPath);

            // adapter changed
            comicSourceImages.add(comicSourceImage);
            adapter.comicSourceImages = comicSourceImages;
            adapter.notifyDataSetChanged();

            // to Json string
            String str = new Gson().toJson(comicSourceImages);
            // save to preference
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.comic_me_app), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("comic_source_images", str);
            editor.apply();

            // start process picture activity
            Intent imageDetailedActivityIntent = new Intent(this, PicturePreprocessActivity.class);
            imageDetailedActivityIntent.putExtra("ComicSourceImage", comicSourceImage);
            imageDetailedActivityIntent.putExtra("ComicFilter", comicFilter);
            startActivityForResult(imageDetailedActivityIntent, PictureCollectionActivity.REQUEST_IMAGE_PROCESS);
        }
        else if(requestCode == REQUEST_IMAGE_PROCESS && resultCode == Activity.RESULT_OK){
            // start result picture activity
            String fileName = data.getStringExtra("fileName");
            Intent showResultActivityIntent = new Intent(this, ShowResultActivity.class);
            showResultActivityIntent.putExtra("ComicFilter", comicFilter);
            showResultActivityIntent.putExtra("fileName", fileName);
            startActivityForResult(showResultActivityIntent, PictureCollectionActivity.SHOW_RESULT_ACTIVITY);
        }
    }

    private List<ComicSourceImage> getSavedComicSourceImages() {
        List<ComicSourceImage> CSIList = new ArrayList<>();

        // read back str from shared preferences
        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.comic_me_app), Context.MODE_PRIVATE);
        String str = sharedPref.getString("comic_source_images", null);

        // decode Json
        if(str != null){
            Gson gson = new Gson();
            Type typeListOfComicFilter = new TypeToken<List<ComicSourceImage>>(){}.getType();
            CSIList = gson.fromJson(str, typeListOfComicFilter);
        }

        return CSIList;
    }
}