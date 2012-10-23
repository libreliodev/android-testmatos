package com.niveales.library.ui.productdetail;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.niveales.library.utils.Consts;
import com.niveales.library.utils.db.DBHelper;



public class ProductDetailFragment extends Fragment {
	
	private static final String DIALOG_TAG = "share_dialog";
	View rootView;
	String text;
	DBHelper helper;
	private int productId;
	String pic;
	private Context context;
	private int productDetailLayout;
	private int webViewId;
	private int favoriteId;
	private int shareId;
	ShareProductListener listener;
	/**
	 * 
	 * @param context - Context
	 * @param helper - instance  of DBHelper class
	 * @param productDetailLayout - layout id with product details
	 * @param webViewId - WebView id in @param productDetailLayout
	 * @param productId - id of the product in the database as of id_modele column in Details table
	 * @return ProductDetailFragment instance
	 */
	public static ProductDetailFragment getInstance(Context context,
			DBHelper helper, int productDetailLayout, int webViewId,
			int webPageStringResourceId, int productId, String[] columnKeys,
			String[] htmlKeys, int favoriteCheckboxId, int shareButtonId, ShareProductListener l) {
		ProductDetailFragment f = new ProductDetailFragment();
		try {
			f.init(context, helper, productDetailLayout, webViewId,
					webPageStringResourceId, productId, columnKeys, htmlKeys, favoriteCheckboxId, shareButtonId, l);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}
	
	private void init(Context context, DBHelper helper,
			int productDetailLayout, int webViewId,
			int webPageStringResourceId, int productId, String[] columnKeys,
			String[] htmlKeys, int favoriteCheckboxId, int shareButtonId, ShareProductListener l) throws IOException {
		this.context = context;
		this.helper = helper;
		this.productDetailLayout = productDetailLayout;
		this.webViewId = webViewId;
		this.productId = productId;
		this.favoriteId = favoriteCheckboxId;
		this.shareId = shareButtonId;
		this.listener = l;
		Cursor c = helper.getModele(productId);

		BufferedInputStream bin = new BufferedInputStream(context.getAssets().open(context.getString(webPageStringResourceId)));
		InputStreamReader in = new InputStreamReader(bin, "UTF-8");
		StringWriter w = new StringWriter();
		char[] buffer = new char[1024];
		int count = 0;
		while((count = in.read(buffer, 0, 1024)) > 0) {
			w.write(buffer, 0, count);
		}
		String htmlString = w.toString();
		in.close();
		w.close();
		
		for (int i = 0; i < columnKeys.length; i++) {
			String value = c.getString(c.getColumnIndexOrThrow(columnKeys[i]));
			if (htmlKeys[i].startsWith("%icone") && !value.equals("")) {
				value = "<img src=\"" + Consts.ASSETS_URI + value + "\"/>";
			} else {
				if (value.endsWith("png") || value.endsWith("jpg")) {
					// product image
					value = Consts.ASSETS_URI + value;
				}
			}
			htmlString = htmlString.replace(htmlKeys[i], value);
		}
		Log.d("HTML", htmlString);
		this.text = htmlString;
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.setRetainInstance(true);

		rootView = inflater.inflate(productDetailLayout, container, false);
		WebView w = (WebView) rootView.findViewById(webViewId);
		w.loadDataWithBaseURL(Consts.ASSETS_URI, text, "text/html", "UTF-8", null);
		w.getSettings().setJavaScriptEnabled(true);
		w.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url){
				if(url.startsWith("zoom://")) {
//					view.loadDataWithBaseURL(Consts.ASSETS_URI, text, "text/html", "UTF-8", null);
					showLargeImage(url);
					return true;
				}
				return false;
			}
		});
		
		ImageView shareButton = (ImageView) rootView.findViewById(shareId);
		shareButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				listener.onShareProduct(productId);
			}});
		CheckBox favoriteCkeckBox = (CheckBox) rootView.findViewById(favoriteId);
		favoriteCkeckBox.setChecked(helper.isFavorite(productId));
		favoriteCkeckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton pButtonView,
					boolean pIsChecked) {
				if(!pButtonView.isPressed())
					return;
				if(pIsChecked) {
					helper.addFavorite(productId);
				} else {
					helper.deleteFavorite(productId);
				}
			}});
		
		return rootView;
	}

	protected void showLargeImage(String pUrl) {
		Log.d("ZOOM", pUrl);
	}

	public interface ShareProductListener {
		public void onShareProduct(int productId);
	}
}
