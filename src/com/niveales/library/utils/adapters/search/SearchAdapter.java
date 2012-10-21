package com.niveales.library.utils.adapters.search;

import com.niveales.library.utils.adapters.BoundAdapter;
import com.niveales.library.utils.adapters.CursorViewBinder;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SearchAdapter extends BoundAdapter {

	protected CursorViewBinder binder;
	private int listItemLayoutId;
	private LayoutInflater inflater;
	private Cursor cursor;
	private String mSortOrder = null;
	
	public SearchAdapter(Context context, Cursor c, int listItemLayoutId, CursorViewBinder binder) {
		super(context, c, listItemLayoutId, binder);
	}

	/**
	 * @param pSearch - search string
	 */
	public void setSearch(String pSearch) {
		
	}

}
