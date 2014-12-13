package com.ezzet.eulou.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;

import com.ezzet.eulou.R;

public class WelcomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_welcome);

        ImageButton buttonContinue = (ImageButton) findViewById(R.id.buttonContinue);
        buttonContinue.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(WelcomeActivity.this, SigninActivity.class);
                WelcomeActivity.this.startActivity(mainIntent);
                WelcomeActivity.this.overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                finish();
            }

        });
    }

}
