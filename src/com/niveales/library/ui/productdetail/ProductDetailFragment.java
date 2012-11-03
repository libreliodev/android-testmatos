package com.niveales.library.ui.productdetail;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.niveales.library.utils.db.DBHelper;
import com.niveales.testsnowboards.R;
import com.niveales.testsnowboards.TestSnowboardsApplication;
import com.niveales.testsnowboards.TestSnowboardsApplication.ProductDetailConstants;

public class ProductDetailFragment extends Fragment {

	private static final String DIALOG_TAG = "share_dialog";
	View rootView;
	String htmlBasePage; // text from assets html page to customize
	String customizedHTMLPage; // page after customization
	DBHelper helper;
	private int productId;
	Cursor productCursor;
	String pic;
	private int productDetailLayout;
	private int webViewId;
	private int favoriteId;
	private int shareId;
	ShareProductListener listener;
	private String[] columnKeys;
	private String[] htmlKeys;
	private ImageButton mPrevButton;
	private WebView webView;
	private ImageButton mNextButton;
	private ImageView mProductImage;
	private boolean mZoomStarted;
	private int bitmapWidth;
	private int bitmapHeight;
	private int webPageStringResourceId;
	private ViewGroup mShareHolder;
	private LinearLayout mShareButtonsHolder;

	/**
	 * 
	 * @param productDetailLayout
	 *            - layout id with product details
	 * @param webViewId
	 *            - WebView id in @param productDetailLayout
	 * @param productId
	 *            - id of the product in the database as of id_modele column in
	 *            Details table
	 * @return ProductDetailFragment instance
	 */
//	public static ProductDetailFragment getInstance(int productDetailLayout,
//			int webViewId, int webPageStringResourceId, Cursor productCursor,
//			String[] columnKeys, String[] htmlKeys, int favoriteCheckboxId,
//			int shareButtonId, ShareProductListener l) {
//		ProductDetailFragment f = new ProductDetailFragment();
//
//		f.init(productDetailLayout, webViewId, webPageStringResourceId,
//				productCursor, columnKeys, htmlKeys, favoriteCheckboxId,
//				shareButtonId, l);
//
//		return f;
//	}

	private void init(int productDetailLayout, int webViewId,
			int webPageStringResourceId, 
			String[] columnKeys, String[] htmlKeys, int favoriteCheckboxId,
			int shareButtonId) {
		this.helper = TestSnowboardsApplication.getDBHelper();
		this.productDetailLayout = productDetailLayout;
		this.webViewId = webViewId;

		this.favoriteId = favoriteCheckboxId;
		this.shareId = shareButtonId;
		this.htmlKeys = htmlKeys;
		this.columnKeys = columnKeys;
		this.webPageStringResourceId = webPageStringResourceId;

	}
	
	public void setProductCursor(Cursor c) {
		this.productCursor = c;
	}
	
	public void setOnShareProductListener(ShareProductListener l) {
		this.listener = l;
	}

