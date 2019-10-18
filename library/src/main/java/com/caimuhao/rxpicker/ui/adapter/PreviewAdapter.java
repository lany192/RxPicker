package com.caimuhao.rxpicker.ui.adapter;

import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.caimuhao.rxpicker.RxPicker;
import com.caimuhao.rxpicker.bean.ImageItem;
import com.caimuhao.rxpicker.widget.TouchImageView;

import java.util.List;


public class PreviewAdapter extends PagerAdapter {
    private List<ImageItem> items;

    public PreviewAdapter(List<ImageItem> items) {
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
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
        container.addView(imageView);
        int deviceWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        RxPicker.of().display(imageView, items.get(position).getPath(), deviceWidth, deviceWidth);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
