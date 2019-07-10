package com.github.lany192.rxpicker.picker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.Fragment;

import com.github.lany192.rxpicker.bean.Image;
import com.github.lany192.rxpicker.utils.RxPickerManager;

import java.util.List;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;


public class ResultFragment extends Fragment {
    public static final int REQUEST_CODE = 0x00100;
    private PublishSubject<List<Image>> resultSubject = PublishSubject.create();
    private BehaviorSubject<Boolean> attachSubject = BehaviorSubject.create();

    public static ResultFragment newInstance() {
        return new ResultFragment();
    }

    public PublishSubject<List<Image>> getResultSubject() {
        return resultSubject;
    }

    public BehaviorSubject<Boolean> getAttachSubject() {
        return attachSubject;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && data != null) {
            resultSubject.onNext(RxPickerManager.getInstance().getResult(data));
        }
    }

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        attachSubject.onNext(true);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            attachSubject.onNext(true);
        }
    }
}
