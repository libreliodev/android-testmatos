package com.niveales.library.ui.criteraselectors;

import android.content.Context;
import android.database.Cursor;
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
	private String type;
	private OnCriteriaChangedListener listener;
	private int listViewId;
	private String criteria;
	private int titleResourceId;
	private String title;

	/**
	 * 
	 * @param type - as of AcvancedCriteria "Type" column
	 * @param criteria - 
	 * @param colName - as of in AdvancedCriteria "ColName" column 
	 * @param l - listener to listen criteria change events
	 * @return
	 */
	public static CriteriaSelectorFragment getInstance(int pPosition, 
			OnCriteriaChangedListener l) {
		CriteriaSelectorFragment f = new CriteriaSelectorFragment();
		f.init(pPosition, l);
		return f;
	}

	private void init(int pPosition,  OnCriteriaChangedListener l) {

		Cursor cursor = TestSnowboardsApplication.getDBHelper()
				.getAllAdvancedCriteria();

		cursor.moveToPosition(pPosition);
		this.criteria = cursor.getString(0);
		this.type = cursor.getString(2);
		this.colName = cursor.getString(1);
		
		this.title = cursor.getString(3);

		this.listener = l;
		
		titleResourceId = TestSnowboardsApplication.CriteriaSelectorConstants.CRITERIA_SELECTOR_RIGHTPANE_TITLE_TEXTVIEW;
		layoutId = TestSnowboardsApplication.CriteriaSelectorConstants.CRETERIA_SELECTOR_FRAGMENT_LAYOUT_ID;
		itemLayoutId = TestSnowboardsApplication.CriteriaSelectorConstants.CHECKED_CRITERIA_SELECTOR_ITEM_LAYOUT_ID;
		listViewId = TestSnowboardsApplication.CriteriaSelectorConstants.CRETERIA_SELECTOR_LISTVIEW_VIEW_ID;
		itemTextViewId = TestSnowboardsApplication.CriteriaSelectorConstants.CRITERIA_SELECTOR_CRITERIA_TEXTVIEW_VIEW_ID; 
		itemCheckBoxId = TestSnowboardsApplication.CriteriaSelectorConstants.CRITERIA_SELECTOR_CRITERIA_CHECKBOX_VIEW_ID;
		
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(layoutId, container, false);
		this.helper = TestSnowboardsApplication.getDBHelper();
		this.context = getActivity();
		TextView topTitle = (TextView) rootView.findViewById(titleResourceId);
		topTitle.setText(criteria);
		TextView criteriaTitle = (TextView) rootView.findViewById(TestSnowboardsApplication.CriteriaSelectorConstants.CRETERIA_SELECTOR_TITLE_VIEW_ID);
		criteriaTitle.setText(title);
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
