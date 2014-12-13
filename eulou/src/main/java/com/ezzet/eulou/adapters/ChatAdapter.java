package com.ezzet.eulou.adapters;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.ezzet.eulou.R;
import com.ezzet.eulou.models.ChatBubbleMessageModel;
import com.ezzet.eulou.utilities.LogUtil;
import com.ezzet.eulou.utilities.Utilities;
import com.ezzet.eulou.views.FBProfilePictureView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter {

    private final int MESSAGE_INTERVAL = 120 * 1000;
    private LayoutInflater mInflater;
    private SimpleDateFormat mDateFormat;
    private List<ChatBubbleMessageModel> mChatMessages;

    public ChatAdapter(Context context) {
        // TODO Auto-generated constructor stub

        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDateFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy");
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if (mChatMessages != null) {

            return mChatMessages.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mChatMessages.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        ChatBubbleMessageModel message = (ChatBubbleMessageModel) getItem(pos);
        ViewHolder holder = null;
        if (convertView == null) {

            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.layout_chat_item, parent, false);
            holder.leftImage = (FBProfilePictureView) convertView.findViewById(R.id.chat_left_img);
            holder.leftMsg = (TextView) convertView.findViewById(R.id.chat_left_message);
            holder.rightMsg = (TextView) convertView.findViewById(R.id.chat_right_message);
            holder.messageTime = (TextView) convertView.findViewById(R.id.chat_message_time);
            holder.seenText = (TextView) convertView.findViewById(R.id.chat_item_seen_message);
            convertView.setTag(holder);
        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        if (pos == 0) {

            holder.messageTime.setText(mDateFormat.format(message.getMessageDate()));
            holder.messageTime.setVisibility(View.VISIBLE);
        } else {

            Date curMessageDate = message.getMessageDate();
            Date previousMsgDate = ((ChatBubbleMessageModel) getItem(pos - 1)).getMessageDate();
            if ((curMessageDate.getTime() - previousMsgDate.getTime()) > MESSAGE_INTERVAL) {

                holder.messageTime.setText(mDateFormat.format(message.getMessageDate()));
                holder.messageTime.setVisibility(View.VISIBLE);
            } else {

                holder.messageTime.setVisibility(View.GONE);
            }
        }

        if ((pos == getCount() - 1) && message.isFromMy() && message.getSeenTime() != null) {

            holder.seenText.setVisibility(View.VISIBLE);
            String seenTime = "Seen ";
            if (Utilities.isToday(message.getSeenTime())) {

                SimpleDateFormat seenDateFormat = new SimpleDateFormat("HH:mm");
                seenTime += seenDateFormat.format(message.getSeenTime());
            } else {

                SimpleDateFormat seenDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                seenTime += seenDateFormat.format(message.getSeenTime());
            }

            LogUtil.e("Seeeeeennnnnnnn", seenTime);
            holder.seenText.setText(seenTime);
        } else {

            holder.seenText.setVisibility(View.GONE);
        }

        holder.leftImage.setProfileId(message.getProfileImg());

        if (message.isFromMy()) {

            holder.leftMsg.setVisibility(View.GONE);
            holder.leftImage.setVisibility(View.GONE);
            holder.rightMsg.setVisibility(View.VISIBLE);
            holder.rightMsg.setText(message.getMessage());
        } else {

            holder.rightMsg.setVisibility(View.GONE);
            holder.leftMsg.setVisibility(View.VISIBLE);
            holder.leftImage.setVisibility(View.VISIBLE);
            holder.leftMsg.setText(message.getMessage());
        }

        return convertView;
    }

    public void setData(List<ChatBubbleMessageModel> chatMessages) {

        this.mChatMessages = chatMessages;
        Collections.sort(mChatMessages, new Comparator<ChatBubbleMessageModel>() {

                    @Override
                    public int compare(ChatBubbleMessageModel lhs, ChatBubbleMessageModel rhs) {
                        // TODO Auto-generated method stub
                        if (lhs == null || rhs == null) {

                            return 0;
                        }

                        return lhs.getMessageDate().compareTo(rhs.getMessageDate());
                    }
                });
    }

    private class ViewHolder {

        FBProfilePictureView leftImage;
        TextView leftMsg, rightMsg, messageTime, seenText;
    }
}
