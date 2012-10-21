package com.niveales.testsnowboards.lexique;

import com.niveales.library.ui.lexique.LexiqueFragment;
import com.niveales.library.utils.db.DBHelper;
import com.niveales.testsnowboards.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class LexiqueActivity extends FragmentActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.lexique_activity_layout);
		DBHelper helper = new DBHelper(this, getString(R.string.DatabaseFileName));
		helper.open();
		Fragment lexiqueFragmen = LexiqueFragment.getInstance(helper,
				R.layout.lexique_fragment_layout,
				R.id.LexiqueListView,
				R.layout.lexique_list_item_layout, 
				new int[] {
						R.id.LexiqueItemTerm,
						R.id.LexiqueItemTermDefinition });
		this.getSupportFragmentManager().beginTransaction()
				.replace(R.id.ContentHolder, lexiqueFragmen).commit();
	}

}
