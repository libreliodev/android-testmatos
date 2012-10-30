package com.niveales.library.ui.criteraselectors;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.niveales.library.utils.adapters.CheckedCriteriaViewBinder;
import com.niveales.library.utils.adapters.checked.CheckedCriteriaAdapter;
import com.niveales.library.utils.adapters.checked.CheckedCriteriaAdapter.CriteriaChangeListener;
import com.niveales.library.utils.db.DBHelper;
import com.niveales.testsnowboards.TestSnowboardsApplication;



public class CriteriaSelectorFragment extends Fragment {
	View rootView;
	private String colName;

	// private int layout = R.layout.creteria_selector_fragment_layout;
	CheckedCriteriaAdapter mAdapter;
	private DBHelper helper;
	private Context context;
	private int layoutId;
	private int itemLayoutId;
	private int itemTextViewId;
	private int itemCheckBoxId;
	private int itemEditViewId;
	private String type;
	private OnCriteriaChangedListener listener;
	private int listViewId;
	private String criteria;
	private int titleResourceId;

	/**
	 * 
	 * @param type - as of AcvancedCriteria "Type" column
	 * @param criteria - 
	 * @param helper - instance of DBHelper 
	 * @param context - Context
	 * @param colName - as of in AdvancedCriteria "ColName" column 
	 * @param layoutId - Fragment layout id
	 * @param itemLayoutId - ListView item layout is
	 * @param listViewId - LIstView Vew id in @param layoutId
	 * @param itemTextView - TextView View id in @param itemLayoutId
	 * @param itemEditable - EditText View id in @param itemLayoutId if @param type equals "Numeric" or else CheckBox View id
	 * @param l - listener to listen criteria change events
	 * @return
	 */
	public static CriteriaSelectorFragment getInstance(String type, String criteria, int titleResourceId, 
			DBHelper helper, Context context, String colName, int layoutId,
			int itemLayoutId, int listViewId, int itemTextView, int itemEditable, OnCriteriaChangedListener l) {
		CriteriaSelectorFragment f = new CriteriaSelectorFragment();
		f.init(type, criteria, titleResourceId, helper, context, colName, layoutId, listViewId, itemLayoutId,
				itemTextView, itemEditable, l);
		return f;
	}

	private void init(String type, String criteria, int titleResourceId, DBHelper helper, Context context,
			String colName, int layoutId, int listViewId, int itemLayoutId, int textViewId,
			int editableId, OnCriteriaChangedListener l) {
		this.type = type;
		this.criteria = criteria;
		this.titleResourceId = titleResourceId;
		this.helper = helper;
		this.context = context;
		this.colName = colName;
		this.layoutId = layoutId;
		this.listViewId = listViewId;
		this.itemLayoutId = itemLayoutId;
		this.itemTextViewId = textViewId;
		if (type.equals(TestSnowboardsApplication.NUMERIC)) {
			this.itemEditViewId = editableId;
		} else {
			this.itemCheckBoxId = editableId;
		}
		this.listener = l;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(layoutId, container, false);
		TextView title = (TextView) rootView.findViewById(titleResourceId);
		title.setText(criteria);
		ListView mCreteriaSelectorListView = (ListView) rootView
				.findViewById(listViewId);
		if (!type.equals(TestSnowboardsApplication.NUMERIC)) {
			mAdapter = new CheckedCriteriaAdapter(context, helper.getColumn(colName), itemLayoutId, 
					new CheckedCriteriaViewBinder(context, new String [] {
							colName,
							colName
					}, new int [] {
							itemTextViewId,
							itemCheckBoxId
					}), helper, colName);
		}
		mAdapter.setOnCriteriaCangeListener(new CriteriaChangeListener() {

			@Override
			public void onCriteriaChanged(String value, boolean checked) {
				onCheckedCriteriaChanged(value, checked);				
			}});
		mCreteriaSelectorListView.setAdapter(mAdapter);
		// TODO: add header text
//		String headerText = mAdapter.getHeaderText();
		TextView header = (TextView) rootView
				.findViewById(titleResourceId);
//		header.setText(headerText);
		return rootView;
	};
	
	public void onCheckedCriteriaChanged(String value,
								boolean isChecked) {
		if(isChecked) {
			helper.rawQuery("insert into UserSearchInputs values(?, ?, ?, ? )", new String [] {
					colName,
					value,
					value,
					colName+ " LIKE '%"+value+"%'"
			});
		} else {
			helper.rawQuery("delete from UserSearchInputs where ColName=? AND UserInput LIKE ?", new String [] {
					colName,
					"%"+value+"%"
			});
		}
		listener.onCriteriaChanged(colName);
 	}
	
	public interface OnCriteriaChangedListener {
		public void onCriteriaChanged(String colName);
	}
}
