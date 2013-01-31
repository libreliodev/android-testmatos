package com.niveales.library.utils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Context;

import twitter4j.http.AccessToken;

/**
 * Twitter session.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 *
 */
public class TwitterSession {
	private SharedPreferences sharedPref;
	private Editor editor;
	
	private static final String TWEET_AUTH_KEY = "auth_key";
	private static final String TWEET_AUTH_SECRET_KEY = "auth_secret_key";
	private static final String SHARED = "Twitter_Preferences";
	
	public TwitterSession(Context context) {
		sharedPref 	  = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
		
		editor 		  = sharedPref.edit();
	}
	
	public void storeAccessToken(AccessToken accessToken) {
		editor.putString(TWEET_AUTH_KEY, accessToken.getToken());
		editor.putString(TWEET_AUTH_SECRET_KEY, accessToken.getTokenSecret());
		
		
		editor.commit();
	}
	
	public void resetAccessToken() {
		editor.putString(TWEET_AUTH_KEY, null);
		editor.putString(TWEET_AUTH_SECRET_KEY, null);
		editor.commit();
	}
	
	public AccessToken getAccessToken() {
		String token 		= sharedPref.getString(TWEET_AUTH_KEY, null);
		String tokenSecret 	= sharedPref.getString(TWEET_AUTH_SECRET_KEY, null);
		
		if (token != null && tokenSecret != null) 
			return new AccessToken(token, tokenSecret);
		else
			return null;
	}
}