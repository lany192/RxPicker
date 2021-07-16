package com.github.lany192.rxpicker.sample;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lany192.rxpicker.RxPicker;
import com.github.lany192.rxpicker.bean.ImageItem;
import com.github.lany192.rxpicker.permission.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {
    final RxPermissions rxPermissions = new RxPermissions(this);
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private List<String> mSelectedPath = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        GridLayoutManager manager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(manager);
//        mRecyclerView.addItemDecoration(new GridDecoration(manager)
//                .setWidth(2)
//                .setColor(Color.BLACK)
//                .setShowBorder(true));
    }

    @OnClick(R.id.btn_single_img)
    public void singleClicked() {
        rxPermissions
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        Disposable disposable = RxPicker
                                .of()
                                .start(this)
                                .subscribe(imageItems -> mRecyclerView.setAdapter(new PickerAdapter(imageItems)));
                    } else {
                        Toast.makeText(MainActivity.this, R.string.permissions_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @OnClick(R.id.btn_multi_img)
    public void multiClicked() {
        rxPermissions
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
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
                    } else {
                        Toast.makeText(MainActivity.this, R.string.permissions_error, Toast.LENGTH_SHORT).show();
                    }
                });


    }
}
