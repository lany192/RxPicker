package com.github.lany192.rxpicker.sample;

import android.app.Application;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.lany192.rxpicker.RxPicker;
import com.github.lany192.rxpicker.utils.ImageLoader;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RxPicker.init((imageView, path, width, height) -> Glide.with(imageView.getContext())
                .load(path)
                .centerCrop()
                .override(width, height)
                .into(imageView));
    }
}
