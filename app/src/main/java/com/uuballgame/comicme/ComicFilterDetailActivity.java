package com.uuballgame.comicme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ComicFilterDetailActivity extends AppCompatActivity {
    private ComicFilter comicFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        // get comic filter
        Intent from = getIntent();
        comicFilter = (ComicFilter) from.getSerializableExtra("ComicFilter");

        // back arrow
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.input_picture_to_comic);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // title changed
        TextView title = findViewById(R.id.detailed_name);
        title.setText(comicFilter.name);

        // gif loaded
        ImageView imageView = findViewById(R.id.detailed_gif);
        int radius = 60;
        int margin = 30;
        Glide.with(this)
                .asGif()
                .load(comicFilter.gifUrl)
                .placeholder(R.drawable.loading_holder)
                .transform(new RoundedCornersTransformation(radius, margin))
                .centerCrop()
                .into(imageView);

        // button listener
        Button button = findViewById(R.id.detailed_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPictureCollectionActivity();
            }
        });
    }

    private void startPictureCollectionActivity() {
        Intent intent = new Intent(this, PictureCollectionActivity.class);
        intent.putExtra("ComicFilter", comicFilter);
        startActivity(intent);

        finish();
    }

    // this event will enable the back
    // function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}