package com.ezzet.eulou.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import com.ezzet.eulou.R;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.activities.BaseActivity;
import com.ezzet.eulou.activities.MainActivity;
import com.ezzet.eulou.adapters.ContactAdapter;
import com.ezzet.eulou.utilities.Utilities;
import com.ezzet.eulou.views.IndexableListView;
import com.facebook.android.Util;

public class ContactsFragment extends Fragment
		implements
			View.OnClickListener,
			OnQueryTextListener,
			OnCloseListener {

	private SearchView mSearchView;
	private IndexableListView mListView;
	private RadioGroup mTabTriangleGroup;
	private ContactAdapter mContactAdapter;
	private ServiceBoundReceiver serviceBoundReceiver;
	private FriendsLoadedReceiver friendsLoadedReceiver;
	private ContactsLoadedReceiver contactsLoadedReceiver;
	private ImageView mContactBtn, mFacebookBtn, mTwitterBtn, mInstagramBtn;

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
		mFacebookBtn = (ImageView) rootView
				.findViewById(R.id.contacts_facebook);
		mInstagramBtn = (ImageView) rootView
				.findViewById(R.id.contacts_instagram);
		mTabTriangleGroup = (RadioGroup) rootView
				.findViewById(R.id.contacts_tab_triangles);
		mContactBtn = (ImageView) rootView.findViewById(R.id.contacts_contact);
		mTwitterBtn = (ImageView) rootView.findViewById(R.id.contacts_twitter);

		int searchPlateId = mSearchView.getContext().getResources()
				.getIdentifier("android:id/search_plate", null, null);
		View searchPlate = mSearchView.findViewById(searchPlateId);
		searchPlate.setBackgroundResource(R.color.light_gray);
		mSearchView.setIconified(false);
		mSearchView.onActionViewExpanded();
		mSearchView.clearFocus();
		onButtonFacebook();
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

		mSearchView.setOnCloseListener(this);
		mContactBtn.setOnClickListener(this);
		mTwitterBtn.setOnClickListener(this);
		mFacebookBtn.setOnClickListener(this);
		mInstagramBtn.setOnClickListener(this);
		mSearchView.setOnQueryTextListener(this);
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
	public void onClick(View v) {

		switch (v.getId()) {

			case R.id.contacts_contact :

				// Clear query
				mSearchView.setQuery("", false);
				// Collapse the action view
				Utilities.doHideKeyboard(getActivity(), getActivity()
						.getCurrentFocus());
				onButtonContact();
				break;
			case R.id.contacts_facebook :

				// Clear query
				mSearchView.setQuery("", false);
				// Collapse the action view
				Utilities.doHideKeyboard(getActivity(), getActivity()
						.getCurrentFocus());
				onButtonFacebook();
				break;
			case R.id.contacts_twitter :

				Utilities.doHideKeyboard(getActivity(), getActivity()
						.getCurrentFocus());
				onButtonTwitter();
				break;
			case R.id.contacts_instagram :

				Utilities.doHideKeyboard(getActivity(), getActivity()
						.getCurrentFocus());
				onButtonInstargram();
				break;
		}
	}
	@Override
	public boolean onClose() {
		// TODO Auto-generated method stub

		mContactAdapter.getFilter().filter("");
		return false;
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		// TODO Auto-generated method stub

		mContactAdapter.getFilter().filter(arg0);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		// TODO Auto-generated method stub
		mContactAdapter.getFilter().filter(arg0);
		return false;
	}

	private void onButtonContact() {

		mTabTriangleGroup.check(R.id.contacts_tab_contact_triangle);
		reloadData();
	}

	private void onButtonFacebook() {

		mTabTriangleGroup.check(R.id.contacts_tab_facebook_triangle);
		reloadData();
	}

	private void onButtonTwitter() {

		mTabTriangleGroup.check(R.id.contacts_tab_twitter_triangle);
		mListView.setAdapter(null);
	}

	private void onButtonInstargram() {

		mTabTriangleGroup.check(R.id.contacts_tab_instagram_triangle);
		mListView.setAdapter(null);
	}

	@SuppressWarnings("unused")
	private ArrayList<UserInfo> filterFriendsList(String strFilter) {

		ArrayList<UserInfo> filteredList = new ArrayList<UserInfo>();
		MainActivity activity = (MainActivity) getActivity();
		for (int i = 0; i < BaseActivity.mFriendUsers.size(); i++) {

			UserInfo userInfo = BaseActivity.mFriendUsers.get(i);
			if (userInfo.getUserName().toLowerCase(Locale.US)
					.contains(strFilter.toLowerCase(Locale.US))) {

				filteredList.add(userInfo);
			}
		}

		return filteredList;
	}

	@SuppressWarnings("unused")
	private ArrayList<UserInfo> filterContactList(String strFilter) {

		ArrayList<UserInfo> filteredList = new ArrayList<UserInfo>();
		MainActivity activity = (MainActivity) getActivity();
		for (int i = 0; i < BaseActivity.mContactUsers.size(); i++) {

			UserInfo userInfo = BaseActivity.mContactUsers.get(i);
			if (userInfo.getUserName().toLowerCase(Locale.US)
					.contains(strFilter.toLowerCase(Locale.US))) {

				filteredList.add(userInfo);
			}
		}

		return filteredList;
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

	/**
	 * Reload list data
	 * */
	private void reloadData() {

		mContactAdapter = new ContactAdapter(getActivity());
		mListView.setAdapter(mContactAdapter);
		mListView.setFastScrollEnabled(true);
		List<UserInfo> listData = new ArrayList<UserInfo>();
		if (mTabTriangleGroup.getCheckedRadioButtonId() == R.id.contacts_tab_facebook_triangle) {

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
