package com.example.picturepuzzels;

import android.graphics.Bitmap;

public class Photo {
    Bitmap img;
    int tag;

    public Photo(Bitmap img, int tag) {
        this.img = img;
        this.tag = tag;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
}
