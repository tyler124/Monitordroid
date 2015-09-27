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

import static com.monitordroid.app.CommonUtilities.FILE_UPLOAD_URL;
import static com.monitordroid.app.MainActivity.PREFS_NAME;

import java.io.File;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.android.gcm.GCMRegistrar;

public class UploadFile {

	File file;
	Long initT;
	String fileExtention;
	boolean mShouldDelete = false;

	/**
	 * Uploads the input file
	 * 
	 * @param f1
	 *            The file to upload
	 * @param shouldDelete
	 *            Whether or not to delete the file after uploading. We want to
	 *            do this with things like sound recordings, but not with
	 *            pictures
	 */
	public void uploadFile(Context context, File f1, boolean shouldDelete) {
		initT = System.currentTimeMillis();
		file = f1;
		String regId = GCMRegistrar.getRegistrationId(context);
		// Get saved account name
		SharedPreferences settings = context
				.getSharedPreferences(PREFS_NAME, 0);
		// Get the account name (account email) from memory that the user input
		// This will be used to place the uploaded file in the correct directory
		// on the server
		String username = settings.getString("username", "none");
		// Delete sound recordings after upload, but don't delete pictures
		mShouldDelete = shouldDelete;
		new MyAsyncTask().execute(regId, username);
	}

	// Posts files to the web server
	private class MyAsyncTask extends AsyncTask<String, String, Double> {

		@Override
		protected Double doInBackground(String... params) {
			postData(params[0], params[1]);
			return null;
		}

		protected void onPostExecute(Double result) {

		}

		private void postData(String regId, String username) {
			HttpClient httpclient = new DefaultHttpClient();
			String url = FILE_UPLOAD_URL;
			HttpPost httppost = new HttpPost(url);

			try {
				MultipartEntityBuilder builder = MultipartEntityBuilder
						.create();
				builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
				builder.addPart("data", new FileBody(file));
				// The account name input by the user
				builder.addTextBody("user", username);
				httppost.setEntity(builder.build());
				httpclient.execute(httppost);

				/*
				 * Code to read the response from the web server, if necessary.
				 */
				// HttpResponse response = httpclient.execute(httppost);
				// InputStream inputStream = response.getEntity().getContent();
				// BufferedReader in = new BufferedReader(new InputStreamReader(
				// inputStream));
				// String inputLine;
				// Read response from web server
				// while ((inputLine = in.readLine()) != null) {
				// Log.i("inputline", inputLine);
				// }
				// in.close();

				// If the file needs to be deleted, do so here after uploading
				if (mShouldDelete) {
					file.delete();
				}
			}

			catch (ClientProtocolException e) {

			}

			catch (IOException e) {

			}
		}
	}
}
