package com.github.lany192.rxpicker.activity;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.github.lany192.rxpicker.R;
import com.github.lany192.rxpicker.fragment.PickerFragment;
import com.github.lany192.rxpicker.permission.RxPermissions;

import io.reactivex.disposables.Disposable;


/**
 * @author Administrator
 */
public class PickerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
        Disposable disposable = new RxPermissions(this)
                .request(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        String tag = PickerFragment.class.getSimpleName();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        Fragment fragment = fragmentManager.findFragmentByTag(tag);
                        if (fragment == null) {
                            fragment = new PickerFragment();
                        }
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fl_container, fragment, tag);
                        fragmentTransaction.commitAllowingStateLoss();
                    } else {
                        Toast.makeText(PickerActivity.this, R.string.permissions_error, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
}
