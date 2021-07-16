package com.github.lany192.rxpicker.sample;

import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.github.lany192.rxpicker.RxPicker;
import com.github.lany192.rxpicker.bean.ImageItem;

import java.util.List;

public class PickerAdapter extends BaseQuickAdapter<ImageItem, BaseViewHolder> {

    public PickerAdapter(@Nullable List<ImageItem> data) {
        super(R.layout.item_image, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ImageItem item) {
        ImageView imageView = helper.getView(R.id.image);
        RxPicker.of().display(imageView, item.getPath());
    }
}