	public String readHTML() {
		BufferedInputStream bin;
		try {
			bin = new BufferedInputStream(getActivity().getAssets().open(
					getActivity().getString(webPageStringResourceId)));

			InputStreamReader in = new InputStreamReader(bin, "UTF-8");
			StringWriter w = new StringWriter();
			char[] buffer = new char[1024];
			int count = 0;
			while ((count = in.read(buffer, 0, 1024)) > 0) {
				w.write(buffer, 0, count);
			}

			in.close();
			w.close();
			return w.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	String getHTMLPage(Cursor c) {
		productId = c.getInt(c.getColumnIndexOrThrow("id_modele"));
		String htmlString = new String(htmlBasePage);
		for (int i = 0; i < columnKeys.length; i++) {
			String value = c.getString(c.getColumnIndexOrThrow(columnKeys[i]));
			if (htmlKeys[i].startsWith("%icone") && !value.equals("")) {
				value = "<img src=\"" + TestSnowboardsApplication.ASSETS_URI
						+ value + "\"/>";
			} else {
				if (value.endsWith("png") || value.endsWith("jpg")) {
					// product image
					value = TestSnowboardsApplication.ASSETS_URI + value;
				}
			}
			htmlString = htmlString.replace(htmlKeys[i], value);
		}
		Log.d("HTML", htmlString);
		return htmlString;
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		init(ProductDetailConstants.PRODUCT_DETAIL_LAYOUT, ProductDetailConstants.PRODUCT_DETAIL_WEBVIEW_VIEW_ID,
				ProductDetailConstants.PRODUCT_DETAIL_WEBPAGE_FILE_URI, 
				ProductDetailConstants.PRODUCT_DETAIL_COLUMN_KEYS, ProductDetailConstants.PRODUCT_DETAIL_HTML_FILE_KEYS, 
				ProductDetailConstants.PRODUCT_DETAIL_FAVORITE_CKECKBOX_VIEW_ID, ProductDetailConstants.PRODUCT_DETAIL_SHARE_BUTTON_VIEW_ID);
		rootView = inflater.inflate(productDetailLayout, container, false);
		mProductImage = (ImageView) rootView
				.findViewById(TestSnowboardsApplication.ProductDetailConstants.PRODUCTDETAIL_PRODUCTIMAGE_VIEW_ID);
		mProductImage.setHorizontalScrollBarEnabled(true);
		mProductImage.setVerticalScrollBarEnabled(true);
		// mProductImage.setImageURI(Uri.parse(Consts.ASSETS_URI+productCursor.getString(productCursor.getColumnIndexOrThrow("imgLR"))));
		//
		mProductImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View pV) {
				mZoomStarted = false;
				mProductImage.setVisibility(View.GONE);
			}
		});
		htmlBasePage = readHTML();
		webView = (WebView) rootView.findViewById(webViewId);
		webView.loadDataWithBaseURL(TestSnowboardsApplication.ASSETS_URI,
				getHTMLPage(productCursor), "text/html", "UTF-8", null);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith("zoom://")) {
					// view.loadDataWithBaseURL(Consts.ASSETS_URI, text,
					// "text/html", "UTF-8", null);
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
				// listener.onShareProduct(productCursor, null);
				mShareHolder.setVisibility(View.VISIBLE);
			}
		});
		CheckBox favoriteCkeckBox = (CheckBox) rootView
				.findViewById(favoriteId);
		favoriteCkeckBox.setChecked(helper.isFavorite(productId));
		favoriteCkeckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton pButtonView,
							boolean pIsChecked) {
						if (!pButtonView.isPressed())
							return;
						if (pIsChecked) {
							helper.addFavorite(productId);
						} else {
							helper.deleteFavorite(productId);
						}
					}
				});

		mPrevButton = (ImageButton) rootView
				.findViewById(TestSnowboardsApplication.ProductDetailConstants.PRODUCT_DETAIL_PREVBUTTON_VIEW_ID);
		mPrevButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View pV) {
				if (!productCursor.isFirst()) {
					productCursor.move(-1);
					recycleImageViewBitmap(mProductImage);
					loadImageBitmap();
					webView.loadDataWithBaseURL(
							TestSnowboardsApplication.ASSETS_URI,
							getHTMLPage(productCursor), "text/html", "UTF-8",
							null);
				}
			}
		});
		mNextButton = (ImageButton) rootView
				.findViewById(TestSnowboardsApplication.ProductDetailConstants.PRODUCTDETAIL_NEXTBUTTON_VIEW_ID);
		mNextButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View pV) {
				if (!productCursor.isLast()) {
					productCursor.move(1);
					recycleImageViewBitmap(mProductImage);
					loadImageBitmap();
					webView.loadDataWithBaseURL(
							TestSnowboardsApplication.ASSETS_URI,
							getHTMLPage(productCursor), "text/html", "UTF-8",
							null);
				}
			}
		});

		mShareHolder = (ViewGroup) rootView
				.findViewById(TestSnowboardsApplication.ProductDetailConstants.PRODUCT_DETAIL_SHAREHOLDER_VIEW_ID);
		mShareHolder.setVisibility(View.GONE);
		mShareButtonsHolder = (LinearLayout) rootView
				.findViewById(TestSnowboardsApplication.ProductDetailConstants.PRODUCTDETAIL_SHAREBUTTONSHOLDER_VIEW_ID);
		String shareString = productCursor.getString(productCursor
				.getColumnIndexOrThrow("Lien_Partage"));
		Uri uri = Uri.parse(shareString);
		try {
			String[] sites = URLDecoder.decode(
					uri.getQueryParameter("wasites"), "utf-8").split(",");
			for (int i = 0; i < sites.length; i++) {
				Button b = new Button(getActivity());
				b.setText("Share on " + sites[i]);
				b.setTag(sites[i]);
				b.setBackgroundColor(Color.WHITE);
				b.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View pV) {
						mShareHolder.setVisibility(View.GONE);
						listener.onShareProduct(productCursor,
								(String) pV.getTag());
					}
				});
				b.setLayoutParams(new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT));
				mShareButtonsHolder.addView(b);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return rootView;
	}

	protected void showLargeImage(String pUrl) {
		if (pUrl.startsWith("zoom://touchstart")) {
			mProductImage.setVisibility(View.VISIBLE);
			mZoomStarted = true;
//			webView.setOnTouchListener(new OnTouchListener() {
//
//				@Override
//				public boolean onTouch(View pArg0, MotionEvent pArg1) {
//					// TODO Auto-generated method stub
//					return false;
//				}
//			});
//			webView.setVerticalScrollBarEnabled(false);
//			webView.setHorizontalScrollBarEnabled(false);
		}
//		if (pUrl.startsWith("zoom://touchend")) {
//			webView.setOnTouchListener(null);
//			webView.setVerticalScrollBarEnabled(true);
//			webView.setHorizontalScrollBarEnabled(true);
//			mZoomStarted = false;
//			mProductImage.setVisibility(View.GONE);
//		}
//		if (mZoomStarted) {
//			String[] params = pUrl.split("\\?");
//			params = params[1].split(",");
//			int x = Integer.valueOf(params[0]);
//			int y = Integer.valueOf(params[1]);
//			int maxx = Integer.valueOf(params[2]);
//			int height = Integer.valueOf(params[3]);
//			int maxy = Integer.valueOf(params[4]);
//			int width = Integer.valueOf(params[5]);
//
//			if (x > width)
//				x = width;
//			if (y > height)
//				y = height;
//
//			int scrollx = x - width / 2;
//			int scrolly = y - height / 2;
//
//			Log.d("scroll",
//					String.valueOf(scrollx) + " " + String.valueOf(scrolly));
//			mProductImage.scrollTo(scrollx, scrolly);
//
//		}
		Log.d("ZOOM", pUrl);
	}

	/**
	 * populate mProductImage with product image
	 */
	public void onResume() {
		super.onResume();
		loadImageBitmap();
	}

	public void loadImageBitmap() {
		try {
			pic = productCursor.getString(productCursor
					.getColumnIndexOrThrow("imgLR"));
			Bitmap b = BitmapFactory.decodeStream(getActivity().getAssets()
					.open(pic));
			this.bitmapWidth = b.getWidth();
			this.bitmapHeight = b.getHeight();
			mProductImage.setImageBitmap(b);
			mProductImage.invalidate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * for memory management purpose dispose bitmap from mProductImage
	 */
	public void onPause() {
		super.onPause();
		recycleImageViewBitmap(mProductImage);
	}

	public void recycleImageViewBitmap(ImageView i) {
		Drawable d = i.getDrawable();
		if (d instanceof BitmapDrawable) {
			BitmapDrawable bd = (BitmapDrawable) d;
			bd.getBitmap().recycle();
		}

	}

	public void onShareStarted() {

	}

	public interface ShareProductListener {
		public void onShareProduct(Cursor productCursor, String site);
	}
}
