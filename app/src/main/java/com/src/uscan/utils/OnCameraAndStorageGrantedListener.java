package com.src.uscan.utils;

/**
 * Created by ngoyal on 10/21/2016.
 */

public interface OnCameraAndStorageGrantedListener {
    void onPermissionsGranted();

    void onPermissionRefused(String whichOne);
}
