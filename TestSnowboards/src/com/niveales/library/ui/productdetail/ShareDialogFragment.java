package com.niveales.library.ui.productdetail;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class ShareDialogFragment extends DialogFragment {
	ShareDialogListener listener;
	private int shareDialogLayout;
	private int shareDialogListViewId;
	private int productId;
	private Cursor productCursor;
	
	public static ShareDialogFragment getInstance(int shareDialogLayout, int shareDialogListViewId, Cursor productCursor, ShareDialogListener pListener) {
		ShareDialogFragment f = new ShareDialogFragment();
		f.init(shareDialogLayout, shareDialogListViewId, productCursor, pListener);
		
		return f;
	}
	private  void init(int shareDialogLayout, int shareDialogListViewId, Cursor productCursor, ShareDialogListener pListener) {
		this.shareDialogLayout = shareDialogLayout;
		this.productCursor = productCursor;
		this.shareDialogListViewId = shareDialogListViewId;
		listener = pListener;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
	        Bundle savedInstanceState){
		View rootView = inflater.inflate(shareDialogLayout, container, false);
		
		ListView lv = (ListView) rootView.findViewById(shareDialogListViewId);
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				dismiss();
				share(pos);
			}});
		return rootView;
	}

	protected void share(int pos) {
		listener.onShareItemSelected(pos, productCursor);
	}

	public interface ShareDialogListener {
		public void onShareItemSelected(int pos, Cursor productCursor);
	}
}
