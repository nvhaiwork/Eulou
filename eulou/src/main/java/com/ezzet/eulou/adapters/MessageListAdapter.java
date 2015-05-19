package com.ezzet.eulou.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.utilities.LogUtil;
import com.ezzet.eulou.utilities.Utilities;
import com.ezzet.eulou.views.FBProfilePictureView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

public class MessageListAdapter extends BaseAdapter {

    private MessagesFilter mFilters;
    private LayoutInflater mInflater;
    private SimpleDateFormat mInputDateFormat;
    private SimpleDateFormat mOutputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    private Map<String, Map<String, Object>> mMessages, mOrgMessages;
    private int mCurrentWeek;
    private int mCurrentDayOfWeek;
    private static String[] sDayOfWeek;
    private static String sYesterday;
    private static String sToday;
    private static int sHighLightedColor;
    private static int sNormalColor;

    public MessageListAdapter(Context context) {

        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        mInputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar c = Calendar.getInstance();
        mCurrentWeek = c.get(Calendar.WEEK_OF_YEAR);
        mCurrentDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        sDayOfWeek = context.getResources().getStringArray(R.array.day_of_week);
        sYesterday = context.getString(R.string.str_yesterday);
        sToday = context.getString(R.string.str_today);
        sHighLightedColor = context.getResources().getColor(R.color.date_time_highlighted);
        sNormalColor = context.getResources().getColor(R.color.date_time_normal);
    }

    @Override
    public int getCount() {
        if (mMessages != null) {

            return mMessages.size();
        }

        return 0;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> getItem(int arg0) {
        String key = (String) (new ArrayList(mMessages.keySet())).get(arg0);
        return mMessages.get(key);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parrent) {

        Map<String, Object> message = (Map<String, Object>) getItem(pos);
        ViewHolder holder = null;
        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.layout_message_list_item,
                    parrent, false);
            holder = new ViewHolder();
            holder.profilePic = (FBProfilePictureView) convertView
                    .findViewById(R.id.message_user_profile_pic);
            holder.username = (TextView) convertView
                    .findViewById(R.id.message_username);
            holder.message = (TextView) convertView
                    .findViewById(R.id.message_text);
            holder.time = (TextView) convertView
                    .findViewById(R.id.message_time);
            holder.userType = (ImageView) convertView
                    .findViewById(R.id.message_user_type);
            holder.messageCount = (TextView) convertView
                    .findViewById(R.id.message_unread_count);
            convertView.setTag(holder);
        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        UserInfo user = (UserInfo) message.get("UserInfo");
        Log.v("TAG", (String) message.get("LastTime"));

        holder.time.setText(" ");
        holder.time.setTextColor(sNormalColor);
        try {
            Date startDate = mInputDateFormat
                    .parse((String) message.get("LastTime"));
            OutputTime ot = formatTime(startDate);
            holder.time.setText(ot.time);
            if (ot.highlighted) {
                holder.time.setTextColor(sHighLightedColor);
            }
        } catch (ParseException e) {
        }


        holder.message.setText(((String) message.get("LastMessage")));

        // Log for crashes
        LogUtil.e("Message history list all data: ", mMessages.toString());
        LogUtil.e("Message history list current data: ", message.toString());
        holder.username.setText(user.getUserName());
        holder.userType.setVisibility(View.VISIBLE);

        int receivedMsg = 0;
        int shownMsg = 0;
        if (message.containsKey("ReceivedMsgCount")) {

            receivedMsg = (Integer) message.get("ReceivedMsgCount");
        }

        if (message.containsKey("ShownMsgCount")) {
            shownMsg = (Integer) message.get("ShownMsgCount");
        }

        int messageCount = receivedMsg - shownMsg;
        if (messageCount > 0) {

            holder.messageCount.setText(messageCount + "");
            holder.messageCount.setVisibility(View.VISIBLE);
        } else {

            holder.messageCount.setVisibility(View.INVISIBLE);
        }

        if (!user.getFacebookID().equals("")) {
            holder.profilePic.setProfileId(user.getFacebookID());
            holder.userType.setImageResource(R.drawable.ic_facebook_list_item);
        } else if (!user.getTwitterID().equals("")) {

            holder.userType.setImageResource(R.drawable.ic_twitter_list_item);
        } else if (!user.getTwitterID().equals("")) {

            holder.userType
                    .setImageResource(R.drawable.ic_instargram_list_item);
        } else {

            holder.userType.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public void setData(Map<String, Map<String, Object>> messages) {

        this.mMessages = messages;
        this.mOrgMessages = messages;
    }

    /**
     * Return message filter
     */
    public Filter getFilter() {

        if (mFilters == null) {

            mFilters = new MessagesFilter();
        }

        return mFilters;
    }

    private class ViewHolder {

        ImageView userType;
        FBProfilePictureView profilePic;
        TextView username, message, time, messageCount;
    }

    private class MessagesFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults result = new FilterResults();
            if (constraint == null || constraint.length() == 0) {

                result.values = mOrgMessages;
                result.count = mOrgMessages.size();
            } else {

                Map<String, Map<String, Object>> messages = new HashMap<String, Map<String, Object>>();
                Iterator<Entry<String, Map<String, Object>>> it = mOrgMessages
                        .entrySet().iterator();
                while (it.hasNext()) {

                    Map.Entry<String, Map<String, Object>> message = it.next();
                    UserInfo user = (UserInfo) message.getValue().get(
                            "UserInfo");
                    if (user.getUserName().toLowerCase()
                            .contains(constraint.toString().toLowerCase())) {

                        messages.put(message.getKey(), message.getValue());
                    }
                }

                result.values = Utilities.sortMessage(messages);
                result.count = messages.size();
            }

            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {

            mMessages = (Map<String, Map<String, Object>>) results.values;
            notifyDataSetChanged();
        }
    }

    private OutputTime formatTime(Date sentDate) {
        OutputTime out = new OutputTime();
        String time;
        boolean highlighted = false;
        Calendar c = Calendar.getInstance();
        c.setTime(sentDate);
        int cweek = c.get(Calendar.WEEK_OF_YEAR);
        if (cweek == mCurrentWeek) {
            // The same week.
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            Log.v("TAG", "DOW: " + dayOfWeek + "   " + mCurrentDayOfWeek);
            if (dayOfWeek == mCurrentDayOfWeek - 1) {
                time = sYesterday;
                highlighted = true;
            } else if (dayOfWeek == mCurrentDayOfWeek) {
                time = sToday;
                highlighted = true;
            } else {
                time = sDayOfWeek[dayOfWeek - 1];
            }
        } else {
            time = mOutputDateFormat.format(sentDate);
        }

        out.time = time;
        out.highlighted = highlighted;

        return out;
    }

    private class OutputTime {
        String time;
        boolean highlighted;
    }
}
