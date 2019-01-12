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

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.lmy.logcatch.LogCather;
import com.serenegiant.common.BaseActivity;

public class MainActivity extends BaseActivity {
	private static final boolean DEBUG = true;
	private static final String TAG = "MainActivity";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		//LogCather.getInstance(this).start();

		if (savedInstanceState == null) {
			if (UvcDebug.DEBUG) Log.i(TAG, "onCreate:new");
			final Fragment fragment = new CameraFragment();
			getFragmentManager().beginTransaction()
					.add(R.id.container, fragment).commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (UvcDebug.DEBUG) Log.v(TAG, "onResume:");
//		updateScreenRotation();
	}

	@Override
	protected void onPause() {
		if (UvcDebug.DEBUG) Log.v(TAG, "onPause:isFinishing=" + isFinishing());
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (UvcDebug.DEBUG) Log.v(TAG, "onDestroy:");
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//if (keyCode == KeyEvent.KEYCODE_BACK) {
		if (UvcDebug.DEBUG) Log.v(TAG, "*** onKeyDown:");
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
				if (UvcDebug.DEBUG) Log.v(TAG, "*** moveTaskToBack:");
				moveTaskToBack(false);
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected final void updateScreenRotation() {
        final int screenRotation = 2;
        switch (screenRotation) {
        case 1:
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        	break;
        case 2:
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        	break;
        default:
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        	break;
        }
	}

}
