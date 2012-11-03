package com.niveales.library.utils.adapters;

import com.niveales.library.utils.db.DBHelper;
import com.niveales.testsnowboards.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AdvancedCriteriaMainListAdapter extends BaseAdapter {

	DBHelper helper;
	Context context;
	int count;
	Cursor cursor;
	LayoutInflater inflater;
	int layout_id;
	int textViewId;
	private int criteriaTextView;

	public AdvancedCriteriaMainListAdapter(DBHelper helper, Context context, int layout_id, int textViewId, int criteriaTextView) {
		super();
		this.helper = helper;
		this.context = context;
		this.layout_id = layout_id;
		this.textViewId = textViewId;
		this.criteriaTextView = criteriaTextView;
		notifyDataSetChanged();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	
	@Override
	public void notifyDataSetChanged() {
		cursor = helper.getAllAdvancedCriteria();
	}
	
	@Override
	public int getCount() {
		return cursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		cursor.moveToPosition(position);
		return cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.ADVANCED_CRITERIA_TITLE));
	}
	
	public String getColumnName(int position) {
		cursor.moveToPosition(position);
		return cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.ADVANCED_CRITERIA_COLNAME));
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {
		View v = view;
		if(v == null) {
			v = this.inflater.inflate(layout_id, viewGroup, false);
		}
		TextView mTitle = (TextView) v.findViewById(textViewId);
		mTitle.setText(getItem(position).toString());
		TextView mDescription = (TextView) v.findViewById(criteriaTextView);
		mDescription.setText(helper.getUserSearchINputStringByColumn(getColumnName(position)));
		if(mDescription.getText().length() > 0) {
			mTitle.setTextColor(context.getResources().getColor(R.color.SelectedColor));
		}
		return v;
	}

}
