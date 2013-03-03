package com.niveales.library.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.http.protocol.HTTP;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.niveales.library.ui.about.AboutFragment;
import com.niveales.library.ui.about.FacebookFragment;
import com.niveales.library.ui.activity.TwitterAuthActivity;
import com.niveales.library.ui.criteraselectors.CheckedCriteriaSelectorFragment.OnCriteriaChangedListener;
import com.niveales.library.ui.criteraselectors.RangeCriteriaSelectorFragment.OnRangeCriteriaChangedListener;
import com.niveales.library.ui.productdetail.ProductDetailFragment;
import com.niveales.library.ui.productdetail.ProductDetailFragment.ShareProductListener;
import com.niveales.library.ui.productlist.FavoriteProductListFragment;
import com.niveales.library.ui.productlist.ProductListFragment;
import com.niveales.library.ui.productlist.ProductListFragment.ProductSelectedListener;
import com.niveales.library.utils.BitlyAndroid;
import com.niveales.library.utils.adapters.AdvancedCriteriaMainListAdapter;
import com.niveales.library.utils.adapters.search.SearchAdapter;
import com.niveales.library.utils.db.DBHelper;
import com.niveales.testskis.R;
import com.niveales.testskis.R.array;
import com.niveales.testskis.R.drawable;
import com.niveales.testskis.R.id;
import com.niveales.testskis.R.layout;
import com.niveales.testskis.R.menu;
import com.niveales.testskis.R.string;

public class MainActivity extends FragmentActivity {

