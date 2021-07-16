package com.github.lany192.rxpicker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.github.lany192.rxpicker.R;
import com.github.lany192.rxpicker.RxPicker;
import com.github.lany192.rxpicker.bean.ImageItem;
import com.github.lany192.rxpicker.utils.RxBus;

import java.util.ArrayList;
import java.util.List;

public class PickerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int CAMERA_TYPE = 0;
    private static final int NORMAL_TYPE = 1;

    private View.OnClickListener cameraClickListener;

    private int imageWidth;

    private List<ImageItem> datas;
    private List<ImageItem> checkedImages;

    public PickerAdapter(int imageWidth) {
        this.imageWidth = imageWidth;
        checkedImages = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (CAMERA_TYPE == viewType) {
            return new CameraViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camera, parent, false));
        } else {
            return new PickerViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picker, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CameraViewHolder) {
            holder.itemView.setOnClickListener(cameraClickListener);
            return;
        }
        int dataPosition = RxPicker.of().isShowCamera() ? position - 1 : position;

        ImageItem imageItem = datas.get(dataPosition);

        if (RxPicker.of().isInitSelected(imageItem.getPath())) {
            checkedImages.add(imageItem);
        }


        PickerViewHolder pickerViewHolder = (PickerViewHolder) holder;
        pickerViewHolder.bind(imageItem);

        pickerViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (RxPicker.of().isSingle()) {
                    RxBus.singleton().post(imageItem);
                } else {
                    int maxValue = RxPicker.of().getMaxValue();
                    if (checkedImages.size() == maxValue && !checkedImages.contains(imageItem)) {
                        Toast.makeText(holder.itemView.getContext(), holder.itemView.getContext().getString(R.string.max_select, maxValue), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (checkedImages.contains(imageItem)) {
                        checkedImages.remove(imageItem);
                        RxPicker.of().removeInitSelected(imageItem.getPath());
                    } else {
                        checkedImages.add(imageItem);
                    }
                    notifyItemChanged(holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (datas != null && RxPicker.of().isShowCamera()) {
            return datas.size() + 1;
        } else if (datas != null) {
            return datas.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (RxPicker.of().isShowCamera() && position == 0) {
            return CAMERA_TYPE;
        } else {
            return NORMAL_TYPE;
        }
    }

    public void setData(List<ImageItem> data) {
        this.datas = data;
    }

    public void setCameraClickListener(View.OnClickListener cameraClickListener) {
        this.cameraClickListener = cameraClickListener;
    }

    public ArrayList<ImageItem> getCheckImage() {
        return (ArrayList<ImageItem>) checkedImages;
    }

    private class PickerViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private AppCompatCheckBox cbCheck;

        private PickerViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_image);
            cbCheck = itemView.findViewById(R.id.cb_check);
        }

        private void bind(ImageItem imageItem) {
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.width = imageWidth;
            layoutParams.height = imageWidth;
            imageView.setLayoutParams(layoutParams);
            RxPicker.of().display(imageView, imageItem.getPath());
            cbCheck.setVisibility(RxPicker.of().isSingle() ? View.GONE : View.VISIBLE);
            cbCheck.setChecked(checkedImages.contains(imageItem));
        }
    }

    private class CameraViewHolder extends RecyclerView.ViewHolder {

        private CameraViewHolder(View itemView) {
            super(itemView);
        }
    }
}


