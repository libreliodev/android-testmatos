/**
 * 
 */
package com.niveales.library.ui.activity;

import com.niveales.library.utils.db.DBHelper;

import android.support.v4.app.FragmentActivity;

/**
 * @author Dmitry Valetin
 *
 */
public class BaseNivealesActivity extends FragmentActivity {

	public DBHelper getDBHelper() {
		return getMyApplication().mDBHelper;
	}
	
	public void setDBHelper(DBHelper h) {
		getMyApplication().mDBHelper = h;
	}
	
	public BaseNivealesApplication getMyApplication() {
		return (BaseNivealesApplication)getApplication();
	}
}
