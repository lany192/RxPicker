package com.github.lany192.rxpicker.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.lany192.rxpicker.utils.ClassUtils;


public abstract class AbstractFragment<P extends BasePresenter> extends Fragment
        implements BaseView {

    public P presenter;
    protected ProgressDialog waitDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        presenter = ClassUtils.getT(this, 0);
        presenter.attachModelView(this);
        init(view);
        return view;
    }

    protected abstract int getLayoutId();

    protected abstract void init(View view);

    public void showWaitDialog() {
        if (!Thread.currentThread().getName().equals("main")) {
            new Handler(Looper.getMainLooper()).post(new DialogRunnable());
        } else {
            new DialogRunnable().run();
        }
    }

    public void hideWaitDialog() {
        if (waitDialog != null) {
            waitDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    private class DialogRunnable implements Runnable {
        @Override
        public void run() {
            if (waitDialog == null) {
                waitDialog = new ProgressDialog(getActivity());
                waitDialog.setMessage("加载数据..");
            }
            waitDialog.show();
        }
    }
}
