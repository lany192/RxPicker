package com.github.lany192.rxpicker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.lany192.rxpicker.R;
import com.github.lany192.rxpicker.RxPicker;
import com.github.lany192.rxpicker.bean.FolderClickEvent;
import com.github.lany192.rxpicker.bean.ImageFolder;
import com.github.lany192.rxpicker.utils.RxBus;

import java.util.List;


public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {
    private final List<ImageFolder> datas;
    private int checkPosition = 0;
    private View.OnClickListener dismissListener;

    public FolderAdapter(List<ImageFolder> datas, int i) {
        this.datas = datas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.bind(datas.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissListener.onClick(v);
                if (checkPosition == position) {
                    return;
                }

                ImageFolder newFolder = datas.get(position);
                ImageFolder oldFolder = datas.get(checkPosition);

                oldFolder.setChecked(false);
                newFolder.setChecked(true);
                notifyItemChanged(checkPosition);
                notifyItemChanged(position);

                checkPosition = position;

                RxBus.singleton().post(new FolderClickEvent(position, newFolder));

            }
        });
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
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

        private void bind(ImageFolder imageFolder) {
            tvName.setText(imageFolder.getName());
            String path = imageFolder.getImages().get(0).getPath();
            RxPicker.of().display(ivPreView, path);
            ivCheck.setVisibility(imageFolder.isChecked() ? View.VISIBLE : View.GONE);
        }
    }
}
