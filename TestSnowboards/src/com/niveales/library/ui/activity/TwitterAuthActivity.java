/**
 * 
 */
package com.niveales.library.ui.activity;

import oauth.signpost.OAuth;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.niveales.library.ui.NivealesApplication;

/**
 * @author Dmitry Valetin
 *
 */
public class TwitterAuthActivity extends Activity {
	int productId;
	private WebView webView;
	Intent data;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// Setup UI with webview
		LinearLayout ll = new LinearLayout(this);
		webView = new WebView(this);
		webView.setWebViewClient(new WebViewClient(){
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				if(url.startsWith(NivealesApplication.TWITTER_CALLBACK_URL)) {
					new TwitterAuthCallbackAsyncTask().execute(url);
					return true;
				}
				return false;
			}
		});
		ll.addView(webView);
		
		data = getIntent();
		productId = data.getIntExtra("productid", 0);
		setContentView(ll);
		new TwitterAuthAsyncTask().execute(webView);
	}
	
	
	public class TwitterAuthAsyncTask extends AsyncTask<WebView, Void, Void> {

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(WebView... pParams) {
			String authURL;
			try {
				authURL = NivealesApplication.provider.retrieveRequestToken(
						NivealesApplication.consumer, NivealesApplication.TWITTER_CALLBACK_URL);

				webView.loadUrl(authURL);
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
				data.putExtra("error", e.getMessage());
				setResult(Activity.RESULT_CANCELED, data);	
				finish();
			} catch (OAuthNotAuthorizedException e) {
				e.printStackTrace();
				data.putExtra("error", e.getMessage());
				setResult(Activity.RESULT_CANCELED, data);	
				finish();
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
				data.putExtra("error", e.getMessage());
				setResult(Activity.RESULT_CANCELED, data);	
				finish();
			} catch (OAuthCommunicationException e) {
				e.printStackTrace();				
				data.putExtra("error", e.getMessage());
				setResult(Activity.RESULT_CANCELED, data);	
				finish();
			}
			return null;
		}
		
	}
	
	
	public class TwitterAuthCallbackAsyncTask extends AsyncTask<String, Void, Void> {

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(String... pArg0) {
			// TODO Auto-generated method stub
			String url = pArg0[0];
			onTwitterCallback(url);
			return null;
		}
		
	}

	public void onTwitterCallback(String url) {
		Uri uri = Uri.parse(url);

		if (uri != null && uri.toString().startsWith(NivealesApplication.TWITTER_CALLBACK_URL)) {
			//We get here after successful twitter login
			Log.d("OAuthTwitter", uri.toString());
			String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
			if(verifier == null) {
				// user cancelled twitter auth
				setResult(Activity.RESULT_CANCELED, data);
				finish();
				return;
			}
			Log.d("OAuthTwitter", verifier);
			try {

				NivealesApplication.provider.retrieveAccessToken(NivealesApplication.consumer, verifier);
				NivealesApplication.ACCESS_KEY = NivealesApplication.consumer.getToken();
				NivealesApplication.ACCESS_SECRET = NivealesApplication.consumer.getTokenSecret();


				setResult(Activity.RESULT_OK, data);	
				finish();
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
				data.putExtra("error", e.getMessage());

				setResult(Activity.RESULT_CANCELED, data);	
				finish();
			} catch (OAuthNotAuthorizedException e) {
				e.printStackTrace();
				data.putExtra("error", e.getMessage());

				setResult(Activity.RESULT_CANCELED, data);	
				finish();
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
				data.putExtra("error", e.getMessage());

				setResult(Activity.RESULT_CANCELED, data);	
				finish();
			} catch (OAuthCommunicationException e) {
				e.printStackTrace();
				data.putExtra("error", e.getMessage());

				setResult(Activity.RESULT_CANCELED, data);	
				finish();
			} catch (Exception e) {
				data.putExtra("error", e.getMessage());

				setResult(Activity.RESULT_CANCELED, data);	
				finish();
			}
		}
	}
}
