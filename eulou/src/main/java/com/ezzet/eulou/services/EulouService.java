package com.ezzet.eulou.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.ezzet.eulou.activities.BaseActivity;
import com.ezzet.eulou.activities.CallScreenActivity;
import com.ezzet.eulou.activities.ChatActivity;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.extra.ConnectionDetector;
import com.ezzet.eulou.extra.HelperFunction;
import com.ezzet.eulou.models.CurrentCall;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.utilities.CustomSharedPreferences;
import com.ezzet.eulou.utilities.LogUtil;
import com.facebook.Request;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.messaging.WritableMessage;

public class EulouService extends Service {

	private final ServiceBinder binder = new ServiceBinder();
	CallButtonReceiver callButtonReceiver;
	private SinchClientService mSinchClientService;

	@Override
	public void onCreate() {
		super.onCreate();

		BaseActivity.mFriendUsers = new ArrayList<UserInfo>();
		BaseActivity.mContactUsers = new ArrayList<UserInfo>();
		String json = CustomSharedPreferences
				.getPreferences("LoggedinUser", "");
		if (json == null) {

			CustomSharedPreferences.init(getApplicationContext());
			json = CustomSharedPreferences.getPreferences("LoggedinUser", "");
		}

		if (json != null && !json.equals("")) {

			Gson gson = new Gson();
			BaseActivity.mUserInfo = gson.fromJson(json, UserInfo.class);
			onLoginSuccess();
		}

		callButtonReceiver = new CallButtonReceiver();
		callButtonReceiver = new CallButtonReceiver();
		IntentFilter filter = new IntentFilter(
				"com.ezzet.eulou.action.CALLBUTTON");
		registerReceiver(callButtonReceiver, filter);
	}

