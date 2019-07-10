package com.github.lany192.rxpicker.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lany192.rxpicker.R;
import com.github.lany192.rxpicker.bean.FolderClickEvent;
import com.github.lany192.rxpicker.bean.ImageFolder;
import com.github.lany192.rxpicker.utils.RxBus;
import com.github.lany192.rxpicker.utils.RxPickerManager;

import java.util.List;


public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private int imageWidth;
    private List<ImageFolder> folders;
    private int checkPosition = 0;

    private View.OnClickListener dismissListener;

    public FolderAdapter(List<ImageFolder> folders, int i) {
        this.folders = folders;
        imageWidth = i;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rx_picker_item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.bind(folders.get(position));
        holder.itemView.setOnClickListener(v -> {
            dismissListener.onClick(v);
            if (checkPosition == position) return;

            ImageFolder newFolder = folders.get(position);
            ImageFolder oldFolder = folders.get(checkPosition);

            oldFolder.setChecked(false);
            newFolder.setChecked(true);
            notifyItemChanged(checkPosition);
            notifyItemChanged(position);

            checkPosition = position;

            RxBus.singleton().post(new FolderClickEvent(position, newFolder));

        });
    }

    @Override
    public int getItemCount() {
        return folders == null ? 0 : folders.size();
    }

    public void setDismissListener(View.OnClickListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private ImageView ivPreView;
        private ImageView ivCheck;

        private ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_album_name);
            ivPreView = itemView.findViewById(R.id.iv_preview);
            ivCheck = itemView.findViewById(R.id.iv_check);
        }

        private void bind(ImageFolder folder) {
            tvName.setText(folder.getName() + " (" + folder.getImages().size() + ")");
            String path = folder.getImages().get(0).getPath();
            RxPickerManager.getInstance().display(ivPreView, path, imageWidth, imageWidth);
            ivCheck.setVisibility(folder.isChecked() ? View.VISIBLE : View.GONE);
        }
    }
}
