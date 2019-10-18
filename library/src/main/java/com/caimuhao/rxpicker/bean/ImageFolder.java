package com.caimuhao.rxpicker.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
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

    public ImageFolder(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addPhoto(ImageItem photo) {
        this.images.add(photo);
    }
}
