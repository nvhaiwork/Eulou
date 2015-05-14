package com.ezzet.eulou.activities;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ezzet.eulou.R;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.fragments.RecentCallFragment;
import com.ezzet.eulou.fragments.ContactsFragment;
import com.ezzet.eulou.fragments.MessagesFragment;
import com.ezzet.eulou.fragments.ProfileFragment;
import com.ezzet.eulou.models.CallHistoryItem;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.services.EulouService;
import com.ezzet.eulou.utilities.Utilities;
import com.ezzet.eulou.views.CustomSwipeViewPager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class MainActivity extends BaseActivity
		implements
			View.OnClickListener,
			ProfileFragment.OnDisplaySocialInfoListener {

	public EulouService service = null;
	private CustomSwipeViewPager mPager;
	private PagerAdapter mPagerAdapter;
	private boolean isFromVerify = false, isProfileShown;
	private AsyncHttpClient calls_client = new AsyncHttpClient();

	private BroadcastReceiver mUpdateMessageBroadcast = new BroadcastReceiver() {

		public void onReceive(android.content.Context context, Intent intent) {

			if (mPager.getCurrentItem() == 0) {

				MessagesFragment currentFragment = (MessagesFragment) mPager
						.getAdapter().instantiateItem(mPager,
								mPager.getCurrentItem());
				if (currentFragment != null) {

					currentFragment.updateMessageHistory();
				}
			}
		}
	};

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			EulouService.ServiceBinder b = (EulouService.ServiceBinder) binder;
			service = b.getService();

			if (isFromVerify) {
				service.requestUsersFromContacts();
			}

			Intent intent = new Intent();
			intent.setAction("com.ezzet.eulou.action.SERVICEBOUND");
			sendBroadcast(intent);
		}

		public void onServiceDisconnected(ComponentName className) {
			service = null;
		}
	};

	private RelativeLayout mSettingLayout;
	private ImageView mLogoutBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initNavigationComponents();
		if (getIntent().getExtras() != null) {
			isFromVerify = getIntent().getExtras().getBoolean("isFromVerify",
					false);
		}

		mPager = (CustomSwipeViewPager) findViewById(R.id.pager);
		mLogoutBtn = (ImageView) findViewById(R.id.main_setting_logout_btn);
		mSettingLayout = (RelativeLayout) findViewById(R.id.main_setting_layout);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {

				doPageChange(position);
			}
		});

		mPager.setOffscreenPageLimit(4);
		mLogoutBtn.setOnClickListener(this);
		mLeftImgBtn.setOnClickListener(this);
		mRightImgBtn.setOnClickListener(this);
		mPager.setCurrentItem(Constants.TAB_CONTACTS);
		Intent serviceIntent = new Intent(MainActivity.this, EulouService.class);
		startService(serviceIntent);
		getApplicationContext().bindService(serviceIntent, mConnection,
				BIND_AUTO_CREATE);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateStatusOnline();
			}
		}, 3000);

		IntentFilter messageUpdateFilter = new IntentFilter(
				Constants.INTENT_BROADCAST_UPDATE_MESSAGE_HIST);
		LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(
				mUpdateMessageBroadcast, messageUpdateFilter);
		new GetMessageHistoryTask().execute();
	}

	@Override
	public void onDisplay() {

		setPagerSwipeable(false);
		mLeftImgBtn.setImageResource(R.drawable.ic_back);
		isProfileShown = true;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (isProfileShown && mSettingLayout.getVisibility() != View.VISIBLE) {

			isProfileShown = false;
			mLeftImgBtn.setImageResource(R.drawable.ic_recent_calls);
			ProfileFragment currentFragment = (ProfileFragment) mPager
					.getAdapter().instantiateItem(mPager, 3);
			if (currentFragment != null) {

				currentFragment.closeSocialLayout();
			}

			setPagerSwipeable(true);
		} else if (mSettingLayout.getVisibility() == View.VISIBLE) {

			doShowHideSettingLayout(false);
		} else {

			super.onBackPressed();
		}
	}
	@Override
	public void onDestroy() {
		getApplicationContext().unbindService(mConnection);
		calls_client.cancelAllRequests(true);
		LocalBroadcastManager.getInstance(MainActivity.this)
				.unregisterReceiver(mUpdateMessageBroadcast);
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (service != null) {

			if (mFriendUsers.size() == 0) {

				service.requestFriendsFacebook();
			}

			if (mContactUsers.size() == 0) {

				service.requestUsersFromContacts();
			}

			if (mMessages == null || mMessages.size() == 0) {

				new GetMessageHistoryTask().execute();
			}
		}

		readCallHistory();
	}

	@Override
	public void onClick(View v) {

		super.onClick(v);
		switch (v.getId()) {
			case R.id.main_navigation_left_img_btn :

				if (isProfileShown
						&& mSettingLayout.getVisibility() != View.VISIBLE) {

					isProfileShown = false;
					mLeftImgBtn.setImageResource(R.drawable.ic_recent_calls);
					ProfileFragment currentFragment = (ProfileFragment) mPager
							.getAdapter().instantiateItem(mPager, 3);
					if (currentFragment != null) {

						currentFragment.closeSocialLayout();
					}

					setPagerSwipeable(true);
				} else {
					int curPos = mPager.getCurrentItem();
					if (mSettingLayout.getVisibility() == View.VISIBLE) {

						doShowHideSettingLayout(false);
					} else if (curPos == 0) {

						Intent newMsgIntent = new Intent(MainActivity.this,
								NewMessageActivity.class);
						startActivity(newMsgIntent);
					} else {

						curPos -= 1;
						mPager.setCurrentItem(curPos, true);
					}
				}
				break;
			case R.id.main_navigation_right_img_btn :

				int curPos = mPager.getCurrentItem();
				if (curPos == Constants.TAB_PROFILE) {

					doShowHideSettingLayout(true);
				} else {

					curPos += 1;
					mPager.setCurrentItem(curPos, true);
				}
				break;
			case R.id.main_setting_logout_btn :

				doLogout();
				break;
		}
	}

	/**
	 * Do log out
	 */
	private void doLogout() {

		service.doLogout();
		CallHistoryItem.historyArray.clear();
		Intent signinIntent = new Intent(MainActivity.this,
				SignInActivity.class);
		startActivity(signinIntent);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		finish();
	}

	private void setPagerSwipeable(boolean isSwipeable) {

		mPager.setCanSwipe(isSwipeable);
	}

	/**
	 * Set up views/resources base on current page
	 */
	private void doPageChange(int position) {

		Utilities.doHideKeyboard(MainActivity.this, getCurrentFocus());
		switch (position) {
			case Constants.TAB_MESSAGES :

				mTitleTxt.setText(getString(R.string.messages));
				mLeftImgBtn.setImageResource(R.drawable.ic_write_msg);
				mRightImgBtn.setImageResource(R.drawable.ic_contacts);
				mNavigatorGroup.check(R.id.main_header_navigator_item1);
				break;
			case Constants.TAB_CONTACTS :

				mTitleTxt.setText(getString(R.string.contacts));
				mLeftImgBtn.setImageResource(R.drawable.ic_messages);
				mRightImgBtn.setImageResource(R.drawable.ic_recent_calls);
				mNavigatorGroup.check(R.id.main_header_navigator_item2);
				break;
			case Constants.TAB_RECENT_CALLS :

				mTitleTxt.setText(getString(R.string.recent_calls));
				mLeftImgBtn.setImageResource(R.drawable.ic_contacts);
				mRightImgBtn.setImageResource(R.drawable.ic_profile);
				mNavigatorGroup.check(R.id.main_header_navigator_item3);
				break;
			case Constants.TAB_PROFILE :

				mTitleTxt.setText(getString(R.string.profile));
				mLeftImgBtn.setImageResource(R.drawable.ic_recent_calls);
				mRightImgBtn.setImageResource(R.drawable.ic_setting);
				mNavigatorGroup.check(R.id.main_header_navigator_item4);
				break;
		}
	}

	/**
	 * Show or hide setting layout and other components
	 *
	 * @param isShow
	 *            true is show setting layout
	 */
	private void doShowHideSettingLayout(boolean isShow) {

		if (isShow) {

			mNavigatorGroup.setVisibility(View.GONE);
			mRightImgBtn.setVisibility(View.INVISIBLE);
			mSettingLayout.setVisibility(View.VISIBLE);
			mTitleTxt.setText(getString(R.string.setting));
			mLeftImgBtn.setImageResource(R.drawable.ic_back);
		} else {

			mNavigatorGroup.setVisibility(View.VISIBLE);
			mSettingLayout.setVisibility(View.GONE);
			mRightImgBtn.setVisibility(View.VISIBLE);
			mTitleTxt.setText(getString(R.string.profile));
			if (!isProfileShown) {

				mLeftImgBtn.setImageResource(R.drawable.ic_recent_calls);
			}
		}
	}

	private void readCallHistory() {
		calls_client.cancelAllRequests(true);

		String requestString = Constants.EULOU_SERVICE_URL + "?action="
				+ Constants.API_CALL_HISTORY + "&userid="
				+ mUserInfo.getUserID() + "&loaded=0&limit=20";
		calls_client.get(requestString, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {

				try {
					JSONArray responseArray = new JSONArray(new String(
							responseBody));
					CallHistoryItem.historyArray.clear();

					for (int i = 0; i < responseArray.length(); i++) {
						JSONObject historyObject = (JSONObject) responseArray
								.get(i);

						CallHistoryItem item = new CallHistoryItem();
						item.userInfo = new UserInfo();
						item.userInfo.setUserID(historyObject.getInt("user"));
						item.userInfo.setMainSocial(historyObject
								.getInt("mainSocial"));
						item.userInfo.setGender(historyObject.getInt("gender"));
						item.userInfo.setFacebookID(historyObject
								.getString("facebookID"));
						item.userInfo.setTwitterID(historyObject
								.getString("twitterID"));
						item.userInfo.setInstagramID(historyObject
								.getString("instagramID"));
						item.userInfo.setUserMail(historyObject
								.getString("usermail"));
						item.userInfo.setUserName(historyObject
								.getString("username"));
						// item.userInfo.setLastOnline(historyObject
						// .getString("last_online"));
						// if (historyObject.getString("is_online").equals("1"))
						// {
						//
						// item.userInfo.setOnline(true);
						// }

						item.contact = historyObject.getString("username");
						item.contactID = historyObject.getString("user");
						item.startDate = historyObject.getString("startdate");
						item.endDate = historyObject.getString("enddate");
						item.duration = historyObject.getString("duration");
						item.endCause = historyObject.getInt("callstatus");
						item.direction = historyObject.getString("direction");
						item.count = historyObject.getString("cnt");
						item.groupdate = historyObject.getString("groupdate");

						CallHistoryItem.historyArray.add(item);
					}

				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this.getApplicationContext(),
							e.getMessage(), Toast.LENGTH_LONG).show();
					return;
				}

				Intent intent = new Intent();
				intent.setAction("com.ezzet.eulou.action.READCALLS");
				sendBroadcast(intent);

				return;
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {

				Toast.makeText(MainActivity.this.getApplicationContext(),
						"Failed to retrieve friend users.", Toast.LENGTH_LONG)
						.show();
				return;

			}
		});
	}

	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			Fragment fragment = null;
			switch (position) {
				case Constants.TAB_MESSAGES :

					fragment = new MessagesFragment();
					break;
				case Constants.TAB_CONTACTS :

					fragment = new ContactsFragment();
					break;
				case Constants.TAB_RECENT_CALLS :

					fragment = new RecentCallFragment();
					break;
				case Constants.TAB_PROFILE :

					fragment = new ProfileFragment();
					break;
			}

			return fragment;
		}

		@Override
		public int getCount() {

			return 4;
		}
	}
}
