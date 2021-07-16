package com.github.lany192.rxpicker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.github.lany192.rxpicker.R;
import com.github.lany192.rxpicker.adapter.PreviewAdapter;
import com.github.lany192.rxpicker.bean.ImageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Administrator
 */
public class PreviewActivity extends AppCompatActivity {
    private static final String KEY_LIST = "preview_list";

    public static void start(Context context, ArrayList<ImageItem> data) {
        Intent intent = new Intent(context, PreviewActivity.class);
        intent.putExtra(KEY_LIST, data);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        List<ImageItem> data = (ArrayList<ImageItem>) getIntent().getSerializableExtra(KEY_LIST);

        TextView titleTextView = findViewById(R.id.preview_title);
        findViewById(R.id.preview_back).setOnClickListener(view -> onBackPressed());

        ViewPager2 mViewPager = findViewById(R.id.vp_preview);
        mViewPager.setAdapter(new PreviewAdapter(data));
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                titleTextView.setText(String.format(Locale.getDefault(), "%d/%d", position + 1, data.size()));
            }
        });
    }
}
