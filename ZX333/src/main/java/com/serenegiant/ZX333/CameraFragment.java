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

package com.serenegiant.ZX333;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.serenegiant.common.BaseFragment;
import com.serenegiant.encoder.MediaMuxerWrapper;
import com.serenegiant.service.UVCService;
import com.serenegiant.serviceclient.CameraClient;
import com.serenegiant.serviceclient.ICameraClient;
import com.serenegiant.serviceclient.ICameraClientCallback;

import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.USBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb.USBMonitor.UsbControlBlock;
import com.serenegiant.utils.HandlerThreadHandler;
import com.serenegiant.widget.CameraViewInterface;

public class CameraFragment extends BaseFragment {

	private static final boolean DEBUG = true;
	private static final String TAG = "CameraFragment";

	private static final int DEFAULT_WIDTH = 1280;
	private static final int DEFAULT_HEIGHT = 720;

	private static final int FILE_SELECT_CODE = 0;
	private static final int FILE_SELECT_USB = 42;

	private USBMonitor mUSBMonitor;
	private ICameraClient mCameraClient;

	private ImageButton mRecordButton;
	private ImageButton mStillCaptureButton;
	private ImageButton mSettingButton,mBackButton;
	private CameraViewInterface mCameraView;
	private boolean isSubView;
	private Handler mWHandler, mloopHandler;
	private int vid_cnt = 0; //this is for tmp video cnt
	private AvaliableStorage sInfo;

	private Uri usbUri = null;
	private DocumentFile pickedDir;
	private DocumentFile zxDir;
	private String mySDpath = null;
	private String localPath;
	private List<Uri> usbUriArray;
	String pathVid;

	private boolean dofinish = false;

	public CameraFragment() {
		if (UvcDebug.DEBUG) Log.v(TAG, "Constructor:");
//		setRetainInstance(true);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		if (UvcDebug.DEBUG) Log.v(TAG, "onAttach:");
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (UvcDebug.DEBUG) Log.v(TAG, "onCreate:");
		if (mUSBMonitor == null) {
			mUSBMonitor = new USBMonitor(getActivity().getApplicationContext(), mOnDeviceConnectListener);
			final List<DeviceFilter> filters = DeviceFilter.getDeviceFilters(getActivity(), R.xml.device_filter);
			mUSBMonitor.setDeviceFilter(filters);
		}
		if (mWHandler == null) {
			mWHandler = HandlerThreadHandler.createHandler(TAG);
		}
		if (mloopHandler == null) {
			mloopHandler = HandlerThreadHandler.createHandler("loopp");
		}
		sInfo = new AvaliableStorage();
		//getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		localPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/ZXcam/";
		Log.v(TAG, "localPath:" + localPath);
		pathVid = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/ZXcam";

		usbUriArray = new ArrayList<Uri>();

		new miscThread().start();
		new Thread(miscWork, "miscWork").start();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (UvcDebug.DEBUG) Log.v(TAG, "onCreateView:");
		final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		View view = rootView.findViewById(R.id.start_button);
		view.setOnClickListener(mOnClickListener);
		view.setVisibility(View.INVISIBLE);
		view =rootView.findViewById(R.id.stop_button);
		view.setOnClickListener(mOnClickListener);
		view.setVisibility(View.INVISIBLE);
		mRecordButton = (ImageButton)rootView.findViewById(R.id.record_button);
		mRecordButton.setOnClickListener(mOnClickListener);
		mRecordButton.setEnabled(false);
		mStillCaptureButton = (ImageButton)rootView.findViewById(R.id.still_button);
		mStillCaptureButton.setOnClickListener(mOnClickListener);
		mStillCaptureButton.setEnabled(false);

		mSettingButton = (ImageButton)rootView.findViewById(R.id.setting_button);
		mSettingButton.setOnClickListener(mOnClickListener);
		//mSettingButton.setEnabled(false);
		mBackButton = (ImageButton)rootView.findViewById(R.id.back_button);
		mBackButton.setOnClickListener(mOnClickListener);

		mCameraView = (CameraViewInterface)rootView.findViewById(R.id.camera_view);
		mCameraView.setAspectRatio(DEFAULT_WIDTH / (float)DEFAULT_HEIGHT);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (UvcDebug.DEBUG) Log.v(TAG, "onResume:");
		mUSBMonitor.register();
	}

