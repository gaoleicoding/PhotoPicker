package com.danielfu.camerademo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity  {

    ImageView ivImage;

    private static final String TAG = "CameraDemo_MainActivity";

    private static final int REQUEST_OPEN_CAMERA = 0x011;
    private static final int REQUEST_OPEN_GALLERY = 0x022;
    private static final int REQUEST_CROP_PHOTO = 0x033;
    private static final int REQUEST_PERMISSIONS = 0x044;
   ImageUtils imageUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPermission();
        initViews();
        imageUtils=new ImageUtils(this);
        imageUtils.initChoosePop();
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageUtils.popWinChoose.showAtLocation(MainActivity.this.findViewById(R.id.activity_main), Gravity.BOTTOM, 0, 0); // 在底部显示
            }
        });
    }

    /**
     * 初始化相机相关权限
     * 适配6.0+手机的运行时权限
     */
    private void initPermission() {
        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        //检查权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //判断权限是否被拒绝过
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "用户曾拒绝打开相机权限", Toast.LENGTH_SHORT).show();
            } else {
                //注册相机权限
                ActivityCompat.requestPermissions(this,
                        permissions,
                        REQUEST_PERMISSIONS);
            }
        }
    }

    private void initViews() {
        ivImage = (ImageView) findViewById(R.id.iv_user_photo);
    }


//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_first:
//                openCamera();
//                break;
//            case R.id.btn_second:
//                openGallery();
//                break;
//            case R.id.btn_third:
//                popWinChoose.dismiss();
//                break;
//        }
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //成功
                    Toast.makeText(this, "用户授权打开相机权限", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "用户拒绝打开相机权限", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        //data的返回值根据
        switch (requestCode) {
            case REQUEST_OPEN_CAMERA:
                imageUtils.addPicToGallery(imageUtils.imgPathOri);
                imageUtils.cropPhoto(imageUtils.imgUriOri);
                Log.i(TAG, "openCameraResult_imgPathOri:" + imageUtils.imgPathOri);
                Log.i(TAG, "openCameraResult_imgUriOri:" + imageUtils.imgUriOri.toString());
                break;
            case REQUEST_OPEN_GALLERY:
                if (data != null) {
                    Uri imgUriSel = data.getData();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        //打开相册会返回一个经过图像选择器安全化的Uri，直接放入裁剪程序会不识别，抛出[暂不支持此类型：华为7.0]
                        //formatUri会返回根据Uri解析出的真实路径
                        String imgPathSel = UriUtils.formatUri(this, imgUriSel);
                        //根据真实路径转成File,然后通过应用程序重新安全化，再放入裁剪程序中才可以识别
                        imageUtils.cropPhoto(FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(imgPathSel)));
                        Log.i(TAG, "Kit_sel_path:" + imgPathSel);
                        Log.i(TAG, "Kit_sel_uri:" + Uri.fromFile(new File(imgPathSel)));
                    } else {
                        imageUtils.cropPhoto(imgUriSel);
                    }
                    Log.i(TAG, "openGalleryResult_imgUriSel:" + imgUriSel);
                }
                break;
            case REQUEST_CROP_PHOTO:
                imageUtils.addPicToGallery(imageUtils.imgPathCrop);
                ImageUtils.imageViewSetPic(ivImage, imageUtils.imgPathCrop);
                revokeUriPermission(imageUtils.imgUriCrop, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                Log.i(TAG, "cropPhotoResult_imgPathCrop:" + imageUtils.imgPathCrop);
                Log.i(TAG, "cropPhotoResult_imgUriCrop:" + imageUtils.imgUriCrop.toString());
                break;
        }
    }




}
