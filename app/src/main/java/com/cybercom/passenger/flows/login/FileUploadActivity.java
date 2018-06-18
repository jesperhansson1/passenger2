package com.cybercom.passenger.flows.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.cybercom.passenger.R;
import com.cybercom.passenger.flows.payment.StripeAsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.File;

import timber.log.Timber;

import static android.os.Build.VERSION_CODES.M;
import static com.cybercom.passenger.flows.accounts.CardFragment.FILE_ID;
import static com.cybercom.passenger.flows.accounts.CardFragment.FILE_UPLOAD_ACTIVITY;
import static com.cybercom.passenger.flows.accounts.CardFragment.TOKEN_ID;
import static com.cybercom.passenger.flows.payment.PaymentConstants.FILE_UPLOAD;
import static com.cybercom.passenger.flows.payment.PaymentConstants.SPLIT_CHAR;
import static com.cybercom.passenger.flows.payment.PaymentHelper.createUploadFileHashMap;

public class FileUploadActivity extends AppCompatActivity implements StripeAsyncTask.StripeAsyncTaskDelegate {
    private Uri mImageCaptureUri;
    private static final int CAMERA_PIC_REQUEST = 1337;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    String mTokenId;

    ImageView mImageViewFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null)
        {
            mTokenId = savedInstanceState.getString(TOKEN_ID);
        }

        setContentView(R.layout.activity_file_upload);
        FloatingActionButton cameraAction = (FloatingActionButton) findViewById(R.id.fab);
        cameraAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkPermissions(FileUploadActivity.this);
            }
        });
    }

    private void checkPermissions(Activity activity) {
        PackageManager mPackageManager = activity.getPackageManager();
        int hasPermStorage = mPackageManager.checkPermission(Manifest.permission.CAMERA,
                activity.getPackageName());
        if (hasPermStorage != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            }
        } else {
            openMediaSelector(FileUploadActivity.this);
        }
    }

    public void openMediaSelector(Activity activity)
    {
        Intent cameraIntent =
                new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 2 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED ||
                    grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                Timber.d("permission not granted");
            }
            else {
                Timber.d("permission granted");
                openMediaSelector(FileUploadActivity.this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            mImageViewFile = (ImageView) findViewById(R.id.imageView_activityfileupload_imagefile);
            mImageViewFile.setImageBitmap(thumbnail);

            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            Uri tempUri = getImageUri(getApplicationContext(), thumbnail);

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            File finalFile = new File(getRealPathFromURI(tempUri));

            Timber.d("file path is " + finalFile.getAbsolutePath() + " file exists  " + finalFile.exists());
            new StripeAsyncTask(createUploadFileHashMap(finalFile), this, FILE_UPLOAD).execute();
            }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        try
        {
            @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
        catch(Exception e)
        {
            Timber.d("error in getting real file path %s", e.getMessage());
            return null;
        }

    }

    public void onFileUploaded(String fileUploadId) {
        Intent intent=new Intent();
        intent.putExtra(FILE_ID,fileUploadId);
        intent.putExtra(TOKEN_ID,mTokenId);
        setResult(FILE_UPLOAD_ACTIVITY,intent);
        Timber.d("file uploaded successfully %s", fileUploadId);
        finish();//finishing activity
    }

    @Override
    public void onStripeTaskCompleted(String result) {
        Timber.d("result is %s", result);
        String[] value = result.split(SPLIT_CHAR);
        switch (value[1]){
            case FILE_UPLOAD:
                onFileUploaded(value[0]);
                break;
            default:
                break;
        }

    }
}
