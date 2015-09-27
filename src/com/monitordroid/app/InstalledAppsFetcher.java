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

import static com.monitordroid.app.CommonUtilities.INSTALLED_APPS_URL;

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

import com.google.android.gcm.GCMRegistrar;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

public class InstalledAppsFetcher {

	/**
	 * Gets a list of the applications installed on the device then executes an
	 * Asynctask to post it to the web server
	 */
	public void fetchInstalledApps(Context context) {
		final PackageManager pm = context.getPackageManager();
		// get a list of installed apps.
		List<ApplicationInfo> packages = pm
				.getInstalledApplications(PackageManager.GET_META_DATA);
		String output = "";
		String regId = GCMRegistrar.getRegistrationId(context);
		for (ApplicationInfo packageInfo : packages) {
			output += "\n" + packageInfo.loadLabel(pm).toString() + "\n";
			output += packageInfo.packageName + "\n";
		}
		new MyAsyncTask().execute(output, regId);
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

		private void postData(String installedApps, String regId) {
			HttpClient httpclient = new DefaultHttpClient();
			String url = INSTALLED_APPS_URL;
			HttpPost httppost = new HttpPost(url);

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("installedApps",
						installedApps));
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
