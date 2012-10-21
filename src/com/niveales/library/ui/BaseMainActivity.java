package com.niveales.library.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BaseMainActivity extends FragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	
	public void init(){
	}
}
