package com.ezzet.eulou.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.ezzet.eulou.R;
import com.ezzet.eulou.models.ChatBubbleMessageModel;
import com.ezzet.eulou.models.MessageModel;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.services.EulouService;
import com.ezzet.eulou.utilities.LogUtil;
import com.ezzet.eulou.utilities.Utilities;
import com.sinch.android.rtc.messaging.WritableMessage;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import com.ezzet.eulou.adapters.ChatAdapter;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.extra.HelperFunction;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatActivity extends BaseActivity implements TextWatcher,
		OnRefreshListener {

	private final int LIMIT_LOAD_MESSAGE = 10;

	private int mLoaded = 0;
	private String mLastMsgId;
	private ListView mChatList;
	private EditText mMessageEdt;
	private ChatAdapter mChatAdapter;
	private EulouService mService = null;
	private ImageView mCallBtn, mInforBtn;
	private SwipeRefreshLayout mRefreshLayout;
	private List<ChatBubbleMessageModel> mChatMessages;
	private TextView mSendBtn, mUsernameTxt, mStatusTxt;
	private boolean isEditing, isKeyboardShowing, isRefreshing;

	public static UserInfo mRecipient;

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
		setContentView(R.layout.activity_chat);
		Intent serviceIntent = new Intent(ChatActivity.this, EulouService.class);
		bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

		mRecipient = (UserInfo) getIntent().getSerializableExtra(
				Constants.INTENT_MESAGE_USER_ID);
		if (mRecipient == null) {

			finish();
		}

		mSendBtn = (TextView) findViewById(R.id.chat_send_btn);
		mChatList = (ListView) findViewById(R.id.chat_listview);
		mMessageEdt = (EditText) findViewById(R.id.chat_message_edt);
		mStatusTxt = (TextView) findViewById(R.id.chat_navigation_status);
		mCallBtn = (ImageView) findViewById(R.id.chat_navigation_call_btn);
		mInforBtn = (ImageView) findViewById(R.id.chat_navigation_info_btn);
		mUsernameTxt = (TextView) findViewById(R.id.chat_navigation_username);
		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.chat_refresh_layout);
		mRefreshLayout.setColorSchemeResources(R.color.app_color,
				R.color.white, R.color.app_color, R.color.white);
		mChatMessages = new LinkedList<ChatBubbleMessageModel>();
		mChatAdapter = new ChatAdapter(ChatActivity.this);
		mChatList.setAdapter(mChatAdapter);

		final RelativeLayout rootView = (RelativeLayout) findViewById(R.id.chat_root_view);
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						// TODO Auto-generated method stub

						int heightDiff = rootView.getRootView().getHeight()
								- rootView.getHeight();
						if (heightDiff > 100) {

							if (!isKeyboardShowing) {

								displayNewestMessageInList();
								isKeyboardShowing = true;
								if (!mMessageEdt.getText().toString().trim()
										.equals("")
										&& !isEditing) {

									sendTypingMessage();
									isEditing = true;
								}
							}
						} else {

							if (isKeyboardShowing && isEditing) {

								sendTypingMessage();
							}

							isEditing = false;
							isKeyboardShowing = false;
						}
					}
				});

		dispayUserInfor();
		mSendBtn.setOnClickListener(this);
		mCallBtn.setOnClickListener(this);
		mInforBtn.setOnClickListener(this);
		mMessageEdt.addTextChangedListener(this);
		mRefreshLayout.setOnRefreshListener(this);
		IntentFilter mesageIntentFilter = new IntentFilter(
				Constants.INTENT_MESSAGE);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMesasgeReceiver, mesageIntentFilter);
		new LoadMessageHistory().execute();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		unbindService(mConnection);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mMesasgeReceiver);
		ChatActivity.mRecipient = null;
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.chat_send_btn:

			if (!mMessageEdt.getText().toString().trim().equals("")) {

				ChatBubbleMessageModel newMessage = new ChatBubbleMessageModel();
				newMessage.setMessage(mMessageEdt.getText().toString());
				newMessage.setFromMy(true);
				newMessage.setMessageDate(new Date());
				mChatMessages.add(newMessage);
				mChatAdapter.setData(mChatMessages);
				mChatAdapter.notifyDataSetChanged();
				displayNewestMessageInList();
				sendMessage(mMessageEdt.getText().toString(), true);
				mMessageEdt.setText("");
			}

			break;

		case R.id.chat_navigation_call_btn:

			UserInfo userInfo = mRecipient;
			int mainSocial = userInfo.getMainSocial();
			String userIDString = "";
			if (mainSocial == Constants.FACEBOOK) {

				userIDString = "fb" + userInfo.getFacebookID();
			} else if (mainSocial == Constants.TWITTER) {

				userIDString = "tw" + userInfo.getTwitterID();
			} else if (mainSocial == Constants.INSTAGRAM) {

				userIDString = "in" + userInfo.getInstagramID();
			} else {

				return;
			}

			Intent intent = new Intent();
			intent.setAction("com.ezzet.eulou.action.CALLBUTTON");
			intent.putExtra("SinchID", userIDString);
			sendBroadcast(intent);
			break;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		new GetMessageHistoryTask().execute();
		super.onBackPressed();
	}

	// Text change edit text
	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub

		if (s.toString().trim().equals("")) {

			if (isEditing) {

				sendTypingMessage();
			}

			isEditing = false;
		} else {

			if (!isEditing) {

				sendTypingMessage();
			}

			isEditing = true;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

		isRefreshing = true;
		mRefreshLayout.setRefreshing(false);
		new LoadMessageHistory().execute();
	}

	private String formatTime(String dateStr) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm",
				Locale.ENGLISH);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date startDate = new Date();
		try {

			startDate = dateFormat.parse(dateStr);
		} catch (ParseException e) {

			LogUtil.e("calcMessageTime", e.getMessage());
		}

		return timeFormat.format(startDate);
	}

	private String formatDate(String dateStr) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm",
				Locale.ENGLISH);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date startDate = new Date();
		try {

			startDate = dateFormat.parse(dateStr);
		} catch (ParseException e) {

			LogUtil.e("calcMessageTime", e.getMessage());
		}

		return timeFormat.format(startDate);
	}

	private void dispayUserInfor() {

		String status = "";
		if (mRecipient.isOnline()) {

			status = "online";
		} else {

			String lastOnlStr = mRecipient.getLastOnline();
			if (lastOnlStr.contains("0000")) {

				status = "no messenger";
			} else {

				String messageTime = "";
				if (Utilities.isToday(lastOnlStr)) {

					messageTime = "today " + formatTime(lastOnlStr);
				} else if (Utilities.isYesterday(lastOnlStr)) {

					messageTime = "yesterday " + formatTime(lastOnlStr);
				} else {

					messageTime = formatDate(lastOnlStr);
				}

				if (messageTime.equals("01/01/1970")) {

					status = "no messenger";
				} else {

					status = "seen " + messageTime;
				}
			}
		}

		mStatusTxt.setText(status);
		mUsernameTxt.setText(mRecipient.getUserName());
	}

	@SuppressWarnings("unchecked")
	private void sendMessage(String messageStr, boolean saveHistory) {

		String remoteId = "";
		if (mRecipient.getMainSocial() == Constants.FACEBOOK) {

			if (!mRecipient.getFacebookID().contains("fb")) {

				remoteId = "fb" + mRecipient.getFacebookID();
			} else {

				remoteId = mRecipient.getFacebookID();
			}
		} else if (mRecipient.getMainSocial() == Constants.TWITTER) {

			remoteId = "tw" + mRecipient.getTwitterID();
		} else if (mRecipient.getMainSocial() == Constants.INSTAGRAM) {

			remoteId = "in" + mRecipient.getInstagramID();
		} else {

			remoteId = "un" + mRecipient.getUserID();
		}

		isEditing = false;
		if (!remoteId.equals("")) {

			// Send message
			WritableMessage message = new WritableMessage(remoteId, messageStr);
			message.addHeader("Time", new Date().toString());
			mService.sendMessage(message);

			if (saveHistory) {

				// Store mesage to db
				Map<String, Object> chatData = new HashMap<String, Object>();
				chatData.put("fromuser", BaseActivity.mUserInfo.getUserID());
				chatData.put("touser", mRecipient.getUserID());
				chatData.put("message", messageStr);
				displayNewestMessageInList();
				new UploadChatHistory().execute(chatData);
			}
		}
	}

	private void sendTypingMessage() {

		String userRemoteId = "";
		if (mRecipient.getMainSocial() == Constants.FACEBOOK) {

			if (!mRecipient.getFacebookID().contains("@fb")) {

				userRemoteId = "fb" + mRecipient.getFacebookID();
			} else {

				userRemoteId = mRecipient.getFacebookID();
			}
		} else if (mRecipient.getMainSocial() == Constants.TWITTER) {

			userRemoteId = "tw" + mRecipient.getTwitterID();
		} else if (mRecipient.getMainSocial() == Constants.INSTAGRAM) {

			userRemoteId = "in" + mRecipient.getInstagramID();
		} else {

			userRemoteId = "un" + mRecipient.getUserID();
		}

		String message = "";
		if (!isEditing) {

			message = userRemoteId;
		} else {

			message = userRemoteId + "-end";
		}

		sendMessage(message, false);
	}

	private void displayNewestMessageInList() {

		mChatList.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				mChatList.setSelection(mChatAdapter.getCount() - 1);
				mChatList.setVisibility(View.VISIBLE);
			}
		}, 200);
	}

	private BroadcastReceiver mMesasgeReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {

			if (mCurrentClass.equals(ChatActivity.class.getSimpleName())) {

				int messageEvent = intent.getIntExtra(
						Constants.INTENT_MESSAGE_EVENT, 0);
				switch (messageEvent) {

				case Constants.INTENT_RECEIVED_CHAT_MESSAGE:

					ChatBubbleMessageModel newMessage = (ChatBubbleMessageModel) intent
							.getExtras().get(Constants.INTENT_NEW_MESSAGE);
					if (newMessage != null) {

						mChatMessages.add(newMessage);
					}

					mStatusTxt.setText("online");
					mChatAdapter.setData(mChatMessages);
					mChatAdapter.notifyDataSetChanged();
					displayNewestMessageInList();
					new MarkupNewMessage().execute(newMessage);
					break;
				case Constants.INTENT_SENT_MESSAGE:

					mLastMsgId = intent
							.getStringExtra(Constants.INTENT_MESSAGE_DELIVERED_ID);
					break;
				case Constants.INTENT_DELIVERED_MESSAGE:

					if (mChatMessages != null && mChatMessages.size() > 0) {

						String lastMessageId = intent
								.getStringExtra(Constants.INTENT_MESSAGE_DELIVERED_ID);
						ChatBubbleMessageModel lastMessage = mChatMessages
								.get(mChatMessages.size() - 1);
						if (lastMessage.isFromMy()
								&& lastMessageId.equals(mLastMsgId)) {

							Date currentDate = new Date();
							lastMessage.setSeenTime(currentDate);
							mChatAdapter.setData(mChatMessages);
							mChatAdapter.notifyDataSetChanged();
							displayNewestMessageInList();
						}
					}

					break;
				case Constants.INTENT_FAIL_MESSAGE:

					break;
				case Constants.INTENT_PUSH_NOTIFICATION_MESSAGE:

					break;
				case Constants.INTENT_END_TYPING_MESSAGE:

					mStatusTxt.setText("online");
					break;
				case Constants.INTENT_TYPING_MESSAGE:

					mStatusTxt.setText("writing...");
					break;
				case Constants.INTENT_USER_GO_OFFLINE:

					mRecipient.setOnline(false);
					dispayUserInfor();
					break;
				case Constants.INTENT_USER_GO_ONLINE:

					mRecipient.setOnline(true);
					dispayUserInfor();
					break;
				}
			}
		};
	};

	private class UploadChatHistory extends
			AsyncTask<Map<String, Object>, Void, Void> {

		@Override
		protected Void doInBackground(Map<String, Object>... params) {
			// TODO Auto-generated method stub

			new HelperFunction(ChatActivity.this).uploadChatHistory(params[0]);
			return null;
		}

	}

	private class MarkupNewMessage extends
			AsyncTask<ChatBubbleMessageModel, Void, Void> {

		@Override
		protected Void doInBackground(ChatBubbleMessageModel... params) {
			// TODO Auto-generated method stub

			ChatBubbleMessageModel message = params[0];
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("fromuser", message.getFromUser());
			data.put("touser", message.getToUser());
			data.put("messageID", message.getMessageId());

			new HelperFunction(ChatActivity.this).markshownmessage(data);
			return null;
		}
	}

	private class MarkupAllMessage extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			try {
				Map<String, Boolean> readFlg = new HashMap<String, Boolean>();
				String userId = String.valueOf(BaseActivity.mUserInfo
						.getUserID());
				String recipientId = String.valueOf(mRecipient.getUserID());
				List<MessageModel> messageShow = new HelperFunction(
						ChatActivity.this).getShownMessageHistoryByUser(userId);
				for (MessageModel message : messageShow) {

					if (message.getToUser().equals(userId)
							&& message.getFromUser().equals(recipientId)) {

						readFlg.put(message.getMessageId(), true);
					}
				}

				List<MessageModel> messages = new HelperFunction(
						ChatActivity.this).getMessageHistoryByUserId(userId);
				for (MessageModel message : messages) {

					if (message.getToUser().equals(userId)
							&& message.getFromUser().equals(recipientId)) {

						if (!readFlg.containsKey(message.getMessageId())) {

							Map<String, Object> data = new HashMap<String, Object>();
							data.put("fromuser", message.getFromUser());
							data.put("touser", message.getToUser());
							data.put("messageID", message.getMessageId());

							new HelperFunction(ChatActivity.this)
									.markshownmessage(data);
						}
					}
				}
			} catch (Exception ex) {

			}

			return null;
		}
	}

	private class LoadMessageHistory extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			// new MarkupAllMessage().execute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			String userId = String.valueOf(BaseActivity.mUserInfo.getUserID());
			Map<String, Object> requestData = new HashMap<String, Object>();
			requestData.put("fromuser", userId);
			requestData.put("touser", mRecipient.getUserID());
			requestData.put("loaded", mLoaded);
			requestData.put("limit", LIMIT_LOAD_MESSAGE);
			if (mRecipient.getFacebookID() != null) {

				requestData.put("receiptfbid", mRecipient.getFacebookID());
			} else {

				requestData.put("receiptfbid", "");
			}

			List<ChatBubbleMessageModel> tempMessages = new HelperFunction(
					ChatActivity.this).getChatHistory(requestData);
			if (tempMessages != null) {

				mChatMessages.addAll(0, tempMessages);
			}

			mLoaded += LIMIT_LOAD_MESSAGE;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			mChatAdapter.setData(mChatMessages);
			mChatAdapter.notifyDataSetChanged();
			if (!isRefreshing) {

				displayNewestMessageInList();
			}

			isRefreshing = false;
			new MarkupAllMessage().execute();
		}
	}

}