	private static final String DIALOG_TAG = null;
	private static final int TWITTER_CALLBACK_ID = 9890;
	@SuppressWarnings("unused")
	private static final String TAG = MainActivity.class
			.getSimpleName();
	public static String mTwitterMessage;
	private int mActiveTab;
	private TabHost mMainActivityTabHost;
	private View mRightFrameFragmentHolder;
	private ListView mMainActivityCreteriaSelectionListView;
	private AdvancedCriteriaMainListAdapter mainAdapter;
	// private FrameLayout mSearchResultHolder;
	private AutoCompleteTextView mSearchEditText;
	private ImageButton mMainLayoutSearchButton;
	public ProgressDialog mProgressDialog;
	public String mRecentSearch;
	private TextView mPrevSearchTextView;
	private TextView mNewSearchTextView;
	private int mLastSelectedMainItem = 1;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mActiveTab = savedInstanceState
					.getInt(NivealesApplication.MAIN_TAB_ID);
		}

		// Set our layout
		setContentView(R.layout.main_activity_layout);

		// Init DB
		NivealesApplication.setDBHelper(new DBHelper(this,
				NivealesApplication.DB_FILE_NAME));
		NivealesApplication.getDBHelper().open();

		initViews();
		restoreAppState();
	}

	@SuppressLint("NewApi")
	public void initViews() {

		// Setup device screen configuration
		Configuration newConfig = getResources().getConfiguration();
		if ((newConfig.screenLayout & Configuration.SCREENLAYOUT_SIZE_XLARGE) == 0)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		else
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

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
		mMainLayoutSearchButton = (ImageButton) findViewById(R.id.MainLayoutSearchButton);
		mNewSearchTextView = (TextView) findViewById(R.id.NewSearchTextView);
		this.initSearchButton();

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
		mMainActivityCreteriaSelectionListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		mainAdapter = new AdvancedCriteriaMainListAdapter(
				NivealesApplication.getDBHelper(), this,
				R.layout.creteria_group_selector_item_layout,
				R.id.CreteriaGroupTextView, R.id.CreteriaSelectedListTextView);
		mMainActivityCreteriaSelectionListView.setAdapter(mainAdapter);

		if (android.os.Build.VERSION.SDK_INT >= 11) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		// mSearchResultHolder = (FrameLayout)
		// findViewById(R.id.SearchResultHolder);

		mSearchEditText = (AutoCompleteTextView) findViewById(R.id.SearchEditText);
		mSearchEditText.setAdapter(new SearchAdapter(this));
		mSearchEditText.setThreshold(1);
		mSearchEditText.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> pArg0, View pArg1,
					int pArg2, long pArg3) {
				InputMethodManager imm = (InputMethodManager) MainActivity.this
						.getSystemService(Activity.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
				showProductDetail((Cursor) pArg0.getAdapter().getItem(pArg2));

			}
		});
		mSearchEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		mPrevSearchTextView = (TextView) findViewById(R.id.PrevSearchTextView);
		mPrevSearchTextView.setText(getPrevSearchText());
		mPrevSearchTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View pArg0) {
				onPrevSearchClick();

			}
		});

	}

	/**
	 * 
	 */
	protected void onPrevSearchClick() {
		DBHelper helper = NivealesApplication.getDBHelper();
		try {
			helper.rawQuery("delete from UserSearchInputs", null);
			helper.rawQuery(
					"insert into UserSearchInputs select * from UserSearchInputsOld",
					null);
			this.mMainActivityCreteriaSelectionListView.invalidateViews();
			this.mMainLayoutSearchButton.setVisibility(View.VISIBLE);
			this.mNewSearchTextView.setVisibility(View.INVISIBLE);
			onSearchButtonClick();

		} catch (Exception e) {
			// table does not exists, do nothing
			e.printStackTrace();
		}

	}

	public String getPrevSearchText() {
		String text = "";
		try {
			Cursor crit = NivealesApplication.getDBHelper()
					.getAllFromTable("AdvancedCriteria");
			while (!crit.isAfterLast()) {
				String result = "";
				String title = crit.getString(crit
						.getColumnIndexOrThrow("Title"));
				String critColName = crit.getString(crit
						.getColumnIndexOrThrow("ColName"));
				Cursor c = NivealesApplication.getDBHelper()
						.getAllFromTableWithWhereAndOrder(
								"UserSearchInputsOld",
								"ColName LIKE '%" + critColName + "%'", null);
				if (c != null && c.getCount() > 0) {
					while (!c.isAfterLast()) {
						result += c.getString(c.getColumnIndexOrThrow("Title"))
								+ ",";
						c.moveToNext();
					}
				}
				if (!result.equals("")) {
					text += title + ":" + result;

				}
				crit.moveToNext();

			}

			if (!text.equals("")) {
				text = Html.fromHtml("<b>Ma dernière recherche:</b><br>")
						+ text;
				this.mPrevSearchTextView.setVisibility(View.VISIBLE);
			} else {
				this.mPrevSearchTextView.setVisibility(View.GONE);
			}

		} catch (Exception e) {
			// table does not exists, exiting
		}
		return text;
	}

	private void initTabs(TabHost pTabHost) {
		String[] tabNames = this.getResources().getStringArray(
				R.array.TabsNames);
		pTabHost.setup();
		TabHost.TabSpec spec = pTabHost.newTabSpec(tabNames[0]);
		Button b = new Button(this);
		b.setPadding(0, b.getPaddingTop(), 0, b.getPaddingBottom());
		b.setBackgroundResource(R.drawable.tab_button);
		b.setTextColor(getResources().getColorStateList(R.drawable.tab_button_textcolor));
		b.setText(tabNames[0]);

		spec.setIndicator(b);
		spec.setContent(R.id.main_list_tab);
		// spec1.setIndicator(tabNames[0]);
		pTabHost.addTab(spec);

		spec = pTabHost.newTabSpec(tabNames[1]);
		spec.setContent(R.id.favorites_tab);
		b = new Button(this);
		b.setBackgroundResource(R.drawable.tab_button);
		b.setTextColor(getResources().getColorStateList(R.drawable.tab_button_textcolor));
		b.setText(tabNames[1]);
		spec.setIndicator(b);
		pTabHost.addTab(spec);

		spec = pTabHost.newTabSpec(tabNames[2]);
		spec.setContent(R.id.terms_tab);
		b = new Button(this);
		b.setBackgroundResource(R.drawable.tab_button);
		b.setTextColor(getResources().getColorStateList(R.drawable.tab_button_textcolor));
		b.setText(tabNames[2]);
		spec.setIndicator(b);
		pTabHost.addTab(spec);

	}

	private void changeTab(String tabId) {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		prefs.edit().putString("TAB_ID", tabId).commit();
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
					.getFavoriteProductListFragment();
			f.setOnProductSelectedListener(new ProductSelectedListener() {
				@Override
				public void showProductDetails(Cursor c) {

					showProductDetail(c);
				}
			});
			attachFavorites(f, "favorite");

		} else if (tabId.equals(tabNames[2])) {
			/**
			 * Lexique
			 */
			if (this.mRightFrameFragmentHolder != null) {
				// Tablet
				Fragment lexiqueFragment = getMyApplication()
						.getLexiqueFragment(
								NivealesApplication.getDBHelper());
				this.getSupportFragmentManager().beginTransaction()
						.replace(R.id.ContentHolder, lexiqueFragment)
						.addToBackStack(null).commit();
			} else {
				// Phone
				Fragment lexiqueFragment = getMyApplication()
						.getLexiqueFragment(
								NivealesApplication.getDBHelper());
				this.getSupportFragmentManager().beginTransaction()
						.replace(R.id.terms_tab, lexiqueFragment).commit();
			}
		}
	}

	/**
	 * @param f
	 * @param fragmentTag
	 */
	private void attachFavorites(FavoriteProductListFragment f,
			String fragmentTag) {
		if (this.mRightFrameFragmentHolder != null) {
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.ContentHolder, f, fragmentTag)
					.addToBackStack(fragmentTag).commit();
		} else {
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.favorites_tab, f, fragmentTag).commit();
		}
	}

	protected void onSearchButtonClick() {
		String whereClaus = null;
		DBHelper helper = NivealesApplication.getDBHelper();
		Cursor tempCursor = helper.getAllFromTableWithOrder("AdvancedCriteria",
				"Title");
		boolean isFirst = true;
		while (!tempCursor.isAfterLast()) {
			String title = tempCursor.getString(0);
			String advColName = tempCursor.getString(1);
			String type = tempCursor.getString(2);
			String headerText = tempCursor.getString(3);
			String operation;
			if (type.toLowerCase().equals("numeric"))
				operation = "AND";
			else
				operation = type;
			Cursor searchInputsCursor = helper.rawQuery(
					"select group_concat(querystring, \" " + operation
							+ " \") from UserSearchInputs where colname=?",
					new String[] { advColName });
			String tempWhere = searchInputsCursor.getString(0);
			if (tempWhere != null) {
				if (isFirst) {
					whereClaus = "( " + tempWhere + ") ";
					isFirst = false;
				} else {
					whereClaus += " AND " + "( " + tempWhere + ") ";
				}
			}
			tempCursor.moveToNext();
		}

		Cursor productCursor = helper.getAllFromTableWithWhereAndOrder(
				NivealesApplication.DETAIL_TABLE_NAME, whereClaus, null);
		if (productCursor.getCount() > 0) {
			ProductListFragment f = getMyApplication().getProductListFragment();
			f.setOnProductSelectedListener(new ProductSelectedListener() {
				@Override
				public void showProductDetails(Cursor c) {
					showProductDetail(c);
				}
			});
			attachSearchResults(f, "searchresults");
		} else {
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setMessage(R.string.nothing_found_message);
			b.setPositiveButton("Close", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface pDialog, int pWhich) {
					pDialog.dismiss();

				}
			});
			Dialog d = b.create();
			d.requestWindowFeature(Window.FEATURE_NO_TITLE);
			d.show();
		}

	}

	public void onClearSearchClick() {
		NivealesApplication.getDBHelper().rawQuery(
				"delete from UserSearchInputsOld", null);
		NivealesApplication
				.getDBHelper()
				.rawQuery(
						"insert into UserSearchInputsOld select * from UserSearchInputs",
						null);
		NivealesApplication.getDBHelper().rawQuery(
				"delete from UserSearchInputs", null);
		// TestSnowboardsApplication.getDBHelper().rawQuery(
		// "delete from UserSearchInputs", null);
		mMainActivityCreteriaSelectionListView.invalidateViews();
		mPrevSearchTextView.setText(getPrevSearchText());
		this.initSearchButton();
	}

	// /**
	// * called when user clicks on search input field
	// */
	// protected void onSearchStarted() {
	// final SearchPopup mSearchPopup =
	// getMyApplication().getProductSearchPopup(this.mSearchEditText);
	// mSearchPopup.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
	// LayoutParams.MATCH_PARENT));
	// mSearchPopup.show();
	//
	// }
	// protected void onOldSearchStarted() {
	// final ProductSearchFragment f = getMyApplication()
	// .getProductSearchFragment(
	// R.id.SearchEditText);
	// f.setOnProductSearchSelectedListener(new
	// OnProductSearchSelectedListener() {
	//
	// @Override
	// public void onSearchProductSelected(Cursor c) {
	// InputMethodManager imm = (InputMethodManager)
	// TestSnowboardsMainActivity.this
	// .getSystemService(Activity.INPUT_METHOD_SERVICE);
	// imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
	// getSupportFragmentManager().beginTransaction().remove(f)
	// .commit();
	// showProductDetail(c);
	// }
	// });
	//
	// if (this.mSearchResultHolder != null) {
	// this.getSupportFragmentManager().beginTransaction()
	// .replace(R.id.SearchResultHolder, f)
	// .addToBackStack("search").commit();
	// }
	// }

	protected void showProductDetail(Cursor c) {
		ProductDetailFragment productDetailFragment = getMyApplication()
				.getProductDetailFragment(c, new ShareProductListener() {

					@Override
					public void onShareProduct(Cursor productId, String site) {
						showShareDialog(productId, site);
					}
				});
		attachProductDetailFragment(productDetailFragment, "productdetail");
	}

	/**
	 * @param productDetailFragment
	 * @param fragmentTag
	 */
	private void attachProductDetailFragment(Fragment productDetailFragment,
			String fragmentTag) {
		if (mRightFrameFragmentHolder != null) {

			int orientation = getResources().getConfiguration().orientation;
			int layout = getResources().getConfiguration().screenLayout;
			if (orientation == Configuration.ORIENTATION_PORTRAIT
					&& ((layout & Configuration.SCREENLAYOUT_SIZE_XLARGE) != 0))
				this.getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.ProductDetailsHolder,
								productDetailFragment, fragmentTag)
						.addToBackStack(fragmentTag).commit();
			else
				this.getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.ContentHolder, productDetailFragment,
								fragmentTag).addToBackStack(fragmentTag)
						.commit();
		} else {

			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.FragmentHolder, productDetailFragment,
							fragmentTag).addToBackStack(fragmentTag).commit();
		}
	}

	/**
	 * @param pFragment
	 */
	private void attachSearchResults(Fragment pFragment, String fragmentTag) {
		if (this.mRightFrameFragmentHolder != null) {
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.ContentHolder, pFragment, fragmentTag)
					.addToBackStack(fragmentTag).commit();
		} else {
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.FragmentHolder, pFragment, fragmentTag)
					.addToBackStack(fragmentTag).commit();
		}
	}

	/**
	 * @return Application instance
	 */
	private NivealesApplication getMyApplication() {
		return (NivealesApplication) getApplication();
	}

	protected void showSelectionCategory(int position) {

		mLastSelectedMainItem = position;

		Cursor cursor = NivealesApplication.getDBHelper()
				.getAllAdvancedCriteria();

		cursor.moveToPosition(position);
		String criteria = cursor.getString(0);
		String type = cursor.getString(2);
		String colName = cursor.getString(1);
		Fragment f;
		if (type.equals("Numeric")) {
			f = getMyApplication().getRangeCriteriaSelectorFragment(
					NivealesApplication.getDBHelper(), type, criteria,
					colName, new RangeCriteriaChangedListener());

		} else {
			f = getMyApplication().getCheckedCriteriaSelectorFragment(position,
					new CriteriaChangeListener());

		}
		attachCriteriaFragment(f, "criteriacategory");

	}

	/**
	 * @param f
	 *            - Fragment
	 */
	private void attachCriteriaFragment(Fragment f, String fragmentTag) {
		if (this.mRightFrameFragmentHolder != null) {
			// Tablet
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.ContentHolder, f, fragmentTag).commit();
		} else {
			// phone
			this.getSupportFragmentManager().beginTransaction()
					.replace(R.id.FragmentHolder, f, fragmentTag)
					.addToBackStack(fragmentTag).commit();
		}
	}

	public class RangeCriteriaChangedListener implements
			OnRangeCriteriaChangedListener {

		@Override
		public void onCriteriaChanged(String colName) {
			initSearchButton();
			mMainActivityCreteriaSelectionListView.invalidateViews();

		}

	}

	public class CriteriaChangeListener implements OnCriteriaChangedListener {

		@Override
		public void onCriteriaChanged(String colName) {
			initSearchButton();
			mMainActivityCreteriaSelectionListView.invalidateViews();
		}
	}

	private void showShareDialog(Cursor productCursor, String site) {

		if (site.toLowerCase().equals("facebook")) {
			shareByFacebook(productCursor);
		}
		if (site.toLowerCase().equals("twitter")) {
			// twitter
			TwitterSharingTask t = new TwitterSharingTask();
			t.execute(new Cursor[] { productCursor });

		}
		if (site.toLowerCase().equals("mail")) {
			// emal
			shareByEmail(productCursor);
		}
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
		// aboutMenuItem.setIcon(android.R.drawable.ic_menu_info_details);
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
		if (this.mRightFrameFragmentHolder != null) {
			// we are on a tablet
			AboutFragment f = new AboutFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.ProductDetailsHolder, f, "about")
					.addToBackStack("about").commit();
		} else {
			AboutFragment f = new AboutFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.FragmentHolder, f, "about")
					.addToBackStack("about").commit();
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
		if (this.mRightFrameFragmentHolder != null) {
			// we are on a tablet
			FacebookFragment f = new FacebookFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.ProductDetailsHolder, f, "facebook")
					.addToBackStack("facebook").commit();
		} else {
			FacebookFragment f = new FacebookFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.FragmentHolder, f, "facebook")
					.addToBackStack("facebook").commit();
		}
		// Intent intent = new Intent(this, PrivacyActivity.class);
		// Bundle extras = new Bundle();
		// extras.putString("url", "http://www.facebook.com");
		// intent.putExtras(extras);
		// startActivity(intent);
	}

	protected void shareByFacebook(final Cursor productCursor) {

		if (NivealesApplication.mFacebook == null) {
			NivealesApplication.mFacebook = new Facebook(
					NivealesApplication.FACEBOOK_APP_ID);
			NivealesApplication.mAsyncRunner = new AsyncFacebookRunner(
					NivealesApplication.mFacebook);
		}
		if (!NivealesApplication.mFacebook.isSessionValid()) {
			NivealesApplication.mFacebook.authorize(this,
					NivealesApplication.facebookPermissions,
					new DialogListener() {

						@Override
						public void onComplete(Bundle pValues) {
							//

							shareByFacebook(productCursor);
						}

						@Override
						public void onFacebookError(FacebookError pE) {
							//
							Util.showAlert(MainActivity.this,
									"Error:", pE.getMessage());

						}

						@Override
						public void onError(DialogError pE) {
							//
							Util.showAlert(MainActivity.this,
									"Warning", pE.getMessage());

						}

						@Override
						public void onCancel() {
							//
							Util.showAlert(MainActivity.this,
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
				+ NivealesApplication.copyFileToExternalDirectory(pic,
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
	 * @return error or null if no error
	 */
	@SuppressWarnings("unused")
	protected String shareByTwitter(Cursor productCursor) throws Exception {
		if (NivealesApplication.mTwitter == null) {
			NivealesApplication.mTwitter = new TwitterFactory()
					.getInstance();
			NivealesApplication.mTwitter.setOAuthConsumer(
					NivealesApplication.TWITTER_CONSUMER_KEY,
					NivealesApplication.TWITTER_SECRET);
			NivealesApplication.mTwitterSession = new com.niveales.library.utils.TwitterSession(
					this);
			NivealesApplication.mTwitterAccessToken = NivealesApplication.mTwitterSession
					.getAccessToken();
		}
		if (NivealesApplication.mTwitterAccessToken == null) {

			Intent intent = new Intent(this, TwitterAuthActivity.class);
			intent.putExtra("productid", productCursor.getInt(productCursor
					.getColumnIndexOrThrow("id_modele")));
			startActivityForResult(intent, TWITTER_CALLBACK_ID);

		} else {

			// we has been authenticated before
			NivealesApplication.mTwitter = new TwitterFactory()
					.getOAuthAuthorizedInstance(
							NivealesApplication.TWITTER_CONSUMER_KEY,
							NivealesApplication.TWITTER_SECRET,
							NivealesApplication.mTwitterAccessToken);
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
						NivealesApplication.BITLY_USER,
						NivealesApplication.BITLY_API_KEY);
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
					NivealesApplication.mFacebook.authorizeCallback(mReq,
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
			if (resultCode != Activity.RESULT_OK) {
				if (data != null) {
					String message = data.getExtras().getString("error");
					if (message == null) {
						message = "Twitter authentication failed";
					}
					AlertDialog.Builder b = new AlertDialog.Builder(this);
					b.setTitle("Twitter auth error:");
					b.setMessage(message);
					b.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface pDialog,
										int pWhich) {
									pDialog.dismiss();
								}
							});
					b.create().show();
				}
				return;
			}
			int productId = data.getIntExtra("productid", 0);
			Log.d("OAuthTwitter KEY", NivealesApplication.ACCESS_KEY);
			Log.d("OAuthTwitter SECRET",
					NivealesApplication.ACCESS_SECRET);
			try {

				NivealesApplication.mTwitterAccessToken = new AccessToken(
						NivealesApplication.ACCESS_KEY,
						NivealesApplication.ACCESS_SECRET);
				Twitter t = new TwitterFactory().getOAuthAuthorizedInstance(
						NivealesApplication.TWITTER_CONSUMER_KEY,
						NivealesApplication.TWITTER_SECRET,
						NivealesApplication.mTwitterAccessToken);
				NivealesApplication.mTwitter = t;
				NivealesApplication.mTwitterSession = new com.niveales.library.utils.TwitterSession(
						this);
				NivealesApplication.mTwitterSession
						.storeAccessToken(NivealesApplication.mTwitterAccessToken);
				// Avoid Android HONEYCOMB+ NetworkOnUIThreadException
				new TwitterSharingTask().execute(NivealesApplication
						.getDBHelper()
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
		saveFragments();
		setContentView(R.layout.main_activity_layout);
		initViews();
		restoreAppState();
		// if ((newConfig.screenLayout & Configuration.SCREENLAYOUT_SIZE_XLARGE)
		// == 0)
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// else
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	public void initSearchButton() {
		Cursor c = NivealesApplication.getDBHelper().getAllFromTable(
				"UserSearchInputs");
		if (c.getCount() > 0) {
			mMainLayoutSearchButton
					.setOnClickListener(new SearchButtonClickListener());
			mMainLayoutSearchButton
					.setBackgroundResource(R.drawable.bout_aff_resultat);
			mMainLayoutSearchButton.setVisibility(View.VISIBLE);
			mNewSearchTextView.setVisibility(View.INVISIBLE);
		} else {
			mMainLayoutSearchButton.setVisibility(View.INVISIBLE);
			mNewSearchTextView.setVisibility(View.VISIBLE);
		}
	}

	public class SearchButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View pView) {
			ImageButton b = (ImageButton) pView;
			b.setBackgroundResource(R.drawable.bout_new_recherc_vert);
			b.setOnClickListener(new ClearSearchButtonClickListener());
			onSearchButtonClick();
		}
	}

	public class ClearSearchButtonClickListener implements OnClickListener {
		@Override
		public void onClick(View pView) {
			onClearSearchClick();
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
			if (result == null && error != null) {
				AlertDialog.Builder b = new AlertDialog.Builder(
						MainActivity.this);
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
				if (result != null) {
					TwitterPostPreviewDialogFragment f = new TwitterPostPreviewDialogFragment();
					f.setMessage(result);
					f.show(getSupportFragmentManager(), null);
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();
		Fragment f = fm.findFragmentByTag("productdetail");
		SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
		Boolean isProcessed = false;
		if (f != null && f instanceof BaseNivealesFragment ) {
			BaseNivealesFragment bf = (BaseNivealesFragment) f;
			isProcessed |= bf.onBackPressed();
		}
		f = fm.findFragmentByTag("about");
		if (f != null && f instanceof BaseNivealesFragment) {
			BaseNivealesFragment bf = (BaseNivealesFragment) f;
			isProcessed |= bf.onBackPressed();
		}
		f = fm.findFragmentByTag("facebook");
		if (!isProcessed && f != null && f instanceof BaseNivealesFragment) {
			BaseNivealesFragment bf = (BaseNivealesFragment) f;
			isProcessed |= bf.onBackPressed();
		}
		if (isProcessed)
			return;

		super.onBackPressed();
		fm.executePendingTransactions();
		if (fm.getBackStackEntryCount() > 0) {
			String name = fm.getBackStackEntryAt(
					fm.getBackStackEntryCount() - 1).getName();
			prefs.edit().putString("FRAGMENT_TAG", name).commit();
		} else {
			prefs.edit().remove("FRAGMENT_TAG").commit();
		}

	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		// TODO Auto-generated method stub
		super.onAttachFragment(fragment);
		SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
		// prefs.edit().putString("FRAGMENT", fragment.getTag()).commit();
		// BackStackEntry e =
		// this.getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount
		// () - 1);
		prefs.edit().putInt("FRAGEMENT_ID", fragment.getId())
				.putString("FRAGMENT_TAG", fragment.getTag()).commit();
		// Toast.makeText(getApplicationContext(), fragment.getId(),
		// Toast.LENGTH_SHORT).show();

	}

	public void saveFragments() {
		SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
		Editor e = prefs.edit();
		if (this.getSupportFragmentManager().getBackStackEntryCount() > 0) {
			String tag = this
					.getSupportFragmentManager()
					.getBackStackEntryAt(
							this.getSupportFragmentManager()
									.getBackStackEntryCount() - 1).getName();
			e.putString("FRAGMENT_TAG", tag);
		} else {
			e.remove("FRAGMENT_TAG");
		}
		e.putInt("MAIN_POSITION", mLastSelectedMainItem);
		e.commit();
	}

	public void restoreAppState() {
		SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
		String[] tabNames = this.getResources().getStringArray(
				R.array.TabsNames);
		String tabId = prefs.getString("TAB_ID", tabNames[0]);
		changeTab(tabId);
		if (!tabId.equals(tabNames[0]))
			return;
		mLastSelectedMainItem = prefs.getInt("MAIN_POSITION",
				mLastSelectedMainItem);
		String mFragmentTag = prefs.getString("FRAGMENT_TAG", "");
		if (this.mRightFrameFragmentHolder != null) {
			this.mMainActivityCreteriaSelectionListView.setItemChecked(mLastSelectedMainItem, true);
			showSelectionCategory(mLastSelectedMainItem);
		}
		// I give up restoring correct app state with all fragments, thus all state restore has been commented out
		
//		if (mFragmentTag.equals("facebook")) {
//			showFacebookPage();
//		}
//		if (mFragmentTag.equals("about")) {
//			showAboutPage();
//		}
//		if (mFragmentTag.equals("productdetail")) {
//			FragmentManager fm = getSupportFragmentManager();
//			ProductDetailFragment f = (ProductDetailFragment) fm
//					.findFragmentByTag(mFragmentTag);
//			if (f != null) {
//				Cursor c = f.getCursor();
//				this.showProductDetail(c);
//				fm.beginTransaction().remove(f).commit();
//				fm.executePendingTransactions();
//				// attachProductDetailFragment(f, mFragmentTag);
//			}
//		}
//		if (mFragmentTag.equals("searchresults")) {
//			FragmentManager fm = getSupportFragmentManager();
//			ProductListFragment f = (ProductListFragment) fm
//					.findFragmentByTag(mFragmentTag);
//			if (f != null) {
//				attachSearchResults(f, mFragmentTag);
//			}
//		}
//		if (mFragmentTag.equals("favorites")) {
//			FragmentManager fm = getSupportFragmentManager();
//			ProductListFragment f = (ProductListFragment) fm
//					.findFragmentByTag(mFragmentTag);
//			fm.executePendingTransactions();
//			if (f != null) {
//				fm.beginTransaction().remove(f).commit();
//
//			}
//		}
	}

	public void attachFragment(Fragment f) {

	}
}
