/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.serenegiant.simpletexture.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.serenegiant.common.BaseActivity;
import com.serenegiant.simpletexture.MediaButtonReceiver;
import com.serenegiant.simpletexture.R;
import com.serenegiant.simpletexture.utils.AlertDialogUtil;
import com.serenegiant.simpletexture.utils.ToastUtil;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.video.Encoder;
import com.serenegiant.video.Encoder.EncodeListener;
import com.serenegiant.video.SurfaceEncoder;
import com.serenegiant.widget.SimpleUVCCameraTextureView;

import org.angmarch.views.NiceSpinner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static android.support.constraint.solver.widgets.ConstraintTableLayout.ALIGN_RIGHT;
import static android.view.TouchDelegate.TO_RIGHT;

public final class MainActivity extends BaseActivity implements CameraDialog.CameraDialogParent, OnClickListener {
    private static final boolean DEBUG = true;    // set false when releasing
    private static final String TAG = "MainActivity";

    private static final int CAPTURE_STOP = 0;
    private static final int CAPTURE_PREPARE = 1;
    private static final int CAPTURE_RUNNING = 2;

    private final Object mSync = new Object();
    // for accessing USB and USB camera
    private USBMonitor mUSBMonitor;
    private UVCCamera mUVCCamera;
    private SimpleUVCCameraTextureView mUVCCameraView;
    // for open&start / stop&close camera preview
    private Button mCameraOpen, mCameraButton;
    // for start & stop movie capture
    private ImageButton mCaptureButton;

    private int mCaptureState = 0;
    private Surface mPreviewSurface;
    private int width;
    private int height;
    private boolean ISpreview = true;
    private MediaButtonReceiver receiver;
    private AudioManager maudioManager;
    private ComponentName mComponentName;
    private NiceSpinner niceSpinner;
    private List<String> dataset;

    private LinearLayout llContainer;
//    private View viewTitle;
    private LinearLayout llTitle;
    private ImageView mIvSport;
    private ImageView mIvControl;
    private ImageView mIvUsb;
    private ImageView mTvSetting;
    private ImageView mIvMenu;
    private FloatingActionMenu fab;



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        llContainer = (LinearLayout) findViewById(R.id.ll_container);
        llContainer.setOnClickListener(this);
        llTitle = (LinearLayout) findViewById(R.id.ll);
//        viewTitle =(View) findViewById(R.id.view);
        mIvSport = (ImageView) findViewById(R.id.iv_sport);
        mIvControl = (ImageView) findViewById(R.id.iv_control);
        mIvUsb = (ImageView) findViewById(R.id.iv_usb);
        mTvSetting = (ImageView) findViewById(R.id.iv_setting);
        mIvMenu = (ImageView) findViewById(R.id.iv_menu);
        mIvSport.setOnClickListener(this);
        mIvControl.setOnClickListener(this);
        mIvUsb.setOnClickListener(this);
        mTvSetting.setOnClickListener(this);
        mIvMenu.setOnClickListener(this);
        fab = (FloatingActionMenu) findViewById(R.id.fab);
        fab.setClosedOnTouchOutside(true);
//		fab.setAlpha(0);
//		fab.setAlpha(getResources().getColor(android.R.color.transparent));
//        viewTitle.setVisibility(View.GONE);
        llTitle.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);

        //mCameraOpen.setVisibility(View.GONE);原分辨率数值设在这里的
        mUVCCameraView = (SimpleUVCCameraTextureView) findViewById(R.id.UVCCameraTextureView1);
        mUVCCameraView.setAspectRatio(width / (float) height);
        mUVCCameraView.setSurfaceTextureListener(mSurfaceTextureListener);

        mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
        final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
        mUSBMonitor.setDeviceFilter(filters);

        IntentFilter filter = new IntentFilter("android.intent.action.MEDIA_BUTTON");
        receiver = new MediaButtonReceiver();
        filter.addAction(Intent.ACTION_MEDIA_BUTTON);
        filter.setPriority(1000);
        registerReceiver(receiver, filter);

        maudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mComponentName = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        maudioManager.registerMediaButtonEventReceiver(mComponentName);
        Log.v(TAG, "end onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        synchronized (mSync) {
            if (mUSBMonitor != null) {
                mUSBMonitor.register();
            }
            if (mUVCCamera != null)
                mUVCCamera.startPreview();
        }
        setCameraButton(false);
        updateItems();
    }

    @Override
    protected void onStop() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                stopCapture();
                mUVCCamera.stopPreview();
            }
            mUSBMonitor.unregister();
        }
        setCameraButton(false);
