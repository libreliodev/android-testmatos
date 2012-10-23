/**
 * 
 */
package com.niveales.library.ui.privacy;

import com.niveales.testsnowboards.R;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * @author Dmitry Valetin
 *
 */
public class PrivacyActivity extends Activity {
	WebView wv;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_dialog_fragment_ayout);
		Bundle extras = this.getIntent().getExtras();
		String url = extras.getString("url");
		wv = (WebView) findViewById(R.id.AboutDialogWebView);
		wv.loadUrl(url);
	}
	
	@Override
	public void onBackPressed() {
		if(wv.canGoBack()) {
			wv.goBack();
		}
		else 
			super.onBackPressed();
	}
}
