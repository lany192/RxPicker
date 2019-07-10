package com.github.lany192.rxpicker.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.github.lany192.rxpicker.bean.Image;
import com.github.lany192.rxpicker.utils.DensityUtil;
import com.github.lany192.rxpicker.utils.RxPickerManager;
import com.github.lany192.rxpicker.widget.TouchImageView;

import java.util.List;


public class PreviewAdapter extends PagerAdapter {

    private List<Image> data;

    public PreviewAdapter(List<Image> data) {
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imageView = new TouchImageView(container.getContext());
        ViewPager.LayoutParams layoutParams = new ViewPager.LayoutParams();
        imageView.setLayoutParams(layoutParams);
        Image imageItem = data.get(position);
        container.addView(imageView);
        int deviceWidth = DensityUtil.getDeviceWidth(container.getContext());
        RxPickerManager.getInstance()
                .display(imageView, imageItem.getPath(), deviceWidth, deviceWidth);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
