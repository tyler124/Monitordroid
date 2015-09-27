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

import static com.monitordroid.app.CommonUtilities.LOCATION_URL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationService extends Service implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	String regId = "";
	String mTime = "";
	String mAccuracy = "";
	int minutesTillRefresh;

	private boolean currentlyProcessingLocation = false;
	private LocationRequest locationRequest;
	private LocationClient locationClient;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	/**
	 * When the service is initially started, extract the desired minutes
	 * between location refreshes from the intent
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		minutesTillRefresh = (Integer) intent.getExtras().get(
				"minutesTillRefresh");
		if (!currentlyProcessingLocation) {
			currentlyProcessingLocation = true;
			startTracking();
		}

		return START_NOT_STICKY;
	}

	/**
	 * Connect to Google Play Services
	 */
	private void startTracking() {
		if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
			locationClient = new LocationClient(this, this, this);

			if (!locationClient.isConnected() || !locationClient.isConnecting()) {
				locationClient.connect();
			}
		}
		else {
		}
	}

	/**
	 * Stop location updates and kill the service
	 */
	@Override
	public void onDestroy() {
		stopLocationUpdates();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Called when a new location has been acquired
	 * 
	 * Extracts the latitude, longitude, time, and accuracy from the location
	 * object then executes an Asynctask to post them to the web server.
	 */
	@Override
	public void onLocationChanged(Location loc) {
		if (loc != null) {
			loc.getLatitude();
			loc.getLongitude();
			loc.getTime();
			// Returns accuracy of location lock in meters
			loc.getAccuracy();
			String newLat = String.valueOf(loc.getLatitude());
			String newLong = String.valueOf(loc.getLongitude());
			String time = String.valueOf(loc.getTime());
			String formattedDate = millisToDate(Long.parseLong(time));
			String accuracy = String.valueOf(loc.getAccuracy());
			mTime = formattedDate;
			mAccuracy = accuracy;
			regId = GCMRegistrar.getRegistrationId(LocationService.this);
			new MyAsyncTask().execute(newLat, newLong);
			if (minutesTillRefresh == 0) {
				stopSelf();
			}
		}
	}

	/**
	 * Takes in a measured amount of milliseconds since January 1st, 1970 and
	 * converts it into a calendar date and time
	 * 
	 * @param currentTime
	 *            in milliseconds since January 1st, 1970
	 * @return The formatted calendar date of that time
	 */
	private String millisToDate(long currentTime) {
		String finalDate;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(currentTime);
		Date date = calendar.getTime();
		finalDate = date.toString();
		return finalDate;
	}

	/**
	 * Stop requesting location updates and disconnect from Google Play Services
	 */
	private void stopLocationUpdates() {
		if (locationClient != null && locationClient.isConnected()) {
			locationClient.removeLocationUpdates(this);
			locationClient.disconnect();
		}
	}

	/**
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle bundle) {
		locationRequest = LocationRequest.create();
		if (minutesTillRefresh != 0) {
			locationRequest.setInterval(1000 * 60 * minutesTillRefresh);
			locationRequest.setFastestInterval(1000 * 60 * minutesTillRefresh);
		}
		else {
			locationRequest.setInterval(1); // Single update, set interval to
											// 1ms
			locationRequest.setFastestInterval(1);
		}
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationClient.requestLocationUpdates(locationRequest, this);
	}

	/**
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		stopLocationUpdates();
		stopSelf();
	}

	/**
	 * Called when connection to Google Play Services failed Stops the
	 * requesting of location updates and kill the service
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		stopLocationUpdates();
		stopSelf();
	}

	private class MyAsyncTask extends AsyncTask<String, String, Double> {

		@Override
		protected Double doInBackground(String... params) {
			postData(params[0], params[1]);
			return null;
		}

		protected void onPostExecute(Double result) {
		}

		private void postData(String value1, String value2) {
			HttpClient httpclient = new DefaultHttpClient();
			String url = LOCATION_URL;
			HttpPost httppost = new HttpPost(url);

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("Latitude", value1));
				nameValuePairs.add(new BasicNameValuePair("Longitude", value2));
				nameValuePairs.add(new BasicNameValuePair("Time", mTime));
				nameValuePairs
						.add(new BasicNameValuePair("Accuracy", mAccuracy));
				nameValuePairs.add(new BasicNameValuePair("regName", regId));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

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