package com.niveales.library.ui.productdetail;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import android.R;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.niveales.library.ui.BaseNivealesFragment;
import com.niveales.library.ui.popup.ActionItem;
import com.niveales.library.ui.popup.QuickAction;
import com.niveales.library.utils.db.DBHelper;
import com.niveales.testsnowboards.TestSnowboardsApplication;
import com.niveales.testsnowboards.TestSnowboardsApplication.ProductDetailConstants;

public class ProductDetailFragment extends BaseNivealesFragment {

	/**
	 * 
	 */
	private static final int PRODUCT_IMAGE_TIMEOUT = 5000;
	/**
	 * 
	 */
	private static final String ZOOM_FINISH = "zoom://finish";
	/**
	 * 
	 */
	private static final String ZOOM_TOUCHSTART = "zoom://touchstart";
	/**
	 * 
	 */
	private static final String ZOOM_TOUCHEND = "zoom://touchend";
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
	private QuickAction mShareHolder;
	private LinearLayout mShareButtonsHolder;
	protected float downX;
	protected float downY;
	protected boolean isZoomStarted = false;
	private ArrayList<ActionItem> mActionItems = new ArrayList<ActionItem>();


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

	Runnable mProductImageGoneRunnable = new Runnable() {

		@Override
		public void run() {
			mProductImage.setVisibility(View.GONE);
			isZoomStarted = false;
			
		}
	};
	
	Handler mProductImageGoneHandler = new Handler();
	
	private View.OnTouchListener mProductImageTouchListener = new View.OnTouchListener() {
		private float mx;
		private float my;
		private float deltaX, deltaY;
		int maxX = 0, maxY = 0;

		@Override
		public boolean onTouch(View pV, MotionEvent pEvent) {

			float curX, curY;

			switch (pEvent.getAction()) {

			case MotionEvent.ACTION_DOWN:
				mProductImageGoneHandler.postDelayed(mProductImageGoneRunnable, PRODUCT_IMAGE_TIMEOUT);
				mx = pEvent.getX();
				my = pEvent.getY();
				deltaX = deltaY = 0;
				maxX = Math.abs(bitmapWidth - mProductImage.getWidth()) / 2;
				maxY = Math.abs(bitmapHeight - mProductImage.getHeight()) / 2;
				break;
			case MotionEvent.ACTION_MOVE:
				mProductImageGoneHandler.removeCallbacks(mProductImageGoneRunnable);
				mProductImageGoneHandler.postDelayed(mProductImageGoneRunnable, PRODUCT_IMAGE_TIMEOUT);
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
	};
	private CheckBox mFavoriteCkeckBox;
	
	
	public void loadProduct(Cursor c) {
		webView.loadDataWithBaseURL(TestSnowboardsApplication.ASSETS_URI,
				getHTMLPage(c), "text/html", "UTF-8", null);
		mFavoriteCkeckBox.setChecked(helper.isFavorite(productId));
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

		mProductImage.setOnTouchListener(mProductImageTouchListener);
		htmlBasePage = readHTML();
		webView = (WebView) rootView.findViewById(webViewId);
		
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

		webView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View pV, MotionEvent pEvent) {
				if(isZoomStarted) {
					mProductImageTouchListener.onTouch(pV, pEvent);
					return true;
				}
				return false;
			}
		});

		ImageView shareButton = (ImageView) rootView.findViewById(shareId);
		shareButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mShareHolder.dismiss();
				mShareHolder.show();
			}
		});
		mFavoriteCkeckBox = (CheckBox) rootView
				.findViewById(favoriteId);
		
		mFavoriteCkeckBox
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
					loadProduct(productCursor);
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
					loadProduct(productCursor);
				}
			}
		});

		mShareHolder = new QuickAction(shareButton);
		String shareString = productCursor.getString(productCursor
				.getColumnIndexOrThrow("Lien_Partage"));
		Uri uri = Uri.parse(shareString);
		try {
			String[] sites = URLDecoder.decode(
					uri.getQueryParameter("wasites"), "utf-8").split(",");
			for (int i = 0; i < sites.length; i++) {
				ActionItem item = new ActionItem();
				item.setTitle("Share by "+sites[i]);
				item.setTag(sites[i]);
				Button b = new Button(getActivity());
				b.setText("Share on " + sites[i]);
				b.setTag(sites[i]);
				b.setBackgroundColor(Color.WHITE);
				b.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View pV) {
						mShareHolder.dismiss();
						listener.onShareProduct(productCursor,
								(String) pV.getTag());
					}
				});
				LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				p.setMargins(1, 1, 1, 1);
				b.setLayoutParams(p);
//				mShareButtonsHolder.addView(b);
				item.setActionItemView(b);
				mShareHolder.addActionItem(item);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		loadProduct(productCursor);
		return rootView;
	}

	protected void showLargeImage(String pUrl) {
		if (pUrl.startsWith(ZOOM_TOUCHSTART)) {
			mProductImage.setVisibility(View.VISIBLE);
			Handler h = new Handler();
			isZoomStarted = true;			
		}
		
		if(pUrl.startsWith(ZOOM_FINISH)) {
			this.mProductImageGoneHandler.removeCallbacks(mProductImageGoneRunnable);
			mProductImage.setVisibility(View.GONE);
			isZoomStarted = false;
		}
//		if(pUrl.startsWith(ZOOM_TOUCHEND)) {
//			mProductImage.setVisibility(View.GONE);
//			isZoomStarted = false;
//		}
		
		Log.d("zoom", pUrl);
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
			showLargeImage(ZOOM_FINISH);
			return true;
		}
		return false;
	}
}
