package com.caimuhao.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.caimuhao.rxpicker.RxPicker;
import com.caimuhao.rxpicker.bean.ImageItem;
import com.github.lany192.box.activity.BaseActivity;
import com.github.lany192.box.config.ActivityConfig;
import com.github.lany192.decoration.GridDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;

public class MainActivity extends BaseActivity {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private List<String> mSelectedPath = new ArrayList<>();

    @android.support.annotation.NonNull
    @Override
    protected ActivityConfig getConfig(ActivityConfig config) {
        return config.layoutId(R.layout.activity_main)
                .hasBackBtn(false);
    }

    @Override
    protected void init(Bundle bundle) {
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(new GridDecoration(manager)
                .setWidth(2)
                .setColor(Color.BLACK)
                .setShowBorder(true));
    }

    @OnClick(R.id.btn_single_img)
    public void singleClicked() {
        Disposable disposable = RxPicker
                .of()
                .start(this)
                .subscribe(imageItems -> mRecyclerView.setAdapter(new PickerAdapter(imageItems)));
    }

    @OnClick(R.id.btn_multi_img)
    public void multiClicked() {
        Disposable disposable = RxPicker
                .of()
                .single(false)
                .camera(true)
                .limit(0, 9)
                .selected(mSelectedPath)
                .start(this)
                .subscribe(items -> {
                    for (ImageItem item : items) {
                        mSelectedPath.add(item.getPath());
                    }
                    mRecyclerView.setAdapter(new PickerAdapter(items));
                });
    }
}
