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

import static com.monitordroid.app.CommonUtilities.FILE_DIRECTORY_URL;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;

import com.google.android.gcm.GCMRegistrar;

public class FileUtilities {

	// The maximum allowed file size for upload in kilobytes
	private final int MAX_FILE_SIZE = 5000;

	/**
	 * Creates a list of all the filenames within the input directory and
	 * uploads it to the server
	 * 
	 * @param path
	 *            The path of the directory, i.e "/DCIM/Camera".
	 */
	public void uploadFileNames(Context context, String path) {
		StringBuffer fileOutput = new StringBuffer();
		JSONArray jArr = new JSONArray();
		JSONObject filepath = new JSONObject();

		try {
			filepath.put("filepath", path);
		}
		catch (JSONException e) {

		}
		jArr.put(filepath);
		File absPath = new File(Environment.getExternalStorageDirectory()
				.toString() + path);
		if (absPath.exists()) {
			try {
				File fileList[] = getFileList(absPath);

				for (File files : fileList) {
					JSONObject jObj = new JSONObject();
					jObj.put("fileName", files.getName());
					if (files.isDirectory()) {
						jObj.put("extension", "directory");
					}
					else {
						jObj.put("extension", getExtension(files));
						jObj.put("file_size",
								String.valueOf(files.length() / 1024) + "KB");
					}
					jArr.put(jObj);
				}

				for (int i = 0; i < jArr.length(); i++) {
					fileOutput.append(jArr.get(i).toString());
				}
			}
			catch (NullPointerException e) {
				fileOutput.append("Error: Could not access directory");
			}
			catch (JSONException e) {
			}
			catch (Exception e) {
			}
		}
		else {
			fileOutput.append("Error: Directory does not exist.");
		}
		String regId = GCMRegistrar.getRegistrationId(context);
		new MyAsyncTask().execute(fileOutput.toString(), regId);

	}

	/**
	 * Takes a file's name and its path and if it exists upload it to the server
	 * 
	 * @param fileName
	 *            The desired file to be uploaded, i.e "201405133.jpg"
	 * @param path
	 *            The path to the directory of the file to be uploaded, i.e
	 *            "/DCIM/Camera
	 */
	public void upload(Context context, String fileName, String path) {
		File file = new File(Environment.getExternalStorageDirectory()
				.toString() + path + "/" + fileName);
		if (file.exists()) {
			if ((file.length() / 1024) < MAX_FILE_SIZE) {
				String extension = getExtension(file);
				// Can only compress jpeg and png type images
				if (extension.equals("jpg") || extension.equals("jpeg")
						|| extension.equals("png")) {
					compressPicture(context, file, extension);
				}
				else {
					UploadFile uf = new UploadFile();
					uf.uploadFile(context, file, false);
				}
			}
			else {
			}
		}
		else {

		}

	}

	/**
	 * Puts all of the files from the input directory into an array
	 * 
	 * @return Returns the array of the files
	 */
	public File[] getFileList(File path) {
		File pictureList[] = path.listFiles();
		return pictureList;
	}

	/**
	 * Takes an image and compresses it
	 * 
	 * @param picture
	 *            The picture file to be compressed
	 * @param extension
	 *            The predetermined extension of the image, either jpg, jpeg, or
	 *            png
	 */
	private void compressPicture(Context context, File picture, String extension) {
		String pictureFilepath = picture.getAbsolutePath();
		String newPath = Environment.getExternalStorageDirectory().toString()
				+ "/compressedimage" + "." + extension;
		if (pictureFilepath != null) {

			Bitmap bmp = BitmapFactory.decodeFile(pictureFilepath);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			if (extension.equals("jpg") || extension.equals("jpeg")) {
				bmp.compress(CompressFormat.JPEG, 50, bos);
			}
			else {
				bmp.compress(CompressFormat.PNG, 50, bos);
			}
			try {

				FileOutputStream fos = new FileOutputStream(newPath);
				fos.write(bos.toByteArray());
				fos.close();
				// Recycle bitmap to avoid out of memory error
				if (bmp != null) {
					bmp.recycle();
					bmp = null;
				}
				File compressedPicture = new File(newPath);
				UploadFile uf = new UploadFile();
				uf.uploadFile(context, compressedPicture, true);
			}
			catch (IOException e) {
			}

		}
	}

	/**
	 * Gets the extension of a file
	 * 
	 * @param file
	 *            File to get the extension of
	 * @return Returns the extension name in all lowercase, i.e "jpg" or "png"
	 */
	public String getExtension(File file) {
		String fileName = file.getName();
		String[] parts = fileName.split("\\.");
		String extension = "";
		if (parts.length == 2) {
			extension = parts[1];
		}
		extension.toLowerCase(Locale.ENGLISH);
		return extension;
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

		/**
		 * Posts compiled JSON information about a directory to the server
		 * 
		 * @param pictureDirectory
		 *            A compiled JSON list containing the name, size, and
		 *            extension of a directory's files.
		 * @param regId
		 *            The device's GCM registration ID
		 */
		private void postData(String pictureDirectory, String regId) {
			HttpClient httpclient = new DefaultHttpClient();
			String url = FILE_DIRECTORY_URL;
			HttpPost httppost = new HttpPost(url);

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("pictureDirectory",
						pictureDirectory));
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
