package com.uuballgame.comicme;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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





}