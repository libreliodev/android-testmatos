package com.niveales.testsnowboards;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.http.protocol.HTTP;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.niveales.library.ui.FacebookImagePostPreviewDialogFragment;
import com.niveales.library.ui.TwitterPostPreviewDialogFragment;
import com.niveales.library.ui.activity.TwitterAuthActivity;
import com.niveales.library.ui.criteraselectors.CriteriaSelectorFragment;
import com.niveales.library.ui.criteraselectors.CriteriaSelectorFragment.OnCriteriaChangedListener;
import com.niveales.library.ui.criteraselectors.RangeCriteriaSelectorFragment;
import com.niveales.library.ui.criteraselectors.RangeCriteriaSelectorFragment.OnRangeCriteriaChangedListener;
import com.niveales.library.ui.privacy.AboutFragment;
import com.niveales.library.ui.privacy.FacebookFragment;
import com.niveales.library.ui.productdetail.ProductDetailFragment;
import com.niveales.library.ui.productdetail.ProductDetailFragment.ShareProductListener;
import com.niveales.library.ui.productdetail.ShareDialogFragment;
import com.niveales.library.ui.productdetail.ShareDialogFragment.ShareDialogListener;
import com.niveales.library.ui.productlist.FavoriteProductListFragment;
import com.niveales.library.ui.productlist.ProductListFragment;
import com.niveales.library.ui.productlist.ProductListFragment.ProductSelectedListener;
import com.niveales.library.ui.productsearch.ProductSearchFragment;
import com.niveales.library.ui.productsearch.ProductSearchFragment.OnProductSearchSelectedListener;
import com.niveales.library.utils.BitlyAndroid;
import com.niveales.library.utils.adapters.AdvancedCriteriaMainListAdapter;
import com.niveales.library.utils.db.DBHelper;
import com.niveales.testsnowboards.lexique.LexiqueActivity;

public class TestSnowboardsMainActivity extends FragmentActivity {

