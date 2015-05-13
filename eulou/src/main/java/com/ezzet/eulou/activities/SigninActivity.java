package com.ezzet.eulou.activities;

import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import com.ezzet.eulou.R;
import com.ezzet.eulou.adapters.LoginExplainAdapter;
import com.ezzet.eulou.models.UserInfo;

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


public class SigninActivity extends FragmentActivity implements View.OnClickListener {

    private CallbackManager mCallbackManager;
    private FacebookConnectCallBack mFacebookCallback;
    private final List<String> mPermissions = Arrays.asList("public_profile",
            "email", "user_friends");
    private ViewPager mVpLoginExplain;
    private LoginExplainAdapter mLoginExplainAdapter;
    private CirclePageIndicatorView mLoginExplainIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_signin);
        ImageButton buttonFacebook = (ImageButton) findViewById(R.id.buttonFacebook);


        mVpLoginExplain = (ViewPager) findViewById(R.id.login_explain_vpr);
        mLoginExplainAdapter = new LoginExplainAdapter(this);
        mVpLoginExplain.setAdapter(mLoginExplainAdapter);

        mLoginExplainIndicator = (CirclePageIndicatorView) findViewById(R.id.login_explain_indicator);
        mLoginExplainIndicator.setViewPager(mVpLoginExplain);
        ((CirclePageIndicatorView) mLoginExplainIndicator).setSnap(true);
        mLoginExplainIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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

    public interface FacebookConnectCallBack {

        public void onStartFbLogin();

        /**
         * Successfully connect to facebook
         * */
        public void onFbConnectSuccess();

        /**
         * User cancel or fails to connect to facebook
         * */
        public void onFbConnectFails();

        /**
         * Request user data after successfully connected to facebook
         * */
        public void onFbRequestComplete(UserInfo userInfo);

        /**
         * Fails to request user data
         * */
        public void onFbRequestError();
    }

    private FacebookCallback facebookCallback = new FacebookCallback<LoginResult>() {

        @Override
        public void onSuccess(LoginResult loginResult) {

            //mFacebookCallback.onFbConnectSuccess();
            makeRequest(loginResult);
        }

        @Override
        public void onCancel() {

            //mFacebookCallback.onFbConnectFails();
        }

        @Override
        public void onError(FacebookException e) {

            //mFacebookCallback.onFbConnectFails();
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
                            userInfo.setUserName(jsonObject
                                    .optString("name"));
                            userInfo.setUserMail(jsonObject.optString("email"));
                            userInfo.setFacebookID(jsonObject.optString("id"));
                            userInfo.setUserID(jsonObject.getInt("userID"));
                            userInfo.setGender(jsonObject.getInt("gender"));
                            userInfo.setPhone(jsonObject.getString("phone"));
                            userInfo.setMainSocial(jsonObject.getInt("mainSocial"));
                            userInfo.setTwitterID(jsonObject.getString("twitterID"));
                            userInfo.setInstagramID(jsonObject.getString("instagramID"));
                            LogUtil.e("makeRequest", "jsonObject: " + jsonObject.toString());
                            Intent mainIntent = null;
                            mainIntent = new Intent(SigninActivity.this, MainActivity.class);
                            startActivity(mainIntent);
                            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                            finish();
                            // new CheckUserRegisterAsyncTask()
                            // .execute(userEntity);
                            //mFacebookCallback.onFbRequestComplete(userInfo);

                        } catch (Exception ex) {

                            //mFacebookCallback.onFbRequestError();
                        }
                    }
                }).executeAsync();
    }

    /**
     * Do login user with facebook
     */
    private void doLogInFacebook() {

        //mFacebookCallback.onStartFbLogin();
        LoginManager.getInstance().logInWithReadPermissions(this, mPermissions);
    }
}
