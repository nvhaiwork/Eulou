package com.ezzet.eulou.activities;

import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.extra.HelperFunction;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.utilities.LogUtil;
import com.ezzet.eulou.utilities.Utilities;
import com.ezzet.eulou.views.FBProfilePictureView;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by hainguyen on 12/15/14.
 */
public class FriendProfileActivity extends BaseActivity {

	private UserInfo mFriendInfo;
	private TextView mFriendNumber;
	private LinearLayout mFriendListLv;
	private List<UserInfo> mFriendList;
	private FBProfilePictureView mProfilePictureViewLarge;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_friend_profile);
		mFriendInfo = (UserInfo) getIntent().getSerializableExtra(
				Constants.INTENT_FRIEND_PROFILE_USER_INFO);
		if (mFriendInfo == null) {

			finish();
		}

		mFriendListLv = (LinearLayout) findViewById(R.id.profile_friend_list);
		mFriendNumber = (TextView) findViewById(R.id.profile_info_friends);
		mProfilePictureViewLarge = (FBProfilePictureView) findViewById(R.id.profile_user_large_img);
		LinearLayout socialInfoLy = (LinearLayout) findViewById(R.id.profile_user_social_root_ly);

		// Social information views
		TextView statusTxt = (TextView) findViewById(R.id.profile_info_status);
		TextView userNameTxt = (TextView) findViewById(R.id.profile_info_username);
		TextView fbName = (TextView) findViewById(R.id.profile_info_fb_name);
		TextView phoneNumber = (TextView) findViewById(R.id.profile_info_phone);

		// Navigation bars
		ImageView backBtn = (ImageView) findViewById(R.id.friend_navigation_back_btn);
		ImageView callBtn = (ImageView) findViewById(R.id.friend_navigation_call_btn);
		ImageView messageBtn = (ImageView) findViewById(R.id.friend_navigation_msg_btn);

		String statusStr;

		if (mFriendInfo.isOnline()) {

			statusStr = "online";
		} else {

			String lastOnlStr = mFriendInfo.getLastOnline();
			if (lastOnlStr.contains("0000")) {

				statusStr = "no messenger";
			} else {

				String messageTime;
				if (Utilities.isToday(lastOnlStr)) {

					messageTime = "today " + Utilities.formatTime(lastOnlStr);
				} else if (Utilities.isYesterday(lastOnlStr)) {

					messageTime = "yesterday "
							+ Utilities.formatTime(lastOnlStr);
				} else {

					messageTime = Utilities.formatDate(lastOnlStr);
				}

				if (messageTime.equals("01/01/1970")) {

					statusStr = "no messenger";
				} else {

					statusStr = "seen " + messageTime;
				}
			}
		}

		if (mFriendInfo.getPhone() == null || mFriendInfo.getPhone().equals("")) {

			phoneNumber.setText(getString(R.string.profile_no_phone));
		} else {

			phoneNumber.setText(mFriendInfo.getPhone());
		}

		statusTxt.setText(statusStr);
		fbName.setText(mFriendInfo.getUserName());
		userNameTxt.setText(mFriendInfo.getUserName());

		mProfilePictureViewLarge.setSquare(true);
		mProfilePictureViewLarge.setProfileId(mFriendInfo.getFacebookID());
		ViewTreeObserver viewTreeObserver = mProfilePictureViewLarge
				.getViewTreeObserver();
		viewTreeObserver
				.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {

						Display display = getWindowManager()
								.getDefaultDisplay();
						Point size = new Point();
						display.getSize(size);
						int width = size.x;

						mProfilePictureViewLarge.getLayoutParams().width = width;
						mProfilePictureViewLarge.getLayoutParams().height = width;
					}
				});

		backBtn.setOnClickListener(this);
		callBtn.setOnClickListener(this);
		messageBtn.setOnClickListener(this);
		socialInfoLy.setVisibility(View.VISIBLE);
		getFbFriend();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);

		switch (v.getId()) {

			case R.id.friend_navigation_back_btn :

				finish();
				break;
			case R.id.friend_navigation_call_btn :

				int mainSocial = mFriendInfo.getMainSocial();
				String userIDString;
				if (mainSocial == Constants.FACEBOOK) {

					userIDString = "fb" + mFriendInfo.getFacebookID();
				} else if (mainSocial == Constants.TWITTER) {

					userIDString = "tw" + mFriendInfo.getTwitterID();
				} else if (mainSocial == Constants.INSTAGRAM) {

					userIDString = "in" + mFriendInfo.getInstagramID();
				} else {

					return;
				}

				Intent intent = new Intent();
				intent.setAction("com.ezzet.eulou.action.CALLBUTTON");
				intent.putExtra("SinchID", userIDString);
				sendBroadcast(intent);
				break;
			case R.id.friend_navigation_msg_btn :

				Intent chatIntent = new Intent(FriendProfileActivity.this,
						ChatActivity.class);
				chatIntent.putExtra(Constants.INTENT_MESAGE_USER_ID,
						mFriendInfo);
				startActivity(chatIntent);
				break;
		}
	}

	/**
	 * Display friend list
	 * */
	private void displayFriendList() {

		mFriendListLv.removeAllViews();
		mFriendNumber.setText("Shared Friends (" + mFriendList.size() + ")");
		for (final UserInfo user : mFriendList) {

			View itemView = getLayoutInflater().inflate(
					R.layout.layout_profile_friend_list_item, null, false);
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

					Intent friendProfileIntent = new Intent(
							FriendProfileActivity.this,
							FriendProfileActivity.class);
					friendProfileIntent.putExtra(
							Constants.INTENT_FRIEND_PROFILE_USER_INFO, user);
					startActivity(friendProfileIntent);
				}
			});

			mFriendListLv.addView(itemView);
		}
	}

	private void getFbFriend() {

		Session activeSession = Session.getActiveSession();
		String path = String.format("/%s/friends", mFriendInfo.getFacebookID());
		if (activeSession == null || !activeSession.isOpened()) {
			Session.openActiveSessionFromCache(this);
			getFbFriend();
		} else {
			Request.newGraphPathRequest(Session.getActiveSession(), path,
					new Request.Callback() {
						@Override
						public void onCompleted(Response response) {

							try {

								GraphObject responseGraphObject = response
										.getGraphObject();
								JSONObject json = responseGraphObject
										.getInnerJSONObject();
								JSONArray friendIds = json.getJSONArray("data");

								String fbIDs = "";
								for (int i = 0; i < friendIds.length(); i++) {

									JSONObject friend = friendIds
											.getJSONObject(i);
									if (i == 0) {

										fbIDs += "fb" + friend.getString("id");
									} else {

										fbIDs += ",fb" + friend.getString("id");
									}
								}

								new GetUserFbFriend().execute(fbIDs);
							} catch (JSONException je) {

								LogUtil.e("newGraphPathRequest",
										je.getMessage());
							}
						}
					}).executeAsync();
		}
	}

	private class GetUserFbFriend extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... strings) {

			mFriendList = new HelperFunction(FriendProfileActivity.this)
					.requestFbFriendsByFbId(strings[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			displayFriendList();
		}
	}
}
