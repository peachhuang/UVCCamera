package com.serenegiant.usbcameracommon;

import android.os.Environment;

public class Const {
	public static final int USB_CTRL_TRANSFER_TIMEOUT = 100;

	// Jni Notify
	public static final int NOTIFY_CANNOT_LOCK_WINDOW = 14;
	public static final int JNI_NOTIFY_GL_REQUEST_RENDER = 15;
	public static final int JNI_NOTIFY_CODEC_INIT = 16;
	public static final int JNI_NOTIFY_DECODE_TIME = 17;

	// Database
	public static final String LIBVLC_SN = "SONIX-9yjp-10eb-bu11-76at";
	public static final String FILE_FOLDER = "CarCam";

	public static final String download_path = Environment.getExternalStorageDirectory() + "/"+FILE_FOLDER;

	//sharepreference
	public static final String SHARED_PREFERENCE_PHONE_RECORD_LENGTH="SHARED_PREFERENCE_PHONE_RECORD_LENGTH";
	public static final String SHARED_PREFERENCE_CONTINUS_PIC_LENGTH="SHARED_PREFERENCE_CONTINUS_PIC_LENGTH";
	public static final String SHARED_PREFERENCE_IMAGE_FILTER_SWITCH="SHARED_PREFERENCE_IMAGE_FILTER_SWITCH";
	public static final String SHARED_PREFERENCE_FILTER_TIPS_SWITCH="SHARED_PREFERENCE_FILTER_TIPS_SWITCH";
	public static final String SHARED_PREFERENCE_SET_COMMAND_COUNT="SHARED_PREFERENCE_SET_COMMAND_COUNT";

	//MANUFACTURE-MODEL
	public final static String PREVIEW_SPECIAL_LIST="LeMobile-Le X620,HUAWEI-HUAWEI MT,Xiaomi-HM 1S";///HUAWEI-HUAWEI MT7-TL00

	public static final String SHARED_PREFERENCE_SPECIAL_PREVIEW="SHARED_PREFERENCE_SPECIAL_PREVIEW";

	public static final int LOW_PERF=1;
	public static final int HiGH_PERF=2;

	// Broadcast
	public static final String BROADCAST_CHANGE_TO_HWDECODE = "BROADCAST_CHANGE_TO_HWDECODE";

	//Decode method
	public static final int SW_DECODE = 1;
	public static final int HW_DECODE = 2;
	public static int DECODE_METHOD = HW_DECODE;
}
