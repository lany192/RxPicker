
package com.github.lany192.rxpicker.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ImageFolder implements Serializable {
    private int id;
    private String name;
    private List<Image> images = new ArrayList<>();
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

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public void addImage(Image image) {
        this.images.add(image);
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
