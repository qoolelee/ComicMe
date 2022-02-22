package com.uuballgame.comicme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.List;

public class PictureCollectionAdapter extends RecyclerView.Adapter<PictureCollectionAdapter.PicViewHolder> {
    public  ComicFilter comicFilter;
    public List<ComicSourceImage> comicSourceImages;
    public Context context;

    // inner ViewHolder
    class PicViewHolder extends RecyclerView.ViewHolder{
        public ImageView picImageView;
        public ImageView picImageClose;

        public PicViewHolder(@NonNull View itemView) {
            super(itemView);

            picImageView = itemView.findViewById(R.id.thumb_pic);
            adjustWidthHeight(PictureCollectionActivity.NUMBER_OF_COLUMNS, picImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // show musk
                    PictureCollectionActivity pictureCollectionActivity = (PictureCollectionActivity) context;
                    pictureCollectionActivity.findViewById(R.id.picture_collection_top_view).setVisibility(View.VISIBLE);
                    pictureCollectionActivity.findViewById(R.id.picture_collection_progressbar).setVisibility(View.VISIBLE);
                    pictureCollectionActivity.findViewById(R.id.picture_collection_button_camera).setClickable(false);
                    pictureCollectionActivity.findViewById(R.id.picture_collection_button_gallery).setClickable(false);

                    int position = getAdapterPosition();
                    ComicSourceImage comicSourceImage = comicSourceImages.get(position);

                    Intent imageDetailedActivityIntent = new Intent(context, PicturePreprocessActivity.class);
                    imageDetailedActivityIntent.putExtra("ComicSourceImage", comicSourceImage);
                    imageDetailedActivityIntent.putExtra("ComicFilter", comicFilter);
                    ((PictureCollectionActivity) context).startActivityForResult(imageDetailedActivityIntent, PictureCollectionActivity.REQUEST_IMAGE_PROCESS);
                }
            });

            picImageClose = itemView.findViewById(R.id.thumb_pic_delete);
            picImageClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setMessage(R.string.delete_this_picture);
                    alertDialog.setPositiveButton(R.string.image_detailed_confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = getAdapterPosition();
                            comicSourceImages.remove(position);

                            PictureCollectionActivity pictureCollectionActivity = (PictureCollectionActivity) context;
                            pictureCollectionActivity.comicSourceImages = comicSourceImages;

                            // to Json string
                            String str = new Gson().toJson(comicSourceImages);
                            // save to preference
                            SharedPreferences sharedPref = pictureCollectionActivity.getSharedPreferences(pictureCollectionActivity.getString(R.string.comic_me_app), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("comic_source_images", str);
                            editor.apply();

                            // update list
                            notifyDataSetChanged();

                            dialog.dismiss();
                        }
                    });
                    alertDialog.setNegativeButton(R.string.image_detailed_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
            });
        }
    }

    private void adjustWidthHeight(int numberOfColumns, ImageView picImageView) {
        ViewGroup.LayoutParams params = picImageView.getLayoutParams();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidthPx = displayMetrics.widthPixels / PictureCollectionActivity.NUMBER_OF_COLUMNS;
        params.height = screenWidthPx;
        params.width = screenWidthPx;
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
        if(bitmapOrg.getWidth()>bitmapOrg.getHeight()) bitmapOrg = Constants.rotateBmap(bitmapOrg, -90);
        holder.picImageView.setImageBitmap(bitmapOrg);
    }

    @Override
    public int getItemCount() {
        return comicSourceImages.size();
    }


}
