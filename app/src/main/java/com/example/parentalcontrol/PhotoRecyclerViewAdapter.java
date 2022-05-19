package com.example.parentalcontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PhotoRecyclerViewAdapter extends RecyclerView.Adapter<PhotoRecyclerViewAdapter.MyViewHolder> {
    ArrayList<Photo> photos;
    Context context;

    public PhotoRecyclerViewAdapter(Context context, ArrayList<Photo> photos) {
        this.context = context;
        this.photos = photos;
    }
    public void setData(ArrayList<Photo> photos){
        this.photos = photos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Uri imgUri = photos.get(position).getImgUri();
        Glide.with(context)
                .load(imgUri)
                .centerCrop()
                .into(holder.img);

        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickGoToDetailImg(photos.get(position));

            }
        });
    }

    private void onClickGoToDetailImg(Photo photo) {
//        Intent detailIntent = new Intent(context,DetailPhotoActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putParcelable( "object_photo",photo);
//        detailIntent.putExtras(bundle);
//        context.startActivity(detailIntent);
        Intent intent = new Intent(context, DetailPhotoActivity.class);
        Bundle bundle =  new Bundle();
        bundle.putParcelable("capture-item", photo);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.photo);
        }
    }
}
