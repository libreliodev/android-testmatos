package com.niveales.library.ui.privacy;

import java.io.IOException;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.niveales.testsnowboards.R;
import com.niveales.testsnowboards.TestSnowboardsApplication;

public class AboutFragment extends Fragment {


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.about_fragment_layout,  container, false);
		WebView webView = (WebView) rootView.findViewById(R.id.AboutDialogWebView);
		webView.loadUrl(TestSnowboardsApplication.ASSETS_URI+"Privacy.html");
		return rootView;
	}
}
