package com.ezzet.eulou.services;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.ezzet.eulou.activities.BaseActivity;
import com.ezzet.eulou.activities.ChatActivity;
import com.ezzet.eulou.activities.IncomingCallScreenActivity;
import com.ezzet.eulou.activities.MainActivity;
import com.ezzet.eulou.R;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.models.ChatBubbleMessageModel;
import com.ezzet.eulou.models.CurrentCall;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.utilities.CustomSharedPreferences;
import com.ezzet.eulou.utilities.LogUtil;
import com.ezzet.eulou.utilities.Utilities;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

public class SinchClientService {

	static final String LOG_TAG = SinchClientService.class.getSimpleName();
	public static AudioController audioController = null;
	private SinchClient mSinchClient = null;
	private CallClient mCallClient = null;
	private MessageClient mMessageClient = null;
	private Context mContext;

	public void start(Context context, String userName) {
		this.mContext = context.getApplicationContext();

		mSinchClient = Sinch.getSinchClientBuilder().context(context)
				.userId(userName)
				.applicationKey("c3d28286-6c1e-446a-947f-2006c794d679")
				.applicationSecret("pNIJc4oYokGNNR101fmecQ==")
				.environmentHost("clientapi.sinch.com").build();

		mSinchClient.setSupportCalling(true);
		mSinchClient.setSupportActiveConnectionInBackground(true);
		mSinchClient.setSupportPushNotifications(true);
		mSinchClient.setSupportMessaging(true);

		mSinchClient.startListeningOnActiveConnection();

		mSinchClient.addSinchClientListener(new MySinchClientListener());
		mSinchClient.start();

		mCallClient = mSinchClient.getCallClient();
		mCallClient.addCallClientListener(new SinchCallClientListener());

		mMessageClient = mSinchClient.getMessageClient();
		mMessageClient
				.addMessageClientListener(new SinchMessageClientListener());
	}

	public void stop() {
		mSinchClient.terminate();
	}

	public boolean isStarted() {
		return mSinchClient.isStarted();
	}

	public SinchClient getSinchClient() {
		return mSinchClient;
	}

	public void sendMessage(WritableMessage message) {

		if (mMessageClient != null) {

			mMessageClient.send(message);
		}
	}

	/**
	 * Send broadcast when send messages
	 */
	private void sendMessageBroadcast(Intent intent) {

		LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
	}

	private class MySinchClientListener implements SinchClientListener {

		@Override
		public void onClientFailed(SinchClient client, SinchError error) {
		}

		@Override
		public void onClientStarted(SinchClient client) {
			audioController = client.getAudioController();
		}

		@Override
		public void onClientStopped(SinchClient client) {
			audioController = null;
		}

		@Override
		public void onLogMessage(int level, String area, String message) {
		}

		@Override
		public void onRegistrationCredentialsRequired(SinchClient client,
				ClientRegistration clientRegistration) {
		}
	}

	private class SinchCallClientListener implements CallClientListener {

		@Override
		public void onIncomingCall(CallClient callClient, Call call) {

			CurrentCall.currentCall = call;
			Intent intent = new Intent(mContext,
					IncomingCallScreenActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		}
	}

	private class SinchMessageClientListener implements MessageClientListener {

		Intent messageIntent = new Intent(Constants.INTENT_MESSAGE);;

