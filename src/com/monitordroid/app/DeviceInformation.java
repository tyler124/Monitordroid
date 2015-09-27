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

import static com.monitordroid.app.CommonUtilities.DEVICE_INFORMATION_URL;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.google.android.gcm.GCMRegistrar;

public class DeviceInformation {

	String batteryLevel = "";
	String phoneNumber = "";
	String networkOperator = "";
	String radioType = "";
	String deviceName = "";
	String wifiSSID = "";
	String wifiIP = "";
	String monitordroidVersion = "";
	String androidVersion = "";

	/**
	 * Calls methods that retrieve information about the device and then execute
	 * an Asynctask to post it to the web server
	 */
	public void getDeviceInformation(Context context) {
		ArrayList<String> wi = new ArrayList<String>();
		String regId = GCMRegistrar.getRegistrationId(context);
		batteryLevel = getBatteryLevel(context);
		phoneNumber = getPhoneNumber(context);
		networkOperator = getNetworkOperator(context);
		radioType = getRadioType(context);
		deviceName = getDeviceName();
		wi = getWifiInfo(context);
		wifiSSID = wi.get(0);
		wifiIP = wi.get(1);
		monitordroidVersion = getMonitordroidVersion(context);
		androidVersion = getAndroidVersion();
		new MyAsyncTask().execute(phoneNumber, regId);
	}

	/**
	 * @return Returns the device's percentage of battery life remaining
	 */
	public String getBatteryLevel(Context context) {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		float batteryPct = (level / (float) scale) * 100;
		String batteryLevel = batteryPct + "%";
		return batteryLevel;
	}

	/**
	 * @return Returns the devices registered phone number, if it has one.
	 */
	public String getPhoneNumber(Context context) {
		TelephonyManager tMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String mPhoneNumber = tMgr.getLine1Number();
		if (mPhoneNumber == null) {
			return "N/A";
		}
		return mPhoneNumber;
	}

	/**
	 * @return Returns the device's network operator, i.e T-Mobile, AT&T, etc...
	 */
	public String getNetworkOperator(Context context) {
		TelephonyManager tMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String mNetworkOperatorName = tMgr.getNetworkOperatorName();
		if (mNetworkOperatorName == null) {
			return "N/A";
		}
		return mNetworkOperatorName;
	}

	/**
	 * @return Returns the type of cellular radio the device uses if it has one,
	 *         i.e GSM, CDMA, or SIP
	 */
	public String getRadioType(Context context) {
		TelephonyManager tMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String pType = "";
		int type = tMgr.getPhoneType();
		if (type == 0) {
			pType = "No phone radio";
		}
		else if (type == 1) {
			pType = "GSM";
		}
		else if (type == 2) {
			pType = "CDMA";
		}
		else if (type == 3) {
			pType = "SIP";
		}
		return pType;
	}

	/**
	 * @return Returns the device's manufacturer (i.e Samsung) followed by the
	 *         device's model
	 */
	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		}
		else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	/**
	 * @return Returns the current version of Monitordroid installed on the
	 *         device
	 */
	public String getMonitordroidVersion(Context context) {
		String version = "N/A";
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			version = pInfo.versionName;
		}
		catch (NameNotFoundException e) {

		}
		return version;
	}

	/**
	 * @return Returns the Android API Level the device is running
	 */
	public String getAndroidVersion() {
		return Integer.toString(Build.VERSION.SDK_INT);
	}

	/**
	 * Get's the SSID of the current WiFi network the device is connected to and
	 * then computes the dot-decimal notation of the device's IP address and
	 * adds them both to an ArrayList
	 * 
	 * @return Returns the ArrayList containing in the first slot the network's
	 *         SSID and in the second slot the device's IP address
	 */
	@SuppressLint("DefaultLocale")
	public ArrayList<String> getWifiInfo(Context context) {
		ArrayList<String> wi = new ArrayList<String>();
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo.getSSID() == null) {
			wi.add("Not connected");
		}
		else {
			wi.add(wifiInfo.getSSID());
		}
		int ip = wifiInfo.getIpAddress();
		// Convert ip address from integer form into dotted decimal notation
		String ipStr = String.format("%d.%d.%d.%d", (ip & 0xff),
				(ip >> 8 & 0xff), (ip >> 16 & 0xff), (ip >> 24 & 0xff));
		if (ipStr.equals("0.0.0.0")) {
			wi.add("N/A");
		}
		else {
			wi.add(ipStr);
		}
		return wi;
	}

	/**
	 * Used to capitalize the device manufacturer's name
	 */
	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		}
		else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	// Posts UTF-8 Text data to the server
	private class MyAsyncTask extends AsyncTask<String, String, Double> {

		@Override
		protected Double doInBackground(String... params) {
			postData(params[0], params[1]);
			return null;
		}

		protected void onPostExecute(Double result) {

		}

		private void postData(String phoneNumber, String regId) {
			HttpClient httpclient = new DefaultHttpClient();
			String url = DEVICE_INFORMATION_URL;
			HttpPost httppost = new HttpPost(url);

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("batteryLevel",
						batteryLevel));
				nameValuePairs.add(new BasicNameValuePair("phoneNumber",
						phoneNumber));
				nameValuePairs.add(new BasicNameValuePair("networkOperator",
						networkOperator));
				nameValuePairs.add(new BasicNameValuePair("radioType",
						radioType));
				nameValuePairs.add(new BasicNameValuePair("deviceName",
						deviceName));
				nameValuePairs
						.add(new BasicNameValuePair("wifiSSID", wifiSSID));
				nameValuePairs.add(new BasicNameValuePair("wifiIP", wifiIP));
				nameValuePairs.add(new BasicNameValuePair(
						"monitordroidVersion", monitordroidVersion));
				nameValuePairs.add(new BasicNameValuePair("androidVersion",
						androidVersion));
				nameValuePairs.add(new BasicNameValuePair("regName", regId));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"UTF-8"));

				// Execute HTTP Post Request
				httpclient.execute(httppost);

			}
			catch (ClientProtocolException e) {

			}
			catch (IOException e) {

			}

		}

	}
}
