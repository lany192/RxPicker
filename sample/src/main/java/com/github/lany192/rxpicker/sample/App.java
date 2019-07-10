package com.github.lany192.rxpicker.sample;

import android.app.Application;

import com.github.lany192.rxpicker.RxPicker;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RxPicker.init(new GlideImageLoader());
    }
}
