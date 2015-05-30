package com.ezzet.eulou.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.extra.HelperFunction;
import com.ezzet.eulou.models.MessageModel;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.services.EulouService;
import com.ezzet.eulou.utilities.CustomSharedPreferences;
import com.ezzet.eulou.utilities.LogUtil;
import com.ezzet.eulou.utilities.Utilities;
import com.sinch.android.rtc.messaging.WritableMessage;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseActivity extends FragmentActivity
		implements
			OnItemClickListener,
			OnClickListener {

	public static String mCurrentClass;
	public static UserInfo mUserInfo;
	public static ArrayList<UserInfo> mFriendUsers;
	public static ArrayList<UserInfo> mContactUsers;
	public static Map<String, Map<String, Object>> mMessages;
	private static int runningActivities = 0;
	protected CirclePageIndicator mPagerIndicator;
	protected ImageView mLeftImgBtn, mRightImgBtn;
	protected TextView mTitleTxt, mLeftTxtBtn, mRightTxtBtn, mNewMsgNoti;
	private EulouService mService = null;
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			EulouService.ServiceBinder b = (EulouService.ServiceBinder) binder;
			mService = b.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	private BroadcastReceiver mMesasgeReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

			if (!mCurrentClass.equals(ChatActivity.class.getSimpleName())) {

				int messageEvent = intent.getIntExtra(
						Constants.INTENT_MESSAGE_EVENT, 0);
				switch (messageEvent) {

					case Constants.INTENT_RECEIVED_MESSAGE :

						if (mNewMsgNoti != null) {

							String senderId = intent
									.getStringExtra(Constants.INTENT_PUSH_NEW_MESSAGE_SENDER_ID);
							String message = intent
									.getStringExtra(Constants.INTENT_PUSH_NEW_MESSAGE);
							mNewMsgNoti.setTag(senderId);
							mNewMsgNoti.setText(message);
							mNewMsgNoti.setVisibility(View.VISIBLE);
							hideNewMessageNoti();
						}

						new GetMessageHistoryTask().execute();
						break;
					case Constants.INTENT_SENT_MESSAGE :

						break;
					case Constants.INTENT_DELIVERED_MESSAGE :

						break;
					case Constants.INTENT_FAIL_MESSAGE :

						break;
					case Constants.INTENT_PUSH_NOTIFICATION_MESSAGE :

						showNotice(intent);
						break;
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Intent serviceIntent = new Intent(BaseActivity.this, EulouService.class);
		bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
		CustomSharedPreferences.init(BaseActivity.this);

		mCurrentClass = this.getClass().getSimpleName();
		if (mCurrentClass != null
				&& !mCurrentClass.equals(ChatActivity.class.getSimpleName())) {

			IntentFilter mesageIntentFilter = new IntentFilter(
					Constants.INTENT_MESSAGE);
			LocalBroadcastManager.getInstance(this).registerReceiver(
					mMesasgeReceiver, mesageIntentFilter);
		}
	}

	protected void initNavigationComponents() {
		mPagerIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
		mTitleTxt = (TextView) findViewById(R.id.main_title_txt);
		mNewMsgNoti = (TextView) findViewById(R.id.new_message_notice);
		mLeftTxtBtn = (TextView) findViewById(R.id.main_navigation_left_txt_btn);
		mLeftImgBtn = (ImageView) findViewById(R.id.main_navigation_left_img_btn);
		mRightTxtBtn = (TextView) findViewById(R.id.main_navigation_right_txt_btn);
		mRightImgBtn = (ImageView) findViewById(R.id.main_navigation_right_img_btn);

		mNewMsgNoti.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mCurrentClass = this.getClass().getSimpleName();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View itemView, int pos,
			long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
			case R.id.new_message_notice :

				String sinchId = (String) v.getTag();
				mNewMsgNoti.setVisibility(View.GONE);
				Intent chatIntent = new Intent(BaseActivity.this,
						ChatActivity.class);
				UserInfo userInfo = mService.findFriendUserBySinchID(sinchId);
				chatIntent.putExtra(Constants.INTENT_MESAGE_USER_ID, userInfo);
				startActivity(chatIntent);
				break;
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		CustomSharedPreferences.setPreferences(Constants.PREF_APP_STATUS, true);
		if (runningActivities == 0) {

			updateStatusOnline();
		}

		runningActivities++;
	}

	protected void updateStatusOnline() {

		UserInfo currentUser = mUserInfo;
		if (currentUser != null && BaseActivity.mFriendUsers != null) {

			for (UserInfo friend : BaseActivity.mFriendUsers) {

				String remoteId;
				if (friend.getMainSocial() == Constants.FACEBOOK) {

					if (!friend.getFacebookID().contains("@fb")) {

						remoteId = "fb" + friend.getFacebookID();
					} else {

						remoteId = friend.getFacebookID();
					}
				} else if (friend.getMainSocial() == Constants.TWITTER) {

					remoteId = "tw" + friend.getTwitterID();
				} else if (friend.getMainSocial() == Constants.INSTAGRAM) {

					remoteId = "in" + friend.getInstagramID();
				} else {

					remoteId = "un" + friend.getUserID();
				}

				if (!remoteId.equals("")) {

					LogUtil.e("Go online", remoteId + "-online");
					final WritableMessage message = new WritableMessage(
							remoteId, remoteId + "-online");
					message.addHeader("Time", new Date().toString());
					if (mService == null) {

						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub

								if (mService != null) {

									mService.sendMessage(message);
								}
							}
						}, 3000);
					} else {

						mService.sendMessage(message);
					}
				}
			}

			new UpdateUserStatus().execute(true);
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		runningActivities--;
		if (runningActivities == 0) {

			CustomSharedPreferences.setPreferences(Constants.PREF_APP_STATUS,
					false);
			UserInfo currentUser = mUserInfo;
			if (currentUser != null) {

				for (UserInfo friend : BaseActivity.mFriendUsers) {

					String remoteId;
					if (friend.getMainSocial() == Constants.FACEBOOK) {

						if (!friend.getFacebookID().contains("@fb")) {

							remoteId = "fb" + friend.getFacebookID();
						} else {

							remoteId = friend.getFacebookID();
						}
					} else if (friend.getMainSocial() == Constants.TWITTER) {

						remoteId = "tw" + friend.getTwitterID();
					} else if (friend.getMainSocial() == Constants.INSTAGRAM) {

						remoteId = "in" + friend.getInstagramID();
					} else {

						remoteId = "un" + friend.getUserID();
					}

					if (!remoteId.equals("")) {

						LogUtil.e("Go offline", remoteId + "-offline");
						final WritableMessage message = new WritableMessage(
								remoteId, remoteId + "-offline");
						message.addHeader("Time", new Date().toString());
						if (mService == null) {

							new Handler().postDelayed(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub

									if (mService != null) {

										mService.sendMessage(message);
									}
								}
							}, 3000);
						} else {

							mService.sendMessage(message);
						}
					}
				}

				new UpdateUserStatus().execute(false);
			}
		}

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		unbindService(mConnection);
		if (!mCurrentClass.equals(ChatActivity.class.getSimpleName())) {

			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					mMesasgeReceiver);
		}
	}

	private void showNotice(Intent intent) {

		// Notification
		String senderId = intent
				.getStringExtra(Constants.INTENT_PUSH_NEW_MESSAGE_SENDER_ID);
		String message = intent
				.getStringExtra(Constants.INTENT_PUSH_NEW_MESSAGE);
		UserInfo userInfo = mService.findFriendUserBySinchID(senderId);
		Intent pushIntent = new Intent(BaseActivity.this, ChatActivity.class);
		pushIntent.putExtra(Constants.INTENT_MESAGE_USER_ID, userInfo);
		PendingIntent pi = PendingIntent.getActivity(BaseActivity.this, 1000,
				pushIntent, 0);
		Notification noti = new Notification.Builder(BaseActivity.this)
				.setContentIntent(pi).setContentText(message).build();
		NotificationManager notiManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		noti.flags |= Notification.FLAG_ONGOING_EVENT;
		notiManager.notify(0, noti);
	}

	private void hideNewMessageNoti() {

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {

					mNewMsgNoti.setVisibility(View.GONE);
				} catch (Exception e) {

				}
			}
		}, 4000);
	}

	public class GetMessageHistoryTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub

			Map<String, Map<String, Object>> messageHist = new HashMap<String, Map<String, Object>>();
			String userId = String.valueOf(BaseActivity.mUserInfo.getUserID());
			List<MessageModel> messages = new HelperFunction(BaseActivity.this)
					.getMessageHistoryByUserId(userId);
			for (MessageModel message : messages) {

				String partnerId = "";
				boolean isReceived = false;
				if (message.getFromUser().equals(userId)) {

					isReceived = false;
					partnerId = message.getToUser();
				} else if (message.getToUser().equals(userId)) {

					partnerId = message.getFromUser();
					isReceived = true;
				}

				int partnerIdInt = Integer.valueOf(partnerId);
				if (!messageHist.containsKey(partnerId)) {

					Map<String, Object> one = new HashMap<String, Object>();
					for (UserInfo user : BaseActivity.mFriendUsers) {

						if (user.getFacebookID() != null
								&& !user.getFacebookID().equals("")) {

							if (user.getUserID() == partnerIdInt) {

								one.put("UserInfo", user);
								break;
							}
						}
					}

					if (one.get("UserInfo") == null) {

						for (UserInfo user : BaseActivity.mContactUsers) {

							if (user.getUserID() == partnerIdInt) {

								one.put("UserInfo", user);
								break;
							}
						}
					}

					if (one.get("UserInfo") == null) {

						UserInfo userInfo = new HelperFunction(
								BaseActivity.this).getUserInfoById(partnerId);
						one.put("UserInfo", userInfo);
					}

					messageHist.put(partnerId, one);
				}

				Map<String, Object> pack = messageHist.get(partnerId);
				if (isReceived) {

					int count = 0;
					try {

						count = (Integer) pack.get("ReceivedMsgCount");
					} catch (Exception ex) {

						count = 0;
					}

					count++;
					pack.put("ReceivedMsgCount", count);
				}

				if (!pack.containsKey("LastMessage")) {

					pack.put("LastTime", message.getMessageTime());
					pack.put("LastMessage", message.getMessage());
				}
			}

			List<MessageModel> messageShow = new HelperFunction(
					BaseActivity.this).getShownMessageHistoryByUser(userId);
			for (MessageModel message : messageShow) {

				String partnerId = message.getFromUser();
				Map<String, Object> pack = messageHist.get(partnerId);
				if (pack != null) {

					int count = 0;
					try {

						count = (Integer) pack.get("ShownMsgCount");
					} catch (Exception ex) {

						count = 0;
					}

					count++;
					pack.put("ShownMsgCount", count);
				}
			}

			// Sort messages
			mMessages = Utilities.sortMessage(messageHist);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			Intent updateMsgHis = new Intent(
					Constants.INTENT_BROADCAST_UPDATE_MESSAGE_HIST);
			LocalBroadcastManager.getInstance(BaseActivity.this).sendBroadcast(
					updateMsgHis);
		}
	}

	private class UpdateUserStatus extends AsyncTask<Boolean, Void, Void> {

		@Override
		protected Void doInBackground(Boolean... params) {
			// TODO Auto-generated method stub
			boolean isOnline = params[0];

			new HelperFunction(BaseActivity.this).updateStatus(isOnline,
					String.valueOf(mUserInfo.getUserID()));
			return null;
		}
	}
}