package com.niveales.testsnowboards;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;

import org.apache.http.protocol.HTTP;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.niveales.library.ui.activity.BaseNivealesActivity;
import com.niveales.library.ui.criteraselectors.CriteriaSelectorFragment;
import com.niveales.library.ui.criteraselectors.CriteriaSelectorFragment.OnCriteriaChangedListener;
import com.niveales.library.ui.criteraselectors.RangeCriteriaSelectorFragment;
import com.niveales.library.ui.criteraselectors.RangeCriteriaSelectorFragment.OnRangeCriteriaChangedListener;
import com.niveales.library.ui.privacy.PrivacyActivity;
import com.niveales.library.ui.privacy.PrivacyDialogFragment;
import com.niveales.library.ui.productdetail.ProductDetailFragment;
import com.niveales.library.ui.productdetail.ProductDetailFragment.ShareProductListener;
import com.niveales.library.ui.productdetail.ShareDialogFragment;
import com.niveales.library.ui.productdetail.ShareDialogFragment.ShareDialogListener;
import com.niveales.library.ui.productlist.FavoriteProductListFragment;
import com.niveales.library.ui.productlist.ProductListFragment;
import com.niveales.library.ui.productlist.ProductListFragment.ProductSelectedListener;
import com.niveales.library.ui.productsearch.ProductSearchFragment;
import com.niveales.library.ui.productsearch.ProductSearchFragment.OnProductSearchSelectedListener;
import com.niveales.library.utils.Consts;
import com.niveales.library.utils.adapters.AdvancedCriteriaMainListAdapter;
import com.niveales.library.utils.db.DBHelper;
import com.niveales.testsnowboards.lexique.LexiqueActivity;

public class TestSnowboardsMainActivity extends BaseNivealesActivity {

	private static final String DIALOG_TAG = null;
	private static final int TWITTER_CALLBACK_ID = 9890;

