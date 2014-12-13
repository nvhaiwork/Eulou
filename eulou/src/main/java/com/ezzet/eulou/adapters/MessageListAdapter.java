package com.ezzet.eulou.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import com.ezzet.eulou.R;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.utilities.Utilities;
import com.ezzet.eulou.views.FBProfilePictureView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageListAdapter extends BaseAdapter {

    private MessagesFilter mFilters;
    private LayoutInflater mInflater;
    private SimpleDateFormat mDateFormat;
    private Map<String, Map<String, Object>> mMessages, mOrgMessages;

    public MessageListAdapter(Context context) {

        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub

        if (mMessages != null) {

            return mMessages.size();
        }

        return 0;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> getItem(int arg0) {
        // TODO Auto-generated method stub

        String key = (String) (new ArrayList(mMessages.keySet())).get(arg0);
        return mMessages.get(key);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parrent) {
        // TODO Auto-generated method stub

        Map<String, Object> message = (Map<String, Object>) getItem(pos);
        ViewHolder holder = null;
        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.layout_message_list_item, parrent, false);
            holder = new ViewHolder();
            holder.profilePic = (FBProfilePictureView) convertView.findViewById(R.id.message_user_profile_pic);
            holder.username = (TextView) convertView.findViewById(R.id.message_username);
            holder.message = (TextView) convertView.findViewById(R.id.message_text);
            holder.time = (TextView) convertView.findViewById(R.id.message_time);
            holder.userType = (ImageView) convertView.findViewById(R.id.message_user_type);
            holder.messageCount = (TextView) convertView.findViewById(R.id.message_unread_count);
            convertView.setTag(holder);
        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        UserInfo user = (UserInfo) message.get("UserInfo");
        String messageTime = "";
        try {

            mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date startDate = mDateFormat.parse((String) message.get("LastTime"));
            mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            messageTime = mDateFormat.format(startDate);
        } catch (ParseException e) {

            messageTime = (String) message.get("LastTime");
        }

        holder.time.setText(messageTime.replace(" ", "\n"));
        holder.message.setText(((String) message.get("LastMessage")));
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

            holder.userType.setImageResource(R.drawable.ic_instargram_list_item);
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
            // TODO Auto-generated method stub

            FilterResults result = new FilterResults();
            if (constraint == null || constraint.length() == 0) {

                result.values = mOrgMessages;
                result.count = mOrgMessages.size();
            } else {

                Map<String, Map<String, Object>> messages = new HashMap<String, Map<String, Object>>();
                Iterator<Entry<String, Map<String, Object>>> it = mOrgMessages.entrySet().iterator();
                while (it.hasNext()) {

                    Map.Entry<String, Map<String, Object>> message = it.next();
                    UserInfo user = (UserInfo) message.getValue().get("UserInfo");
                    if (user.getUserName().toLowerCase().contains(constraint.toString().toLowerCase())) {

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
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // TODO Auto-generated method stub

            mMessages = (Map<String, Map<String, Object>>) results.values;
            notifyDataSetChanged();
        }
    }

}
