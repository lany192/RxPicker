package com.caimuhao.rxpicker.ui.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caimuhao.rxpicker.R;
import com.caimuhao.rxpicker.RxPicker;
import com.caimuhao.rxpicker.base.AbstractFragment;
import com.caimuhao.rxpicker.bean.FolderClickEvent;
import com.caimuhao.rxpicker.bean.ImageFolder;
import com.caimuhao.rxpicker.bean.ImageItem;
import com.caimuhao.rxpicker.ui.PreviewActivity;
import com.caimuhao.rxpicker.ui.adapter.PickerAdapter;
import com.caimuhao.rxpicker.ui.fragment.mvp.PickerFragmentContract;
import com.caimuhao.rxpicker.ui.fragment.mvp.PickerFragmentPresenter;
import com.caimuhao.rxpicker.utils.CameraHelper;
import com.caimuhao.rxpicker.utils.RxBus;
import com.caimuhao.rxpicker.widget.PopWindowManager;
import com.github.lany192.decoration.GridDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class PickerFragment extends AbstractFragment<PickerFragmentPresenter>
        implements PickerFragmentContract.View, View.OnClickListener {
    public static final int DEFAULT_SPAN_COUNT = 3;
    public static final int CAMERA_REQUEST = 0x001;
    public static final String MEDIA_RESULT = "MEDIA_RESULT";
    private static final int CAMERA_PERMISSION = 0x002;
    private TextView title;
    private RecyclerView recyclerView;
    private ImageView ivSelectPreview;
    private TextView tvSelectOk;
    private RelativeLayout rlBottom;

    private PickerAdapter adapter;
    private List<ImageFolder> allFolder;

    private Disposable folderClicksubscribe;
    private Disposable imageItemsubscribe;

    public static PickerFragment newInstance() {
        return new PickerFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_picker;
    }

    @Override
    protected void initView(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        title = view.findViewById(R.id.title);
        ivSelectPreview = view.findViewById(R.id.iv_select_preview);
        ivSelectPreview.setOnClickListener(this);
        tvSelectOk = view.findViewById(R.id.iv_select_ok);
        tvSelectOk.setOnClickListener(this);
        rlBottom = view.findViewById(R.id.rl_bottom);
        rlBottom.setVisibility(RxPicker.of().isSingle() ? View.GONE : View.VISIBLE);
        initToolbar(view);
        initRecycler();
        initObservable();
        loadData();
    }

    private void initToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.nav_top_bar);
        final AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        appCompatActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appCompatActivity.onBackPressed();
            }
        });
    }

    private void initObservable() {

        folderClicksubscribe = RxBus.singleton().toObservable(FolderClickEvent.class).subscribe(new Consumer<FolderClickEvent>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull FolderClickEvent folderClickEvent) throws Exception {
                String folderName = folderClickEvent.getFolder().getName();
                title.setText(folderName);
                refreshData(allFolder.get(folderClickEvent.getPosition()));
            }
        });

        imageItemsubscribe = RxBus.singleton().toObservable(ImageItem.class).subscribe(new Consumer<ImageItem>() {
            @Override
            public void accept(@io.reactivex.annotations.NonNull ImageItem imageItem) throws Exception {
                ArrayList<ImageItem> data = new ArrayList<>();
                data.add(imageItem);
                handleResult(data);
            }
        });
    }

    private void loadData() {
        presenter.loadAllImage(getContext());
    }

    private void refreshData(ImageFolder folder) {
        adapter.setData(folder.getImages());
        adapter.notifyDataSetChanged();
    }

    private void initPopWindow(List<ImageFolder> data) {
        new PopWindowManager().init(title, data);
    }

    private void initRecycler() {
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), DEFAULT_SPAN_COUNT);
        recyclerView.setLayoutManager(layoutManager);
        int imageWidth = Resources.getSystem().getDisplayMetrics().widthPixels / DEFAULT_SPAN_COUNT;
        adapter = new PickerAdapter(imageWidth);
        adapter.setCameraClickListener(new CameraClickListener());
        recyclerView.addItemDecoration(new GridDecoration(layoutManager).setColor(getResources().getColor(R.color.rxpicker_colorPrimary)).setWidth(2));
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                tvSelectOk.setText(getString(R.string.select_confim, adapter.getCheckImage().size(), RxPicker.of().getMaxValue()));
            }
        });

        tvSelectOk.setText(getString(R.string.select_confim, adapter.getCheckImage().size(), RxPicker.of().getMaxValue()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //take camera
        if (resultCode == Activity.RESULT_OK && requestCode == CAMERA_REQUEST) {
            handleCameraResult();
        }
    }

    private void handleCameraResult() {
        File file = CameraHelper.getTakeImageFile();
        CameraHelper.scanPic(getActivity(), file);
        for (ImageFolder imageFolder : allFolder) {
            imageFolder.setChecked(false);
        }
        ImageFolder allImageFolder = allFolder.get(0);
        allImageFolder.setChecked(true);
        ImageItem item = new ImageItem(0, file.getAbsolutePath(), file.getName(), System.currentTimeMillis());
        allImageFolder.getImages().add(0, item);
        RxBus.singleton().post(new FolderClickEvent(0, allImageFolder));
    }

    private void handleResult(ArrayList<ImageItem> data) {
        Intent intent = new Intent();
        intent.putExtra(MEDIA_RESULT, data);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void showAllImage(List<ImageFolder> datas) {
        allFolder = datas;
        adapter.setData(datas.get(0).getImages());
        adapter.notifyDataSetChanged();
        initPopWindow(datas);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!folderClicksubscribe.isDisposed()) {
            folderClicksubscribe.dispose();
        }

        if (!imageItemsubscribe.isDisposed()) {
            imageItemsubscribe.dispose();
        }
    }

    @Override
    public void onClick(View v) {
        if (tvSelectOk == v) {
            int minValue = RxPicker.of().getMinValue();
            ArrayList<ImageItem> checkImage = adapter.getCheckImage();
            if (checkImage.size() < minValue) {
                Toast.makeText(getActivity(), getString(R.string.min_image, minValue), Toast.LENGTH_SHORT).show();
                return;
            }

            handleResult(checkImage);
        } else if (ivSelectPreview == v) {
            ArrayList<ImageItem> checkImage = adapter.getCheckImage();
            if (checkImage.isEmpty()) {
                Toast.makeText(getActivity(), R.string.select_one_image, Toast.LENGTH_SHORT).show();
                return;
            }
            PreviewActivity.start(getActivity(), checkImage);
        }
    }

    @TargetApi(23)
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else {
            takePictures();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePictures();
            } else {
                Toast.makeText(getActivity(), R.string.permissions_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void takePictures() {
        CameraHelper.take(PickerFragment.this, CAMERA_REQUEST);
    }

    private class CameraClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= 23) {
                requestPermission();
            } else {
                takePictures();
            }
        }
    }
}
