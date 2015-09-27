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

import static com.monitordroid.app.CommonUtilities.SERVER_URL;
import static com.monitordroid.app.CommonUtilities.displayMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.content.Context;

import com.google.android.gcm.GCMRegistrar;

public final class ServerUtilities {
	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	/**
	 * Register this account/device pair within the server using Account
	 * Name/Email Strings from registration screen.
	 */
	static void register(final Context context, String name, String email,
			final String regId) {
		String serverUrl = SERVER_URL;
		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);
		params.put("name", name);
		params.put("email", email);
		boolean deviceAdded;

		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

		// Retry connection 5 times

		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			try {
				displayMessage(context, context.getString(
						R.string.server_registering, i, MAX_ATTEMPTS));

				// Post method will return true or false depending on whether
				// the device was added successfully on the server
				// If false, it is probably because the input account
				// email/username is not an active account
				deviceAdded = post(serverUrl, params);
				if (deviceAdded) {
					GCMRegistrar.setRegisteredOnServer(context, true);
					String message = context
							.getString(R.string.server_registered)
							+ " \""
							+ name
							+ "\" "
							+ context.getString(R.string.to_account)
							+ " \""
							+ email + "\".";
					CommonUtilities.displayMessage(context, message);
				}
				else {
					GCMRegistrar.setRegisteredOnServer(context, false);
					String message = context
							.getString(R.string.server_not_added)
							+ " \""
							+ name
							+ "\" "
							+ context.getString(R.string.to_account)
							+ " \""
							+ email
							+ "\". "
							+ context.getString(R.string.ensure_spelling);
					CommonUtilities.displayMessage(context, message);
				}
				return;
			}
			catch (IOException e) {
				if (i == MAX_ATTEMPTS) {
					break;
				}
				try {
					Thread.sleep(backoff);
				}
				catch (InterruptedException e1) {
					// Activity finished before completed - exit.
					Thread.currentThread().interrupt();
					return;
				}
				// increase backoff exponentially
				backoff *= 2;
			}
		}
		String message = context.getString(R.string.server_register_error,
				MAX_ATTEMPTS);
		CommonUtilities.displayMessage(context, message);
	}

	/**
	 * Unregister this account/device pair within the server.
	 */
	static void unregister(final Context context, final String regId) {
		String serverUrl = SERVER_URL + "/unregister";
		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);
		try {
			post(serverUrl, params);
			GCMRegistrar.setRegisteredOnServer(context, false);
			String message = context.getString(R.string.server_unregistered);
			CommonUtilities.displayMessage(context, message);
		}
		catch (IOException e) {
		}
	}

	/**
	 * Issue a POST request to the server.
	 *
	 * @param endpoint
	 *            POST address.
	 * @param params
	 *            request parameters containing the new GCM ID, account name,
	 *            and device name
	 *
	 * @throws IOException
	 *             propagated from POST.
	 * 
	 * @return boolean indicating whether the device was successfully added to
	 *         the server's database.
	 */
	private static boolean post(String endpoint, Map<String, String> params)
			throws IOException {

		URL url;
		boolean deviceAdded = false;
		try {
			url = new URL(endpoint);
		}
		catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
					.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();

			// Input reader from Monitordroid Server Servers response will
			// either be "existsandcompleted" indicating the device was
			// succesfully added to the database or "didnotcomplete", indicating
			// that the device was not successfully added.

			// If adding the device is not successful, it is probably due to the
			// user entering a non-existent account into the "Account Email"
			// field.
			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				if (inputLine.contains("existsandcompleted")) {
					deviceAdded = true;
				}
				if (inputLine.contains("didnotcomplete")) {
					deviceAdded = false;
				}
			}
			in.close();

			// handle the response
			int status = conn.getResponseCode();
			if (status != 200) {
				throw new IOException("Post failed with error code " + status);
			}
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return deviceAdded;
	}
}
