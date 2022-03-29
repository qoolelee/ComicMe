package com.uuballgame.comicme;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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
                buyPro();
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

    private void buyPro(){
        // register first
        if(Constants.NEW_USERNAME.username.length()<1) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.lobby_already_login))
                    .setTitle(R.string.app_name)
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            builder.create().show();
        }
    }


}