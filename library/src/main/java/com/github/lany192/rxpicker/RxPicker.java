package com.github.lany192.rxpicker;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.github.lany192.rxpicker.activity.PickerActivity;
import com.github.lany192.rxpicker.bean.ImageItem;
import com.github.lany192.rxpicker.fragment.PickerFragment;
import com.github.lany192.rxpicker.fragment.ResultFragment;
import com.github.lany192.rxpicker.utils.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

public class RxPicker {
    private volatile static RxPicker instance = null;
    private int minValue = 1;
    private int maxValue = 9;
    private boolean showCamera = true;
    private boolean isSingle = true;
    private List<String> selectedPath = new ArrayList<>();
    private ImageLoader loader;

    private RxPicker() {
    }

    public static RxPicker of() {
        if (instance == null) {
            synchronized (RxPicker.class) {
                if (instance == null) {
                    instance = new RxPicker();
                }
            }
        }
        return instance;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public void display(ImageView imageView, String path) {
        if (loader == null) {
            throw new NullPointerException("You must fist of all call 'RxPicker.init()' to initialize");
        }
        loader.display(imageView, path);
    }

    public List<ImageItem> getResult(Intent intent) {
        return (List<ImageItem>) intent.getSerializableExtra(PickerFragment.MEDIA_RESULT);
    }

    /**
     * init RxPicker
     */
    public void init(ImageLoader loader) {
        this.loader = loader;
    }


    /**
     * Set the selection mode
     */
    public RxPicker single(boolean single) {
        this.isSingle = single;
        return this;
    }

    /**
     * Set the show  Taking pictures;
     */
    public RxPicker camera(boolean showCamera) {
        this.showCamera = showCamera;
        return this;
    }

    /**
     * Set the select  image limit
     */
    public RxPicker limit(int minValue, int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        return this;
    }

    /**
     * 初始化已经选择的图片
     */
    public RxPicker selected(List<String> paths) {
        this.selectedPath = paths;
        return this;
    }

    public boolean isInitSelected(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        return selectedPath.contains(path);
    }

    public void removeInitSelected(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        selectedPath.remove(path);
    }

    /**
     * start picker from activity
     */
    public Observable<List<ImageItem>> start(FragmentActivity activity) {
        return start(activity.getSupportFragmentManager());
    }

    /**
     * start picker from fragment
     */
    public Observable<List<ImageItem>> start(Fragment fragment) {
        return start(fragment.getParentFragmentManager());
    }

    /**
     * start picker from fragment
     */
    private Observable<List<ImageItem>> start(FragmentManager fragmentManager) {
        ResultFragment fragment = (ResultFragment) fragmentManager.findFragmentByTag(ResultFragment.class.getSimpleName());
        if (fragment == null) {
            fragment = ResultFragment.newInstance();
            fragmentManager.beginTransaction().add(fragment, fragment.getClass().getSimpleName()).commit();
        } else if (fragment.isDetached()) {
            fragmentManager.beginTransaction().attach(fragment).commit();
        }
        return getListItem(fragment);
    }

    private Observable<List<ImageItem>> getListItem(final ResultFragment finalFragment) {
        return finalFragment.getAttachSubject()
                .flatMap(new Function<Boolean, ObservableSource<List<ImageItem>>>() {
                    @Override
                    public ObservableSource<List<ImageItem>> apply(@NonNull Boolean aBoolean) throws Exception {
                        Intent intent = new Intent(finalFragment.getActivity(), PickerActivity.class);
                        finalFragment.startActivityForResult(intent, ResultFragment.REQUEST_CODE);
                        return finalFragment.getResultSubject();
                    }
                }).take(1);
    }
}
