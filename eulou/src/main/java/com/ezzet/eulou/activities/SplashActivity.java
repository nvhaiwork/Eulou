package com.ezzet.eulou.activities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.ezzet.eulou.R;
import com.ezzet.eulou.models.UserInfo;
import com.google.gson.Gson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

public class SplashActivity extends BaseActivity {

	private SharedPreferences prefs;

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
				Log.d("KeyHash:",
						Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		} catch (NameNotFoundException e) {

		} catch (NoSuchAlgorithmException e) {

		}
		prefs = SplashActivity.this.getSharedPreferences("Eulou", MODE_PRIVATE);

		boolean isBeforeLaunched = prefs.getBoolean("EverLaunched", false);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("EverLaunched", true);
		editor.commit();
		Intent mainIntent;
		if (isBeforeLaunched) {

			Gson gson = new Gson();
			String json = prefs.getString("LoggedinUser", "");
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

			mainIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
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
