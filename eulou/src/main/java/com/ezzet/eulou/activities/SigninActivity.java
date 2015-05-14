package com.ezzet.eulou.activities;

import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.adapters.LoginExplainAdapter;
import com.ezzet.eulou.models.UserInfo;

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

public class SigninActivity extends BaseActivity
		implements
			View.OnClickListener {

	private CallbackManager mCallbackManager;
	private final List<String> mPermissions = Arrays.asList("public_profile",
			"email", "user_friends");

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

	private void makeRequest(LoginResult loginResult) {

		GraphRequest.newMeRequest(loginResult.getAccessToken(),
				new GraphRequest.GraphJSONObjectCallback() {
					@Override
					public void onCompleted(JSONObject jsonObject,
							GraphResponse graphResponse) {

						try {

							UserInfo userInfo = new UserInfo();
							userInfo.setUserName(jsonObject.optString("name"));
							userInfo.setUserMail(jsonObject.optString("email"));
							userInfo.setFacebookID(jsonObject.optString("id"));
							LogUtil.e("makeRequest", "jsonObject: "
									+ jsonObject.toString());

							Gson gson = new Gson();
							String json = gson.toJson(userInfo);
							CustomSharedPreferences.setPreferences(
									"LoggedinUser", json);
							mUserInfo = userInfo;
							Intent mainIntent = new Intent(SigninActivity.this,
									MainActivity.class);
							startActivity(mainIntent);
							overridePendingTransition(R.anim.fadein,
									R.anim.fadeout);
							finish();
						} catch (Exception ex) {

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
}
