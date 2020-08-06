package com.src.uscan.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



/**
 * Created by ngoyal on 10/21/2016.
 */

public class PermissionUtils {
    private static final int CAMERA_REQUEST = 222;
    private static final int STORAGE_REQUEST = 111;
    private AppCompatActivity activity;
    private OnCameraAndStorageGrantedListener listener;

    public PermissionUtils(AppCompatActivity context) {
        activity = context;
    }

    public void checkPermissions() {
        if (Build.VERSION.SDK_INT > 22) {
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.CAMERA)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

//                    CommonMethods.displayToast(activity, activity.getResources().getString(R.string.camera_permission_required), true);

                    Toast.makeText(activity, "Camera PErmission Required", Toast.LENGTH_SHORT).show();

                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_REQUEST);

                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_REQUEST);
                }
            } else {

                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                        Toast.makeText(activity, "Store permission Required", Toast.LENGTH_SHORT).show();


                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE},
                                STORAGE_REQUEST);
                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                STORAGE_REQUEST);
                    }
                } else {
                    listener.onPermissionsGranted();
                }
            }
        } else {
            listener.onPermissionsGranted();
        }
    }

    public void setListener(OnCameraAndStorageGrantedListener listener) {
        this.listener = listener;
    }

    public boolean checkWriteExternalPermission()
    {
        String permission = android.Manifest.permission.CAMERA;
        int res = activity.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public void verifyResults(int requestCode, int[] grantResults) {
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                listener.onPermissionRefused("Camera Permission Denied");
            } else {
                checkPermissions();
            }
        } else if (requestCode == STORAGE_REQUEST) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                listener.onPermissionRefused("Storage Permission Denied");
            } else {
                listener.onPermissionsGranted();
            }
        }
    }
}
