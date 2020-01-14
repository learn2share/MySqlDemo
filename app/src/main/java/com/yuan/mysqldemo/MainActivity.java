package com.yuan.mysqldemo;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final String TAG = "MainActivity";
    private Connection conn;
    private LinearLayout layoutScan;
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 100;
    private static final int SCAN_QRCODE = 1;
    private MyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layoutScan = findViewById(R.id.layout_scan);
        myHandler = new MyHandler(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                conn = DBOpenHelper.getConn();
                if (conn != null) {
                    String createTable = "CREATE TABLE IF NOT EXISTS device_info(" +
                            "   id serial PRIMARY KEY NOT NULL," +
                            "   device_id VARCHAR(255) NOT NULL," +
                            "   stock_time VARCHAR(255) NOT NULL)";
                    DBOpenHelper.execSQL(conn, createTable);
                }
            }
        }).start();

        layoutScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (!EasyPermissions.hasPermissions(MainActivity.this, perms)) {
                    EasyPermissions.requestPermissions(MainActivity.this, "扫描二维码需要打开相机和文件的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
                } else {
                    beginScan();
                }

            }
        });
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity activity = mActivity.get();
            if (activity != null) {
                if (msg.what == 1) {
                    Toast.makeText(activity, "入库成功", Toast.LENGTH_SHORT).show();
                } else if (msg.what == 3) {
                    Toast.makeText(activity, "设备已入库", Toast.LENGTH_SHORT).show();
                } else if (msg.what == 2) {
                    Toast.makeText(activity, "入库失败", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        //检查权限
        String[] permissions = CheckPermissionUtil.checkPermission(this);
        if (permissions.length == 0) {
            //权限都申请了
            beginScan();
        }
    }

    private void beginScan() {
        Intent intent = new Intent(MainActivity.this, QrcodeScanActivity.class);
        startActivityForResult(intent, SCAN_QRCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_QRCODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String deviceId = data.getStringExtra("barcode");
                    if (!TextUtils.isEmpty(deviceId)) {
                        Log.i(TAG, "扫描结果" + deviceId);
                        String insertSql = "INSERT INTO device_info(device_id,stock_time) VALUES(" + deviceId + ", '" + DateUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss") + "')";
                        if (conn != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Message msg = myHandler.obtainMessage();
                                    if (DBOpenHelper.getInfoByDeviceId(conn, deviceId).size() == 0) {
                                        if (DBOpenHelper.execSQL(conn, insertSql)) {
                                            msg.what = 1;
                                        } else {
                                            msg.what = 2;
                                        }
                                    } else {
                                        msg.what = 3;
                                    }
                                    myHandler.sendMessage(msg);
                                }
                            }).start();
                        }
                    }
                }
            } else {
                Toast.makeText(MainActivity.this, "扫描失败，请重新扫描", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "扫描二维码需要打开相机和文件的权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DBOpenHelper.closeAll(conn, null);
        myHandler.removeCallbacks(null);
    }
}
