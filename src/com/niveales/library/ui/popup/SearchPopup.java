/**
 * 
 */
package com.niveales.library.ui.popup;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.niveales.library.utils.adapters.search.SearchAdapter;
import com.niveales.testsnowboards.R;

/**
 * @author Dmitry Valetin
 *
 */
public class SearchPopup extends QuickAction {

	private SearchAdapter mAdapter;
	private ListView mProductListView;
	private EditText mSearchEditText;

	/**
	 * @param pAnchor
	 */
	public SearchPopup(EditText pSearchEditText, SearchAdapter adapter) {
		super(pSearchEditText);
		mSearchEditText = pSearchEditText;
		mAdapter = adapter;
		LayoutInflater inflater = (LayoutInflater) mSearchEditText.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View searchView = inflater.inflate( R.layout.product_search_fagment_layout,
				null);
		mProductListView = (ListView) searchView.findViewById(R.id.ProductListView);
		mProductListView.setAdapter(mAdapter);
		this.addActionItem(new ActionItem(searchView));
		mProductListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> pArg0, View pArg1,
					int pArg2, long pArg3) {
				// TODO Auto-generated method stub
				
			}});
		mSearchEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable pArg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence pArg0, int pArg1,
					int pArg2, int pArg3) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence pArg0, int pArg1, int pArg2,
					int pArg3) {
//				String search = mSearchEditText.getEditableText().toString();
				mAdapter.setSearch(pArg0.toString());
				
			}} );
	}
	
	@Override
	public void show() {
		super.show();
		mSearchEditText.requestFocus();
	}
}
