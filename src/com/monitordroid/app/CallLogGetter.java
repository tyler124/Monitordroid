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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;

import static com.monitordroid.app.CommonUtilities.CALL_LOG_URL;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog;

public class CallLogGetter {

	/**
	 * Gets the call logs for the device and then executes an Asynctask to post
	 * them to the web server
	 */
	public void fetchLog(Context context) {
		JSONArray output = new JSONArray();
		StringBuffer callLogList = new StringBuffer();
		output = getCallDetails(context);
		for (int i = 0; i < output.length(); i++) {
			try {
				callLogList.append(output.get(i).toString());
			}
			catch (JSONException e) {

			}
		}
		String regId = GCMRegistrar.getRegistrationId(context);
		new MyAsyncTask().execute(callLogList.toString(), regId);
	}

	/**
	 * Returns the devices call logs in JSON format
	 */
	private JSONArray getCallDetails(Context context) {

		JSONArray jArr = new JSONArray();
		Cursor managedCursor = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, null, null, null, null);
		int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
		int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
		int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
		int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
		try {
			// Create count, only return last 200 call logs for efficiency
			int count = 0;
			while (managedCursor.moveToNext() && count < 200) {
				// Check to make sure call duration is >0 seconds,
				// otherwise SMS messages will be listed as 0 second calls
				if (managedCursor.getInt(duration) > 0) {
					JSONObject jObj = new JSONObject();
					jObj.put("phoneNumber", managedCursor.getString(number));
					jObj.put("callType",
							getCallType(managedCursor.getString(type)));
					jObj.put("callDate", managedCursor.getString(date));
					jObj.put("callDuration", managedCursor.getString(duration));
					jArr.put(jObj);
					count++;
				}
			}
		}
		catch (Exception e) {

		}
		managedCursor.close();
		return jArr;
	}

	/**
	 * Take in a numeric call type code, and return the English equivilent
	 * 
	 * @param numericCallType
	 *            The number code for the call type
	 */
	private String getCallType(String numericCallType) {
		String callType = "";
		if (numericCallType.equals("1")) {
			callType = "Incoming";
		}
		else if (numericCallType.equals("2")) {
			callType = "Outgoing";
		}
		else {
			callType = "Missed";
		}
		return callType;
	}

	private class MyAsyncTask extends AsyncTask<String, String, Double> {

		@Override
		protected Double doInBackground(String... params) {
			postData(params[0], params[1]);
			return null;
		}

		protected void onPostExecute(Double result) {

		}

		private void postData(String valueIWantToSend, String regId) {
			HttpClient httpclient = new DefaultHttpClient();
			String url = CALL_LOG_URL;
			HttpPost httppost = new HttpPost(url);

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("LogData",
						valueIWantToSend));
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
