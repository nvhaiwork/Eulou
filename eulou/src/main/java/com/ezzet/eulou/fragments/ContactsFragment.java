package com.ezzet.eulou.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.activities.BaseActivity;
import com.ezzet.eulou.activities.MainActivity;
import com.ezzet.eulou.adapters.ContactAdapter;
import com.ezzet.eulou.utilities.Utilities;
import com.ezzet.eulou.views.IndexableListView;

public class ContactsFragment extends Fragment
		implements
			OnQueryTextListener,
			OnCloseListener,
			View.OnClickListener {

	private SearchView mSearchView;
	private IndexableListView mListView;
	private ContactAdapter mContactAdapter;
	private ServiceBoundReceiver serviceBoundReceiver;
	private FriendsLoadedReceiver friendsLoadedReceiver;
	private ContactsLoadedReceiver contactsLoadedReceiver;
	private ImageView mContactArrow, mFacebookArrow, mTwitterArrow,
			mInstagramArrow, mContactBtn, mFacebookBtn, mTwitterBtn,
			mInstagramBtn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.activity_contacts, container, false);

		mListView = (IndexableListView) rootView
				.findViewById(R.id.contact_tab_listivew);
		mContactAdapter = new ContactAdapter(getActivity());
		mListView.setAdapter(mContactAdapter);
		mListView.setFastScrollEnabled(true);

		mSearchView = (SearchView) rootView
				.findViewById(R.id.contact_search_view);
		mFacebookArrow = (ImageView) rootView
				.findViewById(R.id.contacts_facebook_triangle);
		mContactArrow = (ImageView) rootView
				.findViewById(R.id.contacts_triangle);
		mTwitterArrow = (ImageView) rootView
				.findViewById(R.id.contacts_twitter_triangle);
		mInstagramArrow = (ImageView) rootView
				.findViewById(R.id.contacts_instagram_triangle);
		mContactBtn = (ImageView) rootView.findViewById(R.id.contacts_contact);
		mFacebookBtn = (ImageView) rootView
				.findViewById(R.id.contacts_facebook);
		mTwitterBtn = (ImageView) rootView.findViewById(R.id.contacts_twitter);
		mInstagramBtn = (ImageView) rootView
				.findViewById(R.id.contacts_instagram);

		int searchPlateId = mSearchView.getContext().getResources()
				.getIdentifier("android:id/search_plate", null, null);
		View searchPlate = mSearchView.findViewById(searchPlateId);
		searchPlate.setBackgroundResource(R.color.white);

		setSearchViewTextSize(true);
		mSearchView.setIconified(false);
		mSearchView.onActionViewExpanded();
		mSearchView.clearFocus();
		friendsLoadedReceiver = new FriendsLoadedReceiver();
		IntentFilter filter = new IntentFilter(
				"com.ezzet.eulou.action.FRIENDSLOADED");
		getActivity().registerReceiver(friendsLoadedReceiver, filter);

		contactsLoadedReceiver = new ContactsLoadedReceiver();
		IntentFilter filterContacts = new IntentFilter(
				"com.ezzet.eulou.action.CONTACTSLOADED");
		getActivity().registerReceiver(contactsLoadedReceiver, filterContacts);

		serviceBoundReceiver = new ServiceBoundReceiver();
		filter = new IntentFilter("com.ezzet.eulou.action.SERVICEBOUND");
		getActivity().registerReceiver(serviceBoundReceiver, filter);

		mFacebookBtn.setSelected(true);
		mContactBtn.setOnClickListener(this);
		mFacebookBtn.setOnClickListener(this);
		mTwitterBtn.setOnClickListener(this);
		mInstagramBtn.setOnClickListener(this);
		mSearchView.setOnCloseListener(this);
		mSearchView.setOnQueryTextListener(this);
		setSelectedTriangle(mFacebookBtn.getId());
		return rootView;
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(friendsLoadedReceiver);
		getActivity().unregisterReceiver(contactsLoadedReceiver);
		getActivity().unregisterReceiver(serviceBoundReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onClose() {
		// TODO Auto-generated method stub

		setSearchViewTextSize(true);
		mContactAdapter.getFilter().filter("");
		return false;
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		// TODO Auto-generated method stub

		setSearchViewTextSize(arg0.equals(""));
		mContactAdapter.getFilter().filter(arg0);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		// TODO Auto-generated method stub

		mContactAdapter.getFilter().filter(arg0);
		return false;
	}

	private void setSearchViewTextSize(boolean isHint) {

		int id = mSearchView.getContext().getResources()
				.getIdentifier("android:id/search_src_text", null, null);
		TextView textView = (TextView) mSearchView.findViewById(id);
		textView.setGravity(Gravity.CENTER_VERTICAL);
		if (isHint) {

			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
			return;
		}

		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {

			case R.id.contacts_contact :
			case R.id.contacts_facebook :
			case R.id.contacts_twitter :
			case R.id.contacts_instagram :

				tabSelect(view);
				break;
		}
	}

	private void tabSelect(View view) {

		switch (view.getId()) {

			case R.id.contacts_contact :

				if (!mContactBtn.isSelected()) {

					view.setSelected(true);
					reloadData();
				}

				break;

			case R.id.contacts_facebook :

				if (!mFacebookBtn.isSelected()) {

					view.setSelected(true);
					reloadData();
				}

				break;
			case R.id.contacts_twitter :

				view.setSelected(true);
				mListView.setAdapter(null);
				break;
			case R.id.contacts_instagram :

				view.setSelected(true);
				mListView.setAdapter(null);
				break;
		}

		mContactBtn.setSelected(false);
		mFacebookBtn.setSelected(false);
		mTwitterBtn.setSelected(false);
		mInstagramBtn.setSelected(false);
		view.setSelected(true);
		setSelectedTriangle(view.getId());
		mSearchView.setQuery("", false);
		Utilities
				.doHideKeyboard(getActivity(), getActivity().getCurrentFocus());
	}

	private class FriendsLoadedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			reloadData();
		}
	}

	private class ContactsLoadedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			reloadData();
		}
	}

	private class ServiceBoundReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			MainActivity activity = (MainActivity) getActivity();
			if (BaseActivity.mFriendUsers.size() == 0) {

				activity.service.requestFriendsFacebook();
			} else {

				reloadData();
			}

			if (BaseActivity.mContactUsers.size() == 0) {

				activity.service.requestUsersFromContacts();
			} else {

				reloadData();
			}
		}
	}

	private void setSelectedTriangle(int selectedId) {

		mFacebookArrow.setSelected(false);
		mContactArrow.setSelected(false);
		mTwitterArrow.setSelected(false);
		mInstagramArrow.setSelected(false);
		switch (selectedId) {

			case R.id.contacts_contact :

				mContactArrow.setSelected(true);
				mFacebookBtn.setSelected(false);
				break;

			case R.id.contacts_facebook :

				mContactArrow.setSelected(false);
				mFacebookArrow.setSelected(true);
				break;

			case R.id.contacts_twitter :

				mTwitterArrow.setSelected(true);
				break;

			case R.id.contacts_instagram :

				mInstagramArrow.setSelected(true);
				break;
		}
	}

	/**
	 * Reload list data
	 */
	private void reloadData() {

		mContactAdapter = new ContactAdapter(getActivity());
		mListView.setAdapter(mContactAdapter);
		mListView.setFastScrollEnabled(true);
		List<UserInfo> listData = new ArrayList<>();
		if (mFacebookBtn.isSelected()) {

			for (UserInfo user : BaseActivity.mFriendUsers) {

				listData.add(user);
			}
		} else {

			for (UserInfo user : BaseActivity.mContactUsers) {

				listData.add(user);
			}
		}

		mContactAdapter.setData(listData);
		mContactAdapter.notifyDataSetChanged();
	}
}
