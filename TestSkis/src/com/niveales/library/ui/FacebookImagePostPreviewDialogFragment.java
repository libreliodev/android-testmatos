/**
 * 
 */
package com.niveales.library.ui;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;
import com.niveales.testskis.R;



/**
 * @author Dmitry Valetin
 *
 */
public class FacebookImagePostPreviewDialogFragment extends DialogFragment {

	private Button mCancelButton;
	private Button mPostButton;
	private ImageView mPostImage;
	private EditText mMessageEditText;
	private Bundle params = new Bundle();
	private String mMessage;
	private String mPic;
	private boolean mRemoved;
	private int mBackStackId;
	private ProgressBar mProgress;
	
	public void setMessage(String pMessage) {
		mMessage = pMessage;
	}

	public void setPicUri(String pic) {
		mPic = pic;
	}
	
	/**
	 * Return the message after user edit
	 * @return
	 */
	public String getEditableMessage() {
		return mMessageEditText.getEditableText().toString();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    View rootView = inflater.inflate(R.layout.facebook_image_post_preview_dialog_layout, null);
	    mProgress = (ProgressBar) rootView.findViewById(R.id.Progress);
	    mCancelButton = (Button) rootView.findViewById(R.id.CancelButton);
	    mCancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View pV) {
				dismiss();
				
			}});
	    mPostButton = (Button) rootView.findViewById(R.id.PostButton);
	    
	    mPostButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View pV) {
				try{
					mProgress.setVisibility(View.VISIBLE);
				params.putString("caption", getEditableMessage());
				params.putByteArray("photo", NivealesApplication
						.scaleImage(getActivity(), mPic));
				NivealesApplication.mAsyncRunner.request("me/photos", params, "POST",
                new RequestListener() {

					@Override
					public void onComplete(String pResponse, Object pState) {
//						Toast.makeText(getActivity(), "Posted", Toast.LENGTH_SHORT).show();
						FacebookImagePostPreviewDialogFragment.this.dismiss();
					}

					@Override
					public void onIOException(IOException pE, Object pState) {
						FacebookImagePostPreviewDialogFragment.this.dismiss();
					}

					@Override
					public void onFileNotFoundException(
							FileNotFoundException pE, Object pState) {
						FacebookImagePostPreviewDialogFragment.this.dismiss();
					}

					@Override
					public void onMalformedURLException(
							MalformedURLException pE, Object pState) {
						FacebookImagePostPreviewDialogFragment.this.dismiss();
					}

					@Override
					public void onFacebookError(final FacebookError pE, Object pState) {
						FacebookImagePostPreviewDialogFragment.this.dismiss();
					}}, FacebookImagePostPreviewDialogFragment.this);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}});
	    mPostImage = (ImageView) rootView.findViewById(R.id.PostImage);
	    byte[] data;
		try {
			data = NivealesApplication.scaleImage(getActivity(), mPic);
			Bitmap imagePreview = BitmapFactory.decodeByteArray(data, 0, data.length);
			mPostImage.setImageBitmap(imagePreview);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    mMessageEditText = (EditText) rootView.findViewById(R.id.MessageEditText);
	    mMessageEditText.setText(this.mMessage);
	    
	    builder.setView(rootView);
	    
	    
	    return builder.create();
	}
	
}
