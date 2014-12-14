package com.ezzet.eulou.activities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ezzet.eulou.R;
import com.ezzet.eulou.utilities.CustomSharedPreferences;

public class EnterNumberActivity extends BaseActivity {

	RelativeLayout rlCountry, rlSkip;
	TextView tvCName, tvCCode;
	Button btContinue;
	EditText txtMobileNumber;
	String userPhoneNumber;
	String countryName;
	String countryCode;

	private static String convertInputStreamToString(InputStream inputStream)
			throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream));
		String line = "";
		String result = "";
		while ((line = bufferedReader.readLine()) != null) {
			result += line;
		}

		inputStream.close();
		return result;

	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_number);
		init();
	}

	private void init() {
		rlCountry = (RelativeLayout) findViewById(R.id.relative);
		rlSkip = (RelativeLayout) findViewById(R.id.relativeSkip);
		tvCName = (TextView) findViewById(R.id.txtCountryName);
		tvCCode = (TextView) findViewById(R.id.txtCountryCode);
		btContinue = (Button) findViewById(R.id.btnVCont);
		txtMobileNumber = (EditText) findViewById(R.id.txtMobileNumber);

		if (countryName != null) {
			tvCName.setText(countryName);
			tvCCode.setText("+" + countryCode);
		}

		rlCountry.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EnterNumberActivity.this,
						CountryListActivity.class);
				startActivityForResult(intent, 101);
			}
		});
		btContinue.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideKeyBoard();
				if (txtMobileNumber.getText().toString().equals("")) {
					Toast.makeText(EnterNumberActivity.this,
							"Please enter number", Toast.LENGTH_SHORT).show();
				} else {
					showCustomDialog(tvCCode.getText().toString()
							+ txtMobileNumber.getText().toString());
				}
			}
		});

		rlSkip.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(EnterNumberActivity.this,
						MainActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.fadein, R.anim.fadeout);
				finish();
			}
		});
	}

	private void sendVerificationCode(final int code) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String result = postData(code);
					if (result != null) {

						startActivity(new Intent(EnterNumberActivity.this,
								VerifyNumberActivity.class).putExtra("mobNo",
								userPhoneNumber));
						finish();
					}
				} catch (Exception e) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							Toast.makeText(
									EnterNumberActivity.this,
									"Verification could not be sent. Please check your number.",
									Toast.LENGTH_LONG).show();
						}
					});

					e.printStackTrace();
				} finally {
					hideProgress();
				}
			}
		}).start();
	}

	private void createAndSaveVerificationCode() {
		Random rnd = new Random();
		int code = 100000 + rnd.nextInt(900000);

		CustomSharedPreferences.setPreferences("code", code);
		sendVerificationCode(code);
	}

	private String postData(int code) {

		String result = null;

		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"https://AC0f635cc862fb62ffb81f4483d0679a95:a40071bfd664c7e646996e5ab67094d1@api.twilio.com/2010-04-01/Accounts/AC0f635cc862fb62ffb81f4483d0679a95/SMS/Messages.json");
		String authString = "AC0f635cc862fb62ffb81f4483d0679a95" + ":"
				+ "a40071bfd664c7e646996e5ab67094d1";
		httppost.setHeader(
				"Authorization",
				"Basic "
						+ Base64.encodeToString((authString).getBytes(),
								Base64.NO_WRAP));

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("From", "+18443344277"));
			nameValuePairs.add(new BasicNameValuePair("To", userPhoneNumber));
			nameValuePairs.add(new BasicNameValuePair("Body",
					"Eulou ! Phone Verification: " + code));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			// 9. receive response as inputStream
			InputStream inputStream = response.getEntity().getContent();
			// 10. convert inputstream to string
			if (inputStream != null) {
				result = convertInputStreamToString(inputStream);
			} else {
				result = "Did not work!";
			}

		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}

		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && requestCode == 101) {
			countryName = data.getExtras().getString("cName");
			countryCode = data.getExtras().getString("cCode");
			tvCName.setText(countryName);
			tvCCode.setText("+" + countryCode);
		}

	}

	private void showProgress() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				findViewById(R.id.loadingLayout).setVisibility(View.VISIBLE);
			}
		});
	}

	private void hideProgress() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				findViewById(R.id.loadingLayout).setVisibility(View.GONE);
			}
		});
	}

	private void hideKeyBoard() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		inputManager.hideSoftInputFromWindow(
				getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public void showCustomDialog(String number) {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(EnterNumberActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.number_dialog);

		Button btEdit = (Button) dialog.findViewById(R.id.btnEdit);
		Button btYes = (Button) dialog.findViewById(R.id.btnYes);
		TextView tvNumber = (TextView) dialog.findViewById(R.id.txtNumber);

		tvNumber.setText(number);
		btEdit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		btYes.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showProgress();
				userPhoneNumber = tvCCode.getText().toString()
						+ txtMobileNumber.getText().toString();
				createAndSaveVerificationCode();
				dialog.dismiss();
			}
		});
		dialog.show();
	}

}
