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

import static com.monitordroid.app.CommonUtilities.SMS_URL;

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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.SmsManager;

import com.google.android.gcm.GCMRegistrar;

public class SMSUtilities {

	boolean isUpdate;
	static final int MESSAGES_PER_BATCH = 600;

	/**
	 * Sends an SMS Message from the device to a specified phone number
	 * 
	 * @param phoneNumber
	 *            The phone number of the intended recipient
	 * @param SMSMessage
	 *            The SMS message to send
	 */
	public void sendSMS(Context context, String phoneNumber, String SMSMessage) {
		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(phoneNumber, null, SMSMessage, null,
					null);
			// Stores the new message in the device's sent SMS folder
			// so that it is visible to the Android GUI
			ContentValues values = new ContentValues();
			values.put("address", phoneNumber);
			values.put("body", SMSMessage);
			context.getContentResolver().insert(
					Uri.parse("content://sms/sent"), values);
		}
		catch (Exception e) {
		}
	}

	/**
	 * Gets 600 SMS Messages from the device per iteration and then executes an
	 * Asynctask to upload them to the server
	 * 
	 * @param iteration
	 *            States which batch of messages to send to the server. For
	 *            example, iteration "1" will send the most recent 600 messages
	 *            iteration 2 will send the next 600 in order, and so on...
	 * 
	 * @param resolveContacts
	 *            Whether to resolve the phone numbers associated with text
	 *            message to a contact's name on the device if one matches. This
	 *            will exponentially increase the time it takes the SMS
	 *            retrieval algorithm to complete
	 */
	public void fetchSMS(Context context, int iteration) {

		StringBuffer messageList = new StringBuffer();
		// Call smsReader to fill output with text data
		JSONArray output = new JSONArray();
		output = smsReader(context, iteration);
		for (int i = 0; i < output.length(); i++) {
			try {
				messageList.append(output.get(i).toString());
			}
			catch (JSONException e) {

			}
		}
		String regId = GCMRegistrar.getRegistrationId(context);

		// If the program is updating the first messages, we don't want them to
		// be
		// concatenated to the end of other messages, so we need to signal the
		// server not to.
		if (iteration == 1) {
			isUpdate = true;
		}
		else {
			isUpdate = false;
		}
		new MyAsyncTask().execute(messageList.toString(), regId);
	}

	/**
	 * Puts a batch of 600 text messages (both inbox, outbox, and other
	 * messages) into JSON format and returns an array of the JSON Objects.
	 * 
	 * @param iteration
	 *            States which batch of messages to send to the server. For
	 *            example, iteration "1" will send the most recent 600 messages
	 *            iteration 2 will send the next 600 in order, and so on...
	 *
	 * @return Returns a JSON array, with each index being a JSON Object
	 *         containing a single formatted text message
	 */
	private JSONArray smsReader(Context context, int iteration) {

		JSONArray jArr = new JSONArray();
		int stoppingPoint = 0;
		boolean validCursor = false;

		try {
			Cursor cursor = context.getContentResolver().query(
					Uri.parse("content://sms/"), null, null, null, null);

			if (iteration == 1) {
				cursor.moveToFirst();
				stoppingPoint = MESSAGES_PER_BATCH;
				validCursor = true;
			}

			else {
				// Each cursor position is a SMS Message, 600 messages is around
				// 60kb of data. Send 600 messages each iteration.
				if (cursor != null
						&& cursor.moveToPosition(MESSAGES_PER_BATCH
								* (iteration - 1))) {
					stoppingPoint = MESSAGES_PER_BATCH * iteration;
					validCursor = true;
				}
				else {
					cursor.close();
				}
			}

			if (validCursor) {

				do {
					JSONObject jObj = new JSONObject();
					for (int idx = 0; idx < cursor.getColumnCount(); idx++) {

						// We only want the address(phone number), body, date,
						// and type of the message
						if (cursor.getColumnName(idx).equals("address")
								|| cursor.getColumnName(idx).equals("body")
								|| cursor.getColumnName(idx).equals("date")
								|| cursor.getColumnName(idx).equals("type")) {
							if (cursor.getColumnName(idx).equals("address")) {
								if (cursor.getString(idx) != null) {
									jObj.put("phonenumber",
											cursor.getString(idx));
								}
								else {
									jObj.put("phonenumber", "Draft Message");
								}
							}

							else if (cursor.getColumnName(idx).equals("type")) {
								if (cursor.getString(idx).contains("1")) {
									jObj.put("mailbox", "Inbox");
								}
								else {
									jObj.put("mailbox", "Outbox");
								}
							}
							else if (cursor.getColumnName(idx).equals("date")) {
								jObj.put("date", cursor.getLong(idx));
							}
							// The remaining field contains the SMS message
							// content
							else {
								jObj.put("message", cursor.getString(idx));
							}

						}
					}
					jArr.put(jObj);
				}
				// Iterate until the desired message size is reached or there is
				// no more data
				while (cursor.moveToNext()
						&& (cursor.getPosition() < stoppingPoint));
				cursor.close();
			}
		}
		catch (Exception e) {
			// Device probably doesn't have SMS Capabilities
			JSONObject errorMessage = new JSONObject();
			try {
				errorMessage
						.put("message",
								"Error retreiving SMS data from the device. Device may not have SMS capabilities");
			}
			catch (JSONException error) {

			}
			jArr.put(errorMessage);
		}

		return jArr;
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

		private void postData(String smsData, String regId) {
			HttpClient httpclient = new DefaultHttpClient();
			String url = SMS_URL;
			HttpPost httppost = new HttpPost(url);

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				// Signal to the server that these are the newest messages and
				// should not be concatenated
				if (isUpdate) {
					nameValuePairs.add(new BasicNameValuePair(
							"FirstUpdateData", smsData));
				}
				// Otherwise signal to concatenate these messages onto previous
				// messages in the database
				else {
					nameValuePairs.add(new BasicNameValuePair("SMSData",
							smsData));
				}
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
