/**
 * 
 */
package com.niveales.testsnowboards;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import org.json.JSONObject;

import twitter4j.Twitter;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.niveales.library.ui.criteraselectors.CriteriaSelectorFragment;
import com.niveales.library.ui.criteraselectors.RangeCriteriaSelectorFragment;
import com.niveales.library.ui.criteraselectors.CriteriaSelectorFragment.OnCriteriaChangedListener;
import com.niveales.library.ui.criteraselectors.RangeCriteriaSelectorFragment.OnRangeCriteriaChangedListener;
import com.niveales.library.ui.lexique.LexiqueFragment;
import com.niveales.library.ui.productdetail.ProductDetailFragment;
import com.niveales.library.ui.productdetail.ProductDetailFragment.ShareProductListener;
import com.niveales.library.ui.productlist.FavoriteProductListFragment;
import com.niveales.library.ui.productlist.ProductListFragment;
import com.niveales.library.ui.productsearch.ProductSearchFragment;
import com.niveales.library.utils.adapters.CursorViewBinder;
import com.niveales.library.utils.db.DBHelper;
import com.niveales.testsnowboards.R;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.net.http.AndroidHttpClient;

/**
 * @author Dmitry Valetin
 * 
 */
public class TestSnowboardsApplication extends Application {
	public static String dbName = "snowsurf_tests2013_.sqlite";
	public static Facebook mFacebook;
	public static Twitter mTwitter;
	public static AsyncFacebookRunner mAsyncRunner;
	public static JSONObject mFriendsList;
	public static String userUID = null;
	public static String objectID = null;
	public static AndroidHttpClient httpclient = null;
	public static String[] facebookPermissions = { "offline_access",
			"publish_stream", "user_photos", "publish_checkins", "photo_upload" };

	public static Hashtable<String, String> currentPermissions = new Hashtable<String, String>();

	private static int MAX_IMAGE_DIMENSION = 720;
	public static final String HACK_ICON_URL = "http://www.facebookmobileweb.com/hackbook/img/facebook_icon_large.png";
	public static String oauthVerifier;
	public static RequestToken rToken;
	public static AccessToken mTwitterAccessToken;

	public ProductDetailFragment getProductDetailFragment(DBHelper helper,
			int id, ShareProductListener l) {
		return ProductDetailFragment.getInstance(this, helper,
				R.layout.product_detail_layout, R.id.ProductDetailsWebView,
				R.string.ProductDetailWebPage, id, new String[] { "Marque",
						"Modele", "Prix_String", "imgLR", "Gamme",
						"test_Taille_testee", "Tailles", "type_de_cambre_text",
						"Test_baseline", "Description_Test", "Test_avantages",
						"test_inconvenients", "icone_genre", "icone_cambres",
						"icone_wide", "icone_top", "img_niveau",
						"img_polyvalence", "Caractéristiques",

				}, new String[] {
						"%TAITLE%",
						"%Modele%",
						"%Budget%",
						"%img%", // product image
						"%GAMME%", "%TAILLE TESTEE%", "%TAILLES DISPONIBLES%",
						"%type_de_cambre_text%", "%Test_baseline%",
						"%Description_Test%", "%Test_avantages%",
						"%test_inconvenients%", "%icone_genre%",
						"%icone_cambres%", "%icone_wide%", "%icone_top%",
						"%img_niveau%", "%img_polyvalence%",
						"%Caractéristiques%",

				}, R.id.FavoriteCkeckBox, R.id.ShareButton, l);
	}
	
