package com.uuballgame.comicme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.cuneytayyildiz.gestureimageview.GestureImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.List;

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
        float sScale = vWidth / (iWidth * bScale);
        pictureView.setStartingScale(sScale);

        // bitmap check and crop
        Button okButton = findViewById(R.id.image_detailed_confirm_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap cropedBitmap = getFramedBitmap(pictureView, bScale);

                uploadPicture(cropedBitmap);
            }
        });

    }

    public static final int MIN_BITMAP_WIDTH = 200; // pixels
    public static final float MIN_FACE_RATIO = 0.2f;
    public static final float MAX_FACE_ROTY = 45.0f;
    public static final float MAX_FACE_ROTX = 35.0f;
    public static final int ERROR001 = 1;
    public static final int ERROR002 = 2;
    public static final int ERROR003 = 3;
    public static final int ERROR004 = 4;
    public static final int ERROR005 = 5;
    public static final int ERROR006 = 6;
    public static final int ERROR007 = 7;

    private void uploadPicture(Bitmap bitmap) {
        // 1. check image size, less than 200x200 pixels disqualified
        if(bitmap.getWidth()< MIN_BITMAP_WIDTH){
            setAlertText(ERROR001);
            return;
        }

        // ML KIT check 2. if picture had no face 3. if picture had two or more faces 4. head width ratio in picture
        // 5. face direction 6. head full face landmark 7. internal error
        // ML KIT option setup
        FaceDetectorOptions highAccuracyOpts = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build();

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        FaceDetector detector = FaceDetection.getClient(highAccuracyOpts);

        Task<List<Face>> result = detector.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        // Task completed successfully
                        // 2. picture had no face
                        if(faces.size()<=0){
                            setAlertText(ERROR002);
                            return;
                        }

                        // 3. if picture had two or more faces
                        if(faces.size() >= 2){
                            setAlertText(ERROR003);
                            return;
                        }

                        // 4. head width ratio in picture
                        Face face = faces.get(0);
                        Rect rect = face.getBoundingBox();
                        float faceRatio = (float) rect.width() / (float) image.getWidth();
                        if(faceRatio < MIN_FACE_RATIO){
                            setAlertText(ERROR004);
                            return;
                        }

                        // 5. face direction
                        float rotY = face.getHeadEulerAngleY();
                        float rotX = face.getHeadEulerAngleX();
                        if(rotY >= MAX_FACE_ROTY || rotY <= -MAX_FACE_ROTY
                                || rotX >= MAX_FACE_ROTX || rotX <= -MAX_FACE_ROTX){
                            setAlertText(ERROR005);
                            return;
                        }

                        // 6. head full face landmark elbow and chink
                        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
                        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
                        FaceLandmark mouse = face.getLandmark(FaceLandmark.MOUTH_BOTTOM);

                        if(mouse == null || (leftEye == null && rightEye == null)){
                            setAlertText(ERROR006);
                            return;
                        }

                        // if all pass show progress bar, and start to upload to server
                        
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        setAlertText(ERROR007);
                    }
                });

    }

    private void setAlertText(int error) {
        int alertMessage = 0;
        switch (error){
            case ERROR001:
                alertMessage = R.string.image_detailed_error01;
                break;
            case ERROR002:
                alertMessage = R.string.image_detailed_error02;
                break;
            case ERROR003:
                alertMessage = R.string.image_detailed_error03;
                break;
            case ERROR004:
                alertMessage = R.string.image_detailed_error04;
                break;
            case ERROR005:
                alertMessage = R.string.image_detailed_error05;
                break;
            case ERROR006:
                alertMessage = R.string.image_detailed_error06;
                break;
            case ERROR007:
                alertMessage = R.string.image_detailed_error07;
                break;
            default:
                break;
        }

        if(alertMessage == 0 )return;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.image_detailed_error);
        alertDialog.setMessage(alertMessage);
        alertDialog.setPositiveButton(R.string.image_detailed_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private float orgX,orgY;
    private Bitmap getFramedBitmap(GestureImageView pictureView, float bScale) {
        float viewWidth = pictureView.getWidth();
        float viewHeight = pictureView.getHeight();
        float imageX = pictureView.getImageX();
        float imageY = pictureView.getImageY();
        if(orgX == 0.0f){
            orgX = imageX;
            orgY = imageY;
        }
        float imageWidth = pictureView.getImageWidth();
        float imageHeight = pictureView.getImageHeight();
        float sScale = pictureView.getScale();

        //sScale = vWidth / (iWidth * bScale)
        float frameWidth = 300.0f / 380.0f * viewWidth / sScale;
        float frameHeight = frameWidth;  // 3:4 width/height ratio
        float shiftX = orgX - imageX;
        float shiftY = orgY - imageY;

        float centerXImage = imageWidth / 2.0f + shiftX / sScale;
        float centerYImage = imageHeight / 2.0f + shiftY / sScale;

        int left = (int)(centerXImage - (frameWidth / 2.0f));
        int top = (int)(centerYImage - (frameHeight / 2.0f));

        Bitmap resultBitmap = Bitmap.createBitmap(originalBitmap, left, top, (int)frameWidth, (int)frameHeight);

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