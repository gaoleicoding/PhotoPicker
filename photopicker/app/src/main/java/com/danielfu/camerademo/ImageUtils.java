package com.danielfu.camerademo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by fuh on 2017/5/4.
 * Email：unableApe@gmail.com
 */

public class ImageUtils {
    //原图像 路径
    public static String imgPathOri;
    //裁剪图像 路径
    public static String imgPathCrop;
    //原图像 URI
    public Uri imgUriOri;
    //裁剪图像 URI
    public Uri imgUriCrop;
    public PopupWindow popWinChoose;
    public static final String TAG = "CameraDemo_MainActivity";
    public static final int REQUEST_OPEN_CAMERA = 0x011;
    public static final int REQUEST_OPEN_GALLERY = 0x022;
    public static final int REQUEST_CROP_PHOTO = 0x033;
    public static final int REQUEST_PERMISSIONS = 0x044;
    Activity activity;

    public ImageUtils(Activity activity) {
        this.activity = activity;
    }

    /**
     * ImageView设置优化内存使用后的Bitmap
     * 返回一个等同于ImageView宽高的bitmap
     *
     * @param view    ImageView
     * @param imgPath 图像路径
     */
    public static void imageViewSetPic(ImageView view, String imgPath) {
        // Get the dimensions of the View
        int targetW = view.getWidth();
        int targetH = view.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, bmOptions);
        view.setImageBitmap(bitmap);
    }

    /**
     * 创建原图像保存的文件
     *
     * @return
     * @throws IOException
     */
    public File createOriImageFile() throws IOException {
        String imgNameOri = "HomePic_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File pictureDirOri = new File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/OriPicture");
        if (!pictureDirOri.exists()) {
            pictureDirOri.mkdirs();
        }
        File image = File.createTempFile(
                imgNameOri,         /* prefix */
                ".jpg",             /* suffix */
                pictureDirOri       /* directory */
        );
        imgPathOri = image.getAbsolutePath();
        return image;
    }

    /**
     * 创建裁剪图像保存的文件
     *
     * @return
     * @throws IOException
     */
    public File createCropImageFile() throws IOException {
        String imgNameCrop = "HomePic_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File pictureDirCrop = new File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/CropPicture");
        if (!pictureDirCrop.exists()) {
            pictureDirCrop.mkdirs();
        }
        File image = File.createTempFile(
                imgNameCrop,         /* prefix */
                ".jpg",             /* suffix */
                pictureDirCrop      /* directory */
        );
        imgPathCrop = image.getAbsolutePath();
        return image;
    }

    /**
     * 把图像添加进系统相册
     *
     * @param imgPath 图像路径
     */
    public void addPicToGallery(String imgPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imgPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);
    }

    public void setWindowAlpha(float alpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = alpha;
        activity.getWindow().setAttributes(lp);
    }

    /**
     * 打开相机
     * 7.0中如果需要调用系统(eg:裁剪)/其他应用，必须用FileProvider提供Content Uri，并且将Uri赋予读写的权限
     */
    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 打开相机
        File oriPhotoFile = null;
        try {
            oriPhotoFile = createOriImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (oriPhotoFile != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                imgUriOri = Uri.fromFile(oriPhotoFile);
            } else {
                imgUriOri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", oriPhotoFile);
            }
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUriOri);
            activity.startActivityForResult(intent, REQUEST_OPEN_CAMERA);

            // 动态grant权限
            // 如果在xml中已经定义android:grantUriPermissions="true"
            // 则只需要intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);即可
            //            List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            //            for (ResolveInfo resolveInfo : resInfoList) {
            //                String packageName = resolveInfo.activityInfo.packageName;
            //                grantUriPermission(packageName, imgUriOri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //            }
            Log.i(TAG, "openCamera_imgPathOri:" + imgPathOri);
            Log.i(TAG, "openCamera_imgUriOri:" + imgUriOri.toString());
        }
    }


    public void openGallery() {
        Intent intent = new Intent();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
//            intent.setAction(Intent.ACTION_PICK);
        }
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_OPEN_GALLERY);
    }


    /**
     * 裁剪图片
     *
     * @param uri 需要 裁剪图像的Uri
     */
    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        File cropPhotoFile = null;
        try {
            cropPhotoFile = createCropImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (cropPhotoFile != null) {
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                imgUriCrop = Uri.fromFile(cropPhotoFile);
//            }else{
//                imgUriCrop = FileProvider.getUriForFile(this, getPackageName() + ".provider", cropPhotoFile);
//            }

            //7.0 安全机制下不允许保存裁剪后的图片
            //所以仅仅将File Uri传入MediaStore.EXTRA_OUTPUT来保存裁剪后的图像
            imgUriCrop = Uri.fromFile(cropPhotoFile);

            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", true);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 300);
            intent.putExtra("outputY", 300);
            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUriCrop);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.startActivityForResult(intent, REQUEST_CROP_PHOTO);

            Log.i(TAG, "cropPhoto_imgPathCrop:" + imgPathCrop.toString());
            Log.i(TAG, "cropPhoto_imgUriCrop:" + imgUriCrop.toString());
        }
    }

    public void initChoosePop() {
        View view = LayoutInflater.from(activity).inflate(R.layout.popwin_sel, null);
        popWinChoose = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        popWinChoose.setFocusable(true); // 设置popWindow弹出窗体可点击
        popWinChoose.setBackgroundDrawable(new ColorDrawable(activity.getResources().getColor(R.color.transparent)));
//        popWinChoose.setAnimationStyle(R.style.storeImageChooseStyle);
        popWinChoose.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setWindowAlpha(1); //Window设置为全透明
            }
        });
        TextView btnFirst = (TextView) view.findViewById(R.id.btn_first);
        TextView btnSecond = (TextView) view.findViewById(R.id.btn_second);
        TextView btnThird =(TextView) view.findViewById(R.id.btn_third);
        btnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
                popWinChoose.dismiss();
            }

        });
        btnSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
                popWinChoose.dismiss();
            }

        });
        btnThird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWinChoose.dismiss();
            }

        });

    }


}
