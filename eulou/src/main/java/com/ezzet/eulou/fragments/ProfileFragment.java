package com.ezzet.eulou.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.activities.BaseActivity;
import com.ezzet.eulou.activities.EnterNumberActivity;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.views.FBProfilePictureView;

public class ProfileFragment extends Fragment {

	boolean isVerified = false;
	private String userfbid;
	private String username;
	private String useremail;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.activity_profile, container, false);

		FBProfilePictureView profilePictureView = (FBProfilePictureView) rootView
				.findViewById(R.id.profilePictureView);
		TextView textViewUserName = (TextView) rootView
				.findViewById(R.id.textViewUserName);
		TextView textViewUserMail = (TextView) rootView
				.findViewById(R.id.textViewUserMail);
		LinearLayout linearNumber = (LinearLayout) rootView
				.findViewById(R.id.linearNumber);
		TextView tvNumber = (TextView) rootView
				.findViewById(R.id.textViewUserNumber);
		UserInfo currentUser = BaseActivity.mUserInfo;
		userfbid = currentUser.getFacebookID();
		username = currentUser.getUserName();
		useremail = currentUser.getUserMail();

		profilePictureView.setProfileId(userfbid);

		textViewUserName.setText(username);
		textViewUserMail.setText(useremail);
		SharedPreferences sharedpreferences = getActivity()
				.getSharedPreferences("CodePref", Context.MODE_PRIVATE);
		String mobNo = sharedpreferences.getString("number", null);
		if (mobNo == null) {
			isVerified = false;
		} else {
			tvNumber.setText(mobNo);
			isVerified = true;
		}

		linearNumber.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (!isVerified) {
					Intent intent = new Intent(getActivity()
							.getApplicationContext(), EnterNumberActivity.class);
					startActivity(intent);
				}
			}
		});

		// buttonInviteSMS.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Intent intent = new Intent(Intent.ACTION_VIEW, Uri
		// .parse("sms:"));
		// intent.putExtra("sms_body",
		// "Hi, I'm using Eulou ! Get it on the Store.\nhttp://eulou.tn");
		// startActivity(intent);
		// }
		//
		// });
		//
		// buttonInviteMail.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Intent intent = new Intent(Intent.ACTION_SEND);
		// intent.setType("text/plain");
		// intent.putExtra(Intent.EXTRA_SUBJECT, "Eulou !  New app VOIP");
		// intent.putExtra(Intent.EXTRA_TEXT,
		// "Hi, I'm using Eulou ! Get it on the Store.\nhttp://eulou.tn");
		// try {
		//
		// startActivity(Intent.createChooser(intent, "Send Email"));
		// } catch (Exception e) {
		// e.printStackTrace();
		// Toast.makeText(getActivity().getApplicationContext(),
		// "There is no email client installed.",
		// Toast.LENGTH_LONG).show();
		// }
		// }
		//
		// });

		return rootView;
	}

}
