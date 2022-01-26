package com.uuballgame.comicme;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.List;

public class PictureCollectionActivity extends AppCompatActivity {
    private ComicFilter comicFilter;

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

        // find recycler view in fragment
        RecyclerView recyclerView = findViewById(R.id.picture_collection_recyclerview);

        /*
        // prepare data for adapter
        List<> comicFilters = Constants.COMIC_FILTERS_LIST;
        // Create the View holder adapter
        AllComicFiltersAdapter filterAdapter = new AllComicFiltersAdapter(comicFilters);
        // attach adapter to recycler view
        recyclerView.setAdapter(filterAdapter);
        // set layout manager to position the items
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        */

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