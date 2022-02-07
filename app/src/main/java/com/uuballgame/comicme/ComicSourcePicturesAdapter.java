package com.uuballgame.comicme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ComicSourcePicturesAdapter extends RecyclerView.Adapter<ComicSourcePicturesAdapter.PicViewHolder> {
    public List<ComicSourceImage> comicSourceImages;
    public Context context;

    // inner ViewHolder
    class PicViewHolder extends RecyclerView.ViewHolder{
        public ImageView picImageView;

        public PicViewHolder(@NonNull View itemView) {
            super(itemView);

            picImageView = itemView.findViewById(R.id.thumb_pic);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();


                }
            });
        }
    }

    public ComicSourcePicturesAdapter(List<ComicSourceImage> comicSourceImages) {
        this.comicSourceImages = comicSourceImages;
    }

    @NonNull
    @Override
    public ComicSourcePicturesAdapter.PicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_source_image_item, parent, false);
        return new ComicSourcePicturesAdapter.PicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComicSourcePicturesAdapter.PicViewHolder holder, int position) {
        ComicSourceImage comicSourceImage = comicSourceImages.get(position);

        holder.picImageView.setImageBitmap(comicSourceImage.thumbnailBitmap);
    }

    @Override
    public int getItemCount() {
        return comicSourceImages.size();
    }
}
