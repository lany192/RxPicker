package com.caimuhao.rxpicker.ui.fragment.mvp;

import android.content.Context;

import com.caimuhao.rxpicker.base.BasePresenter;
import com.caimuhao.rxpicker.base.BaseView;
import com.caimuhao.rxpicker.bean.ImageFolder;

import java.util.List;

public interface PickerFragmentContract {

    interface View extends BaseView {
        void showAllImage(List<ImageFolder> datas);
    }

    abstract class Presenter extends BasePresenter<View> {
        public abstract void loadAllImage(Context context);
    }
}
