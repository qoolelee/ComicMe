package com.uuballgame.comicme;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

public class ShowResultActivity extends AppCompatActivity {
    private ComicFilter comicFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);

        // get comic filter
        Intent from = getIntent();
        String filName = from.getStringExtra("fileName");
        comicFilter = (ComicFilter) from.getSerializableExtra("ComicFilter");

        // back arrow but since previous activity is closed, this will not work
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(comicFilter.name);
        actionBar.setDisplayHomeAsUpEnabled(true);
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