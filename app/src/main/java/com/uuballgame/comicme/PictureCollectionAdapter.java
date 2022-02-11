package com.uuballgame.comicme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PictureCollectionAdapter extends RecyclerView.Adapter<PictureCollectionAdapter.PicViewHolder> {
    public  ComicFilter comicFilter;
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
                    ComicSourceImage comicSourceImage = comicSourceImages.get(position);

                    Intent imageDetailedActivityIntent = new Intent(context, PicturePreprocessActivity.class);
                    imageDetailedActivityIntent.putExtra("ComicSourceImage", comicSourceImage);
                    imageDetailedActivityIntent.putExtra("ComicFilter", comicFilter);
                    context.startActivity(imageDetailedActivityIntent);
                }
            });
        }
    }

    public PictureCollectionAdapter(Context context, List<ComicSourceImage> comicSourceImages, ComicFilter comicFilter) {
        this.context = context;
        this.comicSourceImages = comicSourceImages;
        this.comicFilter = comicFilter;
    }

    @NonNull
    @Override
    public PictureCollectionAdapter.PicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_source_image_item, parent, false);
        return new PictureCollectionAdapter.PicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureCollectionAdapter.PicViewHolder holder, int position) {
        ComicSourceImage comicSourceImage = comicSourceImages.get(position);

        Bitmap bitmapOrg = Constants.convert(comicSourceImage.thumbnailBitmapBase64);
        Bitmap rotatedBitmap = Constants.rotateBmap(bitmapOrg, -90);
        holder.picImageView.setImageBitmap(rotatedBitmap);
    }

    @Override
    public int getItemCount() {
        return comicSourceImages.size();
    }
}