	@Override
	public void onDestroy() {

		unregisterReceiver(callButtonReceiver);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public void onLoginSuccess() {

		UserInfo currentUser = BaseActivity.mUserInfo;
		String userName = "";
		if (currentUser.getMainSocial() == Constants.FACEBOOK) {
			userName = "fb" + currentUser.getFacebookID();
		} else if (currentUser.getMainSocial() == Constants.TWITTER) {
			userName = "tw" + currentUser.getTwitterID();
		} else if (currentUser.getMainSocial() == Constants.INSTAGRAM) {
			userName = "in" + currentUser.getInstagramID();
		} else if (currentUser.getUserID() == Constants.CONTACTS) // added by
		// darshak
		{
			userName = "" + currentUser.getUserID();// added by darshak
		}
		mSinchClientService = new SinchClientService();
		mSinchClientService.start(this, userName);
		requestFriendsFacebook();
		requestUsersFromContacts();
	}

	public void requestUsersFromContacts() {
		UserInfo currentUser = BaseActivity.mUserInfo;
		if (currentUser.getPhone() != null
				&& !currentUser.getPhone().equalsIgnoreCase("")) {
			new AsyncContact().execute();
		}
	}

	public UserInfo findFriendUserBySinchID(String sinchID) {
		for (int i = 0; i < BaseActivity.mFriendUsers.size(); i++) {
			UserInfo userInfo = BaseActivity.mFriendUsers.get(i);
			String sinchIDForUser = "";
			if (userInfo.getMainSocial() == Constants.FACEBOOK) {
				sinchIDForUser = "fb" + userInfo.getFacebookID();
			} else if (userInfo.getMainSocial() == Constants.TWITTER) {
				sinchIDForUser = "tw" + userInfo.getTwitterID();
			} else if (userInfo.getMainSocial() == Constants.INSTAGRAM) {
				sinchIDForUser = "in" + userInfo.getInstagramID();
			} else if (userInfo.getUserID() == Constants.CONTACTS) // added by
			// darshak
			{
				sinchIDForUser = "" + userInfo.getUserID();// added by darshak
			}
			if (sinchID.equalsIgnoreCase(sinchIDForUser)) {
				return userInfo;
			}
		}

		return null;
	}

	public void doLogout() {
		mSinchClientService.stop();
		mSinchClientService = null;

		BaseActivity.mUserInfo = null;
		ChatActivity.mRecipient = null;
		BaseActivity.mFriendUsers = null;
		BaseActivity.mContactUsers = null;
		CustomSharedPreferences.setPreferences("LoggedinUser", "");
		if (Session.getActiveSession() != null
				&& Session.getActiveSession().isOpened()) {
			Session.getActiveSession().closeAndClearTokenInformation();
		}
	}

	private void init() {

		BaseActivity.mContactUsers = new ArrayList<UserInfo>();
	}

	public void sendMessage(WritableMessage message) {

		mSinchClientService.sendMessage(message);
	}

	public void requestFriendsFacebook() {
		Session activeSession = Session.getActiveSession();
		if (activeSession == null || !activeSession.isOpened()) {
			Session.openActiveSessionFromCache(this);
			EulouService.this.requestFriendsFacebook();
		} else {
			Request.newMyFriendsRequest(Session.getActiveSession(),
					new GraphUserListCallback() {

						@Override
						public void onCompleted(List<GraphUser> users,
								Response response) {
							if (users == null) {
								Toast.makeText(
										EulouService.this
												.getApplicationContext(),
										"Failed to retrieve friends list.",
										Toast.LENGTH_LONG).show();
								return;
							}

							String fbIDs = "";
							for (int i = 0; i < users.size(); i++) {
								GraphUser user = users.get(i);
								if (i == 0) {
									fbIDs += "fb" + user.getId();
								} else {
									fbIDs += ",fb" + user.getId();
								}
							}
							EulouService.this.requestFriendsService(fbIDs);
						}

					}).executeAsync();
		}
	}

	private void requestFriendsService(String ids) {
		String requestString = Constants.EULOU_SERVICE_URL + "?action="
				+ Constants.API_USERS_BY_SOCIALIDS + "&socialids=" + ids;
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(requestString, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {

				try {
					JSONObject responseObject = new JSONObject(new String(
							responseBody));
					JSONArray arrayUsers = responseObject.getJSONArray("users");
					synchronized (BaseActivity.mFriendUsers) {

						BaseActivity.mFriendUsers.clear();
						for (int i = 0; i < arrayUsers.length(); i++) {
							JSONObject userObject = arrayUsers.getJSONObject(i);
							UserInfo userInfo = new UserInfo();
							userInfo.setUserID(userObject.getInt("userID"));
							userInfo.setMainSocial(userObject
									.getInt("mainSocial"));
							userInfo.setFacebookID(userObject
									.getString("facebookID"));
							userInfo.setTwitterID(userObject
									.getString("twitterID"));
							userInfo.setInstagramID(userObject
									.getString("instagramID"));
							userInfo.setUserName(userObject
									.getString("username"));
							userInfo.setUserMail(userObject
									.getString("usermail"));
							userInfo.setLastOnline(userObject
									.getString("last_online"));
							userInfo.setPhone(userObject.getString("phone"));
							if (userObject.getString("is_online").equals("1")) {

								userInfo.setOnline(true);
							}

							userInfo.setGender(userObject.getInt("gender"));
							BaseActivity.mFriendUsers.add(userInfo);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Toast.makeText(
							EulouService.this.getApplicationContext(),
							"You have 0 contacts, you can invite friends to join you in Eulou !",
							Toast.LENGTH_LONG).show();
					return;
				}

				Intent intent = new Intent();
				intent.setAction("com.ezzet.eulou.action.FRIENDSLOADED");
				sendBroadcast(intent);
				return;
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {

				Toast.makeText(EulouService.this.getApplicationContext(),
						"Failed to retrieve friend users.", Toast.LENGTH_LONG)
						.show();
				return;
			}
		});
	}

	public class ServiceBinder extends Binder {

		public EulouService getService() {
			return EulouService.this;
		}

	}

	private class CallButtonReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String userIDString = intent.getStringExtra("SinchID");

			CallClient callClient = mSinchClientService.getSinchClient()
					.getCallClient();
			Call call = callClient.callUser(userIDString);
			CurrentCall.currentCall = call;
			Intent callIntent = new Intent(EulouService.this,
					CallScreenActivity.class);
			callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(callIntent);
		}
	}

	class AsyncContact extends AsyncTask<String, String, String> {
		protected void onPreExecute() {
			super.onPreExecute();
			init();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				HelperFunction hf = new HelperFunction(getApplicationContext());
				ArrayList<HashMap<String, String>> contacts = hf
						.getAllContacts();
				StringBuilder s = new StringBuilder();
				for (int i = 0; i < contacts.size(); i++) {
					s.append(contacts.get(i).get("no"));
					s.append(";");
				}
				ConnectionDetector cd = new ConnectionDetector(
						getApplicationContext());
				if (cd.isConnectingToInternet()) {

					BaseActivity.mContactUsers = hf.getAllUSer(
							String.valueOf(BaseActivity.mUserInfo.getUserID()),
							s.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(String file_url) {
			try {
				Intent intent = new Intent();
				intent.setAction("com.ezzet.eulou.action.CONTACTSLOADED");
				sendBroadcast(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
