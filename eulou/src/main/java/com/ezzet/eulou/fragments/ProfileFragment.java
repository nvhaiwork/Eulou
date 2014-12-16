package com.ezzet.eulou.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.activities.BaseActivity;
import com.ezzet.eulou.activities.EnterNumberActivity;
import com.ezzet.eulou.activities.FriendProfileActivity;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.utilities.CustomSharedPreferences;
import com.ezzet.eulou.utilities.LogUtil;
import com.ezzet.eulou.views.FBProfilePictureView;

import org.w3c.dom.Text;

import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener {

	private boolean isVerified;
	private UserInfo mCurrentUser;
	private TextView mFriendNumber;
	private FBProfilePictureView mProfilePictureViewLarge;
	private LinearLayout mShareLayout, mSocialInfoLy, mFriendList;
	private OnDisplaySocialInfoListener mDisplayListener;

	public interface OnDisplaySocialInfoListener {

		public void onDisplay();
	}

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
		if (mobNo != null && !mobNo.equals("")) {

			isVerified = true;
		}

		mFriendList = (LinearLayout) rootView
				.findViewById(R.id.profile_friend_list);
		mShareLayout = (LinearLayout) rootView
				.findViewById(R.id.profile_share_ly);
		ImageView shareFbBtn = (ImageView) rootView
				.findViewById(R.id.profile_share_fb_btn);
		mFriendNumber = (TextView) rootView
				.findViewById(R.id.profile_info_friends);
		ImageView shareSmsBtn = (ImageView) rootView
				.findViewById(R.id.profile_share_sms_btn);
		ImageView shareMailBtn = (ImageView) rootView
				.findViewById(R.id.profile_share_mail_btn);
		ImageView shareTwitterBtn = (ImageView) rootView
				.findViewById(R.id.profile_share_twitter_btn);
		TextView verifyPhoneNumTxt = (TextView) rootView
				.findViewById(R.id.profile_verify_phone_number);
		mSocialInfoLy = (LinearLayout) rootView
				.findViewById(R.id.profile_user_social_root_ly);
		FBProfilePictureView profilePictureView = (FBProfilePictureView) rootView
				.findViewById(R.id.profile_user_img);
		mProfilePictureViewLarge = (FBProfilePictureView) rootView
				.findViewById(R.id.profile_user_large_img);

		// Social information views
		TextView statusTxt = (TextView) rootView
				.findViewById(R.id.profile_info_status);
		TextView userNameTxt = (TextView) rootView
				.findViewById(R.id.profile_info_username);
		TextView fbName = (TextView) rootView
				.findViewById(R.id.profile_info_fb_name);
		// TextView twName = (TextView) rootView
		// .findViewById(R.id.profile_info_twitter_name);
		TextView phoneNumber = (TextView) rootView
				.findViewById(R.id.profile_info_phone);
		// TextView instarName = (TextView) rootView
		// .findViewById(R.id.profile_info_instar_name);
		mProfilePictureViewLarge.setSquare(true);

		// Set views
		fbName.setText(mCurrentUser.getUserName());
		phoneNumber.setText(mCurrentUser.getPhone());
		statusTxt.setText(getString(R.string.online));
		userNameTxt.setText(mCurrentUser.getUserName());
		verifyPhoneNumTxt.setText(mCurrentUser.getPhone());
		profilePictureView.setProfileId(mCurrentUser.getFacebookID());
		mProfilePictureViewLarge.setProfileId(mCurrentUser.getFacebookID());
		ViewTreeObserver viewTreeObserver = mProfilePictureViewLarge
				.getViewTreeObserver();
		viewTreeObserver
				.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {

						Display display = getActivity().getWindowManager()
								.getDefaultDisplay();
						Point size = new Point();
						display.getSize(size);
						int width = size.x;

						mProfilePictureViewLarge.getLayoutParams().width = width;
						mProfilePictureViewLarge.getLayoutParams().height = width;
					}
				});

		shareFbBtn.setOnClickListener(this);
		shareSmsBtn.setOnClickListener(this);
		shareMailBtn.setOnClickListener(this);
		shareTwitterBtn.setOnClickListener(this);
		verifyPhoneNumTxt.setOnClickListener(this);
		profilePictureView.setOnClickListener(this);
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {

			mDisplayListener = (OnDisplaySocialInfoListener) activity;
		} catch (ClassCastException cce) {

			LogUtil.e("ProfileFragment - onAttack", cce.getMessage());
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {

			displayFriendList();
		}
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
			case R.id.profile_user_img :

				mShareLayout.setVisibility(View.GONE);
				mSocialInfoLy.setVisibility(View.VISIBLE);
				mDisplayListener.onDisplay();
				break;
		}
	}

	/**
	 * Close social layout
	 * */
	public void closeSocialLayout() {

		mShareLayout.setVisibility(View.VISIBLE);
		mSocialInfoLy.setVisibility(View.GONE);
	}

	/**
	 * Display friend list
	 * */
	private void displayFriendList() {

		mFriendList.removeAllViews();
		mFriendNumber.setText("Friends (" + BaseActivity.mFriendUsers.size()
				+ ")");
		for (final UserInfo user : BaseActivity.mFriendUsers) {

			View itemView = (LinearLayout) getActivity().getLayoutInflater()
					.inflate(R.layout.layout_profile_friend_list_item, null,
							false);
			FBProfilePictureView image = (FBProfilePictureView) itemView
					.findViewById(R.id.profile_friend_list_img);
			TextView name = (TextView) itemView
					.findViewById(R.id.profile_friend_list_txt);

			// Set values
			image.setProfileId(user.getFacebookID());
			name.setText(user.getUserName());

			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					Intent friendProfileIntent = new Intent(getActivity(),
							FriendProfileActivity.class);
					friendProfileIntent.putExtra(
							Constants.INTENT_FRIEND_PROFILE_USER_INFO, user);
					startActivity(friendProfileIntent);
				}
			});

			mFriendList.addView(itemView);
		}
	}
}
