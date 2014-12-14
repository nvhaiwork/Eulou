package com.ezzet.eulou.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.activities.BaseActivity;
import com.ezzet.eulou.activities.EnterNumberActivity;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.utilities.CustomSharedPreferences;
import com.ezzet.eulou.utilities.LogUtil;
import com.ezzet.eulou.views.FBProfilePictureView;

import org.w3c.dom.Text;

import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener {

	private boolean isVerified;
	private UserInfo mCurrentUser;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.activity_profile, container, false);

		mCurrentUser = BaseActivity.mUserInfo;
		String mobNo = CustomSharedPreferences.getPreferences("number", null);
		isVerified = (mobNo != null);
		FBProfilePictureView profilePictureView = (FBProfilePictureView) rootView
				.findViewById(R.id.profile_user_img);
		ImageView shareFbBtn = (ImageView) rootView
				.findViewById(R.id.profile_share_fb_btn);
		ImageView shareSmsBtn = (ImageView) rootView
				.findViewById(R.id.profile_share_sms_btn);
		ImageView shareMailBtn = (ImageView) rootView
				.findViewById(R.id.profile_share_mail_btn);
		ImageView shareTwitterBtn = (ImageView) rootView
				.findViewById(R.id.profile_share_twitter_btn);
		TextView verifyPhoneNumTxt = (TextView) rootView
				.findViewById(R.id.profile_verify_phone_number);

		verifyPhoneNumTxt.setText(mCurrentUser.getPhone());
		profilePictureView.setProfileId(mCurrentUser.getFacebookID());

		shareFbBtn.setOnClickListener(this);
		shareSmsBtn.setOnClickListener(this);
		shareMailBtn.setOnClickListener(this);
		shareTwitterBtn.setOnClickListener(this);
		verifyPhoneNumTxt.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

			case R.id.profile_verify_phone_number :

				if (!isVerified) {

					Intent intent = new Intent(getActivity(),
							EnterNumberActivity.class);
					startActivity(intent);
				}

				break;
			case R.id.profile_share_fb_btn :

				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT,
						getString(R.string.eulou_homepage));
				boolean facebookAppFound = false;
				List<ResolveInfo> matches = getActivity().getPackageManager()
						.queryIntentActivities(intent, 0);
				for (ResolveInfo info : matches) {
					if (info.activityInfo.packageName.toLowerCase().startsWith(
							"com.facebook.katana")) {

						intent.setPackage(info.activityInfo.packageName);
						facebookAppFound = true;
						break;
					}
				}

				if (!facebookAppFound) {

					String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u="
							+ getString(R.string.eulou_homepage);
					intent = new Intent(Intent.ACTION_VIEW,
							Uri.parse(sharerUrl));
				}

				startActivity(intent);
				break;
			case R.id.profile_share_twitter_btn :

				intent = new Intent(Intent.ACTION_VIEW);
				intent.putExtra(Intent.EXTRA_TEXT,
						getString(R.string.share_eulou_text));
				matches = getActivity().getPackageManager()
						.queryIntentActivities(intent, 0);
				for (ResolveInfo info : matches) {
					if (info.activityInfo.packageName.toLowerCase().startsWith(
							"com.twitter")) {

						intent.setPackage(info.activityInfo.packageName);
					}
				}

				startActivity(intent);
				break;
			case R.id.profile_share_mail_btn :

				intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT,
						getString(R.string.share_eulou_title));
				intent.putExtra(Intent.EXTRA_TEXT,
						getString(R.string.share_eulou_text));
				try {

					startActivity(Intent.createChooser(intent, "Send Email"));
				} catch (Exception e) {

					LogUtil.e(ProfileFragment.class.getSimpleName(),
							e.getMessage());
				}

				break;
			case R.id.profile_share_sms_btn :

				intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
				intent.putExtra("sms_body",
						getString(R.string.share_eulou_text));
				startActivity(intent);
				break;
		}
	}
}
