package com.github.lany192.rxpicker.bean;

/**
 * @author Administrator
 */
public class FolderClickEvent {
    private final int position;
    private final ImageFolder folder;

    public FolderClickEvent(int position, ImageFolder folder) {
        this.position = position;
        this.folder = folder;
    }

    public int getPosition() {
        return position;
    }

    public ImageFolder getFolder() {
        return folder;
    }
}
