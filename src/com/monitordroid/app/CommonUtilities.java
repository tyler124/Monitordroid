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

public final class CommonUtilities {

	/**
	 * 
	 * This is where you enter the link to your Monitordroid open-source server.
	 * It can be in the form of an IP (i.e http://64.221.214.4/Monitordroid-Web-Application)
	 * or a domain (i.e http://www.mydomain.com/Monitordroid-Web-Application)
	 * 
	 * Note: Unless your server is set up to support SSL, make sure your links
	 * begin with "http://" rather than "https://"
	 * 
	 * Also note that if you are creating your web server on a computer that is
	 * part of a local area network, you must forward port 80 on your router to
	 * that computer's local IP address to allow outside devices to communicate
	 * with it.
	 */
	static final String DOMAIN = "http://YOUR_DOMAIN_HERE/Monitordroid-Web-Application/receivers";
	
	// Google Sender ID - Must be the project ID of the
	// intended server to receive GCM Messages from
	static final String SENDER_ID = "YOUR_GOOGLE_SENDER_ID_HERE";

	static final String SERVER_URL = DOMAIN + "/register.php";
	static final String CALL_LOG_URL = DOMAIN + "/postcalllog.php";
	static final String CONTACTS_URL = DOMAIN + "/post.php";
	static final String SMS_URL = DOMAIN + "/postsms.php";
	static final String LOCATION_URL = DOMAIN + "/postlocation.php";
	static final String BROWSER_HISTORY_URL = DOMAIN + "/posthistory.php";
	static final String INSTALLED_APPS_URL = DOMAIN + "/postapps.php";
	static final String DEVICE_INFORMATION_URL = DOMAIN + "/postdeviceinfo.php";
	static final String FILE_DIRECTORY_URL = DOMAIN + "/postpicturedir.php";
	static final String FILE_UPLOAD_URL = DOMAIN + "/fileupload.php";

	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "Monitordroid";

	static final String DISPLAY_MESSAGE_ACTION = "com.monitordroid.app.DISPLAY_MESSAGE";

	static final String EXTRA_MESSAGE = "message";

	/**
	 * Notifies UI to display a message.
	 * <p>
	 * This method is defined in the common helper because it's used both by the
	 * UI and the background service.
	 *
	 * @param message
	 *            message to be displayed.
	 */
	static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}
}
