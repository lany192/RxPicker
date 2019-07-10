package com.github.lany192.rxpicker.widget;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.github.lany192.rxpicker.R;
import com.github.lany192.rxpicker.bean.ImageFolder;
import com.github.lany192.rxpicker.adapter.FolderAdapter;
import com.github.lany192.rxpicker.utils.DensityUtil;

import java.util.List;

public class PopWindowManager {
    private PopupWindow popupWindow;
    private FolderAdapter folderAdapter;

    public void init(final TextView title, final List<ImageFolder> data) {
        folderAdapter = new FolderAdapter(data, DensityUtil.dp2px(title.getContext(), 80));
        folderAdapter.setDismissListener(v -> dismissAlbumWindow());
        title.setOnClickListener(v -> showPopWindow(v, data, folderAdapter));
    }

    private void showPopWindow(View v, List<ImageFolder> data, FolderAdapter albumAdapter) {
        if (popupWindow == null) {
            int height = DensityUtil.dp2px(v.getContext(), 300);
            View windowView = createWindowView(v, albumAdapter);
            popupWindow = new PopupWindow(windowView, ViewGroup.LayoutParams.MATCH_PARENT, height, true);
            popupWindow.setAnimationStyle(R.style.RxPicker_PopupAnimation);
            popupWindow.setContentView(windowView);
            popupWindow.setOutsideTouchable(true);
        }
        popupWindow.showAsDropDown(v, 0, 0);
    }

    @NonNull
    private View createWindowView(View clickView, FolderAdapter albumAdapter) {
        View view = LayoutInflater.from(clickView.getContext()).inflate(R.layout.rx_picker_item_popwindow_album, null);
        RecyclerView recyclerView = view.findViewById(R.id.album_recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false));
        View albumShadowLayout = view.findViewById(R.id.album_shadow);
        albumShadowLayout.setOnClickListener(v -> dismissAlbumWindow());
        recyclerView.setAdapter(albumAdapter);
        return view;
    }

    private void dismissAlbumWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }
}
