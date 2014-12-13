package com.ezzet.eulou.activities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ezzet.eulou.R;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.models.CallHistoryItem;
import com.ezzet.eulou.models.CurrentCall;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.services.AudioPlayer;
import com.ezzet.eulou.services.EulouService;
import com.ezzet.eulou.services.SinchClientService;
import com.ezzet.eulou.views.FBProfilePictureView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallDirection;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.calling.CallState;

public class CallScreenActivity extends BaseActivity {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    static final String LOG_TAG = CallScreenActivity.class.getSimpleName();
    private AudioPlayer mAudioPlayer;
    private Call mCall;
    private long mCallStart;
    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;
    private Button mEndCallButton;
    private FBProfilePictureView profilePictureView;
    private ImageButton mSpeakerButton;
    private ImageButton mMuteButton;
    private String remoteUserDeviceToken = null;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;
    private PowerManager.WakeLock wakeLock;
    private int field = 0x00000020;
    private boolean speakerDisabled = true;
    private boolean microphoneMuted = false;

    private EulouService service = null;
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            EulouService.ServiceBinder b = (EulouService.ServiceBinder) binder;
            service = b.getService();

            UserInfo callerInfo = service.findFriendUserBySinchID(mCall.getRemoteUserId());
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

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_outgoing);
        try {
            // Yeah, this is hidden field.
            field = PowerManager.class.getClass().getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
        } catch (Throwable ignored) {
        }

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(field, getLocalClassName());
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        mCallDuration = (TextView) findViewById(R.id.callDuration);
        mCallerName = (TextView) findViewById(R.id.remoteUser);
        mCallState = (TextView) findViewById(R.id.callState);
        mEndCallButton = (Button) findViewById(R.id.hangupButton);

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

        mCallerName.setText(mCall.getRemoteUserId());

        if (mCall.getState() == CallState.INITIATING) {
            mCallState.setText("RINGING");
        } else {
            mCallState.setText(mCall.getState().toString());
        }
        mAudioPlayer = new AudioPlayer(this);

        mEndCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayer.stopProgressTone();
                endCall();
            }
        });

        profilePictureView = (FBProfilePictureView) findViewById(R.id.profilePictureView);
        profilePictureView.setProfileId(mCall.getRemoteUserId().substring(2));

        Intent serviceIntent = new Intent(CallScreenActivity.this, EulouService.class);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        speakerDisabled = getIntent().getBooleanExtra("SpeakerDisabled", true);
        microphoneMuted = getIntent().getBooleanExtra("MicrophoneMuted", false);
        if (speakerDisabled) {
            SinchClientService.audioController.disableSpeaker();
        } else {
            SinchClientService.audioController.enableSpeaker();
        }
        if (microphoneMuted) {
            SinchClientService.audioController.mute();
        } else {
            SinchClientService.audioController.unmute();
        }

        updateButtonStatus();
    }

    @Override
    public void onDestroy() {
        unbindService(mConnection);
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();

        mDurationTask.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();

        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    private void startCallDuration() {
        mCallStart = System.currentTimeMillis();
        mTimer.schedule(mDurationTask, 0, 500);
    }

    private void endCall() {
        mCall.hangup();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        finish();
    }

    private CharSequence formatTimespan(long timespan) {
        long totalSeconds = timespan / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        mCallDuration.setText(formatTimespan(System.currentTimeMillis() - mCallStart));
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

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {

            SinchClientService.audioController.disableSpeaker();
            SinchClientService.audioController.unmute();
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            UserInfo callerUser = service.findFriendUserBySinchID(call.getRemoteUserId());
            UserInfo currentUser = mUserInfo;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            if (call.getDirection() == CallDirection.OUTGOING) {
                String startdate, groupdate;
                String endCause;
                Date date;

                if (call.getDetails().getEstablishedTime() == 0) {
                    date = new Date(call.getDetails().getEndedTime() * 1000);
                    endCause = "1";
                } else {
                    date = new Date(call.getDetails().getEstablishedTime() * 1000);
                    endCause = call.getDetails().getEndCause() == CallEndCause.HUNG_UP ? "5" : "1";
                }

                startdate = dateFormat.format(date);
                groupdate = dateFormat1.format(date);

                date = new Date(call.getDetails().getEndedTime() * 1000);
                String enddate = dateFormat.format(date);

                String requestString = "";
                try {
                    requestString = Constants.EULOU_SERVICE_URL + "?action=" + Constants.API_CALL_ADD + "&fromuser=" + currentUser.getUserID() + "&touser=" + callerUser.getUserID() + "&fromname=" + URLEncoder.encode(currentUser.getUserName(), "UTF-8") + "&toname=" + URLEncoder.encode(callerUser.getUserName(), "UTF-8") + "&startdate=" + URLEncoder.encode(startdate, "UTF-8") + "&enddate=" + URLEncoder.encode(enddate, "UTF-8") + "&callstatus=" + endCause + "&groupdate=" + URLEncoder.encode(groupdate, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Toast.makeText(CallScreenActivity.this, "Not Updated", Toast.LENGTH_SHORT).show();
                }

                AsyncHttpClient client = new AsyncHttpClient();
                client.get(requestString, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(CallScreenActivity.this.getApplicationContext(), "Failed to save call history.", Toast.LENGTH_LONG).show();
                        return;
                    }
                });

                CallHistoryItem item = new CallHistoryItem();
                item.userInfo = callerUser;
                item.contact = callerUser.getUserName();
                item.contactID = String.valueOf(callerUser.getUserID());
                item.startDate = dateFormat.format(new Date(call.getDetails().getStartedTime() * 1000));
                item.endDate = enddate;
                item.endCause = Integer.valueOf(endCause);
                item.direction = "outgoing";
                item.count = "1";
                item.groupdate = groupdate;

                CallHistoryItem.historyArray.add(item);

                if (call.getDetails().getEndCause() == CallEndCause.NO_ANSWER || call.getDetails().getEndCause() == CallEndCause.CANCELED) {
                    if (remoteUserDeviceToken != null && !remoteUserDeviceToken.equals("")) {
                        String message = String.format("%s : Missed call", mUserInfo.getUserName());
                        requestString = "";
                        try {
                            requestString = String.format("%s?action=%s&remotedevice=%s&message=%s&payload=%s&sound=&badge=1", Constants.EULOU_SERVICE_URL, Constants.API_SEND_PUSH, remoteUserDeviceToken, URLEncoder.encode(message, "UTF-8"), "");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        AsyncHttpClient client1 = new AsyncHttpClient();
                        client1.get(requestString, new AsyncHttpResponseHandler() {

                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        Toast.makeText(CallScreenActivity.this.getApplicationContext(), "Failed to send push notification.", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                });
                    }
                }
            } else {
                CallHistoryItem item = new CallHistoryItem();
                item.userInfo = callerUser;
                item.contact = callerUser.getUserName();
                item.contactID = String.valueOf(callerUser.getUserID());
                item.startDate = dateFormat.format(new Date(call.getDetails().getStartedTime() * 1000));
                item.endDate = dateFormat.format(new Date(call.getDetails().getEndedTime() * 1000));
                item.endCause = call.getDetails().getEndCause().getValue();
                item.direction = "incoming";
                item.count = "1";
                item.groupdate = dateFormat1.format(new Date(call.getDetails().getStartedTime() * 1000));

                CallHistoryItem.historyArray.add(item);
            }

            CallScreenActivity.this.finish();
        }

        @Override
        public void onCallEstablished(Call call) {

            CallScreenActivity.this.startCallDuration();
            mAudioPlayer.stopProgressTone();
            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallProgressing(Call call) {

            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            if (pushPairs.size() <= 0) {
                return;
            }
            PushPair pushPair = pushPairs.get(0);
            if (pushPair.getPushData().length > 0) {
                String deviceToken = bytesToHex(pushPair.getPushData());
                remoteUserDeviceToken = deviceToken;

                String message = String.format("%s : Incoming call", mUserInfo.getUserName());
                String sound = "incoming.wav";
                String requestString = "";
                try {

                    requestString = String.format("%s?action=%s&remotedevice=%s&message=%s&payload=%s&sound=%s&badge=0", Constants.EULOU_SERVICE_URL, Constants.API_SEND_PUSH, deviceToken, URLEncoder.encode(message, "UTF-8"), URLEncoder.encode(pushPair.getPushPayload(), "UTF-8"), URLEncoder.encode(sound, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                AsyncHttpClient client = new AsyncHttpClient();
                client.get(requestString, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Toast.makeText(CallScreenActivity.this.getApplicationContext(), "Failed to send push notification.", Toast.LENGTH_LONG).show();
                        return;
                    }
                });
            }
        }
    }
}
