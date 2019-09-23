package com.example.worddart;

import android.graphics.Bitmap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserPreview {

    private Bitmap profImage;
    private String name;
    public UserPreview(Bitmap c, String name)
    {
        this.profImage=c;
        this.name=name;
    }
    public Bitmap getProfImage() {
        return profImage;
    }

    public void setProfImage(Bitmap profImage) {
        this.profImage = profImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