		@Override
		public void onIncomingMessage(MessageClient arg0, Message message) {
			// TODO Auto-generated method stub

			String recId = message.getSenderId();
			if (!Utilities.isToday(message.getTimestamp())) {

				return;
			}

			LogUtil.e("SinchMessageClientListener", "onIncomingMessage : "
					+ message.getTextBody());
			int i = 0;
			String senderName = "";
			UserInfo pushUserInfo = null;
			for (UserInfo userInfo : BaseActivity.mFriendUsers) {

				String userRemoteId = "";
				if (userInfo.getMainSocial() == Constants.FACEBOOK) {

					if (!userInfo.getFacebookID().contains("@fb")) {

						userRemoteId = "fb" + userInfo.getFacebookID();
					} else {

						userRemoteId = userInfo.getFacebookID();
					}
				} else if (userInfo.getMainSocial() == Constants.TWITTER) {

					userRemoteId = "tw" + userInfo.getTwitterID();
				} else if (userInfo.getMainSocial() == Constants.INSTAGRAM) {

					userRemoteId = "in" + userInfo.getInstagramID();
				} else {

					userRemoteId = "un" + userInfo.getUserID();
				}

				if (userRemoteId.equals(recId)) {

					pushUserInfo = userInfo;
					senderName = userInfo.getUserName();
					break;
				}

				i++;
			}

			if (i == BaseActivity.mFriendUsers.size()) {

				int j = 0;
				for (UserInfo user : BaseActivity.mContactUsers) {

					String userRemoteId = "un" + user.getUserID();
					if (userRemoteId.equals(recId)) {

						pushUserInfo = user;
						senderName = user.getUserName();
						break;
					}

					j++;
				}

				if (j == BaseActivity.mContactUsers.size()) {

					return;
				}
			}

			String messageText = message.getTextBody();
			String iRecId = message.getRecipientIds().get(0);
			boolean appStatus = CustomSharedPreferences.getPreferences(
					Constants.PREF_APP_STATUS, false);
			if (appStatus) {

				if (BaseActivity.mCurrentClass != null
						&& BaseActivity.mCurrentClass.equals(ChatActivity.class
								.getSimpleName())) {

					String remoteId = "";
					if (ChatActivity.mRecipient != null) {

						if (ChatActivity.mRecipient.getMainSocial() == Constants.FACEBOOK) {

							if (!ChatActivity.mRecipient.getFacebookID()
									.contains("@fb")) {

								remoteId = "fb"
										+ ChatActivity.mRecipient
												.getFacebookID();
							} else {

								remoteId = ChatActivity.mRecipient
										.getFacebookID();
							}
						} else if (ChatActivity.mRecipient.getMainSocial() == Constants.TWITTER) {

							remoteId = "tw"
									+ ChatActivity.mRecipient.getTwitterID();
						} else if (ChatActivity.mRecipient.getMainSocial() == Constants.INSTAGRAM) {

							remoteId = "in"
									+ ChatActivity.mRecipient.getInstagramID();
						} else {

							remoteId = "un"
									+ ChatActivity.mRecipient.getUserID();
						}
					}

					if (remoteId.equals(recId)) {

						if (messageText.equals(iRecId)) {

							// Typing
							messageIntent.putExtra(
									Constants.INTENT_MESSAGE_EVENT,
									Constants.INTENT_TYPING_MESSAGE);
							sendMessageBroadcast(messageIntent);
							return;
						} else if (messageText.equals(iRecId + "-end")) {

							// End typing
							messageIntent.putExtra(
									Constants.INTENT_MESSAGE_EVENT,
									Constants.INTENT_END_TYPING_MESSAGE);
							sendMessageBroadcast(messageIntent);
							return;
						} else if (messageText.equals(iRecId + "-offline")) {

							// Update status offline
							messageIntent.putExtra(
									Constants.INTENT_MESSAGE_EVENT,
									Constants.INTENT_USER_GO_OFFLINE);
							sendMessageBroadcast(messageIntent);
							return;
						} else if (messageText.equals(iRecId + "-online")) {

							// Update status online
							messageIntent.putExtra(
									Constants.INTENT_MESSAGE_EVENT,
									Constants.INTENT_USER_GO_ONLINE);
							sendMessageBroadcast(messageIntent);
							return;
						}

						ChatBubbleMessageModel newMessage = new ChatBubbleMessageModel();
						newMessage.setFromMy(false);
						newMessage.setFromUser(message.getSenderId());
						newMessage.setMessage(message.getTextBody());
						newMessage.setMessageId(message.getMessageId());
						newMessage.setMessageDate(message.getTimestamp());
						newMessage.setToUser(message.getRecipientIds().get(0));
						messageIntent.putExtra(Constants.INTENT_NEW_MESSAGE,
								newMessage);
						messageIntent.putExtra(Constants.INTENT_MESSAGE_EVENT,
								Constants.INTENT_RECEIVED_CHAT_MESSAGE);
					}
				} else {

					if (messageText.equals(iRecId)
							|| messageText.contains("-end")
							|| messageText.contains("-offline")
							|| messageText.contains("-online")) {

						return;
					}

					messageIntent.putExtra(
							Constants.INTENT_PUSH_NEW_MESSAGE_SENDER_ID, recId);
					messageIntent.putExtra(Constants.INTENT_PUSH_NEW_MESSAGE,
							senderName + ": " + messageText);
					messageIntent.putExtra(Constants.INTENT_MESSAGE_EVENT,
							Constants.INTENT_RECEIVED_MESSAGE);
				}

				sendMessageBroadcast(messageIntent);
			} else {

				// Push
				if (messageText.equals(iRecId) || messageText.contains("-end")
						|| messageText.contains("-offline")
						|| messageText.contains("-online")) {

					return;
				}

				Intent pushIntent = new Intent(mContext, ChatActivity.class);
				pushIntent.putExtra(Constants.INTENT_MESAGE_USER_ID,
						pushUserInfo);
				TaskStackBuilder stackBuilder = TaskStackBuilder
						.create(mContext);
				stackBuilder.addParentStack(MainActivity.class);
				stackBuilder.addNextIntent(pushIntent);
				PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
						PendingIntent.FLAG_UPDATE_CURRENT);

				NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
						mContext).setContentTitle(pushUserInfo.getUserName())
						.setSmallIcon(R.drawable.ic_messages)
						.setDefaults(Notification.DEFAULT_ALL)
						.setContentText(messageText).setAutoCancel(true);
				mBuilder.setContentIntent(pendingIntent);
				NotificationManager notiManager = (NotificationManager) mContext
						.getSystemService(Context.NOTIFICATION_SERVICE);
				notiManager.notify(10, mBuilder.build());
			}
		}

		@Override
		public void onMessageDelivered(MessageClient arg0,
				MessageDeliveryInfo arg1) {
			// TODO Auto-generated method stub
			messageIntent.putExtra(Constants.INTENT_MESSAGE_EVENT,
					Constants.INTENT_DELIVERED_MESSAGE);
			messageIntent.putExtra(Constants.INTENT_MESSAGE_DELIVERED_ID,
					arg1.getMessageId());
			sendMessageBroadcast(messageIntent);
		}

		@Override
		public void onMessageFailed(MessageClient arg0, Message arg1,
				MessageFailureInfo arg2) {
			// TODO Auto-generated method stub
			messageIntent.putExtra(Constants.INTENT_MESSAGE_EVENT,
					Constants.INTENT_FAIL_MESSAGE);
			sendMessageBroadcast(messageIntent);
		}

		@Override
		public void onMessageSent(MessageClient arg0, Message arg1, String arg2) {
			// TODO Auto-generated method stub

			LogUtil.e("onMessageSent", arg1.getTextBody());
			messageIntent.putExtra(Constants.INTENT_MESSAGE_EVENT,
					Constants.INTENT_SENT_MESSAGE);
			messageIntent.putExtra(Constants.INTENT_MESSAGE_DELIVERED_ID,
					arg1.getMessageId());
			sendMessageBroadcast(messageIntent);
		}

		@Override
		public void onShouldSendPushData(MessageClient arg0, Message arg1,
				List<PushPair> arg2) {
			// TODO Auto-generated method stub

		}
	}
}
