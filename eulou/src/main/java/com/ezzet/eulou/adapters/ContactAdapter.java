package com.ezzet.eulou.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.activities.ChatActivity;
import com.ezzet.eulou.activities.FriendProfileActivity;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.models.ContactItem;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.utilities.StringMatcher;
import com.ezzet.eulou.views.FBProfilePictureView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactAdapter extends ArrayAdapter<ContactItem>
		implements
			SectionIndexer {

	private Context mContext;
	private ContactFiler mFilter;
	private List<ContactItem> mData;
	private List<UserInfo> mUserInfo;
	private LayoutInflater mLayoutInflater;
	private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public ContactAdapter(Context context) {
		super(context, 0);
		this.mContext = context;
		this.mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {

		if (mData != null) {

			return mData.size();
		}

		return 0;
	}

	@Override
	public ContactItem getItem(int position) {

		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		SectionItemHolder sectionItemHolder;
		final ContactItem item = getItem(position);

		if (convertView == null) {
			sectionItemHolder = new SectionItemHolder();

			convertView = mLayoutInflater.inflate(
					R.layout.layout_friend_list_item, parent, false);
			sectionItemHolder.userImg = (FBProfilePictureView) convertView
					.findViewById(R.id.friend_profile);
			sectionItemHolder.userName = (TextView) convertView
					.findViewById(R.id.friend_username);
			sectionItemHolder.header = (TextView) convertView
					.findViewById(R.id.friend_item_header);
			sectionItemHolder.itemLy = (RelativeLayout) convertView
					.findViewById(R.id.friend_item_layout);
			sectionItemHolder.callBtn = (ImageView) convertView
					.findViewById(R.id.friend_call_btn);
			sectionItemHolder.messageBtn = (ImageView) convertView
					.findViewById(R.id.friend_message_conversation_btn);
			convertView.setTag(R.layout.layout_friend_list_item,
					sectionItemHolder);

		} else {

			sectionItemHolder = (SectionItemHolder) convertView
					.getTag(R.layout.layout_friend_list_item);
		}

		if (item.isHeader()) {

			sectionItemHolder.header.setVisibility(View.VISIBLE);
			sectionItemHolder.header.setText(item.getHeaderText());
		} else {

			sectionItemHolder.header.setVisibility(View.GONE);
		}

		sectionItemHolder.userName.setText(item.getUserInfo().getUserName());
		sectionItemHolder.userImg.setProfileId(item.getUserInfo()
				.getFacebookID());
		sectionItemHolder.itemLy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				UserInfo user = item.getUserInfo();
				Intent friendProfileIntent = new Intent(mContext,
						FriendProfileActivity.class);
				friendProfileIntent.putExtra(
						Constants.INTENT_FRIEND_PROFILE_USER_INFO, user);
				mContext.startActivity(friendProfileIntent);
			}
		});

		sectionItemHolder.callBtn
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {

						UserInfo userInfo = item.getUserInfo();
						int mainSocial = userInfo.getMainSocial();
						String userIDString;
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
						mContext.sendBroadcast(intent);
					}
				});

		sectionItemHolder.messageBtn
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {

						UserInfo userInfo = item.getUserInfo();
						Intent chatIntent = new Intent(mContext,
								ChatActivity.class);
						chatIntent.putExtra(Constants.INTENT_MESAGE_USER_ID,
								userInfo);
						mContext.startActivity(chatIntent);
					}
				});

		return convertView;
	}

	public void setData(List<UserInfo> userInfo) {

		Collections.sort(userInfo, new Comparator<UserInfo>() {
			@Override
			public int compare(UserInfo userInfo, UserInfo userInfo2) {

				return userInfo.getUserName()
						.compareTo(userInfo2.getUserName());
			}
		});

		this.mUserInfo = userInfo;
		processData(userInfo);
	}

	private void processData(List<UserInfo> userInfo) {

		List<ContactItem> items = new ArrayList<ContactItem>();
		if (userInfo != null && userInfo.size() > 0) {

			String headerText = "";
			for (UserInfo user : userInfo) {

				ContactItem item = new ContactItem();
				item.setUserInfo(user);
				String startChar = String.valueOf(user.getUserName().charAt(0));
				if (!startChar.equalsIgnoreCase(headerText)) {

					headerText = startChar;
					item.setHeaderText(headerText);
					item.setHeader(true);
				} else {

					item.setHeader(false);
				}

				items.add(item);
			}
		}

		this.mData = items;
	}

	@Override
	public Object[] getSections() {

		String[] sections = new String[mSections.length()];
		for (int i = 0; i < mSections.length(); i++)
			sections[i] = String.valueOf(mSections.charAt(i));
		return sections;
	}

	@Override
	public int getPositionForSection(int section) {

		for (int i = section; i >= 0; i--) {
			for (int j = 0; j < getCount(); j++) {
				if (i == 0) {
					// For numeric section
					for (int k = 0; k <= 9; k++) {
						if (StringMatcher.match(
								String.valueOf(getItem(j).getUserInfo()
										.getUserName().charAt(0)),
								String.valueOf(k)))
							return j;
					}
				} else {
					if (StringMatcher.match(
							String.valueOf(getItem(j).getUserInfo()
									.getUserName().charAt(0)),
							String.valueOf(mSections.charAt(i))))
						return j;
				}
			}
		}

		return 0;
	}

	public Filter getFilter() {

		if (mFilter == null) {

			mFilter = new ContactFiler();
		}

		return mFilter;
	}

	@Override
	public int getSectionForPosition(int i) {
		return 0;
	}

	private class SectionItemHolder {

		TextView userName, header;
		RelativeLayout itemLy;
		FBProfilePictureView userImg;
		ImageView callBtn, messageBtn;
	}

	private class ContactFiler extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence charSequence) {

			FilterResults result = new FilterResults();
			if (charSequence == null || charSequence.length() == 0) {

				processData(mUserInfo);
			} else {

				List<UserInfo> tempUser = new ArrayList<UserInfo>();
				for (UserInfo userInfo : mUserInfo) {

					if (userInfo.getUserName().toLowerCase()
							.contains(charSequence.toString().toLowerCase())) {

						tempUser.add(userInfo);
					}
				}

				processData(tempUser);
			}

			result.count = mData.size();
			result.values = mData;
			return result;
		}

		@Override
		protected void publishResults(CharSequence charSequence,
				FilterResults filterResults) {

			notifyDataSetChanged();
		}
	}
}
