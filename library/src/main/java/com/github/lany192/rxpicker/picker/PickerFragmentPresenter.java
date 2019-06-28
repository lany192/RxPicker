package com.github.lany192.rxpicker.picker;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.github.lany192.rxpicker.R;
import com.github.lany192.rxpicker.bean.ImageFolder;
import com.github.lany192.rxpicker.bean.ImageItem;
import com.github.lany192.rxpicker.utils.T;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


public class PickerFragmentPresenter extends PickerFragmentContract.Presenter {


    private static final String[] STORE_IMAGES = {
            MediaStore.Images.Media._ID,             MediaStore.Images.Media.DATA,             MediaStore.Images.Media.DISPLAY_NAME,             MediaStore.Images.Media.DATE_ADDED,             MediaStore.Images.Media.BUCKET_ID,             MediaStore.Images.Media.BUCKET_DISPLAY_NAME     };

    @Override
    public void start() {

    }


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

    @Override
    public void loadAllImage(final Context context) {
        loadAllFolder(context).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(@NonNull Disposable disposable) throws Exception {
                        view.showWaitDialog();
                    }
                })
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        view.hideWaitDialog();
                    }
                })
                .subscribe(new Consumer<List<ImageFolder>>() {
                    @Override
                    public void accept(@NonNull List<ImageFolder> imageFolders) throws Exception {
                        view.showAllImage(imageFolders);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        T.show(context, context.getString(R.string.load_image_error));
                    }
                });
    }
}
