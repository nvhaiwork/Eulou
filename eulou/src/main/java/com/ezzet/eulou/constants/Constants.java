package com.ezzet.eulou.constants;

public class Constants {

	// Facebook profile link
	public static final String FACEBOOK_PROFILE_PIC_URL = "https://graph.facebook.com/%@/picture?width=640&height=640";
	public static final String EULOU_SERVICE_URL = "http://eulou.tn/service/index.php";

	public static final String API_SIGNIN_FACEBOOK = "signinfacebook";
	public static final String API_REGISTER_FACEBOOK = "registerfacebook";
	public static final String API_USERS_BY_SOCIALIDS = "usersbysocialids";
	public static final String API_CALL_ADD = "calladd";
	public static final String API_CALL_HISTORY = "callhistory";
	public static final String API_DETAIL_HISTORY = "detailedHistory";
	public static final String API_SEND_PUSH = "sendpush";

	public static final int FACEBOOK = 1;
	public static final int TWITTER = 2;
	public static final int INSTAGRAM = 3;
	public static final int CONTACTS = 4; // Added by darshak

	// public static final String GOOGLE_PROJECT_NUMBER = "233376687074";
	// public static final String GOOGLE_API_KEY =
	// "AIzaSyDmzzDnPvd_wejKWtc7E-pGVUd0fExyARc";

	// Tab identifiers
	public static final int TAB_MESSAGES = 0;
	public static final int TAB_CONTACTS = 1;
	public static final int TAB_RECENT_CALLS = 2;
	public static final int TAB_PROFILE = 3;

	// Intent
	public static final String INTENT_MESAGE_USER_ID = "com.ezzet.eulou.message_user_id";
	public static final String INTENT_BROADCAST_UPDATE_MESSAGE_HIST = "com.ezzet.eulou.update_message_hist";
	public static final String INTENT_PUSH_NEW_MESSAGE = "com.ezzet.eulou.push_new_message_message";
	public static final String INTENT_PUSH_NEW_MESSAGE_SENDER_ID = "com.ezzet.eulou.push_new_message_sender_id";

	// Receiver intent filters
	public static final String INTENT_MESSAGE = "com.ezzet.eulou.message";
	public static final String INTENT_MESSAGE_EVENT = "com.ezzet.eulou.message";
	public static final String INTENT_NEW_MESSAGE = "com.ezzet.eulou.new_message";
	public static final String INTENT_MESSAGE_DELIVERED_ID = "com.ezzet.eulou.message_delivered_id";
	public static final int INTENT_RECEIVED_MESSAGE = 1001;
	public static final int INTENT_SENT_MESSAGE = 1002;
	public static final int INTENT_DELIVERED_MESSAGE = 1003;
	public static final int INTENT_FAIL_MESSAGE = 1004;
	public static final int INTENT_PUSH_NOTIFICATION_MESSAGE = 1005;
	public static final int INTENT_TYPING_MESSAGE = 1006;
	public static final int INTENT_END_TYPING_MESSAGE = 1007;
	public static final int INTENT_USER_GO_OFFLINE = 1008;
	public static final int INTENT_USER_GO_ONLINE = 1009;
	public static final int INTENT_RECEIVED_CHAT_MESSAGE = 1010;

	// Preference
	public static final String PREF_APP_STATUS = "com.ezzet.eulou.pref_app_status";

}
