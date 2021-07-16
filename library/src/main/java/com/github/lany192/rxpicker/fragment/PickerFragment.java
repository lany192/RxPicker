package com.github.lany192.rxpicker.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lany192.rxpicker.R;
import com.github.lany192.rxpicker.RxPicker;
import com.github.lany192.rxpicker.activity.PreviewActivity;
import com.github.lany192.rxpicker.adapter.PickerAdapter;
import com.github.lany192.rxpicker.bean.FolderClickEvent;
import com.github.lany192.rxpicker.bean.ImageFolder;
import com.github.lany192.rxpicker.bean.ImageItem;
import com.github.lany192.rxpicker.permission.RxPermissions;
import com.github.lany192.rxpicker.utils.CameraHelper;
import com.github.lany192.rxpicker.utils.RxBus;
import com.github.lany192.rxpicker.widget.PopWindowManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Administrator
 */
public class PickerFragment extends Fragment {
    public static final int DEFAULT_SPAN_COUNT = 3;
    public static final int CAMERA_REQUEST = 0x001;
    public static final String MEDIA_RESULT = "MEDIA_RESULT";
    private TextView title;
    private RecyclerView recyclerView;
    private ImageView ivSelectPreview;
    private TextView tvSelectOk;
    private RelativeLayout rlBottom;

    private PickerAdapter adapter;
    private List<ImageFolder> allFolder;

    private Disposable folderClicksubscribe;
    private Disposable imageItemsubscribe;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picker, container, false);
        initView(view);
        return view;
    }

    protected void initView(View view) {
        view.findViewById(R.id.picker_back_button).setOnClickListener(view1 -> requireActivity().finish());

        recyclerView = view.findViewById(R.id.recyclerView);
        title = view.findViewById(R.id.title);
        ivSelectPreview = view.findViewById(R.id.iv_select_preview);
        ivSelectPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<ImageItem> checkImage = adapter.getCheckImage();
                if (checkImage.isEmpty()) {
                    Toast.makeText(getActivity(), R.string.select_one_image, Toast.LENGTH_SHORT).show();
                    return;
                }
                PreviewActivity.start(getActivity(), checkImage);
            }
        });
        tvSelectOk = view.findViewById(R.id.iv_select_ok);
        tvSelectOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int minValue = RxPicker.of().getMinValue();
                ArrayList<ImageItem> checkImage = adapter.getCheckImage();
                if (checkImage.size() < minValue) {
                    Toast.makeText(getActivity(), getString(R.string.min_image, minValue), Toast.LENGTH_SHORT).show();
                    return;
                }

                handleResult(checkImage);
            }
        });
        rlBottom = view.findViewById(R.id.rl_bottom);
        rlBottom.setVisibility(RxPicker.of().isSingle() ? View.GONE : View.VISIBLE);
        initRecycler();
        initObservable();
        loadData();
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
        loadAllImage(getContext());
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

    private void takePictures() {
        CameraHelper.take(PickerFragment.this, CAMERA_REQUEST);
    }

    private class CameraClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Disposable disposable = new RxPermissions(PickerFragment.this)
                    .request(Manifest.permission.CAMERA)
                    .subscribe(granted -> {
                        if (granted) {
                            takePictures();
                        } else {
                            Toast.makeText(getActivity(), R.string.permissions_error, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    /**
     * Media attribute.
     */
    private static final String[] STORE_IMAGES = {
            MediaStore.Images.Media._ID, // image id.
            MediaStore.Images.Media.DATA, // image absolute path.
            MediaStore.Images.Media.DISPLAY_NAME, // image name.
            MediaStore.Images.Media.DATE_ADDED, // The time to be added to the library.
            MediaStore.Images.Media.BUCKET_ID, // folder id.
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME // folder name.
    };

    /**
     * Scan the list of pictures in the library.
     */
    private Observable<List<ImageFolder>> loadAllFolder(final Context context) {
        return Observable.just(true).map(new Function<Boolean, List<ImageFolder>>() {
            @Override
            public List<ImageFolder> apply(@NonNull Boolean aBoolean) throws Exception {

                Cursor cursor = MediaStore.Images.Media.query(context.getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, STORE_IMAGES);
                Map<String, ImageFolder> albumFolderMap = new HashMap<>();

                ImageFolder allImageImageFolder = new ImageFolder();
                allImageImageFolder.setChecked(true);
                allImageImageFolder.setName(context.getString(R.string.all_phone_album));

                while (cursor.moveToNext()) {
                    int imageId = cursor.getInt(0);
                    String imagePath = cursor.getString(1);
                    String imageName = cursor.getString(2);
                    long addTime = cursor.getLong(3);

                    int bucketId = cursor.getInt(4);
                    String bucketName = cursor.getString(5);

                    ImageItem ImageItem = new ImageItem(imageId, imagePath, imageName, addTime);
                    allImageImageFolder.addPhoto(ImageItem);

                    ImageFolder imageFolder = albumFolderMap.get(bucketName);
                    if (imageFolder != null) {
                        imageFolder.addPhoto(ImageItem);
                    } else {
                        imageFolder = new ImageFolder(bucketId, bucketName);
                        imageFolder.addPhoto(ImageItem);

                        albumFolderMap.put(bucketName, imageFolder);
                    }
                }

                cursor.close();
                List<ImageFolder> imageFolders = new ArrayList<>();

                Collections.sort(allImageImageFolder.getImages());
                imageFolders.add(allImageImageFolder);

                for (Map.Entry<String, ImageFolder> folderEntry : albumFolderMap.entrySet()) {
                    ImageFolder imageFolder = folderEntry.getValue();
                    Collections.sort(imageFolder.getImages());
                    imageFolders.add(imageFolder);
                }
                return imageFolders;
            }
        });
    }

    public void loadAllImage(final Context context) {
        loadAllFolder(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
//                        view.showWaitDialog();
                    }
                })
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
//                        view.hideWaitDialog();
                    }
                })
                .subscribe(new Consumer<List<ImageFolder>>() {
                    @Override
                    public void accept(@NonNull List<ImageFolder> imageFolders) throws Exception {
                        showAllImage(imageFolders);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Toast.makeText(context, R.string.load_image_error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
