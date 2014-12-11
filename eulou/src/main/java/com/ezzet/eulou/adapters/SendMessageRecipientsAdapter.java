package com.ezzet.eulou.adapters;

import java.util.ArrayList;
import java.util.List;

import com.ezzet.eulou.R;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.views.FBProfilePictureView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

public class SendMessageRecipientsAdapter extends BaseAdapter {

	private UserFitler mFilter;
	private LayoutInflater mInflater;
	private List<UserInfo> mFriends, mOrgFriends;

	public SendMessageRecipientsAdapter(Context context, List<UserInfo> friends) {

		this.mFriends = friends;
		this.mOrgFriends = friends;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub

		if (mFriends != null) {

			return mFriends.size();
		}

		return 0;
	}

	/**
	 * Return message filter
	 * */
	public Filter getFilter() {

		if (mFilter == null) {

			mFilter = new UserFitler();
		}

		return mFilter;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mFriends.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parrent) {
		// TODO Auto-generated method stub

		UserInfo user = (UserInfo) getItem(pos);
		ViewHolder holder = null;
		if (convertView == null) {

			holder = new ViewHolder();
			convertView = mInflater.inflate(
					R.layout.layout_send_message_friend_list_item, parrent,
					false);

			holder.profilePic = (FBProfilePictureView) convertView
					.findViewById(R.id.send_message_friend_image);
			holder.username = (TextView) convertView
					.findViewById(R.id.send_message_friend_name);
			holder.userType = (ImageView) convertView
					.findViewById(R.id.send_message_friend_type);
			convertView.setTag(holder);
		} else {

			holder = (ViewHolder) convertView.getTag();
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

		holder.username.setText(user.getUserName());
		return convertView;
	}

	private class ViewHolder {

		TextView username;
		ImageView userType;
		FBProfilePictureView profilePic;
	}

	private class UserFitler extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			// TODO Auto-generated method stub
			FilterResults result = new FilterResults();
			if (constraint == null || constraint.length() == 0) {

				result.values = mOrgFriends;
				result.count = mOrgFriends.size();
			} else {

				List<UserInfo> friends = new ArrayList<UserInfo>();
				for (UserInfo user : mOrgFriends) {

					if (user.getUserName().toLowerCase()
							.contains(constraint.toString().toLowerCase())) {

						friends.add(user);
					}
				}

				result.values = friends;
				result.count = friends.size();
			}

			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence arg0, FilterResults results) {
			// TODO Auto-generated method stub

			mFriends = (List<UserInfo>) results.values;
			notifyDataSetChanged();
		}
	}
}
