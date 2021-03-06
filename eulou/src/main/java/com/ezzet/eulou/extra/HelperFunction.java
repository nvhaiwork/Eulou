package com.ezzet.eulou.extra;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.models.ChatBubbleMessageModel;
import com.ezzet.eulou.models.Code;
import com.ezzet.eulou.models.MessageModel;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.utilities.LogUtil;

public class HelperFunction {
	public ArrayList<UserInfo> contactUsers;
	Context context;
	ArrayList<Code> codeList;
	private JSONParser jsonParser;
	private String URL = "http://eulou.tn/service/index.php?";

	public HelperFunction(Context context) {
		this.context = context;
		jsonParser = new JSONParser();
		codeList = new ArrayList<Code>();
	}

	public String submitNumber(String userId, String phone) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action", "registerphone"));
		params.add(new BasicNameValuePair("userid", userId));
		params.add(new BasicNameValuePair("phone", phone));

		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		return jsonParser.getJSONFromUrl(urls);
	}

	public List<MessageModel> getMessageHistoryByUserId(String userId) {

		List<MessageModel> messages = new ArrayList<MessageModel>();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action", "getMessageHistoryByUser"));
		params.add(new BasicNameValuePair("userid", userId));

		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		String json = jsonParser.getJSONFromUrl(urls);
		try {

			JSONObject jObject = new JSONObject(json);
			JSONArray jArray = jObject.getJSONArray("messages");
			for (int i = 0; i < jArray.length(); i++) {

				MessageModel message = new MessageModel();
				jObject = jArray.getJSONObject(i);
				message.setMessageId(jObject.getString("messageID"));
				message.setFromUser(jObject.getString("fromuser"));
				message.setDeletedByFrom(jObject.getString("deletedbyfrom"));
				message.setToUser(jObject.getString("touser"));
				message.setDeletedByTo(jObject.getString("deletedbyto"));
				message.setMessage(jObject.getString("message"));
				message.setMesageTime(jObject.getString("messagetime"));
				messages.add(message);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return messages;
	}

	public List<MessageModel> getShownMessageHistoryByUser(String userId) {

		List<MessageModel> messages = new ArrayList<MessageModel>();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action",
				"getShownMessageHistoryByUser"));
		params.add(new BasicNameValuePair("userid", userId));

		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		String json = jsonParser.getJSONFromUrl(urls);
		try {

			JSONObject jObject = new JSONObject(json);
			JSONArray jArray = jObject.getJSONArray("messages");
			for (int i = 0; i < jArray.length(); i++) {

				MessageModel message = new MessageModel();
				jObject = jArray.getJSONObject(i);
				message.setShowId(jObject.getString("showID"));
				message.setMessageId(jObject.getString("messageID"));
				message.setFromUser(jObject.getString("fromuser"));
				message.setDeletedByFrom(jObject.getString("deletedbyfrom"));
				message.setToUser(jObject.getString("touser"));
				message.setDeletedByTo(jObject.getString("deletedbyto"));
				message.setMesageTime(jObject.getString("showntime"));
				messages.add(message);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return messages;
	}

	public void markShownMessage(Map<String, Object> data) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action",
				"markShownAllMessageBetweenUserAndSpecificUser"));
		params.add(new BasicNameValuePair("userid", data.get("userid")
				.toString()));
		params.add(new BasicNameValuePair("specificuserid", data.get(
				"specificuserid").toString()));
		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		jsonParser.getJSONFromUrl(urls);
	}

	public List<ChatBubbleMessageModel> getChatHistory(
			Map<String, Object> requestData) {

		List<ChatBubbleMessageModel> chatHistory = new ArrayList<ChatBubbleMessageModel>();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action", "messagehistory"));
		params.add(new BasicNameValuePair("fromuser", requestData.get(
				"fromuser").toString()));
		params.add(new BasicNameValuePair("touser", requestData.get("touser")
				.toString()));
		params.add(new BasicNameValuePair("loaded", requestData.get("loaded")
				.toString()));
		params.add(new BasicNameValuePair("limit", requestData.get("limit")
				.toString()));
		String recipient = requestData.get("receiptfbid").toString();

		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		String json = jsonParser.getJSONFromUrl(urls);
		try {

			SimpleDateFormat dateFormate = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			dateFormate.setTimeZone(TimeZone.getTimeZone("UTC"));
			JSONObject jObject = new JSONObject(json);
			JSONArray jArray = jObject.getJSONArray("messages");
			for (int i = 0; i < jArray.length(); i++) {

				jObject = jArray.getJSONObject(i);
				ChatBubbleMessageModel history = new ChatBubbleMessageModel();
				history.setMessage(jObject.getString("message"));
				history.setMessageId(jObject.getString("messageID"));
				history.setProfileImg(recipient);
				String fromUser = jObject.getString("fromuser");
				if (fromUser.equals(requestData.get("fromuser"))) {

					history.setFromMy(true);
				} else {

					history.setFromMy(false);
				}

				Date messageDate = dateFormate.parse(jObject
						.getString("messagetime"));
				history.setMessageDate(messageDate);
				chatHistory.add(history);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return chatHistory;
	}

	public ArrayList<UserInfo> getRetrivedUser(String userId, String phone) {

		ArrayList<UserInfo> userList = new ArrayList<UserInfo>();
		String json = jsonParser.postData(userId, phone);
		try {
			JSONObject jsonObject = new JSONObject(json);
			JSONArray jArrUser = jsonObject.getJSONArray("users");
			for (int i = 0; i < jArrUser.length(); i++) {
				UserInfo userI = new UserInfo();
				JSONObject jObj = jArrUser.getJSONObject(i);
				JSONObject jObjU = jObj.getJSONObject("data");

				userI.setUserID(jObjU.getInt("userID"));
				userI.setMainSocial(jObjU.getInt("mainSocial"));
				userI.setFacebookID(jObjU.getString("facebookID"));
				userI.setTwitterID(jObjU.getString("twitterID"));
				userI.setInstagramID(jObjU.getString("instagramID"));
				userI.setUserName(jObjU.getString("username"));
				userI.setUserMail(jObjU.getString("usermail"));
				userI.setGender(jObjU.getInt("gender"));
				userI.setPhone(jObjU.getString("phone"));
				userI.setLastOnline(jObjU.getString("last_online"));
				if (jObjU.getString("is_online").equals("1")) {

					userI.setOnline(true);
				}

				userList.add(userI);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return userList;
	}

	public ArrayList<HashMap<String, String>> getAllContacts() {
		ArrayList<HashMap<String, String>> conList = new ArrayList<HashMap<String, String>>();
		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
				null, null, null);
		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					Cursor pCur = cr.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID
									+ " = ?", new String[]{id}, null);
					while (pCur.moveToNext()) {
						String phoneNo = pCur
								.getString(pCur
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						String number = phoneNo.replaceAll("[\\D]", "");
						int len = number.length();
						String no = null;
						if (len > 8) {
							no = number.substring(len - 8).trim();
						}
						HashMap<String, String> hashMap = new HashMap<String, String>();
						hashMap.put("name", name);
						hashMap.put("no", no);
						hashMap.put("phoneNo", phoneNo);
						conList.add(hashMap);
					}
					pCur.close();
				}
			}
		}
		return conList;
	}

	public ArrayList<UserInfo> getAllUSer(String userId, String phone) {

		contactUsers = new ArrayList<UserInfo>();
		ArrayList<UserInfo> allUser = new ArrayList<UserInfo>();
		contactUsers = getRetrivedUser(userId, phone);
		ArrayList<HashMap<String, String>> conName = getAllContacts();

		for (int j = 0; j < contactUsers.size(); j++) {
			for (int i = 0; i < conName.size(); i++) {
				UserInfo user = new UserInfo();
				String number = contactUsers.get(j).getPhone()
						.replaceAll("[\\D]", "");
				int len = number.length();
				String no = null;
				if (len > 8) {
					no = number.substring(len - 8).trim();
				}
				if (no.equals(conName.get(i).get("no"))) {
					user.setUserName(conName.get(i).get("name"));
					user.setFacebookID(contactUsers.get(j).getFacebookID());
					user.setPhone(contactUsers.get(j).getPhone());
					user.setGender(contactUsers.get(j).getGender());
					user.setInstagramID(contactUsers.get(j).getInstagramID());
					user.setMainSocial(contactUsers.get(j).getMainSocial());
					user.setUserMail(contactUsers.get(j).getUserMail());
					user.setOnline(contactUsers.get(j).isOnline());
					user.setLastOnline(contactUsers.get(j).getLastOnline());
					allUser.add(user);
				}
			}
		}
		return allUser;
	}

	public ArrayList<Code> getInforamation() {

		String jSon = jsonParser
				.getJSONFromUrl("http://restcountries.eu/rest/v1/all");

		try {
			JSONArray jsonArray = new JSONArray(jSon);
			for (int i = 0; i < jsonArray.length(); i++) {
				Code code = new Code();
				JSONObject jsonObj = jsonArray.getJSONObject(i);
				code.setName(jsonObj.getString("name"));
				JSONArray jArr = jsonObj.getJSONArray("callingCodes");
				for (int j = 0; j < jArr.length(); j++) {

					code.setCallingCodes(jArr.get(0).toString());
				}
				codeList.add(code);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return codeList;
	}

	public void uploadChatHistory(Map<String, Object> chatData) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action", "messageadd"));
		params.add(new BasicNameValuePair("fromuser", chatData.get("fromuser")
				.toString()));
		params.add(new BasicNameValuePair("touser", chatData.get("touser")
				.toString()));
		params.add(new BasicNameValuePair("message", (String) chatData
				.get("message")));
		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		jsonParser.getJSONFromUrl(urls);
	}

	public void updateStatus(boolean isOnline, String userId) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (isOnline) {

			params.add(new BasicNameValuePair("action",
					"updateUserStatusToOnline"));
		} else {

			params.add(new BasicNameValuePair("action",
					"updateUserStatusToOffline"));
		}

		params.add(new BasicNameValuePair("userid", userId));
		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		jsonParser.getJSONFromUrl(urls);
	}

	public List<UserInfo> requestFbFriendsByFbId(String ids) {

		final List<UserInfo> friends = new ArrayList<UserInfo>();
		String requestString = Constants.EULOU_SERVICE_URL + "?action="
				+ Constants.API_USERS_BY_SOCIALIDS + "&socialids=" + ids;
		String jSon = jsonParser.getJSONFromUrl(requestString);
		try {

			JSONObject responseObject = new JSONObject(jSon);
			JSONArray arrayUsers = responseObject.getJSONArray("users");
			for (int i = 0; i < arrayUsers.length(); i++) {
				JSONObject userObject = arrayUsers.getJSONObject(i);
				UserInfo userInfo = new UserInfo();
				userInfo.setUserID(userObject.getInt("userID"));
				userInfo.setMainSocial(userObject.getInt("mainSocial"));
				userInfo.setFacebookID(userObject.getString("facebookID"));
				userInfo.setTwitterID(userObject.getString("twitterID"));
				userInfo.setInstagramID(userObject.getString("instagramID"));
				userInfo.setUserName(userObject.getString("username"));
				userInfo.setUserMail(userObject.getString("usermail"));
				userInfo.setLastOnline(userObject.getString("last_online"));
				if (userObject.getString("is_online").equals("1")) {

					userInfo.setOnline(true);
				}

				userInfo.setGender(userObject.getInt("gender"));
				friends.add(userInfo);
			}
		} catch (JSONException e) {

			LogUtil.e("requestFbFriendsByFbId", e.getMessage());
		}

		return friends;
	}

	public void deleteMessageHistory(String userId, String partnerId) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action",
				"deleteMessageHistoryBetweenUserAndSpecificUser"));
		params.add(new BasicNameValuePair("userid", userId));
		params.add(new BasicNameValuePair("specificuserid", partnerId));

		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		jsonParser.getJSONFromUrl(urls);
	}

	public void deleteCallHistory(String... data) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action",
				"deleteCallHistoryBetweenUserAndSpecificUser"));
		params.add(new BasicNameValuePair("userid", data[0]));
		params.add(new BasicNameValuePair("specificuserid", data[1]));
		params.add(new BasicNameValuePair("direction", data[2]));
		params.add(new BasicNameValuePair("groupdate", data[3]));

		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		jsonParser.getJSONFromUrl(urls);
	}

	public void deleteAllMessageHistory(String userId) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action",
				"deleteAllMessageHistoryofUser"));
		params.add(new BasicNameValuePair("userid", userId));

		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		jsonParser.getJSONFromUrl(urls);
	}

	public void deleteAllCallHistory(String userId) {

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action",
				"deleteAllCallHistoryofUser"));
		params.add(new BasicNameValuePair("userid", userId));

		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		jsonParser.getJSONFromUrl(urls);
	}

	public UserInfo getUserInfoById(String userId) {

		UserInfo userInfo = new UserInfo();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("action", "getUserInfoById"));
		params.add(new BasicNameValuePair("userid", userId));

		String paramString = URLEncodedUtils.format(params, "utf-8");
		String urls = URL + paramString;
		String jsonStr = jsonParser.getJSONFromUrl(urls);
		try {

			JSONObject jsonObject = new JSONObject(jsonStr);
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			jsonObject = jsonArray.getJSONObject(0);

			userInfo.setUserID(jsonObject.getInt("userID"));
			userInfo.setMainSocial(jsonObject.getInt("mainSocial"));
			userInfo.setFacebookID(jsonObject.getString("facebookID"));
			userInfo.setTwitterID(jsonObject.getString("twitterID"));
			userInfo.setInstagramID(jsonObject.getString("instagramID"));
			userInfo.setUserName(jsonObject.getString("username"));
			userInfo.setUserMail(jsonObject.getString("usermail"));
			userInfo.setLastOnline(jsonObject.getString("last_online"));
			if (jsonObject.getString("is_online").equals("1")) {

				userInfo.setOnline(true);
			}

			userInfo.setGender(jsonObject.getInt("gender"));
		} catch (Exception ex) {

			LogUtil.e("getUserInfoById", ex.getMessage());
		}

		return userInfo;
	}
}
