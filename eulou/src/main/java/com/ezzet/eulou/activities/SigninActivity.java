package com.ezzet.eulou.activities;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.ezzet.eulou.R;
import com.ezzet.eulou.adapters.LoginExplainAdapter;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.models.UserInfo;

import com.ezzet.eulou.services.EulouService;
import com.ezzet.eulou.utilities.CustomSharedPreferences;
import com.ezzet.eulou.utilities.LogUtil;
import com.ezzet.eulou.views.CirclePageIndicatorView;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class SigninActivity extends BaseActivity
		implements
			View.OnClickListener {

	private CallbackManager mCallbackManager;
	private final List<String> mPermissions = Arrays.asList("public_profile",
			"email", "user_friends");

    private String mSocialID;
    private String mUserName;
    private String mUserMail;
    private String mBirthday;
    private String mGender;

    private ProgressDialog mProgressDialog;

    private EulouService mEulouService = null;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            EulouService.ServiceBinder b = (EulouService.ServiceBinder) binder;
            mEulouService = b.getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            mEulouService = null;
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		FacebookSdk.sdkInitialize(this.getApplicationContext());

		setContentView(R.layout.activity_signin);
		ImageView buttonFacebook = (ImageView) findViewById(R.id.buttonFacebook);

		ViewPager vpLoginExplain = (ViewPager) findViewById(R.id.login_explain_vpr);
		LoginExplainAdapter loginExplainAdapter = new LoginExplainAdapter(this);
		vpLoginExplain.setAdapter(loginExplainAdapter);
		vpLoginExplain.setOffscreenPageLimit(4);

		CirclePageIndicatorView loginExplainIndicator = (CirclePageIndicatorView) findViewById(R.id.login_explain_indicator);
		loginExplainIndicator.setViewPager(vpLoginExplain);
		loginExplainIndicator.setSnap(true);

		// Facebook
		mCallbackManager = CallbackManager.Factory.create();
		LoginManager.getInstance().registerCallback(mCallbackManager,
				facebookCallback);

		buttonFacebook.setOnClickListener(SigninActivity.this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Signing in...");
        mProgressDialog.setCancelable(false);

        Intent serviceIntent = new Intent(SigninActivity.this,
                EulouService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.buttonFacebook :

				doLogInFacebook();
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mCallbackManager.onActivityResult(requestCode, resultCode, data);
	}

	private FacebookCallback facebookCallback = new FacebookCallback<LoginResult>() {

		@Override
		public void onSuccess(LoginResult loginResult) {

			makeRequest(loginResult);
		}

		@Override
		public void onCancel() {

		}

		@Override
		public void onError(FacebookException e) {

		}
	};


    private void loginCompleted(JSONObject jsonObject,
                                GraphResponse graphResponse) {

        mSocialID = jsonObject.optString("id");
        mUserName = jsonObject.optString("name");
        mUserMail = jsonObject.optString("email");
        mUserMail = (mUserMail == null) ? "" : mUserMail;
        mBirthday = null;//user.getBirthday();
        mGender = jsonObject.optString("gender");//(String) user.getProperty("gender");
        String requestString = Constants.EULOU_SERVICE_URL + "?action="
                + Constants.API_SIGNIN_FACEBOOK + "&facebookID=" + mSocialID;
        LogUtil.e("onSuccess EULOU_SERVICE_URL", "Error: "
                + requestString);
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
                        if (mBirthday == null
                                || mBirthday.equalsIgnoreCase("null")) {
                            mBirthday = "";
                        }
                        String requestString = Constants.EULOU_SERVICE_URL
                                + "?action=" + Constants.API_REGISTER_FACEBOOK
                                + "&facebookID=" + mSocialID + "&username="
                                + URLEncoder.encode(mUserName, "UTF-8")
                                + "&usermail="
                                + URLEncoder.encode(mUserMail, "UTF-8")
                                + "&birthday=" + mBirthday + "&gender="
                                + (mGender.equalsIgnoreCase("male") ? "1" : "0");
                        AsyncHttpClient client = new AsyncHttpClient();
                        client.get(requestString,
                                new AsyncHttpResponseHandler() {

                                    @Override
                                    public void onFailure(int statusCode,
                                                          Header[] headers,
                                                          byte[] responseBody, Throwable error) {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(getApplicationContext(),
                                                error.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                        LogUtil.e("onSuccess EULOU_SERVICE_URL", "Error: "
                                                + error.getMessage());
                                        return;
                                    }

                                    @Override
                                    public void onSuccess(int statusCode,
                                                          Header[] headers,
                                                          byte[] responseBody) {
                                        mProgressDialog.dismiss();
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
                                                LogUtil.e("onSuccess EULOU_SERVICE_URL", "Error: "
                                                        + responseObject
                                                        .getString("message"));
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
                                                LogUtil.e("onSuccess EULOU_SERVICE_URL", "userObject: "
                                                        + userObject.toString());
                                                SigninActivity.this
                                                        .onLoginSuccessWithUser(userInfo);
                                                return;
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            mProgressDialog.dismiss();
                                            LogUtil.e("onSuccess EULOU_SERVICE_URL", "Error: "
                                                    + e.getMessage());
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

                    mProgressDialog.dismiss();
                    JSONObject userObject = responseObject
                            .getJSONObject("user");
                    LogUtil.e("onSuccess EULOU_SERVICE_URL Exception", "Error: "
                            + userObject.toString());
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
                    mProgressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    LogUtil.e("onSuccess EULOU_SERVICE_URL Exception", "Error: "
                            + e.getMessage());
                    return;
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] responseBody, Throwable error) {
                mProgressDialog.dismiss();
                Toast.makeText(getApplicationContext(), error.getMessage(),
                        Toast.LENGTH_LONG).show();
                return;
            }
        });
    }

	private void makeRequest(LoginResult loginResult) {

		GraphRequest.newMeRequest(loginResult.getAccessToken(),
				new GraphRequest.GraphJSONObjectCallback() {
					@Override
					public void onCompleted(JSONObject jsonObject,
							GraphResponse graphResponse) {

						try {


                            loginCompleted(jsonObject, graphResponse);

						} catch (Exception ex) {

                            Toast.makeText(getApplicationContext(),
                                    "Failed to retrieve facebook user information.",
                                    Toast.LENGTH_LONG).show();

							LogUtil.e("makeRequest", ex.getMessage());
						}
					}
				}).executeAsync();
	}

	/**
	 * Do login user with facebook
	 */
	private void doLogInFacebook() {

		LoginManager.getInstance().logInWithReadPermissions(this, mPermissions);
	}


    public void onLoginSuccessWithUser(UserInfo userInfo) {
        Gson gson = new Gson();
        String json = gson.toJson(userInfo);
        CustomSharedPreferences.setPreferences("LoggedinUser", json);
        mUserInfo = userInfo;
        mEulouService.onLoginSuccess();
        Intent mainIntent = new Intent(SigninActivity.this,
                MainActivity.class);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.fadein,
                R.anim.fadeout);
        finish();
    }

    @Override
    public void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }
}
