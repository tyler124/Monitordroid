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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class GCMHeartbeatAlarm extends BroadcastReceiver {

	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;

	/**
	 * Function called when the alarm set below is triggered (every 5 minutes).
	 * 
	 * Attempts to fix the GCM bug that results in a dropped connection by
	 * sending heartbeats every 5 minutes to keep the connection alive.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			context.sendBroadcast(new Intent(
					"com.google.android.intent.action.GTALK_HEARTBEAT"));
			context.sendBroadcast(new Intent(
					"com.google.android.intent.action.MCS_HEARTBEAT"));
		}
		catch (Exception e) {
		}
	}

	/**
	 * Sets a repeating alarm to be triggered every 5 minutes.
	 */
	public void setAlarm(Context context) {
		try {
			alarmMgr = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(context, GCMHeartbeatAlarm.class);
			alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime() + 60 * 1000 * 5,
					60 * 1000 * 5, alarmIntent);
		}
		catch (Exception e) {
		}
	}

}