	public ProductListFragment getProductListFragment(DBHelper helper) {
		return ProductListFragment.getInstance(helper,
				"Detail", R.layout.product_list_fagment_layout,
				R.id.ProductListView, R.layout.product_list_item_layout,
				new int[] { R.id.ProductListMarqueSortButton,
						R.id.ProductListGammeSortButton,
						R.id.ProductListPrixSortButton }, new String[] {
						"Marque", "Gamme", "Prix_de_reference" },
				new CursorViewBinder(this, new String[] {
						DBHelper.MODELE_MARQUE_KEY, DBHelper.MODELE_MODELE_KEY,
						"icone_genre", "icone_cambres", "Gamme", "Prix_String",
						"icone_wide", "icone_top", "imgLR"
				// DBHelper.MODELE_PRIX_DE_REFERENCE_KEY,
				// DBHelper.MODELE_GENRE_KEY,
				// DBHelper.MODELE_IMG_KEY
						}, new int[] { R.id.productListItemGenre,
								R.id.productListItemModele,
								R.id.productListItemFemale,
								R.id.productListItemChambre,
								R.id.productListItemGamme,
								R.id.productListItemBudget,
								R.id.productListItemWide,
								R.id.productListItemPop,
								R.id.productListItemPicture }));
	}
	/**
	 * @param pHelper
	 * @return
	 */
	public FavoriteProductListFragment getFavoriteProductListFragment(DBHelper pHelper) {
		// TODO Auto-generated method stub
		return FavoriteProductListFragment.getInstance(pHelper,
				"Detail", R.layout.product_list_fagment_layout,
				R.id.ProductListView, R.layout.product_list_item_layout,
				new int[] { R.id.ProductListMarqueSortButton,
						R.id.ProductListGammeSortButton,
						R.id.ProductListPrixSortButton }, new String[] {
						"Marque", "Gamme", "Prix_de_reference" },
				new CursorViewBinder(this, new String[] {
						DBHelper.MODELE_MARQUE_KEY, DBHelper.MODELE_MODELE_KEY,
						"icone_genre", "icone_cambres", "Gamme", "Prix_String",
						"icone_wide", "icone_top", "imgLR"
				// DBHelper.MODELE_PRIX_DE_REFERENCE_KEY,
				// DBHelper.MODELE_GENRE_KEY,
				// DBHelper.MODELE_IMG_KEY
						}, new int[] { R.id.productListItemGenre,
								R.id.productListItemModele,
								R.id.productListItemFemale,
								R.id.productListItemChambre,
								R.id.productListItemGamme,
								R.id.productListItemBudget,
								R.id.productListItemWide,
								R.id.productListItemPop,
								R.id.productListItemPicture }));
	}

	public ProductSearchFragment getProductSearchFragment(DBHelper helper, int searchEditTextId) {
		return ProductSearchFragment.getInstance(helper,
				"Detail", R.layout.product_search_fagment_layout,
				R.id.ProductListView, R.layout.product_search_item_layout,
				searchEditTextId, new String [] {
				// Search columns
							DBHelper.MODELE_MARQUE_KEY, 
							DBHelper.MODELE_MODELE_KEY,
							"Gamme"
									},
				new CursorViewBinder(this, new String[] {
						// columns to display in search results list
							DBHelper.MODELE_MARQUE_KEY, 
							DBHelper.MODELE_MODELE_KEY,
							"Gamme", 
							"Prix_String",
						}, new int[] { 
								R.id.productListItemGenre,
								R.id.productListItemModele,
								R.id.productListItemGamme,
								R.id.productListItemBudget
				}));
	}

	
	public LexiqueFragment getLexiqueFragment(DBHelper helper) {
		return LexiqueFragment.getInstance(helper,
				R.layout.lexique_fragment_layout, R.id.LexiqueListView,
				R.layout.lexique_list_item_layout, new int[] {
						R.id.LexiqueItemTerm,
						R.id.LexiqueItemTermDefinition });
	}
	
	public RangeCriteriaSelectorFragment getRangeCriteriaSelectorFragment(DBHelper helper, String type, String criteria, String colName, OnRangeCriteriaChangedListener l) {
		return RangeCriteriaSelectorFragment
		.getInstance(type, criteria, R.id.RightPaneTitleTextView,
				helper, this, colName,
				R.layout.range_criteria_selector_layout,
				R.id.MinPriceInputField, R.id.MaxPriceInputField,
				l);
	}
	
