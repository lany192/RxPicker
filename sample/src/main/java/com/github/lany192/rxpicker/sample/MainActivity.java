package com.github.lany192.rxpicker.sample;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lany192.rxpicker.RxPicker;
import com.github.lany192.rxpicker.bean.ImageItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private List<String> mSelectedPath = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);
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
