
package com.github.lany192.rxpicker.bean;

import java.io.Serializable;
import java.util.ArrayList;


public class ImageFolder implements Serializable {

    private int id;

    private String name;

    private ArrayList<ImageItem> images = new ArrayList<>();

    private boolean isChecked;

    public ImageFolder() {
        super();
    }

    public ImageFolder(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ImageItem> getImages() {
        return images;
    }

    public void addPhoto(ImageItem photo) {
        this.images.add(photo);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
