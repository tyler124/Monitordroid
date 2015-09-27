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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	/**
	 * Method called when the device has successfully rebooted. Used to recreate
	 * the alarm that keeps the GCM connection alive.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			if (intent.getAction().equals(
					"android.intent.action.BOOT_COMPLETED")) {
				GCMHeartbeatAlarm g = new GCMHeartbeatAlarm();
				g.setAlarm(context);
			}
		}
		catch (Exception e) {

		}
	}

}
