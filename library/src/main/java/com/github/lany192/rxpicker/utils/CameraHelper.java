package com.github.lany192.rxpicker.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CameraHelper {

    private static File takeImageFile;

    public static void take(Fragment fragment, int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (intent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
            takeImageFile = createFile(fragment.getActivity());
            Uri uri;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                String authorities = ProviderUtil.getFileProviderName(fragment.getContext());
                uri = FileProvider.getUriForFile(fragment.getActivity(), authorities, takeImageFile);
            } else {
                uri = Uri.fromFile(takeImageFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        fragment.startActivityForResult(intent, requestCode);
    }

    public static File getTakeImageFile() {
        return takeImageFile;
    }

    private static File createFile(Context context) {
        //这个路径要和rx_picker_file_path.xml中配置的匹配
        String path = context.getFilesDir() + File.separator + "images" + File.separator;
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = "RxPicker_" + dateFormat.format(new Date()) + ".jpg";
        File file = new File(path, filename);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }

    public static void scanPic(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(file);
        intent.setData(contentUri);
        context.sendBroadcast(intent);
    }
}