	@Override
	public void onPause() {
		if (UvcDebug.DEBUG) Log.v(TAG, "onPause:");
		if (mCameraClient != null) {
			mCameraClient.removeSurface(mCameraView.getSurface());
			isSubView = false;
		}
		mUSBMonitor.unregister();
		enableButtons(false);
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		if (UvcDebug.DEBUG) Log.v(TAG, "onDestroyView:");
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		if (UvcDebug.DEBUG) Log.v(TAG, "onDestroy:");
		if (mCameraClient != null) {
			mCameraClient.release();
			mCameraClient = null;
		}
		dofinish = true;
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		if (UvcDebug.DEBUG) Log.v(TAG, "onDetach:");
		super.onDetach();
	}

	public USBMonitor getUSBMonitor() {
		return mUSBMonitor;
	}

	private final OnDeviceConnectListener mOnDeviceConnectListener = new OnDeviceConnectListener() {
		@Override
		public void onAttach(final UsbDevice device) {
			if (UvcDebug.DEBUG) Log.v(TAG, "OnDeviceConnectListener#onAttach:");
			if (!updateCameraDialog() && (mCameraView.hasSurface())) {
				tryOpenUVCCamera(true);
			}
		}

		@Override
		public void onConnect(final UsbDevice device, final UsbControlBlock ctrlBlock, final boolean createNew) {
			if (UvcDebug.DEBUG) Log.v(TAG, "OnDeviceConnectListener#onConnect:");
		}

		@Override
		public void onDisconnect(final UsbDevice device, final UsbControlBlock ctrlBlock) {
			if (UvcDebug.DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDisconnect:");
		}

		@Override
		public void onDettach(final UsbDevice device) {
			if (UvcDebug.DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDettach:");
			queueEvent(new Runnable() {
				@Override
				public void run() {
					if (mCameraClient != null) {
						mCameraClient.disconnect();
						mCameraClient.release();
						mCameraClient = null;
					}
				}
			}, 0);
			enableButtons(false);
			updateCameraDialog();
		}

		@Override
		public void onCancel(final UsbDevice device) {
			if (UvcDebug.DEBUG) Log.v(TAG, "OnDeviceConnectListener#onCancel:");
			enableButtons(false);
		}
	};

	private boolean updateCameraDialog() {
		final Fragment fragment = getFragmentManager().findFragmentByTag("CameraDialog");
		if (fragment instanceof CameraDialog) {
			((CameraDialog)fragment).updateDevices();
			return true;
		}
		return false;
	}

	private void tryOpenUVCCamera(final boolean requestPermission) {
		if (UvcDebug.DEBUG) Log.v(TAG, "tryOpenUVCCamera:");
		openUVCCamera(0);
	}

	private void openUVCCamera(final int index) {
		if (UvcDebug.DEBUG) Log.v(TAG, "openUVCCamera:index=" + index);
		if (!mUSBMonitor.isRegistered()) return;
		final List<UsbDevice> list = mUSBMonitor.getDeviceList();
		if (list.size() > index) {
			enableButtons(false);
			if (mCameraClient == null)
				mCameraClient = new CameraClient(getActivity(), mCameraListener);
			mCameraClient.select(list.get(index));
			mCameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
			mCameraClient.connect();
		}
	}

	private final ICameraClientCallback mCameraListener = new ICameraClientCallback() {
		@Override
		public void onConnect() {
			if (UvcDebug.DEBUG) Log.v(TAG, "onConnect:");
			mCameraClient.addSurface(mCameraView.getSurface(), false);
			isSubView = true;
			enableButtons(true);
			// start UVCService
			final Intent intent = new Intent(getActivity(), UVCService.class);
			getActivity().startService(intent);
			//if (!mCameraClient.isRecording())
				//mWHandler.postDelayed(mLoopRecord, 5 * 1000); //start recording after connected 5s
			if(usbUri == null){
				for (final Uri mUri: usbUriArray ) { //find uri in local file, then user do not need select manually
					DocumentFile vPath  = DocumentFile.fromTreeUri(getActivity(), usbUri);
					DocumentFile FY = null;
					FY = vPath.createFile("text/plain", "FY");
					if(FY != null){
						FY.delete();
						if (UvcDebug.DEBUG) Log.v(TAG, "found OTG:" + mUri.getPath());
						usbUri = mUri;
					}
				}
				//Intent intentFile = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
				//startActivityForResult(intentFile, FILE_SELECT_USB);
			}else{
				pickedDir = DocumentFile.fromTreeUri(getActivity(), usbUri);
			}
		}

		@Override
		public void onDisconnect() {
			if (UvcDebug.DEBUG) Log.v(TAG, "onDisconnect:");
			enableButtons(false);
		}

	};

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			switch (v.getId()) {
			case R.id.start_button:
				if (UvcDebug.DEBUG) Log.v(TAG, "onClick:start");
				// start service
				final List<UsbDevice> list = mUSBMonitor.getDeviceList();
				if (list.size() > 0) {
					if (mCameraClient == null)
						mCameraClient = new CameraClient(getActivity(), mCameraListener);
					mCameraClient.select(list.get(0));
					mCameraClient.resize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
					mCameraClient.connect();
				}
				break;
			case R.id.stop_button:
				if (UvcDebug.DEBUG) Log.v(TAG, "onClick:stop");
				// stop service
				if (mCameraClient != null) {
					mCameraClient.disconnect();
					mCameraClient.release();
					mCameraClient = null;
				}
				enableButtons(false);
				break;
			case R.id.record_button:
				if (UvcDebug.DEBUG) Log.v(TAG, "onClick:record");
				if(sInfo.dfPath(localPath) < 300){
					Toast.makeText(getActivity(), "No enough Memory(<300M)", Toast.LENGTH_SHORT).show();
					return;
				}
				if (usbUri != null) {
					mRecordButton.setEnabled(false);
					if (checkPermissionWriteExternalStorage() && checkPermissionAudio()) {
						queueEvent(new Runnable() {
							@Override
							public void run() {
								if (mCameraClient.isRecording()) {
									if (mWHandler != null) mWHandler.removeCallbacks(mLoopRecord);
									mCameraClient.stopRecording();
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											//mRecordButton.setColorFilter(0);
											mRecordButton.setImageResource(R.drawable.record);
										}
									}, 0);
								} else {
									CheckStorage();
									mCameraClient.startRecording();
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											//mRecordButton.setColorFilter(0x7fff0000);
											mRecordButton.setImageResource(R.drawable.recording);
										}
									}, 0);
									if (mWHandler != null) mWHandler.removeCallbacks(mLoopRecord);
									mWHandler.postDelayed(mLoopRecord, 1 * 60 * 1000);
									//if(mloopHandler != null) mloopHandler.removeCallbacks(mLoopCPFile);
									//mloopHandler.postDelayed(mLoopCPFile, 1000);
								}
							}
						}, 0);
					}
					new Handler().postDelayed(new Runnable() {
						public void run() {
							mRecordButton.setEnabled(true);
						}
					}, 1000);
				}else{
					//alart
				}
				break;
			case R.id.still_button:
				if (UvcDebug.DEBUG) Log.v(TAG, "onClick:still capture");
				if (mCameraClient != null && checkPermissionWriteExternalStorage()) {
					mStillCaptureButton.setImageResource(R.drawable.take_photo_pressed);
					mStillCaptureButton.setEnabled(false);
					queueEvent(new Runnable() {
						@Override
						public void run() {
							mCameraClient.captureStill(MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, ".jpg").toString());
						}
					}, 0);

					new Handler().postDelayed(new Runnable() {public void run() {
						mStillCaptureButton.setImageResource(R.drawable.take_photo);
						mStillCaptureButton.setEnabled(true);
					}}, 300);
				}

				break;
			case R.id.setting_button:
				if (UvcDebug.DEBUG) Log.v(TAG, "onClick:setting");
				HashMap<String,Integer> extPath = sInfo.getAvaliableStorage(getActivity());
				for (String key : extPath.keySet()) {
					if (UvcDebug.DEBUG) Log.v(TAG, "######## " + key + ", " + extPath.get(key)/1024 + "G");
				}

				final String[] arrayFruit = extPath.keySet().toArray(new String[0]);
				Dialog alertDialog = new AlertDialog.Builder(getActivity()).
						setTitle("选择存储路径").
						setItems(arrayFruit, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Toast.makeText(getActivity(), arrayFruit[which], Toast.LENGTH_SHORT).show();
							/*	File destDir = new File(arrayFruit[which] + "/ZX333");
								if (!destDir.exists()) {
									if (UvcDebug.DEBUG) Log.v(TAG, "mkdir " + arrayFruit[which] + "/ZX333");
									if(Build.VERSION.SDK_INT >= 23) {
										getActivity().requestPermissions(new String[]{android
												.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
									}
									boolean ret = destDir.mkdirs();
									if (UvcDebug.DEBUG) Log.v(TAG, "mkdir ret = " + ret);
								}*/
								Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
								startActivityForResult(intent, FILE_SELECT_USB);
								mySDpath = arrayFruit[which];

							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
							}
						}).create();
				alertDialog.show();

                //chooseStorage();
				break;
			case R.id.back_button:
				if (UvcDebug.DEBUG) Log.v(TAG, "click finish");
				if(mWHandler != null) mWHandler.removeCallbacks(mLoopRecord);
				if (mCameraClient != null) {
					mCameraClient.disconnect();
					mCameraClient.release();
					mCameraClient = null;
				}
				dofinish = true;
				getActivity().finish();
				//android.os.Process.killProcess(android.os.Process.myPid());
				break;
			}
		}
	};

	private final OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
			if (UvcDebug.DEBUG) Log.v(TAG, "onCheckedChanged:" + isChecked);
			if (isChecked) {
				mCameraClient.addSurface(mCameraView.getSurface(), false);
			} else {
				mCameraClient.removeSurface(mCameraView.getSurface());
			}
		}
	};

	private final Runnable mLoopRecord = new Runnable() {
		@Override
		public void run() {
			int sleepcnt = 0;

			if(dofinish) return;
			CheckStorage();

			if (mCameraClient != null && mCameraClient.isRecording()) {
				mCameraClient.stopRecording();
			}

			Log.d(TAG, "mLoopRecord currentThread " + Thread.currentThread().getId() + " main " + Looper.getMainLooper().getThread().getId());

			while (true) {
				sleepcnt++;
				try {
					if (UvcDebug.DEBUG) Log.v(TAG, "sleepcnt " + sleepcnt);
					Thread.sleep(1*1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(dofinish) return;
				if(mCameraClient == null) return; // fix me, should put into thread pool
				if (!mCameraClient.isRecording()) {
					mCameraClient.startRecording();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mRecordButton.setImageResource(R.drawable.recording);
						}
					}, 0);
					break;
				}
			}
			mWHandler.postDelayed(this, 1 * 60 * 1000);
		}
	};

	Long srcF = 0L,dstF;
	File[] files;
	String srcFile;
	FileItem destF;
	long sizefile0;
	Long biggerS;
	private final Runnable mLoopCPFile = new Runnable() {
		@Override
		public void run() {

			files = new File(localPath).listFiles();//movies
			if (UvcDebug.DEBUG) Log.v(TAG, "files.length " + files.length + " size " + sInfo.dfPath(mySDpath));

			Log.d(TAG, "mLoopCPFile currentThread " + Thread.currentThread().getId() + " main " + Looper.getMainLooper().getThread().getId());

			if(files.length > 0) {
				try {
					sizefile0 = FileHelper.getFileSize(files[0]);//in case only 1 file, and this file just created for recording, should not copy and delete it
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(files.length > 1 || (mCameraClient != null && !(mCameraClient.isRecording()) &&
					files.length > 0 && sizefile0 > 8)) { //should be filesize > 0
				srcFile = sInfo.findOldfile();
				destF = new FileItem();
				//check if already exist and src == dst, yes-> skip, no->create
				try {
					if (zxDir.findFile(srcFile) != null) {
						srcF = FileHelper.getFileSize(new File(localPath + srcFile));
						dstF = FileHelper.getFileSize(new File(mySDpath + "/ZX333/" + srcFile));
						if (UvcDebug.DEBUG) Log.v(TAG, "00 srcF = " + srcF + " dstF = " + dstF);
						if (!(srcF.equals(dstF))) {
							zxDir.findFile(srcFile).delete();
							try {
								Thread.sleep(1 * 1000);//?
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {
							new File(localPath + srcFile).delete();
							mloopHandler.postDelayed(this, 1 * 1000);
							if (UvcDebug.DEBUG)
								Log.v(TAG, mySDpath + "/ZX333/" + srcFile + "already exist");
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				sizefile0 = 0;
				try {
					sizefile0 = FileHelper.getFileSize(files[0]);//in case only 1 file, and this file just created for recording, should not copy and delete it
					srcF = FileHelper.getFileSize(new File(localPath + srcFile)); //srcF should smaller than romLeft in U disk
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (UvcDebug.DEBUG) Log.v(TAG, " sizefile0 " + sizefile0 + " name " + files[0].getName() + " srcF " + srcF);
				if (UvcDebug.DEBUG) Log.v(TAG, " mySDpath " + mySDpath + " " + sInfo.dfPath(mySDpath));
				if (srcF/1024/1024 < sInfo.dfPath(mySDpath)) { // usually, mySDpath == 300, if srcF record 4min, larger than 300, then all files will not be copied into SD
					//
					destF.file = zxDir.createFile("video/avc", srcFile);
					if (UvcDebug.DEBUG) Log.v(TAG, "start cp " + srcFile);
					FileHelper.copyVideo(getActivity(), localPath + srcFile, destF);

					if (UvcDebug.DEBUG) Log.v(TAG, " now lets check it");
					try { //check if src == dst, yes->cp done, no->cp fail
						srcF = FileHelper.getFileSize(new File(localPath + srcFile));
						dstF = FileHelper.getFileSize(new File(mySDpath + "/ZX333/" + srcFile));
						if (UvcDebug.DEBUG) Log.v(TAG, "01 srcF = " + srcF + " dstF = " + dstF);
						if (srcF.equals(dstF))
							new File(localPath + srcFile).delete();
						else
							destF.file.delete();
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (UvcDebug.DEBUG) Log.v(TAG, "end cp");
				}else{ //skip the larger one ?
					;
				}
			}

			//while(sInfo.dfPath(mySDpath) < 300){
			biggerS = 300 > srcF/1024/1024 ? 300:srcF/1024/1024;
			while(sInfo.dfPath(mySDpath) < (biggerS+1)){
				if(dofinish) break;
				if (UvcDebug.DEBUG) Log.v(TAG, "should delOldfile_SD biggerS " + biggerS);
				sInfo.delOldfile_SD(zxDir);
			}
			mloopHandler.postDelayed(this, 1 * 1000);
		}
	};

	private void CheckStorage(){
		queueEvent(new Runnable() {
			@Override
			public void run() {
				sInfo.delOldfile(getActivity(),localPath);
			}
		}, 0);
	}

	private void chooseFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(Intent.createChooser(intent, "选择文件"), FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(getActivity(), "亲，木有文件管理器啊-_-!!", Toast.LENGTH_SHORT).show();
		}
	}

	private void chooseStorage(){
        HashMap<String,Integer> extPath = sInfo.getAvaliableStorage(getActivity());
        for (String key : extPath.keySet()) {
            if (UvcDebug.DEBUG) Log.v(TAG, "######## " + key + ", " + extPath.get(key)/1024 + "G");
        }

        final String[] arrayFruit = extPath.keySet().toArray(new String[0]);
        Dialog alertDialog = new AlertDialog.Builder(getActivity()).
                setTitle("选择存储路径").
                setItems(arrayFruit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), arrayFruit[which], Toast.LENGTH_SHORT).show();
							/*	File destDir = new File(arrayFruit[which] + "/ZX333");
								if (!destDir.exists()) {
									if (UvcDebug.DEBUG) Log.v(TAG, "mkdir " + arrayFruit[which] + "/ZX333");
									if(Build.VERSION.SDK_INT >= 23) {
										getActivity().requestPermissions(new String[]{android
												.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
									}
									boolean ret = destDir.mkdirs();
									if (UvcDebug.DEBUG) Log.v(TAG, "mkdir ret = " + ret);
								}*/
                        //	Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        //	startActivityForResult(intent, FILE_SELECT_USB);
                        mySDpath = arrayFruit[which];

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        }).create();
        alertDialog.show();
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (UvcDebug.DEBUG) Log.v(TAG, "onActivityResult: fragment");
		if (resultCode != Activity.RESULT_OK) {
			Log.e(TAG, "onActivityResult() error, resultCode: " + resultCode);
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}
		if (requestCode == FILE_SELECT_CODE) {
			Uri uri = data.getData();
			Log.i(TAG, "------->" + uri.getPath());
		}
		if (requestCode == FILE_SELECT_USB) {
			usbUri = data.getData();
			usbUriArray.add(usbUri);
			Log.i(TAG, "------->" + usbUri.getPath());
			Log.i(TAG, "------->" + usbUri);
			String uriStr = usbUri.toString();
			if (UvcDebug.DEBUG) Log.v(TAG, "uriString-" + uriStr);
			usbUri = Uri.parse(uriStr);
			Log.i(TAG, "2------->" + usbUri.getPath() + "  " + usbUri);
		}
		pickedDir = DocumentFile.fromTreeUri(getActivity(), usbUri);
		if(pickedDir.findFile("ZX333") == null) {
			DocumentFile dirZX = pickedDir.createDirectory("ZX333");
			if (dirZX == null) {
				Log.e(TAG, "fail create ZX333");
			} else if (dirZX.exists()) {
				//return;
			}
		}
        //chooseStorage();

		super.onActivityResult(requestCode, resultCode, data);
	}

	private final void enableButtons(final boolean enable) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mRecordButton.setEnabled(enable);
				mStillCaptureButton.setEnabled(enable);
				if(enable == false) {
					mRecordButton.setImageResource(R.drawable.record_gray);
					mStillCaptureButton.setImageResource(R.drawable.take_photo_gray);
				}
				else {
					mRecordButton.setImageResource(R.drawable.record);
					mStillCaptureButton.setImageResource(R.drawable.take_photo);
				}
				if (enable && mCameraClient.isRecording())
					mRecordButton.setImageResource(R.drawable.recording);
			}
		});
	}
	//
	/**
	 * Checks if the app has permission to write to device storage
	 *
	 * If the app does not has permission then the user will be prompted to grant permissions
	 *
	 * @param activity
	 */
	public static void verifyStoragePermissions(Activity activity) {
		// Check if we have write permission
		if(Build.VERSION.SDK_INT >= 23) {
			int permission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
			if (permission != PackageManager.PERMISSION_GRANTED) {
				// We don't have permission so prompt the user
				activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
			}
		}
	}

	private void uriOpt(){
		//content://com.android.externalstorage.documents/tree/F685-F357%3A
		if (UvcDebug.DEBUG) Log.v(TAG, "treeUri  " + usbUri.toString());
		pickedDir = DocumentFile.fromTreeUri(getActivity(), usbUri);
		// List all existing files inside picked directory
		for (DocumentFile file : pickedDir.listFiles()) {
			if (UvcDebug.DEBUG) Log.v(TAG, "Found file " + file.getName() + " with size " + file.length());
		}
		// Create a new file and write into it
		// DocumentFile newFile = pickedDir.createFile("text/plain", "My Novel");
		DocumentFile newFile = pickedDir.createFile("video/avc", "My Novel.mp4");
		DocumentFile newDir=pickedDir.createDirectory("ZX333");
		if(newDir==null){
			return;
		}else if(newDir.exists()){
			//return;
		}
		if(newFile==null){
			return;
		}else if(newFile.exists()){
			//return;
		}
		//content://com.android.externalstorage.documents/tree/F685-F357%3A/document/F685-F357%3Aifaboo
		Log.v(TAG, "uri="+newDir.getUri()+",name="+newDir.getName()+",type="+newDir.getType()+",p="+newDir.getParentFile().getName());
		DocumentFile newFile2=newDir.createFile("text/plain", "ls_copy");
		//vnd.android.document/directory
		OutputStream out=null;
		try {
			out = getActivity().getContentResolver().openOutputStream(newFile2.getUri());
			out.write("A long time ago...测试".getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private final Runnable miscWork = new Runnable() {
		int i;

		@Override
		public void run() {
			if(Looper.getMainLooper() == Looper.myLooper()){
				Log.d(TAG, "miscWork is Main looper ");
			}
			else
				Log.d(TAG, "miscWork is my looper ");
			i = 0;
			while(true) {
				if(dofinish) break;
				if (usbUri != null) {
					zxDir = pickedDir.findFile("ZX333");
					if (mloopHandler != null) mloopHandler.removeCallbacks(mLoopCPFile);
					mloopHandler.postDelayed(mLoopCPFile, 1000);
					break;
				}
				try {
					Log.d(TAG, "wait select usb " + i++);
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		/*	while(true){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/
		}
	};

	/**
	 * Thread to do something
	 * and write them to the MediaCodec encoder
	 */
	private class miscThread extends Thread {
        private Handler miscHandler;
        private final Object mSync = new Object();
		private static final int MSG_COPY = 0;
		private static final int MSG_OPEN = 0;

		@Override
		public void run() {
			Looper.prepare();
			//android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE); // THREAD_PRIORITY_URGENT_AUDIO
			Log.d(TAG, "miscThread currentThread " + Thread.currentThread().getId() + " main " + Looper.getMainLooper().getThread().getId());

			synchronized (mSync) {
				miscHandler = new Handler() {
				public void handleMessage(Message msg) {
                    switch (msg.what) {
						case MSG_COPY:
							break;
                        default:
                            throw new RuntimeException("unsupported message:what=" + msg.what);
                    }
					// process incoming messages here
				}
			};
                mSync.notifyAll();
            }
			if (UvcDebug.DEBUG) Log.v(TAG, "main " + Looper.getMainLooper() + " my " + Looper.myLooper());
			if (UvcDebug.DEBUG) Log.v(TAG, "Thread:" + Thread.currentThread().getId());
			Looper.loop();
		}

        public Handler getHandler() {
            if (UvcDebug.DEBUG) Log.d(TAG, "getHandler:");
            synchronized (mSync) {
                if (miscHandler == null)
                    try {
                        mSync.wait();
                    } catch (final InterruptedException e) {
                    }
            }
            return miscHandler;
        }

	}

}
