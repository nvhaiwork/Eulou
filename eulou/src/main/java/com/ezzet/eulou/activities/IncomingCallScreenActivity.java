package com.ezzet.eulou.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.models.CurrentCall;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.services.AudioPlayer;
import com.ezzet.eulou.services.EulouService;
import com.ezzet.eulou.services.SinchClientService;
import com.ezzet.eulou.views.FBProfilePictureView;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;

@SuppressLint("NewApi")
public class IncomingCallScreenActivity extends BaseActivity {

	static final String LOG_TAG = IncomingCallScreenActivity.class
			.getSimpleName();

	private ImageButton mAnswer;
	private TextView mCallerName;
	private String callerName = null;
	private ImageButton mDecline;
	private FBProfilePictureView profilePictureView;
	private ImageButton mSpeakerButton;
	private ImageButton mMuteButton;
	private Call mCall;
	private AudioPlayer mAudioPlayer;
	private boolean speakerDisabled = true;
	private boolean microphoneMuted = false;
	private Vibrator vibrator = null;
	private PowerManager.WakeLock wl;
	private EulouService service = null;
	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			EulouService.ServiceBinder b = (EulouService.ServiceBinder) binder;
			service = b.getService();

			UserInfo callerInfo = service.findFriendUserBySinchID(mCall
					.getRemoteUserId());
			if (callerInfo == null) {
				mCallerName.setText(mCall.getRemoteUserId());
			} else {
				mCallerName.setText(callerInfo.getUserName());
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			service = null;
		}
	};
	private OnClickListener incomingClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.answerButton :
					answerClicked();
					break;
				case R.id.declineButton :
					declineClicked();
					break;
			}
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_incoming);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNjfdhotDimScreen");
		wl.acquire();
		mAnswer = (ImageButton) findViewById(R.id.answerButton);
		mCallerName = (TextView) findViewById(R.id.remoteUser);
		mDecline = (ImageButton) findViewById(R.id.declineButton);

		mSpeakerButton = (ImageButton) findViewById(R.id.buttonSpeaker);
		mMuteButton = (ImageButton) findViewById(R.id.buttonMute);

		mSpeakerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (speakerDisabled) {
					speakerDisabled = false;
					SinchClientService.audioController.enableSpeaker();
				} else {
					speakerDisabled = true;
					SinchClientService.audioController.disableSpeaker();
				}

				updateButtonStatus();
			}

		});

		mMuteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (microphoneMuted) {
					microphoneMuted = false;
					SinchClientService.audioController.unmute();
				} else {
					microphoneMuted = true;
					SinchClientService.audioController.mute();
				}

				updateButtonStatus();
			}

		});

		mCall = CurrentCall.currentCall;
		mCall.addCallListener(new SinchCallListener());

		mAnswer.setOnClickListener(incomingClickListener);
		mDecline.setOnClickListener(incomingClickListener);
		mCallerName.setText(mCall.getRemoteUserId());
		mAudioPlayer = new AudioPlayer(this);
		mAudioPlayer.playRingtone();

		profilePictureView = (FBProfilePictureView) findViewById(R.id.profilePictureView);
		profilePictureView.setProfileId(mCall.getRemoteUserId().substring(2));

		mCallerName.setText(mCall.getRemoteUserId());

		Intent serviceIntent = new Intent(IncomingCallScreenActivity.this,
				EulouService.class);
		bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

		speakerDisabled = true;
		microphoneMuted = false;
		SinchClientService.audioController.disableSpeaker();
		SinchClientService.audioController.unmute();
		updateButtonStatus();
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator != null && vibrator.hasVibrator()) {
			long[] pattern = {0, 1000, 1000};
			vibrator.vibrate(pattern, 0);
		}
	}

	@Override
	protected void onResume() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onResume();
	}

	@Override
	public void onDestroy() {
		if (vibrator != null && vibrator.hasVibrator()) {
			vibrator.cancel();
		}
		unbindService(mConnection);
		wl.release();
		super.onDestroy();
	}

	private void answerClicked() {
		mAudioPlayer.stopRingtone();
		mCall.answer();
		Intent callIntent = new Intent(this, CallScreenActivity.class);
		if (callerName != null) {
			callIntent.putExtra("CallerName", callerName);
		}
		callIntent.putExtra("SpeakerDisabled", speakerDisabled);
		callIntent.putExtra("MicrophoneMuted", microphoneMuted);
		startActivity(callIntent);
		finish();
	}

	private void declineClicked() {
		mAudioPlayer.stopRingtone();
		mCall.hangup();
		finish();
	}

	private void updateButtonStatus() {

		if (speakerDisabled) {
			mSpeakerButton.setImageResource(R.drawable.speaker);
		} else {
			mSpeakerButton.setImageResource(R.drawable.speaker_pressed);
		}

		if (microphoneMuted) {
			mMuteButton.setImageResource(R.drawable.mute_pressed);
		} else {
			mMuteButton.setImageResource(R.drawable.mute);
		}
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
				case KeyEvent.KEYCODE_VOLUME_UP :
					// Volume up key detected
					// Do something
					return true;
				case KeyEvent.KEYCODE_VOLUME_DOWN :
					// Volume down key detected
					// Do something
					AudioManager audio_mngr = (AudioManager) getBaseContext()
							.getSystemService(Context.AUDIO_SERVICE);
					audio_mngr.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					return true;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	private class SinchCallListener implements CallListener {

		@Override
		public void onCallEnded(Call call) {

			SinchClientService.audioController.disableSpeaker();
			SinchClientService.audioController.unmute();
			mAudioPlayer.stopRingtone();
			finish();
		}

		@Override
		public void onCallEstablished(Call call) {
		}

		@Override
		public void onCallProgressing(Call call) {
		}

		@Override
		public void onShouldSendPushNotification(Call call,
				List<PushPair> pushPairs) {
		}
	}
}
