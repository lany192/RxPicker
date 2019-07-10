package com.github.lany192.rxpicker.sample;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.lany192.rxpicker.bean.Image;
import com.github.lany192.rxpicker.utils.DensityUtil;
import com.github.lany192.rxpicker.utils.RxPickerManager;

import java.util.List;


public class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.ViewHolder> {
    private List<Image> images;
    private int imageSize;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images == null ? 0 : images.size();
    }

    public void setData(List<Image> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        private ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }

        private void bind(Image imageItem) {
            imageSize = DensityUtil.getDeviceWidth(itemView.getContext()) / 3;
            RxPickerManager.getInstance().display(image, imageItem.getPath(), imageSize, imageSize);
        }
    }
}