//        unregisterReceiver(receiver);
        maudioManager.unregisterMediaButtonEventReceiver(mComponentName);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        synchronized (mSync) {
            if (mUVCCamera != null) {
                mUVCCamera.destroy();
                mUVCCamera = null;
            }
            if (mUSBMonitor != null) {
                mUSBMonitor.destroy();
                mUSBMonitor = null;
            }
        }
        mCameraButton = null;
        mCaptureButton = null;
        mUVCCameraView = null;

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }

    private final OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            synchronized (mSync) {
                if (isChecked && mUVCCamera == null) {
                    CameraDialog.showDialog(MainActivity.this);
                } else if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                    mUVCCamera = null;
                }
            }
            updateItems();
        }
    };

    ///lmy
    private void openUVCCamera(final int index) {
        Log.v(TAG, "openUVCCamera:index=" + index);
        if (!mUSBMonitor.isRegistered()) return;
        final List<UsbDevice> list = mUSBMonitor.getDeviceList();
        for (int i = 0; i < list.size(); i++) {
            UsbDevice dev = list.get(i);
            if (dev.getDeviceClass() == 239) {//&& mPreviewSurface == null) {
                mUSBMonitor.requestPermission(list.get(i));
                break;
            }
        }
    }

    private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
        @Override
        public void onAttach(final UsbDevice device) {
            if (DEBUG) Log.v(TAG, "onAttach:");
            openUVCCamera(0);
            Toast.makeText(MainActivity.this, "USB_DEVICE_ATTACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
            if (DEBUG) Log.v(TAG, "onConnect:");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.destroy();
                    mUVCCamera = null;
                }
            }
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    final UVCCamera camera = new UVCCamera();
                    camera.open(ctrlBlock);
                    if (DEBUG) Log.i(TAG, "supportedSize:" + camera.getSupportedSize());
                    //if (DEBUG) Log.i(TAG, "Descriptions:" + camera.getDescriptions());//get-default-fps

                    camera.updateCameraParams();
                    if (mPreviewSurface != null) {
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }
                    try {
                        camera.setPreviewSize(width, height, UVCCamera.FRAME_FORMAT_MJPEG);
                        //camera.setPreviewSize(1040, 720, UVCCamera.FRAME_FORMAT_MJPEG);
                    } catch (final IllegalArgumentException e) {
                        try {
                            // fallback to YUV mode
                            camera.setPreviewSize(UVCCamera.DEFAULT_PREVIEW_WIDTH, UVCCamera.DEFAULT_PREVIEW_HEIGHT, UVCCamera.DEFAULT_PREVIEW_MODE);
                        } catch (final IllegalArgumentException e1) {
                            camera.destroy();
                            return;
                        }
                    }
                    final SurfaceTexture st = mUVCCameraView.getSurfaceTexture();
                    if (st != null) {
                        mPreviewSurface = new Surface(st);
                        camera.setPreviewDisplay(mPreviewSurface);
                        camera.startPreview();
                    }
                    synchronized (mSync) {
                        mUVCCamera = camera;
                    }
                }
            }, 0);
        }

        @Override
        public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
            // XXX you should check whether the comming device equal to camera device that currently using
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    synchronized (mSync) {
                        if (mUVCCamera != null) {
                            mUVCCamera.close();
                        }
                    }
                    if (mPreviewSurface != null) {
                        mPreviewSurface.release();
                        mPreviewSurface = null;
                    }
                }
            }, 0);
            setCameraButton(false);
        }

        @Override
        public void onDettach(final UsbDevice device) {
            Toast.makeText(MainActivity.this, "USB_DEVICE_DETACHED", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(final UsbDevice device) {
            setCameraButton(false);
        }
    };

    /**
     * to access from CameraDialog
     *
     * @return
     */
    @Override
    public USBMonitor getUSBMonitor() {
        return mUSBMonitor;
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            setCameraButton(false);
        }
    }

    private void setCameraButton(final boolean isOn) {
/*		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mCameraButton != null) {
					try {
						mCameraButton.setOnCheckedChangeListener(null);
						mCameraButton.setChecked(isOn);
					} finally {
						mCameraButton.setOnCheckedChangeListener(mOnCheckedChangeListener);
					}
				}
				if (!isOn && (mCaptureButton != null)) {
					mCaptureButton.setVisibility(View.INVISIBLE);
				}
			}
		}, 0);*/
    }

    //**********************************************************************
    private final SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
        }

        @Override
        public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
            if (mPreviewSurface != null) {
                mPreviewSurface.release();
                mPreviewSurface = null;
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
            if (mEncoder != null && mCaptureState == CAPTURE_RUNNING) {
                mEncoder.frameAvailable();
            }
        }
    };

    private Encoder mEncoder;

    /**
     * start capturing
     */
    private final void startCapture() {
        if (DEBUG) Log.v(TAG, "startCapture:");
        if (mEncoder == null && (mCaptureState == CAPTURE_STOP)) {
            mCaptureState = CAPTURE_PREPARE;
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    final String path = getCaptureFile(Environment.DIRECTORY_MOVIES, ".mp4");
                    if (!TextUtils.isEmpty(path)) {
                        mEncoder = new SurfaceEncoder(path);
                        mEncoder.setEncodeListener(mEncodeListener);
                        try {
                            mEncoder.prepare();
                            mEncoder.startRecording();
                        } catch (final IOException e) {
                            mCaptureState = CAPTURE_STOP;
                        }
                    } else
                        throw new RuntimeException("Failed to start capture.");
                }
            }, 0);
            updateItems();
        }
    }

    /**
     * stop capture if capturing
     */
    private final void stopCapture() {
        if (DEBUG) Log.v(TAG, "stopCapture:");
        queueEvent(new Runnable() {
            @Override
            public void run() {
                synchronized (mSync) {
                    if (mUVCCamera != null) {
                        mUVCCamera.stopCapture();
                    }
                }
                if (mEncoder != null) {
                    mEncoder.stopRecording();
                    mEncoder = null;
                }
            }
        }, 0);
    }

    /**
     * callbackds from Encoder
     */
    private final EncodeListener mEncodeListener = new EncodeListener() {
        @Override
        public void onPreapared(final Encoder encoder) {
            if (DEBUG) Log.v(TAG, "onPreapared:");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.startCapture(((SurfaceEncoder) encoder).getInputSurface());
                }
            }
            mCaptureState = CAPTURE_RUNNING;
        }

        @Override
        public void onRelease(final Encoder encoder) {
            if (DEBUG) Log.v(TAG, "onRelease:");
            synchronized (mSync) {
                if (mUVCCamera != null) {
                    mUVCCamera.stopCapture();
                }
            }
            mCaptureState = CAPTURE_STOP;
            updateItems();
        }
    };

    private void updateItems() {
    /*	this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mCaptureButton.setVisibility(mCameraButton.isChecked() ? View.VISIBLE : View.INVISIBLE);
		    	mCaptureButton.setColorFilter(mCaptureState == CAPTURE_STOP ? 0 : 0xffff0000);
			}
    	});*/
    }

    /**
     * create file path for saving movie / still image file
     *
     * @param type Environment.DIRECTORY_MOVIES / Environment.DIRECTORY_DCIM
     * @param ext  .mp4 / .png
     * @return return null if can not write to storage
     */
    private static final String getCaptureFile(final String type, final String ext) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type), "USBCameraTest");
        dir.mkdirs();    // create directories if they do not exist
        if (dir.canWrite()) {
            return (new File(dir, getDateTimeString() + ext)).toString();
        }
        return null;
    }

    private static final SimpleDateFormat sDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);

    private static final String getDateTimeString() {
        final GregorianCalendar now = new GregorianCalendar();
        return sDateTimeFormat.format(now.getTime());
    }

    /**
     * 按键释放时事件
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (DEBUG) Log.v(TAG, "onKeyUp:" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                break;
            case KeyEvent.KEYCODE_HEADSETHOOK:
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                break;
        }
        return super.onKeyUp(keyCode, event);
        // false;//为true,则其它后台按键处理再也无法处理到该按键，为false,则其它后台按键处理可以继续处理该按键事件。
    }

    /**
     * 按键按下时事件
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (DEBUG) Log.v(TAG, "onKeyDown:" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                break;
            case KeyEvent.KEYCODE_HEADSETHOOK:
                if (ISpreview) {
                    ISpreview = false;
                    mCameraOpen.setText("OPEN");
                    mUVCCamera.stopPreview();
                } else {
                    ISpreview = true;
                    mCameraOpen.setText("CLOSE");
                    mUVCCamera.setPreviewDisplay(mPreviewSurface);
                    mUVCCamera.startPreview();
                }
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 长时间按键时事件
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (DEBUG) Log.v(TAG, "onKeyLongPress:" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                break;
        }
        return false;
    }

    private void resizePreview(String select) {
        int rwidth = Integer.parseInt(select.substring(0, select.indexOf("x")));
        int rheight = Integer.parseInt(select.substring(select.indexOf("x") + 1, select.length()));

        if (DEBUG) Log.d(TAG, "resizePreview=" + rwidth + "x" + rheight);
        if (mUVCCamera != null) {
            if ((rwidth != width) || (rheight != height)) {
                mUVCCamera.stopPreview();
                try {
                    mUVCCamera.setPreviewSize(rwidth, rheight);
                } catch (final IllegalArgumentException e) {
                    try {
                        mUVCCamera.setPreviewSize(width, height);
                        Toast.makeText(MainActivity.this, "change fail!", Toast.LENGTH_SHORT).show();
                    } catch (final IllegalArgumentException e1) {
                        // unexpectedly #setPreviewSize failed
                        mUVCCamera.destroy();
                        mUVCCamera = null;
                    }
                }
                if (mUVCCamera == null) return;
                width = rwidth;
                height = rheight;
                mUVCCameraView.setAspectRatio(width / (float) height);
                mUVCCamera.setPreviewDisplay(mPreviewSurface);
                mUVCCamera.startPreview();
            }
        } else {
            if (DEBUG) Log.d(TAG, "mUVCCamera is null");
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_sport:
                showSport();
                break;

            case R.id.iv_control:
                showParamter();
//                showDialog();
                break;

            case R.id.iv_usb:
                showEquipment();
                break;

            case R.id.iv_setting:
//                AlertDialogUtil.showCommonDialog(MainActivity.this, "设置", "  进入设置后，服务将会被重启，是否继续？",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//
//                            }
//                        });
                showSetting();
                break;

            case R.id.iv_menu:
                dealPopupwindow();
                break;

            case R.id.ll_container:
                int visibility = llTitle.getVisibility();
                if (View.GONE == visibility) {
                    llTitle.setVisibility(View.VISIBLE);
//                    viewTitle.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.VISIBLE);
                } else {
//                    viewTitle.setVisibility(View.GONE);
                    llTitle.setVisibility(View.GONE);
                    fab.setVisibility(View.GONE);
                }
                break;

        }
    }

    /**
     * 运动的对话框
     */
    private void showSport() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("运动检测");
        dialog.setMessage("  打开运动检测后，USB摄像头会在检测到运动时自动录像并存储，你还可以通过Web查看。当15秒内检测不到运动后，录像将自动停止。打开前，请保持设备有足够的可用空间，并将设备放置于平稳的地方。");
        dialog.setCancelable(false);
        dialog.setPositiveButton("运动检测", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }

    /**
     * 相机参数界面的对话框
     */
    private void showParamter() {
        View view = getLayoutInflater().inflate(R.layout.parameter_setting, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);                                           //这里添加上这个view
//        Button mBtClose = (Button) findViewById(R.id.bt_close);
//        mBtClose.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialogInterface) {
//                        dialogInterface.dismiss();
//                    }
//                });
//            }
//        });
        builder.create().show();
    }

    /**
     * 设备的对话框
     */
    private void showEquipment() {
        AlertDialog.Builder device = new AlertDialog.Builder(MainActivity.this);
        device.setTitle("设备");
        device.setMessage("USB camera");
        device.setCancelable(false);
        device.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        device.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        device.setNeutralButton("刷新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        device.show();
    }

    /**
     * 设置的对话框
     */
    private void showSetting() {
        AlertDialog.Builder setting = new AlertDialog.Builder(MainActivity.this);
        setting.setTitle("USB摄像头");
        setting.setMessage("  进入设置后，服务将会被重启，是否继续？");
        setting.setCancelable(false);
        setting.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        setting.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        setting.show();
    }

    /**
     * popupwindow的处理
     */
    private void dealPopupwindow() {
        //  构建一个popupwindow的布局
               //View popupView = MainActivity.this.getLayoutInflater().inflate(R.layout.popupwindow, null);
        View popupView = View.inflate(MainActivity.this, R.layout.popupwindow, null);
        //  创建PopupWindow对象，指定宽度和高度
        PopupWindow window = new PopupWindow(popupView, 450, 600);
        // 设置动画
        window.setAnimationStyle(R.style.popup_window_anim);
        //设置背景颜色
//        window.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        //  设置可以获取焦点
        window.setFocusable(true);
        // 设置可以触摸弹出框以外的区域/设置PopupWindow是否能响应外部点击事件
        window.setOutsideTouchable(true);
        //更新popupwindow的状态
        window.update();

        TextView mSecurityPopup = (TextView) popupView.findViewById(R.id.tv_security_popup);
        mSecurityPopup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.show(MainActivity.this, "现在可以安全移除USB设备了");
            }
        });

        TextView mResolution = (TextView) popupView.findViewById(R.id.tv_resolution);
        mResolution.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                        ToastUtil.show(MainActivity.this,"选择分辨率");
                View resolution = getLayoutInflater().inflate(R.layout.choose_resolution, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(resolution);
                builder.show();
            }
        });

        TextView mFlipHorizontal = (TextView) popupView.findViewById(R.id.tv_flip_horizontal);
        mFlipHorizontal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.show(MainActivity.this, "水平翻转");
            }
        });

        TextView mFlipVertical = (TextView) popupView.findViewById(R.id.tv_flip_vertical);
        mFlipVertical.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.show(MainActivity.this, "垂直翻转");
            }
        });

        TextView mIntoBackground = (TextView) popupView.findViewById(R.id.tv_into_background);
        mIntoBackground.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.show(MainActivity.this, "进入后台运行");
            }
        });

        //window.showAsDropDown(mIvMenu, ALIGN_RIGHT,ALIGN_RIGHT);

        // 以下拉的方式显示，并且可以设置显示的位置
        window.showAsDropDown(mIvMenu, 0, 50);
    }

    private void showDialog() {
        Dialog progressDialog = new Dialog(this, R.style.mydialog);
        progressDialog.setContentView(R.layout.parameter_setting);
        //progressDialog.getWindow().setBackgroundDrawableResource(R.color.colorGrey);
        Window window = progressDialog.getWindow();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = window.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight()); // 改变的是dialog框在屏幕中的位置而不是大小
        p.width = (int) (d.getWidth()); // 宽度设置为屏幕的0.65
        window.setAttributes(p);
        //TextView msg = progressDialog.findViewById(R.id.id_tv_loadingmsg);
        //msg.setText(s);
        progressDialog.show();
    }

}
