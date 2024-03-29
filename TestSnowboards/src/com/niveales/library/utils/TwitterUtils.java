/**
 * 
 */
package com.niveales.library.utils;

import com.niveales.library.ui.NivealesApplication;

import oauth.signpost.OAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import android.content.SharedPreferences;

public class TwitterUtils {

	public static boolean isAuthenticated(SharedPreferences prefs) {

		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
		
		AccessToken a = new AccessToken(token,secret);
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(NivealesApplication.TWITTER_CONSUMER_KEY, NivealesApplication.TWITTER_SECRET);
		twitter.setOAuthAccessToken(a);
		
//		try {
//			twitter.getAccountSettings();
//			return true;
//		} catch (TwitterException e) {
//			return false;
//		}
		return false;
	}
	
	public static void sendTweet(SharedPreferences prefs,String msg) throws Exception {
		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
		
		AccessToken a = new AccessToken(token,secret);
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(NivealesApplication.TWITTER_CONSUMER_KEY, NivealesApplication.TWITTER_SECRET);
		twitter.setOAuthAccessToken(a);
        twitter.updateStatus(msg);
	}	
}