package com.niveales.library.ui.privacy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.niveales.library.utils.Consts;

public class PrivacyDialogFragment extends Fragment {
	
	public static PrivacyDialogFragment getInstance(Context context, int layoutId, int webViewId, String assetUri) throws IOException {
		PrivacyDialogFragment f = new PrivacyDialogFragment();
		f.init(context, layoutId, webViewId, assetUri);
		return f;
	}

	private Context context;
	private int layoutId;
	private String htmlString;
	private int webViewId;
	private String assetUri;
	
	public void init(Context context, int layoutId, int webViewId, String assetUri) throws IOException {
		this.context = context;
		this.layoutId = layoutId;
		this.webViewId = webViewId;
		this.assetUri = assetUri;
//		BufferedInputStream bin = new BufferedInputStream(asset);
//		InputStreamReader in = new InputStreamReader(bin, "UTF-8");
//		StringWriter w = new StringWriter();
//		char[] buffer = new char[1024];
//		int count = 0;
//		while((count = in.read(buffer, 0, 1024)) > 0) {
//			w.write(buffer, 0, count);
//		}
//		htmlString = w.toString();
//		in.close();
//		w.close();
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View rootView = inflater.inflate(layoutId,  container, false);
		WebView webView = (WebView) rootView.findViewById(webViewId);
		webView.loadUrl(Consts.ASSETS_URI+assetUri);
		return rootView;
	}
}