	private static final String DIALOG_TAG = null;
	private static final int TWITTER_CALLBACK_ID = 9890;
	private static final String TAG = TestSnowboardsMainActivity.class
			.getSimpleName();
	public static String mTwitterMessage;
	private DBHelper helper;
	private int mActiveTab;
	private TabHost mMainActivityTabHost;
	private View mRightFrameFragmentHolder;
	private ListView mMainActivityCreteriaSelectionListView;
	private AdvancedCriteriaMainListAdapter mainAdapter;
	private FrameLayout mSearchResultHolder;
	private EditText mSearchEditText;
	private Button mMainLayoutSearchButton;
	public ProgressDialog mProgressDialog;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mActiveTab = savedInstanceState
					.getInt(TestSnowboardsApplication.MAIN_TAB_ID);
		}

		// Set our layout
		setContentView(R.layout.main_activity_layout);

		getMyApplication();
		// Init DB
		helper = new DBHelper(this, TestSnowboardsApplication.dbName);
		helper.open();

		// Init tabs
		mMainActivityTabHost = (TabHost) findViewById(R.id.MainLayoutTabHost);
		initTabs(mMainActivityTabHost);

		// Init tab select listener
		mMainActivityTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String arg0) {
				changeTab(arg0);
			}
		});

		// Restore last selected tab from last run
		mMainActivityTabHost.setCurrentTab(mActiveTab);

		// Init Search button click listener
		mMainLayoutSearchButton = (Button) findViewById(R.id.MainLayoutSearchButton);
		mMainLayoutSearchButton
				.setOnClickListener(new SearchButtonClickListener());

		// Right Frame Holder exists only in xlarge layouts. Other devices with
		// screen size
		// less then 7" should not have this view in a main_activity_layout.xml,
		// this way findViewById return null and
		// we use null later to recognize our device type
		mRightFrameFragmentHolder = findViewById(R.id.ContentHolder);

		mMainActivityCreteriaSelectionListView = (ListView) findViewById(R.id.MainActivityCreteriaSelectionListView);
		mMainActivityCreteriaSelectionListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long id) {
						showSelectionCategory(position);
					}
				});

		mainAdapter = new AdvancedCriteriaMainListAdapter(helper, this,
				R.layout.creteria_group_selector_item_layout,
				R.id.CreteriaGroupTextView, R.id.CreteriaSelectedListTextView);
		mMainActivityCreteriaSelectionListView.setAdapter(mainAdapter);

		if (android.os.Build.VERSION.SDK_INT >= 11) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		mSearchResultHolder = (FrameLayout) findViewById(R.id.SearchResultHolder);

		mSearchEditText = (EditText) findViewById(R.id.SearchEditText);
		mSearchEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView pV, int pActionId,
					KeyEvent pEvent) {
				if (pActionId == 0) {

				}
				return false;
			}
		});
		mSearchEditText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View pArg0) {
				onSearchStarted();
			}
		});
	}

	private void initTabs(TabHost pTabHost) {
		String[] tabNames = this.getResources().getStringArray(
				R.array.TabsNames);
		pTabHost.setup();
		TabHost.TabSpec spec1 = pTabHost.newTabSpec(tabNames[0]);
		spec1.setContent(R.id.main_list_tab);
		spec1.setIndicator(tabNames[0]);
		pTabHost.addTab(spec1);

		spec1 = pTabHost.newTabSpec(tabNames[1]);
		spec1.setContent(R.id.favorites_tab);
		spec1.setIndicator(tabNames[1]);
		pTabHost.addTab(spec1);

		spec1 = pTabHost.newTabSpec(tabNames[2]);
		spec1.setContent(R.id.terms_tab);
		spec1.setIndicator(tabNames[2]);
		pTabHost.addTab(spec1);

	}

	private void changeTab(String tabId) {
		String[] tabNames = this.getResources().getStringArray(
				R.array.TabsNames);
		if (tabId.equals(tabNames[0])) {
			/**
			 * Search criteria tab. Clean the right pane if selected And show
			 */

		} else if (tabId.equals(tabNames[1])) {
			/**
			 * Favorites
			 */

			FavoriteProductListFragment f = getMyApplication()
					.getFavoriteProductListFragment(helper);
			f.setOnProductSelectedListener(new ProductSelectedListener() {
				@Override
				public void showProductDetails(Cursor c) {

					showProductDetail(c);
				}
			});
			if (this.mRightFrameFragmentHolder != null) {
				this.getSupportFragmentManager().beginTransaction()
						.replace(R.id.ContentHolder, f).addToBackStack(null)
						.commit();
			} else {
				this.getSupportFragmentManager().beginTransaction()
						.replace(R.id.FragmentHolder, f).addToBackStack(null)
						.commit();
			}

		} else if (tabId.equals(tabNames[2])) {
			/**
			 * Lexique
			 */
			if (this.mRightFrameFragmentHolder != null) {
				// Tablet
				Fragment lexiqueFragment = getMyApplication()
						.getLexiqueFragment(helper);
				this.getSupportFragmentManager().beginTransaction()
						.replace(R.id.ContentHolder, lexiqueFragment)
						.addToBackStack(null).commit();
			} else {
				// Phone
				Intent intent = new Intent(this, LexiqueActivity.class);
				startActivity(intent);
			}
		}
	}

	protected void onSearchButtonPressed() {

		ProductListFragment f = getMyApplication().getProductListFragment(
				helper);
		f.setOnProductSelectedListener(new ProductSelectedListener() {
			@Override
			public void showProductDetails(Cursor c) {
				showProductDetail(c);
			}
		});
		if (this.mRightFrameFragmentHolder != null) {
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.ContentHolder, f).addToBackStack(null)
					.commit();
		} else {
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.FragmentHolder, f).addToBackStack(null)
					.commit();
		}
	}

	public void onSearchClearPressed() {
		helper.rawQuery("delete from UserSearchInputs", null);
		mMainActivityCreteriaSelectionListView.invalidateViews();
	}

	/**
	 * called when user clicks on search input field
	 */
	protected void onSearchStarted() {
		final ProductSearchFragment f = getMyApplication()
				.getProductSearchFragment(helper, R.id.SearchEditText);
		f.setOnProductSearchSelectedListener(new OnProductSearchSelectedListener() {

			@Override
			public void onSearchProductSelected(Cursor c) {
				getSupportFragmentManager().beginTransaction().remove(f)
						.commit();
				showProductDetail(c);
			}
		});
		if (this.mSearchResultHolder != null) {
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.SearchResultHolder, f)
					.addToBackStack("search").commit();
		}
	}

	protected void showProductDetail(Cursor c) {
		if (mRightFrameFragmentHolder != null) {
			// we have a space to show lexique in current activity
			ProductDetailFragment productDetailFragment = getMyApplication()
					.getProductDetailFragment(helper, c,
							new ShareProductListener() {

								@Override
								public void onShareProduct(Cursor productId) {
									showShareDialog(productId);
								}
							});
			int orientation = getResources().getConfiguration().orientation;
			int layout = getResources().getConfiguration().screenLayout;
			if (orientation == Configuration.ORIENTATION_PORTRAIT
					&& ((layout & Configuration.SCREENLAYOUT_SIZE_XLARGE) != 0))
				this.getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.ProductDetailsHolder,
								productDetailFragment)
						.addToBackStack("Selection").commit();
			else
				this.getSupportFragmentManager().beginTransaction()
						.replace(R.id.ContentHolder, productDetailFragment)
						.addToBackStack("Selection").commit();
		} else {
			// start a new LexiqueActivity
			Intent intent = new Intent(this, LexiqueActivity.class);
			startActivity(intent);
		}
	}

	/**
	 * @return Application instance
	 */
	private TestSnowboardsApplication getMyApplication() {
		return (TestSnowboardsApplication) getApplication();
	}

	protected void showSelectionCategory(int position) {

		Cursor cursor = helper.getAllAdvancedCriteria();
		cursor.moveToPosition(position);
		String criteria = cursor.getString(0);
		String type = cursor.getString(2);
		String colName = cursor.getString(1);
		if (type.equals("Numeric")) {
			RangeCriteriaSelectorFragment f = getMyApplication()
					.getRangeCriteriaSelectorFragment(helper, type, criteria,
							colName, new RangeCriteriaChangedListener());
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.ContentHolder, f).addToBackStack(null)
					.commit();
		} else {
			CriteriaSelectorFragment f = getMyApplication()
					.getCriteriaSelectorFragment(helper, type, criteria,
							colName, new CriteriaChangeListener());
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.ContentHolder, f).addToBackStack(null)
					.commit();
		}
	}

	public class RangeCriteriaChangedListener implements
			OnRangeCriteriaChangedListener {

		@Override
		public void onCriteriaChanged(String colName) {
			mMainLayoutSearchButton
					.setOnClickListener(new SearchButtonClickListener());
			mMainLayoutSearchButton
					.setText(R.string.main_layout_search_button_label);
			mMainActivityCreteriaSelectionListView.invalidateViews();

		}
	}

	public class CriteriaChangeListener implements OnCriteriaChangedListener {

		@Override
		public void onCriteriaChanged(String colName) {
			mMainLayoutSearchButton
					.setOnClickListener(new SearchButtonClickListener());
			mMainLayoutSearchButton
					.setText(R.string.main_layout_search_button_label);
			mMainActivityCreteriaSelectionListView.invalidateViews();
		}
	}

	private void showShareDialog(Cursor productCursor) {
		final ShareDialogFragment dialog = ShareDialogFragment.getInstance(
				R.layout.share_dialog_fragment_layout,
				R.id.ShareDialogListView, productCursor,
				new ShareDialogListener() {

					@Override
					public void onShareItemSelected(int pos,
							Cursor productCursor) {
						switch (pos) {
						case 0: {
							shareByFacebook(productCursor);
							break;
						}
						case 1: {
							// twitter
							TwitterSharingTask t = new TwitterSharingTask();
							t.execute(new Cursor[] { productCursor });
							// shareByTwitter(productCursor);
							break;
						}
						case 2: {
							// emal
							shareByEmail(productCursor);
							break;
						}
						}
					}
				});
		dialog.show(dismissDialogs(), DIALOG_TAG);
	}

	private FragmentTransaction dismissDialogs() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		DialogFragment prev = (DialogFragment) getSupportFragmentManager()
				.findFragmentByTag(DIALOG_TAG);
		if (prev != null) {
			prev.dismiss();
		}
		ft.addToBackStack(null);
		return ft;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		MenuItem aboutMenuItem = menu.findItem(R.id.MenuItemAbout);
		aboutMenuItem.setIcon(android.R.drawable.ic_menu_info_details);
		aboutMenuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				showAboutPage();
				return false;
			}
		});

		MenuItem facebookMenuItem = menu.findItem(R.id.MenuItemFacebook);
		facebookMenuItem
				.setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem pArg0) {
						showFacebookPage();
						return true;
					}
				});

		return true;
	}

	protected void showAboutPage() {

		Fragment oldAbout = getSupportFragmentManager().findFragmentByTag(
				"about");
		if (oldAbout != null) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.remove(oldAbout).commit();
		}
		if (findViewById(R.id.ProductDetailsHolder) != null) {
			// we are on a tablet
			AboutFragment f = new AboutFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.ProductDetailsHolder, f, "about")
					.addToBackStack(null).commit();
		} else {
			AboutFragment f = new AboutFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.ContentHolder, f, "about")
					.addToBackStack(null).commit();
		}
		// Intent intent = new Intent(this, PrivacyActivity.class);
		// Bundle extras = new Bundle();
		// extras.putString("url", Consts.ASSETS_URI + "Privacy.html");
		// intent.putExtras(extras);
		// startActivity(intent);
	}

	protected void showFacebookPage() {

		Fragment oldAbout = getSupportFragmentManager().findFragmentByTag(
				"facebook");
		if (oldAbout != null) {
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.remove(oldAbout).commit();
		}
		if (findViewById(R.id.ProductDetailsHolder) != null) {
			// we are on a tablet
			FacebookFragment f = new FacebookFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.ProductDetailsHolder, f, "facebook")
					.addToBackStack(null).commit();
		} else {
			FacebookFragment f = new FacebookFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.ContentHolder, f, "facebook")
					.addToBackStack(null).commit();
		}
		// Intent intent = new Intent(this, PrivacyActivity.class);
		// Bundle extras = new Bundle();
		// extras.putString("url", "http://www.facebook.com");
		// intent.putExtras(extras);
		// startActivity(intent);
	}

	protected void shareByFacebook(final Cursor productCursor) {

		if (TestSnowboardsApplication.mFacebook == null) {
			TestSnowboardsApplication.mFacebook = new Facebook(
					TestSnowboardsApplication.FACEBOOK_APP_ID);
			TestSnowboardsApplication.mAsyncRunner = new AsyncFacebookRunner(
					TestSnowboardsApplication.mFacebook);
		}
		if (!TestSnowboardsApplication.mFacebook.isSessionValid()) {
			TestSnowboardsApplication.mFacebook.authorize(this,
					TestSnowboardsApplication.facebookPermissions,
					new DialogListener() {

						@Override
						public void onComplete(Bundle pValues) {
							//

							shareByFacebook(productCursor);
						}

						@Override
						public void onFacebookError(FacebookError pE) {
							//
							Util.showAlert(TestSnowboardsMainActivity.this,
									"Error:", pE.getMessage());

						}

						@Override
						public void onError(DialogError pE) {
							//
							Util.showAlert(TestSnowboardsMainActivity.this,
									"Warning", pE.getMessage());

						}

						@Override
						public void onCancel() {
							//
							Util.showAlert(TestSnowboardsMainActivity.this,
									"Warning", "Cancelled");

						}
					});
		} else {
			Cursor cursor = productCursor;
			String pic = cursor
					.getString(cursor.getColumnIndexOrThrow("imgLR"));
			String shareString = "";
			String title = "";
			String message = "";
			String url = "";
			try {
				shareString = cursor.getString(cursor
						.getColumnIndexOrThrow("Lien_Partage"));
				Uri uri = Uri.parse(shareString);
				title = URLDecoder.decode(uri.getQueryParameter("watitle"),
						"utf-8");
				message = URLDecoder.decode(uri.getQueryParameter("watext"),
						"utf-8");
				url = URLDecoder.decode(uri.getQueryParameter("walink"),
						"utf-8");
			} catch (UnsupportedEncodingException e1) {
				//
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				//
				e1.printStackTrace();
			}
			Log.d("SHARE", shareString);
			Bundle params = new Bundle();

			// params.putString("caption", getString(R.string.app_name));
			// params.putString("description", getString(R.string.app_desc));
			// params.putString("picture", Utility.HACK_ICON_URL);
			// params.putString("name", getString(R.string.app_action));
			// try {
			// params.putByteArray("photo", TestSnowboardsApplication
			// .scaleImage(getApplicationContext(), pic));
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			params.putString("caption", title);
			params.putString("description", message);
			// params.putString("name", "name");
			// params.putString("picture", pic);
			params.putString("link", url);
			FacebookImagePostPreviewDialogFragment f = new FacebookImagePostPreviewDialogFragment();
			f.setMessage(title + "\n" + url);
			f.setPicUri(pic);
			f.show(dismissDialogs(), DIALOG_TAG);
			// TestSnowboardsApplication.mFacebook.dialog(this, "feed", params,
			// new FacebookUpdateStatusListener());

		}

	}

	/*
	 * callback for the feed dialog which updates the profile status
	 */
	public class FacebookUpdateStatusListener implements
			Facebook.DialogListener {
		@Override
		public void onComplete(Bundle values) {
			final String postId = values.getString("post_id");
			if (postId != null) {
				// AlertDialog.Builder b = new
				// AlertDialog.Builder(TestSnowboardsMainActivity.this);
				// b.setTitle("").setIcon(R.drawable.facebook_icon)
				// .create()
				// .show();
			} else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"No wall post made", Toast.LENGTH_SHORT);
				toast.show();
			}
		}

		@Override
		public void onFacebookError(FacebookError error) {
			Toast.makeText(getApplicationContext(),
					"Facebook Error: " + error.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void onCancel() {
			Toast toast = Toast.makeText(getApplicationContext(),
					"Update status cancelled", Toast.LENGTH_SHORT);
			toast.show();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.facebook.android.Facebook.DialogListener#onError(com.facebook
		 * .android.DialogError)
		 */
		@Override
		public void onError(DialogError pArg0) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * 
	 * @param productCursor
	 *            - id of the product to share
	 */
	public void shareByEmail(Cursor productCursor) {
		Cursor cursor = productCursor;
		String pic = cursor.getString(cursor.getColumnIndexOrThrow("imgLR"));
		String shareString = "";
		String title = "";
		String message = "";
		String url;
		try {
			shareString = cursor.getString(cursor
					.getColumnIndexOrThrow("Lien_Partage"));
			Uri uri = Uri.parse(shareString);
			title = URLDecoder
					.decode(uri.getQueryParameter("watitle"), "utf-8");
			message = URLDecoder.decode(uri.getQueryParameter("watext"),
					"utf-8");
			url = URLDecoder.decode(uri.getQueryParameter("walink"), "utf-8");
		} catch (UnsupportedEncodingException e1) {
			//
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			//
			e1.printStackTrace();
		}
		String newURI = "file://"
				+ TestSnowboardsApplication.copyFileToExternalDirectory(pic,
						getAssets());
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/html");
		intent.setType(HTTP.PLAIN_TEXT_TYPE);
		intent.putExtra(Intent.EXTRA_SUBJECT, title);
		intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(message));
		intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(newURI));
		startActivity(intent);
		Log.d("Html.fromHtml(body)", Html.fromHtml(message).toString());
	}

	/**
	 * @param pProductId
	 * @return
	 */
	@SuppressWarnings("unused")
	protected String shareByTwitter(Cursor productCursor) throws Exception {
		if (TestSnowboardsApplication.mTwitter == null) {
			TestSnowboardsApplication.mTwitter = new TwitterFactory()
					.getInstance();
			TestSnowboardsApplication.mTwitter.setOAuthConsumer(
					TestSnowboardsApplication.TWITTER_CONSUMER_KEY,
					TestSnowboardsApplication.TWITTER_SECRET);
			TestSnowboardsApplication.mTwitterSession = new com.niveales.library.utils.TwitterSession(
					this);
			TestSnowboardsApplication.mTwitterAccessToken = TestSnowboardsApplication.mTwitterSession
					.getAccessToken();
		}
		if (TestSnowboardsApplication.mTwitterAccessToken == null) {

			Intent intent = new Intent(this, TwitterAuthActivity.class);
			intent.putExtra("productid", productCursor.getInt(productCursor
					.getColumnIndexOrThrow("id_modele")));
			startActivityForResult(intent, TWITTER_CALLBACK_ID);

		} else {

			// we has been authenticated before
			TestSnowboardsApplication.mTwitter = new TwitterFactory()
					.getOAuthAuthorizedInstance(
							TestSnowboardsApplication.TWITTER_CONSUMER_KEY,
							TestSnowboardsApplication.TWITTER_SECRET,
							TestSnowboardsApplication.mTwitterAccessToken);
			Cursor cursor = productCursor;
			String pic = cursor
					.getString(cursor.getColumnIndexOrThrow("imgLR"));
			String shareString = "";
			String title = "";
			String message = "";
			String url = "";
			try {
				shareString = cursor.getString(cursor
						.getColumnIndexOrThrow("Lien_Partage"));
				Uri uri = Uri.parse(shareString);
				title = URLDecoder.decode(uri.getQueryParameter("watitle"),
						"utf-8");
				message = URLDecoder.decode(uri.getQueryParameter("watext"),
						"utf-8");
				url = URLDecoder.decode(uri.getQueryParameter("walink"),
						"utf-8").trim();
				BitlyAndroid bitly = new BitlyAndroid(
						TestSnowboardsApplication.BITLY_USER,
						TestSnowboardsApplication.BITLY_API_KEY);
				url = bitly.getShortUrl(url);
				return new String(title + "\n" + url);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new Exception("Error sending tweet");
			}

		}
		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		/*
		 * Do not remove: need for Facebook if this is the activity result from
		 * authorization flow, do a call back to authorizeCallback Source Tag:
		 * login_tag
		 */
		case 32665: {
			// Avoid Runtime Exception bug
			// http://efreedom.com/Question/1-7328392/Android-ViewPager-IllegalStateException-Can-Perform-Action-OnSaveInstanceState
			Handler h = new Handler();
			final int mRes = resultCode;
			final int mReq = requestCode;
			final Intent i = data;
			// Workaround for Android Support library bug , when
			// onActivityResult runs before onResume
			h.postDelayed(new Runnable() {

				@Override
				public void run() {
					TestSnowboardsApplication.mFacebook.authorizeCallback(mReq,
							mRes, i);
				}
			}, 100);

			break;
		}

		/**
		 * return from twitter authorization activity. Peform twitter setup
		 * actions
		 */
		case TWITTER_CALLBACK_ID: {
			int productId = data.getIntExtra("productid", 0);
			Log.d("OAuthTwitter KEY", TestSnowboardsApplication.ACCESS_KEY);
			Log.d("OAuthTwitter SECRET",
					TestSnowboardsApplication.ACCESS_SECRET);
			try {

				TestSnowboardsApplication.mTwitterAccessToken = new AccessToken(
						TestSnowboardsApplication.ACCESS_KEY,
						TestSnowboardsApplication.ACCESS_SECRET);
				Twitter t = new TwitterFactory().getOAuthAuthorizedInstance(
						TestSnowboardsApplication.TWITTER_CONSUMER_KEY,
						TestSnowboardsApplication.TWITTER_SECRET,
						TestSnowboardsApplication.mTwitterAccessToken);
				TestSnowboardsApplication.mTwitter = t;
				TestSnowboardsApplication.mTwitterSession = new com.niveales.library.utils.TwitterSession(
						this);
				TestSnowboardsApplication.mTwitterSession
						.storeAccessToken(TestSnowboardsApplication.mTwitterAccessToken);
				// Avoid Android HONEYCOMB+ NetworkOnUIThreadException
				new TwitterSharingTask().execute(helper
						.getAllFromTableWithWhereAndOrder("Detail",
								"id_modele='" + productId + "'", null));

			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// int j = newConfig.screenLayout &
		// Configuration.SCREENLAYOUT_SIZE_XLARGE;
		if ((newConfig.screenLayout & Configuration.SCREENLAYOUT_SIZE_XLARGE) == 0)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// else
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	public class SearchButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View pView) {
			Button b = (Button) pView;
			b.setText(R.string.main_layout_clear_button_label);
			b.setOnClickListener(new ClearSearchButtonClickListener());
			onSearchButtonPressed();
		}
	}

	public class ClearSearchButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View pView) {
			Button b = (Button) pView;
			b.setText(R.string.main_layout_search_button_label);
			b.setOnClickListener(new SearchButtonClickListener());
			onSearchClearPressed();
		}
	}

	public class TwitterSharingTask extends AsyncTask<Cursor, Integer, String> {
		String error;

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(Cursor... pParams) {

			try {
				return shareByTwitter(pParams[0]);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				error = e.getMessage();
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				AlertDialog.Builder b = new AlertDialog.Builder(
						TestSnowboardsMainActivity.this);
				b.setTitle("Twitter error:");
				b.setMessage(error)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(
											DialogInterface pDialog, int pWhich) {
										pDialog.dismiss();

									}
								}).create().show();
			} else {
				TwitterPostPreviewDialogFragment f = new TwitterPostPreviewDialogFragment();
				f.setMessage(result);
				f.show(getSupportFragmentManager(), null);
			}
		}
	}

}
