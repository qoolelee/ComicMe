package com.uuballgame.comicme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Size;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    ImageCapture imageCapture;
    ImageAnalysis imageAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // get comic filter
        Intent from = getIntent();
        String photoPath = from.getStringExtra(MediaStore.EXTRA_OUTPUT);

        // back arrow but since previous activity is closed, this will not work
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.take_picture);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // initial cameraX
        switchCamera();

        // switch camera button
        ImageView switchImage = findViewById(R.id.camera_switch);
        switchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchCamera();
            }
        });

        // take picture button
        ImageView takePictureImage = findViewById(R.id.camera_picture);
        takePictureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture(photoPath);
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

    private void switchCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // Check for CameraProvider availability
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        // switch current
        if(lensFacing == CameraSelector.LENS_FACING_BACK){
            lensFacing = CameraSelector.LENS_FACING_FRONT;
        }
        else{
            lensFacing = CameraSelector.LENS_FACING_BACK;
        }

        // preview
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        PreviewView previewView = findViewById(R.id.camera_previewView);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.unbindAll();

        // take picture
        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(previewView.getDisplay().getRotation())
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1920, 1080))
                        .build();

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this
                , cameraSelector, imageCapture, imageAnalysis, preview);

        // set camera zoom max
        CameraControl cameraControl = camera.getCameraControl();
        cameraControl.setLinearZoom(0.0f);
    }

    private void takePicture(String path) {
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(new File(path)).build();
        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                        // transfer back
                        setResult(RESULT_OK, getIntent());
                        finish();
                    }
                    @Override
                    public void onError(ImageCaptureException error) {
                        // insert your code here.
                    }
                }
        );
    }
}