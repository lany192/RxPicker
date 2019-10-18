package com.caimuhao.sample;

import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.caimuhao.rxpicker.RxPicker;
import com.caimuhao.rxpicker.bean.ImageItem;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.lany192.box.adapter.ItemViewHolder;

import java.util.List;


public class PickerAdapter extends BaseQuickAdapter<ImageItem, ItemViewHolder> {

    public PickerAdapter(@Nullable List<ImageItem> data) {
        super(R.layout.item_image, data);
    }

    @Override
    protected void convert(ItemViewHolder helper, ImageItem item) {
        ImageView imageView = helper.getView(R.id.image);
        int imageSize = Resources.getSystem().getDisplayMetrics().widthPixels / 3;
        RxPicker.of().display(imageView, item.getPath(), imageSize, imageSize);
    }
}


