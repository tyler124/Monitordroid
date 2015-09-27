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

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class DeviceAdmin extends DeviceAdminReceiver {

	/**
	 * @return Returns an instance of a DevicePolicyManager object
	 */
	private DevicePolicyManager getDevicePolicyManager(Context context) {
		DevicePolicyManager dPM = (DevicePolicyManager) context
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		return dPM;
	}

	/**
	 * @return Returns the DeviceAdminReceiver's component name
	 */
	private ComponentName getAdminName(Context context) {
		ComponentName DeviceAdmin = new ComponentName(context,
				DeviceAdmin.class);
		return DeviceAdmin;
	}

	/**
	 * Immediately locks the device
	 */
	public void lockDevice(Context context) {
		DevicePolicyManager dPM = getDevicePolicyManager(context);
		ComponentName DeviceAdmin = getAdminName(context);
		if (dPM.isAdminActive(DeviceAdmin)) {
			dPM.lockNow();
		}
	}

	/**
	 * Sets a new password used to unlock the device
	 * 
	 * @param password
	 *            The new password that will be used to unlock the device
	 * 
	 *            Note: The password should not contain Java escape characters
	 *            such as "\" or empty quotes (""), otherwise the password will
	 *            not be set as expected
	 */
	public void resetPassword(Context context, String password) {
		DevicePolicyManager dPM = getDevicePolicyManager(context);
		ComponentName DeviceAdmin = getAdminName(context);
		if (dPM.isAdminActive(DeviceAdmin)) {
			dPM.resetPassword(password,
					DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
			lockDevice(context);
		}
	}

	/**
	 * Sets whether the device's camera is disabled
	 * 
	 * @param disable
	 *            Set true to disable devices camera, false to enable device's
	 *            camera
	 */
	public void disableCamera(Context context, boolean disable) {
		DevicePolicyManager dPM = getDevicePolicyManager(context);
		ComponentName DeviceAdmin = getAdminName(context);
		if (dPM.isAdminActive(DeviceAdmin)) {
			boolean isCameraDisabled = dPM.getCameraDisabled(DeviceAdmin);
			// If the camera isn't already disabled and the user wants to
			// disable the camera (disable is true), disable the device's camera
			if (!isCameraDisabled && disable) {
				dPM.setCameraDisabled(DeviceAdmin, disable);
			}
			// If the camera is already disabled and the user wants to enable
			// the camera (disable is false), enable the device's camera
			if (isCameraDisabled && !disable) {
				dPM.setCameraDisabled(DeviceAdmin, disable);
			}
		}
	}

	@Override
	public void onEnabled(Context context, Intent intent) {
		super.onEnabled(context, intent);
	}

	/**
	 * The message to display when a user attempts to disable Monitordroid as a
	 * device administrator, which will allow them to uninstall the Monitordroid
	 * application from the device.
	 */
	@Override
	public String onDisableRequested(Context context, Intent intent) {
		return "Warning: Disabling device administrator privledges for this application will"
				+ " significantly reduce system functionality.";
	}

	@Override
	public void onDisabled(Context context, Intent intent) {
		super.onDisabled(context, intent);
	}

	@Override
	public void onPasswordChanged(Context context, Intent intent) {
		super.onPasswordChanged(context, intent);
	}

}
