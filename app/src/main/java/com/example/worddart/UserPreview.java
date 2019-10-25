package com.example.worddart;

import android.graphics.Bitmap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserPreview {

    private Bitmap profImage;
    private String name;
    private String id;

    public UserPreview(Bitmap c, String name,String id)
    {
        this.profImage=c;
        this.name=name;
        this.id=id;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
