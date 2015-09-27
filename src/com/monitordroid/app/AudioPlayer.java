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

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

public class AudioPlayer extends Service {
	private static MediaPlayer mPlayer;

	/**
	 * Gets the URL of the desired sound file to be played and forwards it to
	 * the playMedia method
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String url = (String) intent.getExtras().get("url");
		playMedia(url);
		return START_NOT_STICKY;
	}

	/**
	 * @param url
	 *            The direct URL of the sound file to be played, i.e
	 *            "http://www.music.com/sound.mp3"
	 */
	public void playMedia(String url) {
		mPlayer = new MediaPlayer();
		mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mPlayer.setDataSource(url);
		}
		catch (IllegalArgumentException e) {

		}
		catch (IllegalStateException e) {

		}
		catch (IOException e) {

		}
		catch (Exception e) {

		}
		try {
			mPlayer.prepare();
		}
		catch (IllegalStateException e) {

		}
		// Thrown when trying to play from a URL that isn't a compatible sound
		// file
		catch (IOException e) {
		}
		// Free up the media player resource after completion
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				stop();
			}
		});
		mPlayer.start();
	}

	/**
	 * Stops playback, releases the MediaPlayer resource, and kills the service
	 */
	public void stop() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
			stopSelf();
		}
	}

	// If the service is destroyed from another class, make sure to free the
	// MediaPlayer resource
	@Override
	public void onDestroy() {
		stop();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
