package com.ezzet.eulou.activities;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ezzet.eulou.R;
import com.ezzet.eulou.extra.HelperFunction;
import com.google.gson.Gson;

public class VerifyNumberActivity extends BaseActivity {

	private ImageButton imgBack;
	private Button btnVCont;
	private EditText etVCode;
	private MyReceiver mMessageReceiver;
	private TextView tvNumber;
	private HelperFunction HF;
	private String mobNo;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_verify_number);
		init();

		Intent i = getIntent();
		mobNo = i.getStringExtra("mobNo");
		tvNumber.setText(mobNo);
		imgBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		btnVCont.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyBoard();
				verify();
			}
		});

	}

	private void hideKeyBoard() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(
				getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	private void verify() {
		String enteredCode = etVCode.getText().toString();
		if (!enteredCode.trim().equalsIgnoreCase("")) {
			SharedPreferences sharedpreferences = getSharedPreferences(
					"CodePref", Context.MODE_PRIVATE);
			int storedCode = sharedpreferences.getInt("code", -1457689888);
			if (enteredCode.equalsIgnoreCase(storedCode + "")) {
				verifyNumber(
						String.valueOf(BaseActivity.mUserInfo.getUserID()),
						mobNo);
				SharedPreferences.Editor editor = sharedpreferences.edit();
				editor.putString("number", mobNo);
				editor.commit();
			} else {
				Toast.makeText(VerifyNumberActivity.this, "Wrong code.",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void verifyNumber(final String userId, final String number) {
		new Thread(new Runnable() {

			public void run() {
				final String verify = HF.submitNumber(userId, number);

				runOnUiThread(new Runnable() {
					public void run() {
						try {
							JSONObject jObj = new JSONObject(verify);
							String result = jObj.getString("result");
							if (result.equals("succeeded")) {
								Toast.makeText(VerifyNumberActivity.this,
										"Verification successful.",
										Toast.LENGTH_LONG).show();
								Intent intent = new Intent(
										VerifyNumberActivity.this,
										MainActivity.class);
								mUserInfo.setPhone(number);
								SharedPreferences prefs = getSharedPreferences(
										"Eulou", MODE_PRIVATE);
								SharedPreferences.Editor editor = prefs.edit();
								String json = new Gson().toJson(mUserInfo);
								editor.putString("CLoggedinUser", json);
								editor.commit();
								intent.putExtra("isFromVerify", true);
								overridePendingTransition(R.anim.fadein,
										R.anim.fadeout);
								startActivity(intent);
								finish();
							} else if (result.equals("lost")) {
								// Toast.makeText(VerifyNumberActivity.this,"Thre is some Problem with Connection.Please Retry.",Toast.LENGTH_SHORT).show();
							}

						} catch (JSONException e) {

							e.printStackTrace();
						}
					}
				});
			}
		}).start();
	}

	private void init() {
		imgBack = (ImageButton) findViewById(R.id.buttonBack);
		btnVCont = (Button) findViewById(R.id.btnVCont);
		etVCode = (EditText) findViewById(R.id.etVCode);
		tvNumber = (TextView) findViewById(R.id.txtPhoneNumber);
		HF = new HelperFunction(VerifyNumberActivity.this);
	}

	@Override
	public void onResume() {
		super.onResume();
		mMessageReceiver = new MyReceiver();
		IntentFilter intentFilter = new IntentFilter("com.ezzet.eulou.code");
		registerReceiver(mMessageReceiver, intentFilter);
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mMessageReceiver);
		super.onPause();
	}

	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			etVCode.setText(intent.getExtras().getString("code", ""));
		}

	}
}
