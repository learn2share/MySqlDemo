package com.yuan.mysqldemo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.IOException;
import java.util.List;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class QrcodeScanActivity extends AppCompatActivity implements QRCodeView.Delegate, EasyPermissions.PermissionCallbacks {
    private static final String TAG = QrcodeScanActivity.class.getSimpleName();
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;

    private ImageView imgBack;
    private QRCodeView mQRCodeView;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);

        imgBack = findViewById(R.id.img_back);
        mQRCodeView = (ZBarView) findViewById(R.id.zbarview);
        mQRCodeView.setDelegate(this);

        mQRCodeView.startSpot();
        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != 2) {
            this.playBeep = false;
        }
        initBeepSound();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        requestCodeQRCodePermissions();

    }

    @Override
    protected void onStop() {
        mQRCodeView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    private void initBeepSound() {
        if (this.playBeep && this.mediaPlayer == null) {
            setVolumeControlStream(3);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(3);
            mediaPlayer.setOnCompletionListener(this.beepListener);
            AssetFileDescriptor file = this.getResources().openRawResourceFd(R.raw.beep);

            try {
                this.mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                this.mediaPlayer.setVolume(0.1F, 0.1F);
                this.mediaPlayer.prepare();
            } catch (IOException var3) {
                this.mediaPlayer = null;
            }
        }

    }

    private void playBeepSoundAndVibrate() {
        if (this.playBeep && this.mediaPlayer != null) {
            this.mediaPlayer.start();
        }
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200L);

    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.i(TAG, "result:" + result);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        playBeepSoundAndVibrate();
//      mQRCodeView.startSpot();
//
        Intent intent = new Intent();
        intent.putExtra("barcode", result);
        // 通过Intent对象返回结果，调用setResult方法。
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和文件的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        } else {
            startScan();
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "权限授权!");
        String[] permissions = CheckPermissionUtil.checkPermission(this);
        if (permissions.length == 0) {
            //权限都申请了
            startScan();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "权限被拒绝!");
        finish();
    }

    private void startScan() {

        mQRCodeView.startCamera();

//        mQRCodeView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);

        mQRCodeView.showScanRect();
    }
}
