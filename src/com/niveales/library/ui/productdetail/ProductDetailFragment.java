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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.niveales.library.ui.BaseNivealesFragment;
import com.niveales.library.utils.db.DBHelper;
import com.niveales.testsnowboards.R;
import com.niveales.testsnowboards.TestSnowboardsApplication;
import com.niveales.testsnowboards.TestSnowboardsApplication.ProductDetailConstants;

public class ProductDetailFragment extends BaseNivealesFragment {

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
	private int bitmapWidth;
	private int bitmapHeight;
	private int webPageStringResourceId;
	private ViewGroup mShareHolder;
	private LinearLayout mShareButtonsHolder;
	private ViewGroup mProductDetailWebviewHolder;
	protected float downX;
	protected float downY;

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
	// public static ProductDetailFragment getInstance(int productDetailLayout,
	// int webViewId, int webPageStringResourceId, Cursor productCursor,
	// String[] columnKeys, String[] htmlKeys, int favoriteCheckboxId,
	// int shareButtonId, ShareProductListener l) {
	// ProductDetailFragment f = new ProductDetailFragment();
	//
	// f.init(productDetailLayout, webViewId, webPageStringResourceId,
	// productCursor, columnKeys, htmlKeys, favoriteCheckboxId,
	// shareButtonId, l);
	//
	// return f;
	// }

	private void init(int productDetailLayout, int webViewId,
			int webPageStringResourceId, String[] columnKeys,
			String[] htmlKeys, int favoriteCheckboxId, int shareButtonId) {
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
		init(ProductDetailConstants.PRODUCT_DETAIL_LAYOUT,
				ProductDetailConstants.PRODUCT_DETAIL_WEBVIEW_VIEW_ID,
				ProductDetailConstants.PRODUCT_DETAIL_WEBPAGE_FILE_URI,
				ProductDetailConstants.PRODUCT_DETAIL_COLUMN_KEYS,
				ProductDetailConstants.PRODUCT_DETAIL_HTML_FILE_KEYS,
				ProductDetailConstants.PRODUCT_DETAIL_FAVORITE_CKECKBOX_VIEW_ID,
				ProductDetailConstants.PRODUCT_DETAIL_SHARE_BUTTON_VIEW_ID);
		rootView = inflater.inflate(productDetailLayout, container, false);
		mProductImage = (ImageView) rootView
				.findViewById(TestSnowboardsApplication.ProductDetailConstants.PRODUCTDETAIL_PRODUCTIMAGE_VIEW_ID);
		mProductImage.setHorizontalScrollBarEnabled(true);
		mProductImage.setVerticalScrollBarEnabled(true);
		// mProductImage.setImageURI(Uri.parse(Consts.ASSETS_URI+productCursor.getString(productCursor.getColumnIndexOrThrow("imgLR"))));
		//
		// mProductImage.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View pV) {
		// mZoomStarted = false;
		// mProductImage.setVisibility(View.GONE);
		// }
		// });

		mProductImage.setOnTouchListener(new View.OnTouchListener() {

			private float mx;
			private float my;
			private float deltaX, deltaY;
			int maxX = 0, maxY = 0;

			@Override
			public boolean onTouch(View pV, MotionEvent pEvent) {

				float curX, curY;

				switch (pEvent.getAction()) {

				case MotionEvent.ACTION_DOWN:
					mx = pEvent.getX();
					my = pEvent.getY();
					deltaX = deltaY = 0;
					maxX = Math.abs(bitmapWidth - mProductImage.getWidth()) / 2;
					maxY = Math.abs(bitmapHeight - mProductImage.getHeight()) / 2;
					break;
				case MotionEvent.ACTION_MOVE:
					curX = pEvent.getX();
					curY = pEvent.getY();
					deltaX = mx - curX;
					deltaY = my - curY;
					mx = curX;
					my = curY;
					float scrollX = mProductImage.getScrollX();
					float scrollY = mProductImage.getScrollY();
					if (scrollX + deltaX < -maxX)
						scrollX = -maxX;
					else if (scrollX + deltaX > maxX)
						scrollX = maxX;
					else
						scrollX += deltaX;

					if (scrollY + deltaY < -maxY)
						scrollY = -maxY;
					else if (scrollY + deltaY > maxY)
						scrollY = maxY;
					else
						scrollY += deltaY;

					mProductImage.scrollTo((int) scrollX, (int) scrollY);
					break;
				case MotionEvent.ACTION_UP:
					curX = pEvent.getX();
					curY = pEvent.getY();
					deltaX = mx - curX;
					deltaY = my - curY;
					break;
				}

				return true;
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

		webView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View pV) {
				mProductImage.setVisibility(View.GONE);

			}
		});

		mProductDetailWebviewHolder = (FrameLayout) rootView
				.findViewById(R.id.ProductDetailWebviewHolder);
		mProductDetailWebviewHolder
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View pV) {
						mProductImage.setVisibility(View.GONE);

					}
				});
		ImageView shareButton = (ImageView) rootView.findViewById(shareId);
		shareButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
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
			e.printStackTrace();
		}

		return rootView;
	}

	protected void showLargeImage(String pUrl) {
		if (pUrl.startsWith("zoom://touchstart")) {
			mProductImage.setVisibility(View.VISIBLE);
		}

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

	@Override
	public boolean onBackPressed() {
		if (mProductImage.getVisibility() == View.VISIBLE) {
			this.mProductImage.setVisibility(View.GONE);
			return true;
		}
		return false;
	}
}
