package com.niveales.library.ui.criteraselectors;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.niveales.library.utils.adapters.checked.CheckedCriteriaAdapter;
import com.niveales.library.utils.db.DBHelper;


public class RangeCriteriaSelectorFragment extends Fragment {
	View rootView;
	private String colName;

	// private int layout = R.layout.creteria_selector_fragment_layout;
	CheckedCriteriaAdapter mAdapter;
	private DBHelper helper;
	private Context context;
	private int layoutId;
	private String type;
	private OnRangeCriteriaChangedListener listener;
	private int listViewId;
	private int minEditTextId;
	private int maxEditTextId;
	private String criteria;
	private int titleResourceId;
	private EditText min;
	private EditText max;

	/**
	 * 
	 * @param type
	 * @param criteria
	 * @param titleResourceId
	 * @param helper
	 * @param context
	 * @param colName
	 * @param layoutId
	 * @param minEditTextId
	 * @param maxEditTextId
	 * @param l
	 * @return
	 */
	public static RangeCriteriaSelectorFragment getInstance(String type, String criteria, int titleResourceId,
			DBHelper helper, Context context, String colName, int layoutId,
			int minEditTextId, int maxEditTextId, OnRangeCriteriaChangedListener l) {
		RangeCriteriaSelectorFragment f = new RangeCriteriaSelectorFragment();
		f.init(type, criteria, titleResourceId, helper, context, colName, layoutId, minEditTextId, maxEditTextId, l);
		return f;
	}

	private void init(String type, String criteria, int titleResourceId, 
			DBHelper helper, Context context, String colName, int layoutId,
			int minEditTextId, int maxEditTextId, OnRangeCriteriaChangedListener l) {
		this.type = type;
		this.criteria = criteria;
		this.titleResourceId = titleResourceId;
		this.helper = helper;
		this.context = context;
		this.colName = colName;
		this.layoutId = layoutId;
		this.minEditTextId = minEditTextId;
		this.maxEditTextId = maxEditTextId;
		this.listener = l;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.setRetainInstance(true);

		rootView = inflater.inflate(layoutId, container, false);
		TextView title = (TextView) rootView.findViewById(titleResourceId);
		title.setText(criteria);
		
		min = (EditText) rootView.findViewById(minEditTextId);
		min.setTag("Mini");
		Cursor cursor = helper.rawQuery("select * from UserSearchInputs where ColName='"+colName+"' AND Title LIKE '%Mini%'", null);
		if(cursor.getCount() > 0) {
			min.setText(cursor.getString(cursor.getColumnIndexOrThrow("UserInput")));
		}
		min.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				if (actionId != EditorInfo.IME_ACTION_DONE) {
					return false;
				}
				EditText et = (EditText) view;
				saveEdit(et);
				return true;
			}});
		
		
		max = (EditText) rootView.findViewById(maxEditTextId);
		max.setTag("Max");
		cursor = helper.rawQuery("select * from UserSearchInputs where ColName='"+colName+"' AND Title LIKE '%Max%'", null);
		if(cursor.getCount() > 0) {
			max.setText(cursor.getString(cursor.getColumnIndexOrThrow("UserInput")));
		}
		max.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				if (actionId != EditorInfo.IME_ACTION_DONE) {
					return false;
				}
				EditText et = (EditText) view;
				saveEdit(et);
				return true;
			}});
		return rootView;
	};
	
	public void saveEdit(EditText et){
		String tag = et.getTag().toString();
		helper.rawQuery("delete from UserSearchInputs where ColName='"+colName+"' AND Title LIKE '%"+tag+"%'", null);
		String value = et.getEditableText().toString();
		if(!value.equals("")){
			helper.rawQuery("insert into userSearchInputs values (?, ?, ?, ?)", new String [] {
				colName,
				value,
				tag+":"+value,
				(tag.equals("Max")) ? colName + " < " + value : colName + " > " + value,
			});
		}
		listener.onCriteriaChanged(colName);
		
	}
	
	public void onPause() {
		super.onPause();
		saveEdit(max);
		saveEdit(min);
	}
	
	public interface OnRangeCriteriaChangedListener {
		public void onCriteriaChanged(String colName);
	}
}
