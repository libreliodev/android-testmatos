/**
 * 
 */
package com.niveales.library.ui.activity;

import android.support.v4.app.Fragment;

/**
 * @author Dmitry Valetin
 *
 */
public class BaseNivealesFragment extends Fragment {

	public BaseNivealesApplication getApplication() {
		return (BaseNivealesApplication) getActivity().getApplication();
	}
}