	public CriteriaSelectorFragment getCriteriaSelectorFragment(DBHelper helper, String type, String criteria, String colName, OnCriteriaChangedListener l) {
		return CriteriaSelectorFragment.getInstance(
				type, criteria, R.id.RightPaneTitleTextView, helper, this,
				colName, R.layout.creteria_selector_fragment_layout,
				R.layout.checked_criteria_selector_item_layout,
				R.id.CreteriaSelectorListView, R.id.CriteriaTextView,
				R.id.CriteriaCheckBox, l);
	}
	
	public static byte[] scaleImage(Context context, Uri photoUri)
			throws IOException {
		InputStream is = context.getContentResolver().openInputStream(photoUri);
		BitmapFactory.Options dbo = new BitmapFactory.Options();
		dbo.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, dbo);
		is.close();

		int rotatedWidth, rotatedHeight;
		int orientation = getOrientation(context, photoUri);

		if (orientation == 90 || orientation == 270) {
			rotatedWidth = dbo.outHeight;
			rotatedHeight = dbo.outWidth;
		} else {
			rotatedWidth = dbo.outWidth;
			rotatedHeight = dbo.outHeight;
		}

		Bitmap srcBitmap;
		is = context.getContentResolver().openInputStream(photoUri);
		if (rotatedWidth > MAX_IMAGE_DIMENSION
				|| rotatedHeight > MAX_IMAGE_DIMENSION) {
			float widthRatio = ((float) rotatedWidth)
					/ ((float) MAX_IMAGE_DIMENSION);
			float heightRatio = ((float) rotatedHeight)
					/ ((float) MAX_IMAGE_DIMENSION);
			float maxRatio = Math.max(widthRatio, heightRatio);

			// Create the bitmap from file
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = (int) maxRatio;
			srcBitmap = BitmapFactory.decodeStream(is, null, options);
		} else {
			srcBitmap = BitmapFactory.decodeStream(is);
		}
		is.close();

		/*
		 * if the orientation is not 0 (or -1, which means we don't know), we
		 * have to do a rotation.
		 */
		if (orientation > 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(orientation);

			srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0,
					srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
		}

		String type = context.getContentResolver().getType(photoUri);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (type.equals("image/png")) {
			srcBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		} else if (type.equals("image/jpg") || type.equals("image/jpeg")) {
			srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		}
		byte[] bMapArray = baos.toByteArray();
		baos.close();
		return bMapArray;
	}

	public static byte[] scaleImage(Context context, String assetUri)
			throws IOException {
		InputStream is = context.getAssets().open(assetUri);
		BitmapFactory.Options dbo = new BitmapFactory.Options();
		dbo.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, dbo);
		is.close();

		int rotatedWidth, rotatedHeight;
		int orientation = 0;
		if (orientation == 90 || orientation == 270) {
			rotatedWidth = dbo.outHeight;
			rotatedHeight = dbo.outWidth;
		} else {
			rotatedWidth = dbo.outWidth;
			rotatedHeight = dbo.outHeight;
		}

		Bitmap srcBitmap;
		is = context.getAssets().open(assetUri);
		if (rotatedWidth > MAX_IMAGE_DIMENSION
				|| rotatedHeight > MAX_IMAGE_DIMENSION) {
			float widthRatio = ((float) rotatedWidth)
					/ ((float) MAX_IMAGE_DIMENSION);
			float heightRatio = ((float) rotatedHeight)
					/ ((float) MAX_IMAGE_DIMENSION);
			float maxRatio = Math.max(widthRatio, heightRatio);

			// Create the bitmap from file
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = (int) maxRatio;
			srcBitmap = BitmapFactory.decodeStream(is, null, options);
		} else {
			srcBitmap = BitmapFactory.decodeStream(is);
		}
		is.close();

		/*
		 * if the orientation is not 0 (or -1, which means we don't know), we
		 * have to do a rotation.
		 */
		if (orientation > 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(orientation);

			srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0,
					srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		srcBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] bMapArray = baos.toByteArray();
		baos.close();
		return bMapArray;
	}

	/**
	 * @param pContext
	 * @param pPhotoUri
	 * @return
	 */
	private static int getOrientation(Context pContext, Uri pPhotoUri) {
		// TODO Auto-generated method stub
		return 0;
	}

	

}
