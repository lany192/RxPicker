package com.github.lany192.rxpicker.sample;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.lany192.rxpicker.utils.ImageLoader;


public class GlideImageLoader implements ImageLoader {

    @Override
    public void display(ImageView imageView, String path, int width, int height) {
        Glide.with(imageView.getContext())
                .load(path)
                .error(R.drawable.rx_picker_preview_image)
                .centerCrop()
                .override(width, height)
                .into(imageView);
    }
}
