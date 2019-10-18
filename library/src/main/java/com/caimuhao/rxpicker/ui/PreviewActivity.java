package com.caimuhao.rxpicker.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.caimuhao.rxpicker.R;
import com.caimuhao.rxpicker.bean.ImageItem;
import com.caimuhao.rxpicker.ui.adapter.PreviewAdapter;

import java.util.ArrayList;
import java.util.List;


public class PreviewActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private PreviewAdapter mAdapter;
    private List<ImageItem> data;
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
        data = (List<ImageItem>) getIntent().getSerializableExtra(KEY_LIST);
        setupToolbar();
        mViewPager = findViewById(R.id.vp_preview);
        mAdapter = new PreviewAdapter(data);
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mToolbar.setTitle(position + 1 + "/" + data.size());
            }
        });
    }

    private void setupToolbar() {
        mToolbar = findViewById(R.id.nav_top_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setTitle("1/" + data.size());
    }
}
