/**
 * 
 */
package com.niveales.library.ui;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import twitter4j.Twitter;
import twitter4j.TwitterException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
public class TwitterPostPreviewDialogFragment extends DialogFragment {

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
	private Twitter mTwitter;
	
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
	    View rootView = inflater.inflate(R.layout.twitter_post_preview_dialog_layout, null);
	    mTwitter = ((NivealesApplication)getActivity().getApplication()).mTwitter;
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
				new TwitterSendTask().execute(mMessageEditText.getEditableText().toString());
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    mMessageEditText = (EditText) rootView.findViewById(R.id.MessageEditText);
	    mMessageEditText.setText(this.mMessage);
	    
	    builder.setView(rootView);
	    
	    
	    return builder.create();
	}

	class TwitterSendTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute(){
			
		}
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected String doInBackground(String... pArg0) {
			try {
				mTwitter.updateStatus(mMessageEditText.getEditableText().toString());
			} catch (TwitterException e) {
				e.printStackTrace();
				return e.getMessage();
			}
			return null;
		}
		@Override
		protected void onPostExecute(String args) {
			
			if(args != null) {
				AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
				b.setTitle("Tweet failed:");
				b.setMessage(args);
				b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface pDialog, int pWhich) {
						pDialog.dismiss();
						
					}
				})
				.create().show();
			}
			TwitterPostPreviewDialogFragment.this.dismiss();
		}
	}
}
