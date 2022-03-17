package com.uuballgame.comicme;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PicturePreprocessActivity extends AppCompatActivity {
    private ComicFilter comicFilter;
    private ComicSourceImage comicSourceImage;
    private Bitmap originalBitmap;
    private ImageButton okButton;
    private ImageButton noButton;
    private View.OnClickListener okListener, noListener;
    private static final String CROPPED_FILE_NAME = "img_" + Constants.NEW_UUID.uuid + "_";
    private static final int NORMALIZED_PIC_WIDTH = 512;

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
        ExifInterface ei = null;
        int orientation = 0;
        if(comicSourceImage.photoPath!=null) {
            originalBitmap = BitmapFactory.decodeFile(comicSourceImage.photoPath);

            // normalize saved photo
            try {
                ei = new ExifInterface(comicSourceImage.photoPath);
                orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), PictureCollectionActivity.SImageUrl);
            } catch (IOException e) {
                e.printStackTrace();
                finish();
            }

            try (InputStream inputStream = this.getContentResolver().openInputStream(PictureCollectionActivity.SImageUrl)) {
                ei = new ExifInterface(inputStream);
                orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // rotate pic. and thumbnail
        switch(orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                originalBitmap = Constants.rotateBitmap(originalBitmap, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                originalBitmap = Constants.rotateBitmap(originalBitmap, 180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                originalBitmap = Constants.rotateBitmap(originalBitmap, 270);
                break;
            default: // 0
                break;
        }

        comicSourceImage.thumbnailBitmapBase64 = Constants.convert(Constants.scaleBitmap(originalBitmap, 100, 100));

        // normalize bitmap
        normalizeBitmap(originalBitmap); // 1080 * 1920 min. sized and musked

    }

    private void normalizeBitmap(Bitmap originalBitmap) {
        // sized
        float w = 1080.0f; // default normalized width
        float h = 1920.0f; // default normalized height
        float ratio = 1.0f;

        if(originalBitmap.getWidth()>=originalBitmap.getHeight()){
            ratio = w/(float) originalBitmap.getWidth();
        }
        else{
            ratio = h/(float) originalBitmap.getHeight();
        }

        Bitmap nBitmap = Constants.scaleBitmap(originalBitmap, ratio);

        // musked
        SelfieSegmenterOptions options =
                new SelfieSegmenterOptions.Builder()
                        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                        .build();

        Segmenter segmenter = Segmentation.getClient(options);
        InputImage image = InputImage.fromBitmap(nBitmap, 0);

        Task<SegmentationMask> result =
                segmenter.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<SegmentationMask>() {
                                    @Override
                                    public void onSuccess(SegmentationMask mask) {
                                        // Task completed successfully
                                        // get musked bitmap
                                        //Bitmap muskedBitmap = getMuskedBitmap(nBitmap, mask);

                                        //findFace(muskedBitmap);
                                        findFace(nBitmap);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

    }

    private Bitmap getMuskedBitmap(Bitmap nBitmap, SegmentationMask segmentationMask) {
        Bitmap result = null;

        // blur
        Bitmap bBitmap = Bitmap.createBitmap(nBitmap);
        bBitmap = Constants.scaleBitmap(bBitmap, 0.1f);
        bBitmap = Constants.scaleBitmap(bBitmap, 10f);
        bBitmap = BlurImage(bBitmap);

        // musked
        int maskWidth = segmentationMask.getWidth();
        int maskHeight = segmentationMask.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(maskColorsFromByteBuffer(segmentationMask, nBitmap, bBitmap),
                maskWidth, maskHeight, Bitmap.Config.ARGB_8888);

        return bitmap;
    }

    /** Converts byteBuffer floats to ColorInt array that can be used as a mask. */
    @ColorInt
    private int[] maskColorsFromByteBuffer(SegmentationMask segmentationMask, Bitmap nBitmap, Bitmap bBitmap) {

        int maskWidth = nBitmap.getWidth();
        int maskHeight = nBitmap.getHeight();

        int[] npixels = new int[maskWidth * maskHeight];
        nBitmap.getPixels(npixels, 0, maskWidth, 0, 0, maskWidth, maskHeight);

        int[] bpixels = new int[maskWidth * maskHeight];
        bBitmap.getPixels(bpixels, 0, maskWidth, 0, 0, maskWidth, maskHeight);

        ByteBuffer maskByteBuffer = segmentationMask.getBuffer();

        @ColorInt int[] colors = new int[maskWidth * maskHeight];
        for (int i = 0; i < maskWidth * maskHeight; i++) {
            float backgroundLikelihood = 1 - maskByteBuffer.getFloat();
            if (backgroundLikelihood > 0.9) {
                //colors[i] = Color.argb(0, 255, 255, 255);
                colors[i] = Color.argb(255, (bpixels[i] >> 16) & 0xff, (bpixels[i] >> 8) & 0xff, (bpixels[i] ) & 0xff);
            }
            //else if (backgroundLikelihood > 0.2) {
                // Linear interpolation to make sure when backgroundLikelihood is 0.2, the alpha is 0 and
                // when backgroundLikelihood is 0.9, the alpha is 128.
                // +0.5 to round the float value to the nearest int.
                //int alpha = (int) (182.9 * backgroundLikelihood - 36.6 + 0.5);
                //colors[i] = Color.argb(alpha, 0, 0, 0);
            //}
            else{
                //colors[i] = Color.argb(255, 255, 255, 255);
                colors[i] = Color.argb(255, (npixels[i] >> 16) & 0xff, (npixels[i] >> 8) & 0xff, (npixels[i] ) & 0xff);
            }
        }
        return colors;
    }

    @SuppressLint("NewApi")
    Bitmap BlurImage (Bitmap input)
    {
        try
        {
            RenderScript rsScript = RenderScript.create(getApplicationContext());
            Allocation alloc = Allocation.createFromBitmap(rsScript, input);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript,   Element.U8_4(rsScript));
            blur.setRadius(23);
            blur.setInput(alloc);

            Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

            blur.forEach(outAlloc);
            outAlloc.copyTo(result);

            rsScript.destroy();
            return result;
        }
        catch (Exception e) {
            // TODO: handle exception
            return input;
        }

    }

    private void findFace(Bitmap muskedBitmap) {
        // enlarge 2 times the bitmap
        Bitmap enlargedBitmap = Constants.enlargeBmap(muskedBitmap, 2.0f);

        SubsamplingScaleImageView pictureView = findViewById(R.id.image_detailed_picture_view);
        pictureView.setImage(ImageSource.bitmap(enlargedBitmap));

        centerFacePos(pictureView, enlargedBitmap);
    }

    private void centerFacePos(SubsamplingScaleImageView pictureView, Bitmap bitmap) {
        FaceDetectorOptions lowAccuracyOpts = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                .build();

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        FaceDetector detector = FaceDetection.getClient(lowAccuracyOpts);

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

                        // 5. face direction
                        float rotY = face.getHeadEulerAngleY();
                        float rotX = face.getHeadEulerAngleX();
                        if(rotY >= MAX_FACE_ROTY || rotY <= -MAX_FACE_ROTY
                                || rotX >= MAX_FACE_ROTX || rotX <= -MAX_FACE_ROTX){
                            setAlertText(ERROR005);
                            return;
                        }

                        // if all pass show progress bar, and start to move and scale
                        // move
                        Rect rect = face.getBoundingBox();
                        float scale = (float)pictureView.getWidth()/((float)rect.width() * 2.0f);
                        pictureView.animateScaleAndCenter(scale, new PointF(rect.centerX(), rect.centerY()))
                                .withDuration(1000)
                                .withEasing(SubsamplingScaleImageView.EASE_OUT_QUAD)
                                .withInterruptible(false)
                                .start();

                        // bitmap check and crop
                        okListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bitmap croppedBitmap = getFramedBitmap(pictureView , bitmap);
                                uploadPicture(croppedBitmap);
                            }
                        };
                        okButton = findViewById(R.id.process_image_yes);
                        okButton.setOnClickListener(okListener);

                        // return to PictureCollectionActivity
                        noListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        };
                        noButton = findViewById(R.id.process_image_no);
                        noButton.setOnClickListener(noListener);

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
    public static final int ERROR008 = 8;

    private void uploadPicture(Bitmap bitmap) {
        // show musk and progress bar
        setMusk(View.VISIBLE);

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
                        startUploading(bitmap);

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


    private void setMusk(int visible) {
        ImageView musk = findViewById(R.id.picture_process_top_view);
        ProgressBar progressBar = findViewById(R.id.picture_process_progressbar);
        musk.setVisibility(visible);
        progressBar.setVisibility(visible);
        TextView text = findViewById(R.id.uploading_text_view);
        text.setVisibility(visible);
        text.setText(R.string.picture_process_uploading);

        if(visible == View.VISIBLE){
            okButton.setOnClickListener(null);
            noButton.setOnClickListener(null);
        }
        else{
            okButton.setOnClickListener(okListener);
            noButton.setOnClickListener(noListener);
        }
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
            case ERROR008:
                alertMessage = R.string.image_detailed_error08;
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

        // hide musk and progress bar
        setMusk(View.GONE);
    }


    private Bitmap getFramedBitmap(SubsamplingScaleImageView pictureView, Bitmap bitmap) {
        float scale = pictureView.getScale();
        PointF centerPoint = pictureView.getCenter();

        float ratio = 300.0f/ 380.0f;
        float frameWidth = ratio * (float)pictureView.getWidth() / scale ;
        //float frameHeight = ratio * (float)pictureView.getHeight() / scale;
        float frameHeight = frameWidth;
        float left = centerPoint.x - frameWidth / 2.0f;
        float top = centerPoint.y - frameHeight / 2.0f;

        Bitmap resultBitmap = Bitmap.createBitmap(bitmap, (int)left, (int)top, (int)frameWidth, (int)frameHeight);

        return resultBitmap;

    }


    private void startUploading(Bitmap bitmap) {
        // normalize to NORMALIZED_PIC_WIDTH
        bitmap = Constants.scaleBitmap(bitmap, NORMALIZED_PIC_WIDTH, NORMALIZED_PIC_WIDTH);
        PictureCollectionActivity.LASTBitmap = bitmap;

        // upload bitmap to server
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = CROPPED_FILE_NAME + timeStamp + ".png";
        File file = new File(getFilesDir(), fileName);
        if(file.exists())file.delete();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new UploadImageTask().execute(file);
    }

    private class UploadImageTask extends AsyncTask<File, Void, String> {
        String fileName;

        @Override
        protected String doInBackground(File... files) {
            File file = files[0];
            fileName = file.getName();

            try {
                final String boundary = "==============";
                final String twoHyphens = "--";
                final String lineEnd = "\r\n";

                FileInputStream fis = openFileInput(fileName);
                URL url = new URL(Constants.IMAGE_UPLOAD_PHP_URL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                //上傳檔案，不是一次就可以傳送上去。要一部份一部份的上傳。
                //所以，要先設定一個buffer，將檔案的內容分次上傳。
                int bytesAvailable = fis.available();
                int bufferSize = Math.min(bytesAvailable, 1024*1024);
                byte[] buffer = new byte[bufferSize];
                int bytesRead = fis.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable, 1024*1024);
                    bytesRead = fis.read(buffer, 0, bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens+ boundary + twoHyphens);	// (結束)寫--==================================--

                // Responses from the server (code and message)
                int serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                fis.close();
                dos.flush();
                dos.close();

                return serverResponseMessage;

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equalsIgnoreCase("OK")){
                startDraw(fileName);
            }
            else{
                setMusk(View.GONE);
                setAlertText(ERROR008);
            }
        }
    }

    private void startDraw(String fileName) {
        // text view change
        TextView textView = findViewById(R.id.uploading_text_view);
        textView.setText(R.string.picture_process_processing);

        // inform server to process image
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.START_PICTURE_PROCESS_URL;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // musk off
                    setMusk(View.GONE);

                    // decode JSON
                    ProcessResult processResult = new Gson().fromJson(response, ProcessResult.class);

                    if(processResult.result.equals("Success")){
                        String url = Constants.SERVER_IP + processResult.url;
                        Intent intent = new Intent();
                        intent.putExtra("url", url);
                        intent.putExtra("ComicSourceImage", comicSourceImage);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    else{
                        setAlertText(ERROR008);
                    }

                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // musk off
                setMusk(View.GONE);
                setAlertText(ERROR008);
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("fileName", fileName);
                map.put("model", String.valueOf(comicFilter.id - 1)); // id minus 1
                return map;
            }
        };

        // 20 seconds time out time
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        );

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    class ProcessResult{
        public String result;
        public String url;

        public ProcessResult(String result, String url){
            this.result = result;
            this.url = url;
        }
    }

}