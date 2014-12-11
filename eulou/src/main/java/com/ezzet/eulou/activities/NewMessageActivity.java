package com.ezzet.eulou.activities;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.ezzet.eulou.R;
import com.ezzet.eulou.adapters.SendMessageRecipientsAdapter;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.extra.HelperFunction;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.services.EulouService;
import com.ezzet.eulou.utilities.Utilities;
import com.ezzet.eulou.views.CustomAlertDialog.OnNegativeButtonClick;
import com.ezzet.eulou.views.CustomLoadingDialog;
import com.sinch.android.rtc.messaging.WritableMessage;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

public class NewMessageActivity extends BaseActivity implements TextWatcher {

	private ListView mFriendList;
	private EulouService mService = null;
	private EditText mMessageEdt, mRecipEdt;
	private SendMessageRecipientsAdapter mFriendsAdapter;

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			EulouService.ServiceBinder b = (EulouService.ServiceBinder) binder;
			mService = b.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_new_message);
		initNavigationComponents();

		Intent serviceIntent = new Intent(NewMessageActivity.this,
				EulouService.class);
		bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);

		// Navigation bar
		mTitleTxt.setVisibility(View.VISIBLE);
		mLeftTxtBtn.setVisibility(View.VISIBLE);
		mRightTxtBtn.setVisibility(View.VISIBLE);
		mNavigatorGroup.setVisibility(View.GONE);
		mLeftImgBtn.setVisibility(View.INVISIBLE);
		mRightImgBtn.setVisibility(View.INVISIBLE);
		mRightTxtBtn.setText(getString(R.string.send));
		mLeftTxtBtn.setText(getString(R.string.cancel));
		mTitleTxt.setText(getString(R.string.new_message));

		mMessageEdt = (EditText) findViewById(R.id.new_message_edt);
		mFriendList = (ListView) findViewById(R.id.new_message_listview);
		mRecipEdt = (EditText) findViewById(R.id.new_message_recipient_edt);

		if (mFriendUsers != null) {

			mFriendsAdapter = new SendMessageRecipientsAdapter(
					NewMessageActivity.this, mFriendUsers);
			mFriendList.setAdapter(mFriendsAdapter);
		}

		mRecipEdt.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub

				if (hasFocus) {

					mFriendList.setVisibility(View.VISIBLE);
					mMessageEdt.setVisibility(View.INVISIBLE);
				}
			}
		});

		mRecipEdt.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {

					mMessageEdt.setVisibility(View.VISIBLE);
					mFriendList.setVisibility(View.INVISIBLE);
				}

				return false;
			}
		});

		mLeftTxtBtn.setOnClickListener(this);
		mRightTxtBtn.setOnClickListener(this);
		mRecipEdt.addTextChangedListener(this);
		mFriendList.setOnItemClickListener(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		unbindService(mConnection);
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View itemView, int pos,
			long id) {
		// TODO Auto-generated method stub
		super.onItemClick(adapterView, itemView, pos, id);

		UserInfo userInfo = (UserInfo) mFriendsAdapter.getItem(pos);
		mRecipEdt.setTag(userInfo);
		mRecipEdt.setText(userInfo.getUserName());
		mMessageEdt.setVisibility(View.VISIBLE);
		mFriendList.setVisibility(View.INVISIBLE);
		mMessageEdt.requestFocus();
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

		mFriendList.setVisibility(View.VISIBLE);
		mMessageEdt.setVisibility(View.INVISIBLE);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		mFriendList.setVisibility(View.VISIBLE);
		mMessageEdt.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

		mFriendsAdapter.getFilter().filter(s.toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.main_navigation_left_txt_btn:

			finish();
			break;

		case R.id.main_navigation_right_txt_btn:

			UserInfo userInfo = (UserInfo) mRecipEdt.getTag();
			if (userInfo == null) {

				Utilities.showAlertMessage(NewMessageActivity.this,
						getString(R.string.select_friend), "", null);
				return;
			}

			try {

				String remoteId = "";
				if (userInfo.getMainSocial() == Constants.FACEBOOK) {

					if (!userInfo.getFacebookID().contains("fb")) {

						remoteId = "fb" + userInfo.getFacebookID();
					} else {

						remoteId = userInfo.getFacebookID();
					}
				} else if (userInfo.getMainSocial() == Constants.TWITTER) {

					remoteId = "tw" + userInfo.getTwitterID();
				} else if (userInfo.getMainSocial() == Constants.INSTAGRAM) {

					remoteId = "in" + userInfo.getInstagramID();
				} else {

					remoteId = "un" + userInfo.getUserID();
				}

				if (!remoteId.equals("")) {

					// Send message
					String messageStr = mMessageEdt.getText().toString().trim();
					if (!messageStr.equals("")) {

						WritableMessage message = new WritableMessage(remoteId,
								messageStr);
						message.addHeader("Time", new Date().toString());
						mService.sendMessage(message);

						// Store mesage to db
						Map<String, Object> chatData = new HashMap<String, Object>();
						chatData.put("fromuser",
								mUserInfo.getUserID());
						chatData.put("touser", userInfo.getUserID());
						chatData.put("message", messageStr);
						new UploadChatHistory().execute(chatData);
					} else {

						Utilities.showAlertMessage(NewMessageActivity.this,
								getString(R.string.please_input_message), "",
								null);
						return;
					}
				}

				Utilities.doHideKeyboard(NewMessageActivity.this, mRecipEdt);
			} catch (Exception ex) {

				Utilities.showAlertMessage(NewMessageActivity.this,
						getString(R.string.message_not_sent), "", null);
			}

			break;
		}
	}

	private class UploadChatHistory extends
			AsyncTask<Map<String, Object>, Void, Void> {

		Dialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = CustomLoadingDialog.show(NewMessageActivity.this, "", "",
					false, false);
		}

		@Override
		protected Void doInBackground(Map<String, Object>... params) {
			// TODO Auto-generated method stub

			new HelperFunction(NewMessageActivity.this)
					.uploadChatHistory(params[0]);
			new GetMessageHistoryTask().execute();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			dialog.dismiss();
			Utilities.showAlertMessage(NewMessageActivity.this,
					getString(R.string.message_sent), "",
					new OnNegativeButtonClick() {

						@Override
						public void onButtonClick(View view) {
							// TODO Auto-generated method stub
							finish();
						}
					});
		}
	}
}
