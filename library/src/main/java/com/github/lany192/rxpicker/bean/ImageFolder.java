package com.github.lany192.rxpicker.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ImageFolder implements Serializable {
    private int id;
    /**
     * Folder name.
     */
    private String name;
    /**
     * Image list in folder.
     */
    private List<ImageItem> images = new ArrayList<>();
    /**
     * checked.
     */
    private boolean isChecked;

    public ImageFolder() {
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

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void addPhoto(ImageItem photo) {
        this.images.add(photo);
    }
}
