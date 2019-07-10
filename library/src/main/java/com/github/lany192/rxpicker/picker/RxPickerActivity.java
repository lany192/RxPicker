package com.github.lany192.rxpicker.picker;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.github.lany192.rxpicker.R;
import com.github.lany192.rxpicker.utils.T;


public class RxPickerActivity extends AppCompatActivity {

    private static final int READ_STORAGE_PERMISSION = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rx_picker_activity_picker);
        requestPermission();
    }

    @TargetApi(16)
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RxPickerActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION);
        } else {
            setupFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupFragment();
            } else {
                T.show(RxPickerActivity.this, getString(R.string.rx_picker_permissions_error));
                finish();
            }
        }
    }

    private void setupFragment() {
        String tag = PickerFragment.class.getSimpleName();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            fragment = PickerFragment.newInstance();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_container, fragment, tag)
                .commitAllowingStateLoss();
    }
}
