package com.github.lany192.rxpicker.picker;

import android.content.Context;

import com.github.lany192.rxpicker.base.BasePresenter;
import com.github.lany192.rxpicker.base.BaseView;
import com.github.lany192.rxpicker.bean.ImageFolder;

import java.util.List;


public interface PickerFragmentContract {

    interface View extends BaseView {
        void showAllImage(List<ImageFolder> datas);
    }

    abstract class Presenter extends BasePresenter<View> {
        public abstract void loadAllImage(Context context);
    }
}
