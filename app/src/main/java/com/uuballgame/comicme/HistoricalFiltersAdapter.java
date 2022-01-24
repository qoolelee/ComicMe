package com.uuballgame.comicme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class HistoricalFiltersAdapter extends RecyclerView.Adapter<HistoricalFiltersAdapter.ComicViewHolder> {
    public List<ComicFilter> comicFilters;
    public Context context;


    // inner ViewHolder
    class ComicViewHolder extends RecyclerView.ViewHolder{
        public ImageView comicImageView;
        public TextView comicLabel;

        public ComicViewHolder(@NonNull View itemView) {
            super(itemView);

            comicImageView = itemView.findViewById(R.id.comic_pic);
            comicLabel = itemView.findViewById(R.id.comic_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    //ComicFilter comicFilter = comicFilters.get(position);

                    // add to HistoricalComicFiltersAdapter

                }
            });
        }
    }

    // constructor
    public HistoricalFiltersAdapter(List<ComicFilter> filters){
        comicFilters = filters;
    }

    @NonNull
    @Override
    public ComicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_comic_item, parent, false);
        return new HistoricalFiltersAdapter.ComicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicViewHolder holder, int position) {
        // get data according to position
        ComicFilter comicFilter = comicFilters.get(position);

        // Set item views according to data model
        Glide.with(context)
                .load(comicFilter.imageUrl)
                .into(holder.comicImageView);
        holder.comicLabel.setText(comicFilter.name);
    }

    @Override
    public int getItemCount() {
        return comicFilters.size();
    }
}
