package com.github.lany192.rxpicker.picker;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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

import com.github.lany192.rxpicker.R;
import com.github.lany192.rxpicker.adapter.PickerFragmentAdapter;
import com.github.lany192.rxpicker.base.AbstractFragment;
import com.github.lany192.rxpicker.bean.FolderClickEvent;
import com.github.lany192.rxpicker.bean.Image;
import com.github.lany192.rxpicker.bean.ImageFolder;
import com.github.lany192.rxpicker.preview.PreviewActivity;
import com.github.lany192.rxpicker.utils.CameraHelper;
import com.github.lany192.rxpicker.utils.DensityUtil;
import com.github.lany192.rxpicker.utils.PickerConfig;
import com.github.lany192.rxpicker.utils.RxBus;
import com.github.lany192.rxpicker.utils.RxPickerManager;
import com.github.lany192.rxpicker.utils.T;
import com.github.lany192.rxpicker.widget.GridDivider;
import com.github.lany192.rxpicker.widget.PopWindowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


public class PickerFragment extends AbstractFragment<PickerFragmentPresenter>
        implements PickerFragmentContract.View {

    public static final int DEFAULT_SPAN_COUNT = 3;
    public static final int CAMERA_REQUEST = 0x001;
    public static final String MEDIA_RESULT = "MEDIA_RESULT";
    private static final int CAMERA_PERMISSION = 0x002;
    private TextView title;
    private RecyclerView recyclerView;
    private ImageView ivSelectPreview;
    private TextView tvSelectOk;
    private RelativeLayout rlBottom;

    private PickerFragmentAdapter adapter;
    private List<ImageFolder> allFolder;

    private PickerConfig config;
    private Disposable folderClicksubscribe;
    private Disposable imageItemsubscribe;

    public static PickerFragment newInstance() {
        return new PickerFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.rx_picker_fragment_picker;
    }

    @Override
    protected void init(View view) {
        initView(view);
        initToolbar(view);
        initRecycler();
        initEvents();
        initData();
    }

    private void initView(View view) {
        config = RxPickerManager.getInstance().getConfig();
        recyclerView = view.findViewById(R.id.recyclerView);
        title = view.findViewById(R.id.title);
        ivSelectPreview = view.findViewById(R.id.iv_select_preview);
        tvSelectOk = view.findViewById(R.id.iv_select_ok);
        rlBottom = view.findViewById(R.id.rl_bottom);
        rlBottom.setVisibility(config.isSingle() ? View.GONE : View.VISIBLE);
        ivSelectPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previewImage();
            }
        });
        tvSelectOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectSuccess();
            }
        });
    }

    private void selectSuccess() {
        handleResult(adapter.getCheckImage());
    }

    private void previewImage() {
        ArrayList<Image> checkImage = adapter.getCheckImage();
        if (checkImage.isEmpty()) {
            T.show(getContext(), getString(R.string.rx_picker_select_one_image));
            return;
        }
        PreviewActivity.start(getActivity(), checkImage);
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

    private void initEvents() {
        folderClicksubscribe = RxBus.singleton()
                .toObservable(FolderClickEvent.class)
                .subscribe(new Consumer<FolderClickEvent>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull FolderClickEvent folderClickEvent)
                            throws Exception {
                        String folderName = folderClickEvent.getFolder().getName();
                        title.setText(folderName);
                        setupData(allFolder.get(folderClickEvent.getPosition()));
                    }
                });

        imageItemsubscribe =
                RxBus.singleton().toObservable(Image.class).subscribe(new Consumer<Image>() {
                    @Override
                    public void accept(@io.reactivex.annotations.NonNull Image imageItem)
                            throws Exception {
                        ArrayList<Image> data = new ArrayList<>();
                        data.add(imageItem);
                        handleResult(data);
                    }
                });
    }

    private void initData() {
        presenter.loadAllImage(getContext());
    }

    private void setupData(ImageFolder folder) {
        adapter.setData(folder.getImages());
        adapter.notifyDataSetChanged();
    }

    private void initPopWindow(List<ImageFolder> data) {
        new PopWindowManager().init(title, data);
    }

    private void initRecycler() {
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), DEFAULT_SPAN_COUNT);
        recyclerView.setLayoutManager(layoutManager);

        final GridDivider decoration = new GridDivider(getContext());
        Drawable divider = decoration.getDivider();
        int imageWidth = DensityUtil.getDeviceWidth(getActivity()) / DEFAULT_SPAN_COUNT
                + divider.getIntrinsicWidth() * DEFAULT_SPAN_COUNT - 1;

        adapter = new PickerFragmentAdapter(imageWidth);
        adapter.setCameraClickListener(new CameraClickListener());
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                tvSelectOk.setText(getString(R.string.rx_picker_select_confim, adapter.getCheckImage().size(),
                        config.getMaxSize()));
            }
        });

        tvSelectOk.setText(
                getString(R.string.rx_picker_select_confim, adapter.getCheckImage().size(), config.getMaxSize()));
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
        Image item =
                new Image(0, file.getAbsolutePath(), file.getName(), System.currentTimeMillis());
        allImageFolder.getImages().add(0, item);
        RxBus.singleton().post(new FolderClickEvent(0, allImageFolder));
    }

    private void handleResult(ArrayList<Image> data) {
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

    @TargetApi(23)
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else {
            takePictures();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePictures();
            } else {
                T.show(getContext(), getString(R.string.rx_picker_permissions_error));
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
