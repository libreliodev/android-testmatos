package com.niveales.library.ui.privacy;


import com.niveales.testsnowboards.R;
import com.niveales.testsnowboards.TestSnowboardsApplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;

public class AboutFragment extends Fragment {

	ProgressDialog progress;
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.about_fragment_layout,  container, false);
		progress = new ProgressDialog(getActivity());
		WebView webView = (WebView) rootView.findViewById(R.id.AboutDialogWebView);
		webView.setWebViewClient(new WebViewClient() {
			
			@Override
			public void onPageFinished(WebView view, String url) {
				progress.dismiss();
			}
			
			@Override
			public void onPageStarted (WebView view, String url, Bitmap favicon) {
				progress.show();
			}
			
			@Override
	        public boolean shouldOverrideUrlLoading( WebView view, String url )
	        {
				
	            return false;
	        }
		});
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(TestSnowboardsApplication.INFO_TAB_PAGE_URL);
		return rootView;
	}
}
