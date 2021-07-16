package com.github.lany192.rxpicker.sample;

import android.app.Application;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.lany192.rxpicker.RxPicker;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RxPicker.of().init((imageView, path) -> {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.default_pic)
                    .error(R.drawable.default_pic)
                    .diskCacheStrategy(DiskCacheStrategy.NONE);
            Glide.with(imageView.getContext())
                    .load(path)
                    .apply(options)
                    .into(imageView);
        });
    }
}
