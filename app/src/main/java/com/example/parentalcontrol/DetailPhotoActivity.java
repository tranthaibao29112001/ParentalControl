package com.example.parentalcontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class DetailPhotoActivity extends AppCompatActivity {
    Photo mPhoto;
    ImageView bigImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_photo);
        Bundle bundle = getIntent().getExtras();
        if(bundle==null){
            return;
        }
        mPhoto = (Photo) bundle.getParcelable("capture-item");
        bigImg = (ImageView)findViewById(R.id.bigImg);
        Glide.with(this)
                .load(mPhoto.getImgUri())
                .into(bigImg);
        setTitle(mPhoto.getImgName());
    }
}