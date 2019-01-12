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

package com.serenegiant.simpletexturecam2;

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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.serenegiant.common.BaseActivity;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.video.Encoder;
import com.serenegiant.video.Encoder.EncodeListener;
import com.serenegiant.video.SurfaceEncoder;
import com.serenegiant.widget.SimpleUVCCameraTextureView;

import org.angmarch.views.NiceSpinner;

public final class MainActivity extends BaseActivity implements CameraDialog.CameraDialogParent {
	private static final boolean DEBUG = true;	// set false when releasing
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
	private Button mCameraOpen,mCameraButton;
	// for start & stop movie capture
	private ImageButton mCaptureButton;

	private int mCaptureState = 0;
	private Surface mPreviewSurface;
	private static final int het = 720;
	private int width;
	private int height;
	private boolean ISpreview = true;
	private MediaButtonReceiver receiver;
	private AudioManager maudioManager;
	private ComponentName mComponentName;
	private NiceSpinner niceSpinner;
	private List<String> dataset;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mCameraOpen = (Button)findViewById(R.id.opencam);
		mCameraOpen.setOnClickListener(mOnClickListener);
		//mCameraOpen.setVisibility(View.GONE);
		switch (het){
			case 360:
				width = 640;height = 360;
				break;
			case 480:
				width = 640;height = 480;
				break;
			case 8480:
				width = 800;height = 480;
				break;
			case 720:
				width = 1280;height = 720;
				break;
			case 1080:
				width = 1920;height = 1080;
				break;
			case 1944:
				width = 2592;height = 1944;
				break;
		};

		mUVCCameraView = (SimpleUVCCameraTextureView)findViewById(R.id.UVCCameraTextureView1);
		mUVCCameraView.setAspectRatio(width / (float)height);
		mUVCCameraView.setSurfaceTextureListener(mSurfaceTextureListener);

		mUSBMonitor = new USBMonitor(this, mOnDeviceConnectListener);
		final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
		mUSBMonitor.setDeviceFilter(filters);

		IntentFilter filter = new IntentFilter("android.intent.action.MEDIA_BUTTON");
		receiver = new MediaButtonReceiver();
		filter.addAction(Intent.ACTION_MEDIA_BUTTON);
		filter.setPriority(1000);
		registerReceiver(receiver, filter);


		maudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		mComponentName = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
		maudioManager.registerMediaButtonEventReceiver(mComponentName);

		niceSpinner = (NiceSpinner) findViewById(R.id.nice_spinner);
		dataset = new LinkedList<>(Arrays.asList(Integer.toString(width) + "x" + Integer.toString(height)));
		niceSpinner.attachDataSource(dataset);
		niceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG,"change before"+niceSpinner.getText().toString().trim());//这个是获得点击前的值、、
				Log.d(TAG,"position"+position);//这个是获得点击前的值、、
				Log.d(TAG,"change after"+dataset.get(position));//这个是获得点击后的值、、
				resizePreview(dataset.get(position));
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
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
		unregisterReceiver(receiver);
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

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			switch (v.getId()) {
				case R.id.opencam:
					if(ISpreview){
						ISpreview = false;
						mCameraOpen.setText("OPEN");
						mUVCCamera.stopPreview();
					}else{
						ISpreview = true;
						mCameraOpen.setText("CLOSE");
						mUVCCamera.setPreviewDisplay(mPreviewSurface);
						mUVCCamera.startPreview();
					}
					break;
				/*
				case R.id.capture:
					if (checkPermissionWriteExternalStorage()) {
						if (mCaptureState == CAPTURE_STOP) {
							startCapture();
						} else {
							stopCapture();
						}
					}
					break;*/
			}
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
					camera.updateCameraParams();
					if (mPreviewSurface != null) {
						mPreviewSurface.release();
						mPreviewSurface = null;
					}
					fillSize(camera);
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
					mUVCCamera.startCapture(((SurfaceEncoder)encoder).getInputSurface());
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
     * @param type Environment.DIRECTORY_MOVIES / Environment.DIRECTORY_DCIM
     * @param ext .mp4 / .png
     * @return return null if can not write to storage
     */
    private static final String getCaptureFile(final String type, final String ext) {
		final File dir = new File(Environment.getExternalStoragePublicDirectory(type), "USBCameraTest");
		dir.mkdirs();	// create directories if they do not exist
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

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (DEBUG) Log.v(TAG, "onKeyUp:" + keyCode);
		switch(keyCode){
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
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (DEBUG) Log.v(TAG, "onKeyDown:" + keyCode);
		switch(keyCode){
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
				break;
			case KeyEvent.KEYCODE_HEADSETHOOK:
				if(ISpreview){
					ISpreview = false;
					mCameraOpen.setText("OPEN");
					mUVCCamera.stopPreview();
				}else{
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
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (DEBUG) Log.v(TAG, "onKeyLongPress:" + keyCode);
		switch(keyCode){
			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
			case KeyEvent.KEYCODE_HEADSETHOOK:
			case KeyEvent.KEYCODE_MEDIA_NEXT:
				break;
		}
		return false;
	}

	private void fillSize(final UVCCamera camera){
		String resolution,resl,resl2;
		String withme="0",heightme="0";
		int start,end, reslCNT = 0;
		ArrayList supportedResA = new ArrayList();
		HashMap<String,Integer> supportedRes = new HashMap<String,Integer>();
		int[][] getResolutions = new int[9][2];

		for(int i=0;i<9;i++){ getResolutions[i][0] = getResolutions[i][1] = 0;}
		resolution = camera.getSupportedSize(); //{"formats":[{"index":1,"type":6,"default":1,"size":["1280x720","640x360"]},{}]}
		start = resolution.indexOf("[\"");
		end = resolution.indexOf("\"]",start);
		resl = resolution.substring(start+1,end+1);

		resl2 = resl;

		while(true){
			withme = resl.substring(resl.indexOf("\"") + 1, resl.indexOf("x"));
			heightme = resl.substring(resl.indexOf("x") + 1, resl.indexOf("\"", resl.indexOf("\"") + 1));
			supportedRes.put(withme, Integer.parseInt(heightme));
			getResolutions[reslCNT][0] = Integer.parseInt(withme);
			getResolutions[reslCNT][1] = Integer.parseInt(heightme);
			reslCNT++;
			Log.d(TAG,"start03 " + start + " end " + end + " resl-" + resl);
			Log.d(TAG," withme-" + withme + " heme-" + heightme);
			if(!(resl.contains(",\""))) break;
			resl = resl.substring(resl.indexOf(",\"") + 1);
		}
		if (DEBUG) Log.d(TAG, "reslCNT=" + reslCNT);
		for (String key : supportedRes.keySet()) {
			if (DEBUG) Log.v(TAG, "######## " + key + ", " + supportedRes.get(key));
		}

		while(true){
			withme = resl2.substring(resl2.indexOf("\"") + 1, resl2.indexOf("\"", resl2.indexOf("\"") + 1));
			dataset.add(withme);
			if(!(resl2.contains(",\""))) break;
			resl2 = resl2.substring(resl2.indexOf(",\"") + 1);
		}
	}

	private void resizePreview(String select){
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
				mUVCCameraView.setAspectRatio(width / (float)height);
				mUVCCamera.setPreviewDisplay(mPreviewSurface);
				mUVCCamera.startPreview();
			}
		}
	}

}
