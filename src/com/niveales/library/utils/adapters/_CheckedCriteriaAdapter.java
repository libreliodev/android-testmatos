package com.niveales.library.utils.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.niveales.library.utils.Consts;
import com.niveales.library.utils.db.DBHelper;

public class _CheckedCriteriaAdapter extends MyBaseCriteriaAdapter {
	DBHelper helper;
	Context context;
	int count;
	Cursor cursor;
	LayoutInflater inflater;
	int layout_id;
	int textViewId;
	int checkBoxId;
	String mColName;
	
	public _CheckedCriteriaAdapter(DBHelper helper, Context context, String ColName,
			int layout_id, int textViewId, int checkBoxId) {
		super();
		this.helper = helper;
		this.context = context;
		this.mColName = ColName;
		this.layout_id = layout_id;
		this.textViewId = textViewId;
		this.checkBoxId = checkBoxId;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		notifyDataSetChanged();
	}
	
	@Override
	public void notifyDataSetChanged() {
		// Workaround DB bugs
		mColName.replaceAll("Genre", "Id_genre");
		cursor = helper.getAllFromTable("AdvancedSelect__"+mColName);
		if(cursor != null) {
			cursor.moveToFirst();
			headerText = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.ADVANCED_SELECT_HEADER_KEY));
		}
		super.notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return cursor.getCount();
	}

	@Override
	public Cursor getItem(int position) {
		cursor.moveToPosition(position);
		return cursor;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if(v == null) {
			v = inflater.inflate(layout_id, parent, false);
		}
		Cursor c = getItem(position);
		String title = c.getString(c.getColumnIndexOrThrow(DBHelper.ADVANCED_CRITERIA_TITLE));
		TextView tv = (TextView) v.findViewById(textViewId);
		tv.setText(title);
		CheckBox cb = (CheckBox) v.findViewById(checkBoxId);
		cb.setText("");
		String selected = c.getString(c.getColumnIndexOrThrow(DBHelper.ADVANCED_SELECT_ICON_KEY));
		cb.setChecked(selected.equals(Consts.SELECTED));
		cb.setTag(position);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cb, boolean checked) {
				if(!cb.isPressed()) {
					// avoid listener to fire on programmatic state change
					return;
				}
				int position = (Integer) cb.getTag();
				Cursor c = getItem(position);
				String action = c.getString(c.getColumnIndexOrThrow(DBHelper.ADVANCED_SELECT_DETAILLINK_KEY));
				helper.rawQuery(action, null).moveToFirst();
				notifyDataSetChanged();
			}});
		return v;
	}


}
