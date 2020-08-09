package com.src.uscan.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;


import com.src.uscan.R;
import com.src.uscan.utils.ImageFilePath;
import com.src.uscan.utils.OnCameraAndStorageGrantedListener;
import com.src.uscan.utils.PermissionUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.yalantis.ucrop.UCrop.REQUEST_CROP;

public class GetPhotoActivity extends AppCompatActivity implements OnCameraAndStorageGrantedListener {

    private static final int CAMERA_REQUEST = 222;
    private static final int STORAGE_REQUEST = 111;
    private static final int VIEW_IMAGE_REQUEST = 321;
    private static final int CAMERA_CODE = 2344;
    private static final int GALLERY_CODE = 2444;
    private static final int GALLERY_PREVIEW_CODE = 2446;
    private static final int CROPPING_CODE = 2345;
    private static final int CREATE_FILE_CODE = 1111;
    private PermissionUtils permissionUtils;
    private File outPutFile;
    private Uri mImageCaptureUri;
    private String imageUrl = "", previousScreen = "", openCamera = "";
    private int imageCroppingAppsListSize = 0;
    private boolean isCameraClicked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_photo);

        if (getIntent().hasExtra("imageUrl")) {
            imageUrl = getIntent().getStringExtra("imageUrl");
        } else {
            imageUrl = "";
        }

        if (getIntent().hasExtra("previousScreen")) {
            previousScreen = getIntent().getStringExtra("previousScreen");
        } else {
            previousScreen = "";
        }

        if (getIntent().hasExtra("openCamera")) {
            openCamera = getIntent().getStringExtra("openCamera");
        } else {
            openCamera = "";
        }

        if (getIntent().hasExtra("Gallery")) {
            isCameraClicked = false;
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, GALLERY_CODE);
        }

        if (getIntent().hasExtra("GalleryPreview")) {
            isCameraClicked = false;
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, GALLERY_PREVIEW_CODE);
        }


        try {
            outPutFile = createImageFile(GetPhotoActivity.this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        permissionUtils = new PermissionUtils(GetPhotoActivity.this);
        permissionUtils.setListener(GetPhotoActivity.this);
        permissionUtils.checkPermissions();

        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setType("image/*");
            List<ResolveInfo> list = this.getPackageManager().queryIntentActivities(intent, 0);
            imageCroppingAppsListSize = list.size();
        } catch (Exception e) {
            e.printStackTrace();
            imageCroppingAppsListSize = 0;
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST:
            case CAMERA_REQUEST:
                permissionUtils.verifyResults(requestCode, grantResults);
                break;
        }
    }

    @Override
    public void onPermissionsGranted() {

        if (!openCamera.equalsIgnoreCase(""))
            OpenCamera();
        else if (!getIntent().hasExtra("Gallery") && !getIntent().hasExtra("GalleryPreview")) {
            selectImageOption();
        }
    }

    @Override
    public void onPermissionRefused(String whichOne) {
        Toast.makeText(this, whichOne, Toast.LENGTH_SHORT).show();
        GetPhotoActivity.this.finish();
    }


    private void selectImageOption() {

        CharSequence[] items;

        if (imageUrl.isEmpty() && previousScreen.isEmpty()) {
            items = new CharSequence[]{getString(R.string.capture_photo), getString(R.string.choose_from_gallery), getString(R.string.cancel)};
        } else {
            items = new CharSequence[]{getString(R.string.capture_photo), getString(R.string.choose_from_gallery), getString(R.string.view_image), getString(R.string.cancel)};
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("");
        builder.setItems(items, (dialog, item) -> {

            if (items[item].equals(getString(R.string.capture_photo))) {

                isCameraClicked = true;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null && outPutFile != null) {
                    mImageCaptureUri = FileProvider.getUriForFile(GetPhotoActivity.this, getPackageName() + ".provider", outPutFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    startActivityForResult(takePictureIntent, CAMERA_CODE);
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                    GetPhotoActivity.this.finish();
                }

            } else if (items[item].equals(getString(R.string.choose_from_gallery))) {

                isCameraClicked = false;
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GALLERY_CODE);

            } else if (items[item].equals(getString(R.string.view_image))) {
                dialog.dismiss();

                if (!previousScreen.isEmpty() || imageUrl != null && !imageUrl.isEmpty()) {

//                    Intent intent = new Intent(GetPhotoActivity.this, ImageFullScreenActivity.class);

//                    if (imageUrl != null && !imageUrl.isEmpty()) {
//                        intent.putExtra("imageUrl", imageUrl);
//                    }
//
//                    if (!previousScreen.isEmpty()) {
//                        intent.putExtra("previousScreen", previousScreen);
//                    }
//
//                    /*if (Utils.hasLollipop()) {
//                        ActivityOptionsCompat options = ActivityOptionsCompat.
//                                makeSceneTransitionAnimation(GetPhotoActivity.this, ivGuardPic, "profile");
//                        startActivity(intent, options.toBundle());
//                    } else {*/
//                    startActivityForResult(intent, VIEW_IMAGE_REQUEST);
//                    }
                }

            } else if (items[item].equals(getString(R.string.cancel))) {
                dialog.dismiss();
                GetPhotoActivity.this.finish();
            }
        });

        builder.show();
    }

    private void OpenCamera() {

        isCameraClicked = true;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null && outPutFile != null) {

            mImageCaptureUri = FileProvider.getUriForFile(GetPhotoActivity.this, getPackageName() + ".provider", outPutFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            startActivityForResult(takePictureIntent, CAMERA_CODE);
        } else {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            GetPhotoActivity.this.finish();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case CAMERA_CODE:


                    System.out.println("Camera Image URI : " + mImageCaptureUri);


                    if (imageCroppingAppsListSize > 0) {
                        CroppingIMG();
                    } else {
                        checkImageSizeAndSend(outPutFile, CAMERA_CODE);

                    }

                    break;

                case GALLERY_PREVIEW_CODE:

                    mImageCaptureUri = data.getData();
                    System.out.println("Gallery Image URI : " + mImageCaptureUri);

                    checkImageSizeAndSend(new File(ImageFilePath.getPath(GetPhotoActivity.this, data.getData())), GALLERY_PREVIEW_CODE);
                    break;



                case GALLERY_CODE:

                    mImageCaptureUri = data.getData();
                    System.out.println("Gallery Image URI : " + mImageCaptureUri);


//                    if (imageCroppingAppsListSize > 0) {
//                        CroppingIMG();
//                    } else {

                    checkImageSizeAndSend(new File(ImageFilePath.getPath(GetPhotoActivity.this, data.getData())), GALLERY_CODE);

//                    ImageFilePath.getPath(GetPhotoActivity.this, data.getData());

//                    }

                    break;




                case CROPPING_CODE:

                    try {
                        if (outPutFile.exists()) {
                            checkImageSizeAndSend(outPutFile, CROPPING_CODE);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                            GetPhotoActivity.this.finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                        GetPhotoActivity.this.finish();
                    }

                    break;

                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:

                    CropImage.ActivityResult result = CropImage.getActivityResult(data);

                    Uri imageURI = result.getUri();

                    createFileFromImageUri(imageURI);

                    break;

                case VIEW_IMAGE_REQUEST:
                    GetPhotoActivity.this.finish();
                    break;

                case REQUEST_CROP:
                    final Uri resultUri = UCrop.getOutput(data);

                    outPutFile = new File(resultUri.getPath());
                    try {
                        if (outPutFile.exists()) {
                            checkImageSizeAndSend(outPutFile, REQUEST_CROP);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                            GetPhotoActivity.this.finish();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                        GetPhotoActivity.this.finish();
                    }

                    break;
            }


        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            cropError.printStackTrace();

        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

            try {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();

                Toast.makeText(this, "CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE(TRY)", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
//                CommonMethods.displayToast(GetPhotoActivity.this, getResources().getString(R.string.operation_cancelled),true);
                Toast.makeText(this, "CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE", Toast.LENGTH_SHORT).show();

            }
            GetPhotoActivity.this.finish();
        } else {
//            CommonMethods.displayToast(GetPhotoActivity.this, getResources().getString(R.string.operation_cancelled),true);

            Toast.makeText(this, String.valueOf(resultCode), Toast.LENGTH_SHORT).show();
            GetPhotoActivity.this.finish();
        }
    }

    private void CroppingIMG() {


        if (isCameraClicked)
            UCrop.of(Uri.fromFile(outPutFile), Uri.fromFile(outPutFile))
                    .withAspectRatio(8, 8)
                    .withMaxResultSize(512, 512)
                    .start(this);
        else
            UCrop.of(mImageCaptureUri, Uri.fromFile(outPutFile))
                    .withAspectRatio(8, 8)
                    .withMaxResultSize(512, 512)
                    .start(this);
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = Objects.requireNonNull(cursor).getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(projection[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }


    private void createFileFromImageUri(Uri imageURI) {
        try {

            File imageFile = createImageFile(GetPhotoActivity.this);

            FileInputStream inStream = new FileInputStream(new File(imageURI.getPath()));
            FileOutputStream outStream = new FileOutputStream(imageFile);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();

            if (imageFile.exists()) {

                checkImageSizeAndSend(imageFile, CREATE_FILE_CODE);
            } else {
                Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                GetPhotoActivity.this.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
            GetPhotoActivity.this.finish();
        }
    }

    private void checkImageSizeAndSend(File file, int code) {

        if (file.exists()) {

            long fileSize = (file.length() / 1024);


            if (fileSize > 3000) {
//                                CommonMethods.compressImage(CommonMethods.getBitmapFromFile(outPutFile), outPutFile);

                file = compressImageFile(file, GetPhotoActivity.this);

                if (file != null) {
                    fileSize = (file.length() / 1024);


                } else {
                    Toast.makeText(getApplicationContext(), "Error while saving", Toast.LENGTH_SHORT).show();
                    GetPhotoActivity.this.finish();
                }
            }

            if (fileSize < 3000) {

                Intent intent1 = new Intent();
                intent1.putExtra("filePath", file.getAbsolutePath());
                intent1.putExtra("Code", code);
                setResult(RESULT_OK, intent1);
                GetPhotoActivity.this.finish();

            } else {
                Toast.makeText(getApplicationContext(), "Can't upload more than 3mb", Toast.LENGTH_SHORT).show();
                GetPhotoActivity.this.finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Can't upload more than 3mb", Toast.LENGTH_SHORT).show();
            GetPhotoActivity.this.finish();
        }

    }

    /**
     * Add image to the gallery
     *
     * @param mCurrentPhotoPath path of the clicked image
     */
    private void galleryAddPic(String mCurrentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public static File compressImageFile(File f, Context context) {
        Bitmap b;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        int IMAGE_MAX_SIZE = 1024;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Log.d("CommonMethod", "Width :" + b.getWidth() + " Height :" + b.getHeight());

        File destFile;
        try {

            destFile = createImageFile(context);

            FileOutputStream out = new FileOutputStream(destFile);
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return destFile;
    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    /**
     * Decode a scaled image
     * <p>Reduce the amount of dynamic heap used by expanding the JPEG into a memory array that's already scaled to match the size of the destination view</p>
     *
     * @param mImageView        destination view
     * @param mCurrentPhotoPath path of the image
     * @return scaled bitmap
     */
    public static Bitmap getScaledBitmap(ImageView mImageView, String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
    }

}


