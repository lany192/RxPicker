package com.caimuhao.rxpicker.widget;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.caimuhao.rxpicker.R;
import com.caimuhao.rxpicker.bean.ImageFolder;
import com.caimuhao.rxpicker.ui.adapter.FolderAdapter;

import java.util.List;


public class PopWindowManager {
    private PopupWindow mPopupWindow;
    private FolderAdapter mAdapter;

    public void init(final TextView title, final List<ImageFolder> data) {
        mAdapter = new FolderAdapter(data, dp2px(80));
        mAdapter.setDismissListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAlbumWindow();
            }
        });

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopWindow(v, data, mAdapter);
            }
        });
    }

    private void showPopWindow(View v, List<ImageFolder> data, FolderAdapter mAdapter) {
        if (mPopupWindow == null) {
            int height = dp2px(300);
            View windowView = createWindowView(v, mAdapter);
            mPopupWindow = new PopupWindow(windowView, ViewGroup.LayoutParams.MATCH_PARENT, height, true);
            mPopupWindow.setAnimationStyle(R.style.RxPicker_PopupAnimation);
            mPopupWindow.setContentView(windowView);
            mPopupWindow.setOutsideTouchable(true);
        }
        mPopupWindow.showAsDropDown(v, 0, 0);
    }

    private int dp2px(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, Resources.getSystem().getDisplayMetrics());
    }

    @NonNull
    private View createWindowView(View clickView, FolderAdapter mAdapter) {
        View view = LayoutInflater.from(clickView.getContext()).inflate(R.layout.item_popwindow_album, null);
        RecyclerView recyclerView = view.findViewById(R.id.album_recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), RecyclerView.VERTICAL, false));
        View albumShadowLayout = view.findViewById(R.id.album_shadow);
        albumShadowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAlbumWindow();
            }
        });
        recyclerView.setAdapter(mAdapter);
        return view;
    }

    private void dismissAlbumWindow() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }
}
