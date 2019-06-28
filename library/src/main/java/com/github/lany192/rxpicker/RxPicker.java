package com.github.lany192.rxpicker;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.github.lany192.rxpicker.bean.ImageItem;
import com.github.lany192.rxpicker.picker.ResultHandlerFragment;
import com.github.lany192.rxpicker.picker.RxPickerActivity;
import com.github.lany192.rxpicker.utils.PickerConfig;
import com.github.lany192.rxpicker.utils.ImageLoader;
import com.github.lany192.rxpicker.utils.RxPickerManager;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public class RxPicker {
    private RxPicker(PickerConfig config) {
        RxPickerManager.getInstance().setConfig(config);
    }

    public static void init(ImageLoader imageLoader) {
        RxPickerManager.getInstance().init(imageLoader);
    }

    static RxPicker of(PickerConfig config) {
        return new RxPicker(config);
    }

    public static RxPicker of() {
        return new RxPicker(new PickerConfig());
    }

    public RxPicker single(boolean single) {
        RxPickerManager.getInstance()
                .setMode(single ? PickerConfig.SINGLE_IMG : PickerConfig.MULTIPLE_IMG);
        return this;
    }

    public RxPicker camera(boolean showCamera) {
        RxPickerManager.getInstance().showCamera(showCamera);
        return this;
    }

    public RxPicker limit(int maxValue) {
        RxPickerManager.getInstance().limit( maxValue);
        return this;
    }

    public Observable<List<ImageItem>> start(FragmentActivity activity) {
        return start(activity.getSupportFragmentManager());
    }

    public Observable<List<ImageItem>> start(Fragment fragment) {
        return start(fragment.getFragmentManager());
    }

    private Observable<List<ImageItem>> start(FragmentManager fragmentManager) {
        ResultHandlerFragment fragment = (ResultHandlerFragment) fragmentManager.findFragmentByTag(
                ResultHandlerFragment.class.getSimpleName());
        if (fragment == null) {
            fragment = ResultHandlerFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(fragment, fragment.getClass().getSimpleName())
                    .commit();
        } else if (fragment.isDetached()) {
            fragmentManager.beginTransaction().attach(fragment).commit();
        }
        return getListItem(fragment);
    }

    private Observable<List<ImageItem>> getListItem(final ResultHandlerFragment finalFragment) {
        return finalFragment.getAttachSubject().filter(new Predicate<Boolean>() {
            @Override
            public boolean test(@NonNull Boolean aBoolean) {
                return aBoolean;
            }
        }).flatMap(new Function<Boolean, ObservableSource<List<ImageItem>>>() {
            @Override
            public ObservableSource<List<ImageItem>> apply(@NonNull Boolean aBoolean) {
                Intent intent = new Intent(finalFragment.getActivity(), RxPickerActivity.class);
                finalFragment.startActivityForResult(intent, ResultHandlerFragment.REQUEST_CODE);
                return finalFragment.getResultSubject();
            }
        }).take(1);
    }
}
