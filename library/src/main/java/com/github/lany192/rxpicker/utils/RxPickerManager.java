package com.github.lany192.rxpicker.utils;

import android.content.Intent;
import android.widget.ImageView;

import com.github.lany192.rxpicker.bean.ImageItem;
import com.github.lany192.rxpicker.picker.PickerFragment;

import java.util.List;


public class RxPickerManager {

    private static RxPickerManager manager;
    private PickerConfig config;
    private ImageLoader imageLoader;

    private RxPickerManager() {
    }

    public static RxPickerManager getInstance() {
        if (manager == null) {
            synchronized (RxPickerManager.class) {
                if (manager == null) {
                    manager = new RxPickerManager();
                }
            }
        }
        return manager;
    }

    public PickerConfig getConfig() {
        return config;
    }

    public RxPickerManager setConfig(PickerConfig config) {
        this.config = config;
        return this;
    }

    public void init(ImageLoader imageLoader) {
        this.imageLoader = imageLoader;
    }

    public void setMode(int mode) {
        config.setMode(mode);
    }

    public void showCamera(boolean showCamera) {
        config.setShowCamera(showCamera);
    }

    public void limit( int maxValue) {
        config.setLimit( maxValue);
    }

    public void display(ImageView imageView, String path, int width, int height) {
        if (imageLoader == null) {
            throw new NullPointerException("You must fist of all call 'RxPicker.init()' to initialize");
        }
        imageLoader.display(imageView, path, width, height);
    }

    public List<ImageItem> getResult(Intent intent) {
        return (List<ImageItem>) intent.getSerializableExtra(PickerFragment.MEDIA_RESULT);
    }
}