	private int mActiveTab;
	private TabHost mMainActivityTabHost;
	private View mRightFrameFragmentHolder;
	private ListView mMainActivityCreteriaSelectionListView;
	private AdvancedCriteriaMainListAdapter mainAdapter;
	private FrameLayout mSearchResultHolder;
	private EditText mSearchEditText;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mActiveTab = savedInstanceState.getInt(Consts.MAIN_TAB_ID);
		}

		// Set our layout
		setContentView(R.layout.main_activity_layout);

		getMyApplication();
		// Init DB
		setDBHelper(new DBHelper(this, TestSnowboardsApplication.dbName));
		getDBHelper().open();

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
		Button mMainLayoutSearchButton = (Button) findViewById(R.id.MainLayoutSearchButton);
		mMainLayoutSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onSearchButtonPressed();
			}
		});

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

		mainAdapter = new AdvancedCriteriaMainListAdapter(getDBHelper(), this,
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
					.getFavoriteProductListFragment(getDBHelper());
			f.setOnProductSelectedListener(new ProductSelectedListener() {
				@Override
				public void showProductDetails(int id) {
					showProductDetail(id);
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
						.getLexiqueFragment(getDBHelper());
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
				getDBHelper());
		f.setOnProductSelectedListener(new ProductSelectedListener() {
			@Override
			public void showProductDetails(int id) {
				showProductDetail(id);
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

	/**
	 * called when user clicks on search input field
	 */
	protected void onSearchStarted() {
		final ProductSearchFragment f = getMyApplication()
				.getProductSearchFragment(getDBHelper(), R.id.SearchEditText);
		f.setOnProductSearchSelectedListener(new OnProductSearchSelectedListener() {

			@Override
			public void onSearchProductSelected(int pId) {
				getSupportFragmentManager().beginTransaction().remove(f)
						.commit();
				showProductDetail(pId);
			}
		});
		if (this.mSearchResultHolder != null) {
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.SearchResultHolder, f)
					.addToBackStack("search").commit();
		}
	}

	protected void showProductDetail(int id) {
		if (mRightFrameFragmentHolder != null) {
			// we have a space to show lexique in current activity
			ProductDetailFragment productDetailFragment = getMyApplication()
					.getProductDetailFragment(getDBHelper(), id,
							new ShareProductListener() {

								@Override
								public void onShareProduct(int productId) {
									showShareDialog(productId);
								}
							});
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
	@Override
	public TestSnowboardsApplication getMyApplication() {
		return (TestSnowboardsApplication) getApplication();
	}

	protected void showSelectionCategory(int position) {

		Cursor cursor = getDBHelper().getAllAdvancedCriteria();
		cursor.moveToPosition(position);
		String criteria = cursor.getString(0);
		String type = cursor.getString(2);
		String colName = cursor.getString(1);
		if (type.equals("Numeric")) {
			RangeCriteriaSelectorFragment f = getMyApplication()
					.getRangeCriteriaSelectorFragment(getDBHelper(), type, criteria,
							colName, new OnRangeCriteriaChangedListener() {

								@Override
								public void onCriteriaChanged(String colName) {
									mMainActivityCreteriaSelectionListView
											.invalidateViews();

								}
							});
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.ContentHolder, f).addToBackStack(null)
					.commit();
		} else {
			CriteriaSelectorFragment f = getMyApplication()
					.getCriteriaSelectorFragment(getDBHelper(), type, criteria,
							colName, new OnCriteriaChangedListener() {

								@Override
								public void onCriteriaChanged(String colName) {
									mMainActivityCreteriaSelectionListView
											.invalidateViews();
								}
							});
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.ContentHolder, f).addToBackStack(null)
					.commit();
		}
	}

	private String copyFileToExternalDirectory(String pic) {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File externalDir = Environment
					.getExternalStoragePublicDirectory("Download");
			if (externalDir.canWrite()) {
				try {
					String fileName = pic.split("/")[pic.split("/").length - 1];
					File newPic = File.createTempFile("pic", fileName);
					byte[] buffer = new byte[1024];
					BufferedOutputStream bos = new BufferedOutputStream(
							new FileOutputStream(newPic));
					BufferedInputStream bis = new BufferedInputStream(
							getAssets().open(pic));
					int count = 0;
					while ((count = bis.read(buffer, 0, 1024)) > 0) {
						bos.write(buffer, 0, count);
					}
					bos.close();
					bis.close();
					return newPic.getAbsolutePath();
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			} else {
				return null;
			}
		}
		return null;
	}

	private void showShareDialog(int productId) {
		final ShareDialogFragment dialog = ShareDialogFragment.getInstance(
				R.layout.share_dialog_fragment_layout,
				R.id.ShareDialogListView, productId, new ShareDialogListener() {

					@Override
					public void onShareItemSelected(int pos, int productId) {
						switch (pos) {
						case 0: {
							shareByFacebook(productId);
							break;
						}
						case 1: {
							// twitter
							shareByTwitter(productId);
							break;
						}
						case 2: {
							// emal
							shareByEmail(productId);
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
				showAboutDialog();
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

	protected void showAboutDialog() {
		Intent intent = new Intent(this, PrivacyActivity.class);
		Bundle extras = new Bundle();
		extras.putString("url", Consts.ASSETS_URI + "Privacy.html");
		intent.putExtras(extras);
		startActivity(intent);
	}

	protected void showFacebookPage() {
		Intent intent = new Intent(this, PrivacyActivity.class);
		Bundle extras = new Bundle();
		extras.putString("url", "http://www.facebook.com");
		intent.putExtras(extras);
		startActivity(intent);
	}

	protected void shareByFacebook(final int productId) {

		if (TestSnowboardsApplication.mFacebook == null) {
			TestSnowboardsApplication.mFacebook = new Facebook(
					Consts.FACEBOOK_APP_ID);
		}
		if (!TestSnowboardsApplication.mFacebook.isSessionValid()) {
			TestSnowboardsApplication.mFacebook.authorize(this,
					TestSnowboardsApplication.facebookPermissions,
					new DialogListener() {

						@Override
						public void onComplete(Bundle pValues) {
							//

							shareByFacebook(productId);
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
			Cursor cursor = getDBHelper().getAllFromTableWithWhereAndOrder("Detail",
					"id_modele = '" + productId + "'", null);
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
			try {
				params.putByteArray("photo", TestSnowboardsApplication
						.scaleImage(getApplicationContext(), pic));
			} catch (IOException e) {
				e.printStackTrace();
			}
			params.putString("caption", title);
			params.putString("name", "<A HREF=\"" + url + message);
			params.putString("link", url);
			getMyApplication();
			AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(
					TestSnowboardsApplication.mFacebook);
			mAsyncRunner.request("me/photos", params, "POST",
					new RequestListener() {

						@Override
						public void onComplete(String pResponse, Object pState) {
							//

						}

						@Override
						public void onIOException(IOException pE, Object pState) {
							Util.showAlert(TestSnowboardsMainActivity.this,
									"Error:", pE.getMessage());

						}

						@Override
						public void onFileNotFoundException(
								FileNotFoundException pE, Object pState) {
							Util.showAlert(TestSnowboardsMainActivity.this,
									"Error:", pE.getMessage());

						}

						@Override
						public void onMalformedURLException(
								MalformedURLException pE, Object pState) {
							Util.showAlert(TestSnowboardsMainActivity.this,
									"Error:", pE.getMessage());

						}

						@Override
						public void onFacebookError(FacebookError pE,
								Object pState) {
							Util.showAlert(TestSnowboardsMainActivity.this,
									"Error:", pE.getMessage());

						}
					}, null);

		}

	}

	/**
	 * 
	 * @param productId
	 *            - id of the product to share
	 */
	public void shareByEmail(int productId) {
		Cursor cursor = getDBHelper().getAllFromTableWithWhereAndOrder("Detail",
				"id_modele = '" + productId + "'", null);
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
		String newURI = "file://" + copyFileToExternalDirectory(pic);
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
	 */
	protected void shareByTwitter(int productId) {
		if (getMyApplication().mTwitter == null) {
			Twitter mTwitter = null;
			try {
				mTwitter = new TwitterFactory().getInstance();
				mTwitter.setOAuthConsumer(Consts.TWITTER_CONSUMER_KEY,
						Consts.TWITTER_SECRET);
				if (getMyApplication().mTwitterAccessToken == null) {
					RequestToken t = mTwitter
							.getOAuthRequestToken(Consts.TWITTER_CALLBACK_URL);
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(t
							.getAuthenticationURL()));
					intent.putExtra("productid", productId);
					startActivityForResult(intent, TWITTER_CALLBACK_ID);
					getMyApplication().rToken = t;
				} else {
					// we has been authenticated before
					mTwitter = new TwitterFactory().getOAuthAuthorizedInstance(
							Consts.TWITTER_CONSUMER_KEY, Consts.TWITTER_SECRET,
							getMyApplication().mTwitterAccessToken);
					Cursor cursor = getDBHelper().getAllFromTableWithWhereAndOrder(
							"Detail", "id_modele = '" + productId + "'", null);
					String pic = cursor.getString(cursor
							.getColumnIndexOrThrow("imgLR"));
					String shareString = "";
					String title = "";
					String message = "";
					String url = "";
					try {
						shareString = cursor.getString(cursor
								.getColumnIndexOrThrow("Lien_Partage"));
						Uri uri = Uri.parse(shareString);
						title = URLDecoder.decode(
								uri.getQueryParameter("watitle"), "utf-8");
						message = URLDecoder.decode(
								uri.getQueryParameter("watext"), "utf-8");
						url = URLDecoder.decode(
								uri.getQueryParameter("walink"), "utf-8");
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					} catch (IllegalArgumentException e1) {
						//
						e1.printStackTrace();
					}
					Log.d("SHARE", shareString);
					mTwitter.updateStatus(shareString);
				}
			} catch (TwitterException e) {
				e.printStackTrace();
			}

			getMyApplication().mTwitter = mTwitter;

		}
		new Thread() {
			@Override
			public void run() {
				int what = 0;

				try {
					getMyApplication().mTwitter.updateStatus("test");
				} catch (Exception e) {
					what = 1;
				}

			}
		}.start();
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
			TestSnowboardsApplication.mFacebook.authorizeCallback(requestCode,
					resultCode, data);
			break;
		}

		/**
		 * return from twitter authorization activity. Peform twitter setup
		 * actions
		 */
		case TWITTER_CALLBACK_ID: {
			Uri uri = data.getData();
			int productId = data.getIntExtra("productid", 0);
			if (uri != null) {

				TestSnowboardsApplication.oauthVerifier = uri
						.getQueryParameter("oauth_verifier");
			}
			try {
				Twitter mTwitter = TestSnowboardsApplication.mTwitter;
				if (mTwitter == null) {
					return;
				}
				AccessToken at = mTwitter.getOAuthAccessToken(
						TestSnowboardsApplication.rToken,
						TestSnowboardsApplication.oauthVerifier);
				String token = at.getToken();
				String secret = at.getTokenSecret();
				// Post to twitter.
				TestSnowboardsApplication.mTwitterAccessToken = new AccessToken(
						token, secret);
				Twitter t = new TwitterFactory().getOAuthAuthorizedInstance(
						Consts.TWITTER_CONSUMER_KEY, Consts.TWITTER_SECRET,
						TestSnowboardsApplication.mTwitterAccessToken);
				TestSnowboardsApplication.mTwitter = t;
				this.shareByTwitter(productId);

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
//		else
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	@Override
	public void onResume() {
		if(getDBHelper()!= null) {
			getDBHelper().open();
		}
		super.onResume();
	}
	@Override
	public void onPause() {
		if(this.getDBHelper() != null) {
			getDBHelper().close();
		}
		super.onPause();
	}

}
