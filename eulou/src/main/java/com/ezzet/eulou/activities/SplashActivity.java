package com.ezzet.eulou.activities;

import java.security.MessageDigest;

import com.ezzet.eulou.R;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.utilities.CustomSharedPreferences;
import com.ezzet.eulou.utilities.LogUtil;
import com.google.gson.Gson;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;

public class SplashActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);

		// Add code to print out the key hash
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					"com.ezzet.eulou", PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				LogUtil.d("KeyHash:",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (Exception e) {

			LogUtil.d("KeyHash:", e.getMessage());
		}

		boolean isBeforeLaunched = CustomSharedPreferences.getPreferences(
				"EverLaunched", false);
		CustomSharedPreferences.setPreferences("EverLaunched", true);
		Intent mainIntent;
		if (isBeforeLaunched) {

			Gson gson = new Gson();
			String json = CustomSharedPreferences.getPreferences(
					"LoggedinUser", "");
			if (json.equals("")) {
				mainIntent = new Intent(SplashActivity.this,
						SigninActivity.class);
				SplashActivity.this.startActivity(mainIntent);
				SplashActivity.this.overridePendingTransition(R.anim.fadein,
						R.anim.fadeout);
				finish();
			} else {

				mUserInfo = gson.fromJson(json, UserInfo.class);
				mainIntent = new Intent(SplashActivity.this, MainActivity.class);
				SplashActivity.this.startActivity(mainIntent);
				SplashActivity.this.overridePendingTransition(R.anim.fadein,
						R.anim.fadeout);
				finish();
			}
		} else {

			mainIntent = new Intent(SplashActivity.this, SigninActivity.class);
			SplashActivity.this.startActivity(mainIntent);
			SplashActivity.this.overridePendingTransition(R.anim.fadein,
					R.anim.fadeout);
			finish();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}
}
