package com.ezzet.eulou.activities;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ezzet.eulou.R;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.services.EulouService;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class SigninActivity extends BaseActivity implements
		Request.GraphUserCallback {

	ProgressDialog progressDialog;
	private String socialID;
	private String userName;
	private String userMail;
	private String birthday;
	private String gender;

	private EulouService service = null;
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			EulouService.ServiceBinder b = (EulouService.ServiceBinder) binder;
			service = b.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			service = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_signin);

		ImageButton buttonFacebook = (ImageButton) findViewById(R.id.buttonFacebook);
		buttonFacebook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				List<String> permissions = new LinkedList<String>();
				permissions.add("public_profile");
				permissions.add("user_friends");
				permissions.add("email");
				Session.openActiveSession(SigninActivity.this, true,
						permissions, new Session.StatusCallback() {

							@Override
							public void call(Session session,
									SessionState state, Exception exception) {
								if (exception != null) {
									Toast.makeText(
											SigninActivity.this
													.getApplicationContext(),
											exception.getMessage(),
											Toast.LENGTH_LONG).show();
									return;
								} else {
									if (session.isOpened()) {
										progressDialog.show();
										Request.newMeRequest(session,
												SigninActivity.this)
												.executeAsync();
									}
								}
							}
						});
			}

		});

		ImageButton buttonWhatIs = (ImageButton) findViewById(R.id.buttonWhatIs);
		buttonWhatIs.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent welcomeIntent = new Intent(SigninActivity.this,
						WelcomeActivity.class);
				SigninActivity.this.startActivity(welcomeIntent);
				SigninActivity.this.overridePendingTransition(R.anim.fadein,
						R.anim.fadeout);
				finish();
			}

		});

		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage("Signing in...");
		progressDialog.setCancelable(false);

		Intent serviceIntent = new Intent(SigninActivity.this,
				EulouService.class);
		startService(serviceIntent);
		bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Session.getActiveSession() == null)
			return;
		Session.getActiveSession().onActivityResult(this, requestCode,
				resultCode, data);
	}

	@Override
	public void onCompleted(GraphUser user, Response response) {
		if (user == null) {
			progressDialog.dismiss();
			Toast.makeText(getApplicationContext(),
					"Failed to retrieve facebook user information.",
					Toast.LENGTH_LONG).show();
			return;
		}

		socialID = user.getId();
		userName = user.getName();
		userMail = (String) user.getProperty("email");
		userMail = userMail == null ? "" : userMail;
		birthday = user.getBirthday();
		gender = (String) user.getProperty("gender");
		String requestString = Constants.EULOU_SERVICE_URL + "?action="
				+ Constants.API_SIGNIN_FACEBOOK + "&facebookID=" + socialID;
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(requestString, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				JSONObject responseObject;
				try {

					responseObject = new JSONObject(new String(responseBody));
					String result = responseObject.getString("result");
					if (!result.equalsIgnoreCase("succeeded")) {
						if (birthday == null
								|| birthday.equalsIgnoreCase("null"))
							birthday = "";
						String requestString = Constants.EULOU_SERVICE_URL
								+ "?action=" + Constants.API_REGISTER_FACEBOOK
								+ "&facebookID=" + socialID + "&username="
								+ URLEncoder.encode(userName, "UTF-8")
								+ "&usermail="
								+ URLEncoder.encode(userMail, "UTF-8")
								+ "&birthday=" + birthday + "&gender="
								+ (gender.equalsIgnoreCase("male") ? "1" : "0");
						AsyncHttpClient client = new AsyncHttpClient();
						client.get(requestString,
								new AsyncHttpResponseHandler() {

									@Override
									public void onFailure(int statusCode,
											Header[] headers,
											byte[] responseBody, Throwable error) {
										progressDialog.dismiss();
										Toast.makeText(getApplicationContext(),
												error.getMessage(),
												Toast.LENGTH_LONG).show();
										return;
									}

									@Override
									public void onSuccess(int statusCode,
											Header[] headers,
											byte[] responseBody) {
										progressDialog.dismiss();
										try {

											JSONObject responseObject = new JSONObject(
													new String(responseBody));
											if (!responseObject.getString(
													"result").equalsIgnoreCase(
													"succeeded")) {
												Toast.makeText(
														getApplicationContext(),
														responseObject
																.getString("message"),
														Toast.LENGTH_LONG)
														.show();
												return;
											} else {
												JSONObject userObject = responseObject
														.getJSONObject("user");
												UserInfo userInfo = new UserInfo();
												userInfo.setUserID(userObject
														.getInt("userID"));
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
												userInfo.setGender(userObject
														.getInt("gender"));
												userInfo.setPhone(userObject
														.getString("phone"));
												userInfo.setLastOnline(userObject
														.getString("last_online"));
												if (userObject.getString(
														"is_online")
														.equals("1")) {

													userInfo.setOnline(true);
												}

												SigninActivity.this
														.onLoginSuccessWithUser(userInfo);
												return;
											}

										} catch (Exception e) {
											e.printStackTrace();
											Toast.makeText(
													getApplicationContext(),
													e.getMessage(),
													Toast.LENGTH_LONG).show();
											return;
										}
									}

								});

						return;
					}

					progressDialog.dismiss();

					JSONObject userObject = responseObject
							.getJSONObject("user");
					UserInfo userInfo = new UserInfo();
					userInfo.setUserID(userObject.getInt("userID"));
					userInfo.setMainSocial(userObject.getInt("mainSocial"));
					userInfo.setFacebookID(userObject.getString("facebookID"));
					userInfo.setTwitterID(userObject.getString("twitterID"));
					userInfo.setInstagramID(userObject.getString("instagramID"));
					userInfo.setUserName(userObject.getString("username"));
					userInfo.setUserMail(userObject.getString("usermail"));
					userInfo.setGender(userObject.getInt("gender"));
					userInfo.setPhone(userObject.getString("phone"));
					SigninActivity.this.onLoginSuccessWithUser(userInfo);
					return;

				} catch (Exception e) {
					e.printStackTrace();
					progressDialog.dismiss();
					Toast.makeText(getApplicationContext(), e.getMessage(),
							Toast.LENGTH_LONG).show();
					return;
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				progressDialog.dismiss();
				Toast.makeText(getApplicationContext(), error.getMessage(),
						Toast.LENGTH_LONG).show();
				return;
			}
		});
	}

	public void onLoginSuccessWithUser(UserInfo userInfo) {
		SharedPreferences.Editor editor = getSharedPreferences("Eulou",
				MODE_PRIVATE).edit();
		Gson gson = new Gson();
		String json = gson.toJson(userInfo);
		editor.putString("LoggedinUser", json);
		editor.commit();
		mUserInfo = userInfo;
		service.onLoginSuccess();
		Intent mainIntent = null;

		if (userInfo.getPhone() != null
				&& !userInfo.getPhone().equalsIgnoreCase("")) {
			mainIntent = new Intent(SigninActivity.this, MainActivity.class);
		} else {
			mainIntent = new Intent(SigninActivity.this,
					EnterNumberActivity.class);
		}

		startActivity(mainIntent);
		overridePendingTransition(R.anim.fadein, R.anim.fadeout);
		finish();
	}
}
