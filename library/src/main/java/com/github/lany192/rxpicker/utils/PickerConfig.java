package com.github.lany192.rxpicker.utils;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class PickerConfig {
    public static final int SINGLE_IMG = 0x001;
    public static final int MULTIPLE_IMG = 0x002;
    private int maxSize = 9;
    private boolean showCamera = true;
    private int mode = SINGLE_IMG;

    public int getMode() {
        return mode;
    }

    public void setMode(@Mode int mode) {
        this.mode = mode;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public boolean isSingle() {
        return mode == SINGLE_IMG;
    }

    @IntDef({SINGLE_IMG, MULTIPLE_IMG})
    @Retention(RetentionPolicy.SOURCE)
    @interface Mode {
    }
}
