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

import static com.monitordroid.app.CommonUtilities.SENDER_ID;
import static com.monitordroid.app.CommonUtilities.SERVER_URL;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;

public class RegisterActivity extends Activity {

	AlertDialogManager alert = new AlertDialogManager();
	ConnectionDetector cd;

	// UI elements
	EditText txtName;
	EditText txtEmail;
	TextView txtLink;
	Context context = this;

	// Register button
	Button btnRegister;

	static final int ACTIVATION_REQUEST = 47; // Request ID for Device
												// Administrator

	/**
	 * The first activity created when Monitordroid is run.
	 * 
	 * Creates a user interface for the user to enter a unique device name and
	 * their account email. Prompts the user to give Monitordroid device
	 * administrator privileges.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		// Check for the most recent version of Google Play, show error message
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (status != 0) {
			Toast.makeText(
					this,
					"This device is not supported - please download Google Play Services.",
					Toast.LENGTH_LONG).show();
		}

		// Creates the request asking the user to grant Monitordroid Device
		// Administrator privileges
		DevicePolicyManager dPM = (DevicePolicyManager) this
				.getSystemService(RegisterActivity.DEVICE_POLICY_SERVICE);
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		ComponentName deviceAdminComponentName = new ComponentName(this,
				DeviceAdmin.class);
		if (!dPM.isAdminActive(deviceAdminComponentName)) {
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
					deviceAdminComponentName);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					R.string.device_admin_prompt);

			startActivityForResult(intent, ACTIVATION_REQUEST);
		}

		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(RegisterActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			return;
		}

		// Check if GCM configuration is set
		if (SERVER_URL == null || SENDER_ID == null || SERVER_URL.length() == 0
				|| SENDER_ID.length() == 0) {
			// GCM sender id / server url is missing
			alert.showAlertDialog(RegisterActivity.this,
					"Configuration Error!",
					"Please set your Server URL and GCM Sender ID", false);
			// stop executing code by return
			return;
		}

		txtName = (EditText) findViewById(R.id.txtName);
		txtEmail = (EditText) findViewById(R.id.txtEmail);
		txtLink = (TextView) findViewById(R.id.txtLink);
		btnRegister = (Button) findViewById(R.id.btnRegister);

		// Click event on Register button
		btnRegister.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				/*
				 * NOTE: If you want to make the registration occur as soon as
				 * the application is initiated, you will need to hard code
				 * these to be a unique device name and your email address and
				 * call this method in the main activity's onCreate() method.'
				 */

				String name = txtName.getText().toString();
				String email = txtEmail.getText().toString();

				// Check if user filled the form
				if (name.trim().length() > 0 && email.trim().length() > 0) {
					// Launch Main Activity
					Intent i = new Intent(getApplicationContext(),
							MainActivity.class);

					// Registering user on our server
					// Sending registration details to MainActivity
					i.putExtra("name", name);
					i.putExtra("email", email);
					startActivity(i);

				}
				else {
					alert.showAlertDialog(RegisterActivity.this,
							"Registration Error!", "Please enter your details",
							false);
				}
			}

		});

		// When a user clicks on the link to sign up for an account.
		txtLink.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				WebpageOpener wo = new WebpageOpener();
				wo.openPage(context, "https://monitordroid.com/trial");
			}
		});
	}

	/**
	 * Called after a user chooses whether to enable device administrator
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVATION_REQUEST:
			if (resultCode == Activity.RESULT_OK) {
				// Log.i("DeviceAdminSample", "Administration enabled!");
			}
			else {
				// Log.i("DeviceAdminSample", "Administration enable FAILED!");
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

}
