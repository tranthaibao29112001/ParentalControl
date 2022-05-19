package com.example.parentalcontrol;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Photo implements Parcelable {
    private String imgName;
    private Uri imgUri;

    public Photo(String imgName, Uri imgUri) {
        this.imgName = imgName;
        this.imgUri = imgUri;
    }

    protected Photo(Parcel in) {
        imgName = in.readString();
        imgUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public Uri getImgUri() {
        return imgUri;
    }

    public void setImgUri(Uri imgUri) {
        this.imgUri = imgUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imgName);
        parcel.writeParcelable(imgUri, i);
    }
}
