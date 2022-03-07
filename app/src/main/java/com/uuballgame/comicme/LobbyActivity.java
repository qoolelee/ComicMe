package com.uuballgame.comicme;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class LobbyActivity extends AppCompatActivity {
    private int currentApiVersion;
    private Fragment collectionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lobby);

        // authorize camera and microphone and internal pictures if not stop executions
        initRequirement();

        // create CollectionFragment
        collectionFragment = CollectionFragment.newInstance("","");
        getSupportFragmentManager().beginTransaction().replace(R.id.lobby_layout, collectionFragment).commit();

        // check uuid if exist
        if(getSavedUUID() == null){
            getNewUUID();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lobby_actionbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_buy_pro:
                //buyPro();
                return true;
            case R.id.action_share:
                //shareComicMe();
                return true;
            case R.id.action_search:
                //searchFilter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initRequirement() {
        String[] permissionNeeded = {
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.INTERNET",
                "android.permission.WAKE_LOCK"};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ( !allPermissionPassed() ) {
                requestPermissions(permissionNeeded, 101);
            }
        }
    }

    private boolean allPermissionPassed() {
        return( ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_NETWORK_STATE") == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, "android.permission.INTERNET") == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, "android.permission.WAKE_LOCK") == PackageManager.PERMISSION_GRANTED);
    }

    class NewUUID{
        public String result;
        public String uuid;

        public NewUUID(String res, String id){
            result = res;
            uuid = id;
        }
    }

    private void getNewUUID() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.GET_NEW_UUID_URL;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // set uuid
                        NewUUID newUUID = new Gson().fromJson(response, NewUUID.class);
                        Constants.COMIC_ME_UUID = newUUID.uuid;

                        // save to preference
                        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.comic_me_app), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("comic_me_uuid", newUUID.uuid);
                        editor.apply();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // alert
                        Alert(getResources().getString(R.string.server_maintain_please_try_again));
                    }
            });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    private String getSavedUUID() {
        // read back str from shared preferences
        SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.comic_me_app), Context.MODE_PRIVATE);
        String uuid = sharedPref.getString("comic_me_uuid", null);
        Constants.COMIC_ME_UUID = uuid;
        return uuid;
    }

    private void Alert(String alertMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alertMessage)
                .setTitle(R.string.error)
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.create().show();
    }

}