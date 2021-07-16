package com.github.lany192.rxpicker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lany192.rxpicker.R;
import com.github.lany192.rxpicker.RxPicker;
import com.github.lany192.rxpicker.bean.ImageItem;
import com.github.lany192.rxpicker.widget.TouchImageView;

import java.util.List;

/**
 * @author Administrator
 */
public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ViewHolder> {
    private final List<ImageItem> items;

    public PreviewAdapter(List<ImageItem> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RxPicker.of().display(holder.imageView, items.get(position).getPath());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TouchImageView imageView;

        public ViewHolder(View view) {
            super(view);
            this.imageView = view.findViewById(R.id.item_preview_image);
        }
    }

}
