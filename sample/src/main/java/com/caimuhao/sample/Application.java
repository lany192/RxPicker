package com.caimuhao.sample;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.caimuhao.rxpicker.RxPicker;
import com.github.lany192.box.Box;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Box.of().init(this);
        RxPicker.of().init((imageView, path, width, height) -> {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.default_pic)
                    .error(R.drawable.default_pic)
                    .override(width, height)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE);
            Glide.with(imageView.getContext())
                    .load(path)
                    .apply(options)
                    .into(imageView);
        });
    }
}
