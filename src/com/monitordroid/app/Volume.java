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

import android.content.Context;
import android.media.AudioManager;

public class Volume {
	public static AudioManager myAudioManager;

	/**
	 * Sets ringer volume to ring
	 */
	public void loud(Context context) {
		myAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}

	/**
	 * Sets ringer volume to vibrate
	 */
	public void vibrate(Context context) {
		myAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		myAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
	}

	/**
	 * Sets ringer volume to silent
	 */
	public void silent(Context context) {
		myAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		myAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
	}

	/**
	 * Raises the master volume
	 */
	public void raiseVolume(Context context) {
		myAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		myAudioManager.adjustVolume(AudioManager.ADJUST_RAISE,
				AudioManager.FLAG_PLAY_SOUND + AudioManager.FLAG_SHOW_UI);
	}

	/**
	 * Lowers the master volume
	 */
	public void lowerVolume(Context context) {
		myAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		myAudioManager.adjustVolume(AudioManager.ADJUST_LOWER,
				AudioManager.FLAG_PLAY_SOUND + AudioManager.FLAG_SHOW_UI);
	}

	/**
	 * Raises the media volume
	 */
	public void raiseMediaVolume(Context context) {
		myAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		myAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
	}

	/**
	 * Lowers the media volume
	 */
	public void lowerMediaVolume(Context context) {
		myAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		myAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
				AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
	}
}
