package com.ezzet.eulou.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class IncomingSmsReceiver extends BroadcastReceiver {
    final SmsManager sms = SmsManager.getDefault();

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[0]);

                String message = currentMessage.getDisplayMessageBody();
                if (message.startsWith("Eulou ! Phone Verification: ")) {
                    String code = message.replace("Eulou ! Phone Verification: ", "");
                    Intent intentSendCode = new Intent();
                    intentSendCode.setAction("com.ezzet.eulou.code");
                    intentSendCode.putExtra("code", code);
                    context.sendBroadcast(intentSendCode);
                }
            }
        } catch (Exception e) {
        }
    }

}