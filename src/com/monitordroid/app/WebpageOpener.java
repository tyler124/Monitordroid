package com.monitordroid.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class WebpageOpener {

	/**
	 * Takes an input URL and opens it in the device's default web browser
	 * 
	 * @param url
	 *            The URL to open on the device
	 */
	public void openPage(Context context, String url) {
		try {
			// Make sure it starts with http:// or https://
			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				url = "http://" + url;

			}
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(url));
			browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(browserIntent);
		}
		catch (ActivityNotFoundException e) {
		}
	}

}
