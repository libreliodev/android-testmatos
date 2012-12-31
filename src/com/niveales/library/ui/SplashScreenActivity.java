/**
 * 
 */
package com.niveales.library.ui;

import com.niveales.library.utils.db.DBHelper;
import com.niveales.testsnowboards.R;
import com.niveales.testsnowboards.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;

/**
 * @author Dmitry Valetin
 *
 */
public class SplashScreenActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Configuration newConfig = getResources().getConfiguration();
		if ((newConfig.screenLayout & Configuration.SCREENLAYOUT_SIZE_XLARGE) == 0)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.splashscreen_layout);
		Handler h = new Handler();
		h.postDelayed(new Runnable() {

			@Override
			public void run() {
				startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
				finish();
			}}, 3000);
		DBHelper helper = new DBHelper(this, NivealesApplication.dbName);
		helper.open();
		helper.close();
		
	}

}
