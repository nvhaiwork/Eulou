package com.ezzet.eulou.activities;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ezzet.eulou.R;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.models.CallHistoryItem;
import com.ezzet.eulou.models.DetailHistoryItem;
import com.ezzet.eulou.models.UserInfo;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CallDetailActivity extends BaseActivity {

    int historyIndex = 0;
    ArrayList<DetailHistoryItem> historyArray;

    ImageView imageViewProfilePicture;
    ListView listViewRecentCalls;
    RecentCallsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_calldetail);

        historyIndex = getIntent().getIntExtra("HistoryIndex", 0);
        CallHistoryItem item = CallHistoryItem.historyArray.get(historyIndex);
        UserInfo userInfo = item.userInfo;

        ImageButton buttonBack = (ImageButton) findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CallDetailActivity.this.finish();
            }

        });

        ImageButton buttonCall = (ImageButton) findViewById(R.id.buttonCall);
        buttonCall.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CallHistoryItem item = CallHistoryItem.historyArray.get(historyIndex);
                UserInfo userInfo = item.userInfo;
                int mainSocial = userInfo.getMainSocial();
                String userIDString = "";
                if (mainSocial == Constants.FACEBOOK) {
                    userIDString = "fb" + userInfo.getFacebookID();
                } else if (mainSocial == Constants.TWITTER) {
                    userIDString = "tw" + userInfo.getTwitterID();
                } else if (mainSocial == Constants.INSTAGRAM) {
                    userIDString = "in" + userInfo.getInstagramID();
                } else {
                    return;
                }

                Intent intent = new Intent();
                intent.setAction("com.ezzet.eulou.action.CALLBUTTON");
                intent.putExtra("SinchID", userIDString);
                sendBroadcast(intent);
            }

        });

        TextView textViewUserName = (TextView) findViewById(R.id.textViewUserName);
        TextView textViewUserMail = (TextView) findViewById(R.id.textViewUserMail);
        textViewUserName.setText(userInfo.getUserName());
        textViewUserMail.setText("Private"/* userInfo.userMail */);

        imageViewProfilePicture = (ImageView) findViewById(R.id.imageViewProfilePicture);
        String URL = String.format("https://graph.facebook.com/%s/picture?width=640&height=640", userInfo.getFacebookID());
        imageViewProfilePicture.setTag(URL);
        new DownloadImagesTask().execute(imageViewProfilePicture);

        historyArray = new ArrayList<DetailHistoryItem>();

        listViewRecentCalls = (ListView) findViewById(R.id.listViewRecentCalls);
        adapter = new RecentCallsListAdapter();
        listViewRecentCalls.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRecentCalls();
    }

    @SuppressLint("DefaultLocale")
    private void updateRecentCalls() {
        String fromuser = "";
        String touser = "";

        CallHistoryItem item = CallHistoryItem.historyArray.get(historyIndex);
        if (item.direction.equalsIgnoreCase("Outgoing")) {
            fromuser = String.format("%d", mUserInfo.getUserID());
            touser = String.format("%d", item.userInfo.getUserID());
        } else {
            fromuser = String.format("%d", item.userInfo.getUserID());
            touser = String.format("%d", mUserInfo.getUserID());
        }

        String requestString = "";
        try {
            requestString = Constants.EULOU_SERVICE_URL + "?action=" + Constants.API_DETAIL_HISTORY + "&fromuser=" + fromuser + "&touser=" + touser + "&groupdate=" + URLEncoder.encode(item.groupdate, "UTF-8") + "&callstatus=" + item.endCause + "&direction=" + item.direction;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(requestString, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                try {
                    JSONArray responseArray = new JSONArray(new String(responseBody));
                    historyArray.clear();

                    for (int i = 0; i < responseArray.length(); i++) {
                        JSONObject historyObject = responseArray.getJSONObject(i);

                        DetailHistoryItem item = new DetailHistoryItem();
                        item.startdate = historyObject.getString("startdate");
                        item.enddate = historyObject.getString("enddate");
                        item.historyID = historyObject.getString("historyID");
                        item.intervals = historyObject.getString("intervals");

                        historyArray.add(item);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(CallDetailActivity.this.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                adapter.notifyDataSetChanged();
                return;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                Toast.makeText(CallDetailActivity.this.getApplicationContext(), "Failed to retrieve friend users.", Toast.LENGTH_LONG).show();
                return;

            }
        });
    }

    public class DownloadImagesTask extends AsyncTask<ImageView, Void, Bitmap> {

        ImageView imageView = null;

        @Override
        protected Bitmap doInBackground(ImageView... imageViews) {
            this.imageView = imageViews[0];
            return download_Image((String) imageView.getTag());
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }

        private Bitmap download_Image(String url) {

            final DefaultHttpClient client = new DefaultHttpClient();
            final HttpGet getRequest = new HttpGet(url);
            try {
                HttpResponse response = client.execute(getRequest);

                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {

                    return null;
                }

                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = entity.getContent();
                        final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        return bitmap;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        entity.consumeContent();
                    }
                }
            } catch (Exception e) {

                getRequest.abort();
            }

            return null;
        }

    }

    private class RecentCallsListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (historyArray.size() == 0) {
                return 0;
            } else {
                return historyArray.size() + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            if (position == 0) {
                return null;
            } else {
                return historyArray.get(position - 1);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout layoutItem;
            if (convertView != null) {
                layoutItem = (LinearLayout) convertView;
            } else {
                LayoutInflater lf = LayoutInflater.from(parent.getContext());
                layoutItem = (LinearLayout) lf.inflate(R.layout.call_list_item, null);
            }

            TextView textViewTime = (TextView) layoutItem.findViewById(R.id.textViewTime);
            TextView textViewDirection = (TextView) layoutItem.findViewById(R.id.textViewDirection);
            TextView textViewDuration = (TextView) layoutItem.findViewById(R.id.textViewDuration);

            CallHistoryItem callItem = CallHistoryItem.historyArray.get(historyIndex);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

            if (position == 0) {
                SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date startDate = new Date();
                try {
                    startDate = dateFormat.parse(callItem.startDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String startDateString = dateFormat1.format(startDate);
                Date yesterdayDate = new Date(new Date().getTime() - (1000 * 60 * 60 * 24));
                if (dateFormat1.format(new Date()).equals(startDateString)) {
                    textViewTime.setText("Today");
                } else if (dateFormat1.format(yesterdayDate).equals(startDateString)) {
                    textViewTime.setText("Yesterday");
                } else {
                    textViewTime.setText(startDateString);
                }

                textViewDirection.setText("");
                textViewDuration.setText("");
            } else {
                DetailHistoryItem item = historyArray.get(position - 1);
                Date sdate = null;
                try {
                    sdate = dateFormat.parse(item.startdate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                textViewTime.setText(timeFormat.format(sdate));
                textViewDirection.setText(callItem.direction + " Call");
                textViewDuration.setText(item.intervals);
            }

            return layoutItem;
        }

    }

}
