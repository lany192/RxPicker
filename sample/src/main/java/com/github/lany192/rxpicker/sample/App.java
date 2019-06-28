package com.github.lany192.rxpicker.sample;

import android.app.Application;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.lany192.rxpicker.RxPicker;
import com.github.lany192.rxpicker.utils.ImageLoader;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RxPicker.init((imageView, path, width, height) -> {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.ic_preview_image)
                    .error(R.drawable.ic_preview_image)
                    .override(imageView.getWidth(), imageView.getHeight())
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(imageView.getContext())
                    .load(path)
                    .apply(options)
                    .into(imageView);
        });
    }
}
