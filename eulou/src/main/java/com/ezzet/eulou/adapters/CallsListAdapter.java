package com.ezzet.eulou.adapters;

import com.ezzet.eulou.activities.CallDetailActivity;
import com.ezzet.eulou.R;
import com.ezzet.eulou.models.CallHistoryItem;
import com.ezzet.eulou.utilities.Utilities;
import com.ezzet.eulou.views.FBProfilePictureView;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CallsListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;

	public CallsListAdapter(Context context) {
		// TODO Auto-generated constructor stub

		this.mContext = context;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return CallHistoryItem.historyArray.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub

		ViewHolder holder = null;
		if (convertView == null) {

			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.recent_calls_list_item,
					arg2, false);
			holder.profilePicture = (FBProfilePictureView) convertView
					.findViewById(R.id.profilePictureView);
			holder.userName = (TextView) convertView
					.findViewById(R.id.textViewName);
			holder.callTypeIcon = (ImageView) convertView
					.findViewById(R.id.imageViewCallType);
			holder.callType = (TextView) convertView
					.findViewById(R.id.textViewCallType);
			holder.callTime = (TextView) convertView
					.findViewById(R.id.textViewTime);
			holder.detailBtn = (ImageView) convertView
					.findViewById(R.id.buttonDetail);
			convertView.setTag(holder);
		} else {

			holder = (ViewHolder) convertView.getTag();
		}

		holder.detailBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				int selpos = ((Integer) v.getTag()).intValue();
				Intent intent = new Intent(mContext, CallDetailActivity.class);
				intent.putExtra("HistoryIndex", selpos);
				mContext.startActivity(intent);
			}
		});

		holder.detailBtn.setTag(Integer.valueOf(pos));
		CallHistoryItem item = CallHistoryItem.historyArray.get(pos);
		holder.profilePicture.setProfileId(item.userInfo.getFacebookID());
		holder.userName.setText(item.contact);
		holder.callTime.setText(Utilities.calcMessageTime(item.startDate));
		if (item.direction.equalsIgnoreCase("outgoing")) {
			holder.callTypeIcon.setImageResource(R.drawable.icon_called);
			holder.callType.setText(String.format("Outgoing (%s)", item.count));
			holder.callType.setTextColor(mContext.getResources().getColor(
					R.color.dark_gray));
		} else if (item.direction.equalsIgnoreCase("incoming")) {
			holder.callTypeIcon.setImageResource(R.drawable.icon_incoming);
			holder.callType.setText(String.format("Incoming (%s)", item.count));
			holder.callType.setTextColor(mContext.getResources().getColor(
					R.color.dark_gray));
		} else {
			holder.callTypeIcon.setImageResource(R.drawable.icon_missedcall);
			holder.callType.setText(String.format("Missed call (%s)",
					item.count));
			holder.callType.setTextColor(mContext.getResources().getColor(
					R.color.red));
		}

		return convertView;
	}

	private class ViewHolder {

		FBProfilePictureView profilePicture;
		TextView userName, callType, callTime;
		ImageView callTypeIcon, detailBtn;
	}

}
