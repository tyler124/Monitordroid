/**
 * Copyright (C) 2015 Monitordroid Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Tyler Butler
 **/

package com.monitordroid.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;

public class Flashlight {

	public static Camera cam;
	public static boolean isOn;

	/**
	 * Turns the device's flashlight on
	 */
	public void flashOn(Context context) {
		try {
			if (!isOn) {
				boolean hasFlash = context.getPackageManager()
						.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

				if (hasFlash) {
					isOn = true;
					cam = Camera.open();
					Parameters p = cam.getParameters();
					p.setFlashMode(Parameters.FLASH_MODE_TORCH);
					cam.setParameters(p);
					cam.startPreview();
				}
			}
		}
		// Can't connect to camera service
		catch (RuntimeException e) {

		}
	}

	/**
	 * Turns the device's flashlight off
	 */
	public void flashOff(Context context) {
		try {
			if (isOn) {
				isOn = false;
				cam.stopPreview();
				cam.release();
			}
		}
		// Can't connect to camera service
		catch (RuntimeException e) {

		}
	}

}
