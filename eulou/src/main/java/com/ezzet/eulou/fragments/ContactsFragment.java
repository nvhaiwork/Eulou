package com.ezzet.eulou.fragments;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.ezzet.eulou.activities.BaseActivity;
import com.ezzet.eulou.activities.ChatActivity;
import com.ezzet.eulou.activities.MainActivity;
import com.ezzet.eulou.R;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.models.UserInfo;
import com.ezzet.eulou.views.FBProfilePictureView;

public class ContactsFragment extends Fragment implements View.OnClickListener, OnQueryTextListener, OnCloseListener {

    private ArrayList<UserInfo> friendsFilterArray = null;
    private ArrayList<UserInfo> contactsFilterArray = null;
    OnClickListener callClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.friend_call_btn:

                    MainActivity activity = (MainActivity) getActivity();
                    int position = ((Integer) (v.getTag())).intValue();
                    UserInfo userInfo;
                    if (contactsFilterArray == null) {

                        userInfo = BaseActivity.mContactUsers.get(position);
                    } else {

                        userInfo = contactsFilterArray.get(position);
                    }

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
                    activity.sendBroadcast(intent);
                    break;
                case R.id.friend_message_conversation_btn:

                    Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                    startActivity(chatIntent);
                    break;
            }

        }
    };
    private FriendsListAdapter mFriendsAdapter;
    private ListView mListview;
    private SearchView mSearchView;
    private RadioGroup mTabTriangleGroup;
    private ImageView mContactBtn, mFacebookBtn, mTwitterBtn, mInstargramBtn;
    private ServiceBoundReceiver serviceBoundReceiver;
    private FriendsLoadedReceiver friendsLoadedReceiver;
    private ContactsLoadedReceiver contactsLoadedReceiver;
    private ContactListAdapter mContactsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.activity_contacts, container, false);

        mListview = (ListView) rootView.findViewById(R.id.contact_tab_listivew);
        mFriendsAdapter = new FriendsListAdapter();
        mListview.setAdapter(mFriendsAdapter);

        mContactsAdapter = new ContactListAdapter();
        mSearchView = (SearchView) rootView.findViewById(R.id.contact_search_view);
        mFacebookBtn = (ImageView) rootView.findViewById(R.id.contacts_facebook);
        mInstargramBtn = (ImageView) rootView.findViewById(R.id.contacts_instagram);
        mTabTriangleGroup = (RadioGroup) rootView.findViewById(R.id.contacts_tab_triangles);
        mContactBtn = (ImageView) rootView.findViewById(R.id.contacts_contact);
        mTwitterBtn = (ImageView) rootView.findViewById(R.id.contacts_twitter);

        mSearchView.setIconified(false);
        mSearchView.onActionViewExpanded();
        // int searchPlateId = mSearchView.getContext().getResources()
        // .getIdentifier("android:id/search_plate", null, null);
        // View searchPlate = mSearchView.findViewById(searchPlateId);
        // searchPlate
        // .setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
        mSearchView.clearFocus();
        onButtonFacebook();
        friendsLoadedReceiver = new FriendsLoadedReceiver();
        IntentFilter filter = new IntentFilter("com.ezzet.eulou.action.FRIENDSLOADED");
        getActivity().registerReceiver(friendsLoadedReceiver, filter);

        contactsLoadedReceiver = new ContactsLoadedReceiver();
        IntentFilter filterContacts = new IntentFilter("com.ezzet.eulou.action.CONTACTSLOADED");
        getActivity().registerReceiver(contactsLoadedReceiver, filterContacts);

        serviceBoundReceiver = new ServiceBoundReceiver();
        filter = new IntentFilter("com.ezzet.eulou.action.SERVICEBOUND");
        getActivity().registerReceiver(serviceBoundReceiver, filter);

        mSearchView.setOnCloseListener(this);
        mContactBtn.setOnClickListener(this);
        mTwitterBtn.setOnClickListener(this);
        mFacebookBtn.setOnClickListener(this);
        mInstargramBtn.setOnClickListener(this);
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

            case R.id.friend_call_btn:

                MainActivity activity = (MainActivity) getActivity();
                int position = ((Integer) (v.getTag())).intValue();
                UserInfo userInfo;
                if (friendsFilterArray == null) {

                    userInfo = BaseActivity.mFriendUsers.get(position);
                } else {

                    userInfo = friendsFilterArray.get(position);
                }

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
                activity.sendBroadcast(intent);
                break;
            case R.id.friend_message_conversation_btn:

                activity = (MainActivity) getActivity();
                position = ((Integer) (v.getTag())).intValue();
                if (friendsFilterArray == null) {

                    userInfo = BaseActivity.mFriendUsers.get(position);
                } else {

                    userInfo = friendsFilterArray.get(position);
                }

                Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                chatIntent.putExtra(Constants.INTENT_MESAGE_USER_ID, userInfo);
                startActivity(chatIntent);
                break;
            case R.id.contacts_contact:

                onButtonContact();
                break;
            case R.id.contacts_facebook:

                onButtonFacebook();
                break;
            case R.id.contacts_twitter:

                onButtonTwitter();
                break;
            case R.id.contacts_instagram:

                onButtonInstargram();
                break;
        }
    }

    @Override
    public boolean onClose() {
        // TODO Auto-generated method stub

        String strFilter = "";
        if (strFilter.equals("")) {

            friendsFilterArray = null;
            contactsFilterArray = null;
        } else {

            friendsFilterArray = filterFriendsList(strFilter);
            contactsFilterArray = filterContactList(strFilter);
        }

        mContactsAdapter.notifyDataSetChanged();
        mFriendsAdapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String arg0) {
        // TODO Auto-generated method stub

        String strFilter = arg0.toString();
        if (strFilter.equals("")) {

            friendsFilterArray = null;
            contactsFilterArray = null;
        } else {

            friendsFilterArray = filterFriendsList(strFilter);
            contactsFilterArray = filterContactList(strFilter);
        }

        mContactsAdapter.notifyDataSetChanged();
        mFriendsAdapter.notifyDataSetChanged();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    private void onButtonContact() {

        mListview.setAdapter(mContactsAdapter);
        mTabTriangleGroup.check(R.id.contacts_tab_contact_triangle);
    }

    private void onButtonFacebook() {

        mListview.setAdapter(mFriendsAdapter);
        mTabTriangleGroup.check(R.id.contacts_tab_facebook_triangle);
    }

    private void onButtonTwitter() {

        mListview.setAdapter(null);
        mTabTriangleGroup.check(R.id.contacts_tab_twitter_triangle);
    }

    private void onButtonInstargram() {

        mListview.setAdapter(null);
        mTabTriangleGroup.check(R.id.contacts_tab_instagram_triangle);
    }

    @SuppressWarnings("unused")
    private ArrayList<UserInfo> filterFriendsList(String strFilter) {

        ArrayList<UserInfo> filteredList = new ArrayList<UserInfo>();
        MainActivity activity = (MainActivity) getActivity();
        for (int i = 0; i < BaseActivity.mFriendUsers.size(); i++) {

            UserInfo userInfo = BaseActivity.mFriendUsers.get(i);
            if (userInfo.getUserName().toLowerCase(Locale.US).contains(strFilter.toLowerCase(Locale.US))) {

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
            if (userInfo.getUserName().toLowerCase(Locale.US).contains(strFilter.toLowerCase(Locale.US))) {

                filteredList.add(userInfo);
            }
        }

        return filteredList;
    }

    private class FriendsListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            MainActivity activity = (MainActivity) getActivity();
            if (activity.service == null) {

                return 0;
            } else if (friendsFilterArray != null) {

                return friendsFilterArray.size();
            } else {

                return BaseActivity.mFriendUsers.size();
            }
        }

        @Override
        public Object getItem(int position) {
            if (friendsFilterArray == null) {

                return BaseActivity.mFriendUsers.get(position);
            } else {

                return friendsFilterArray.get(position);
            }
        }

        @Override
        public long getItemId(int position) {

            if (friendsFilterArray == null) {

                return BaseActivity.mFriendUsers.get(position).getUserID();
            } else {

                return friendsFilterArray.get(position).getUserID();
            }
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            RelativeLayout layoutItem;
            if (convertView != null) {
                layoutItem = (RelativeLayout) convertView;
            } else {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                layoutItem = (RelativeLayout) inflater.inflate(R.layout.layout_friend_list_item, null);
            }

            UserInfo user;
            if (friendsFilterArray == null) {

                user = BaseActivity.mFriendUsers.get(position);
            } else {

                user = friendsFilterArray.get(position);
            }

            FBProfilePictureView profileView = (FBProfilePictureView) layoutItem.findViewById(R.id.friend_profile);
            profileView.setProfileId(user.getFacebookID());
            TextView textViewName = (TextView) layoutItem.findViewById(R.id.friend_username);
            textViewName.setText(user.getUserName());
            ImageButton buttonCall = (ImageButton) layoutItem.findViewById(R.id.friend_call_btn);
            ImageButton buttonMessage = (ImageButton) layoutItem.findViewById(R.id.friend_message_conversation_btn);
            buttonCall.setTag(Integer.valueOf(position));
            buttonMessage.setTag(Integer.valueOf(position));
            buttonCall.setOnClickListener(ContactsFragment.this);
            buttonMessage.setOnClickListener(ContactsFragment.this);
            return layoutItem;
        }

    }

    private class ContactListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            MainActivity activity = (MainActivity) getActivity();
            if (activity.service == null) {
                return 0;
            } else if (contactsFilterArray != null) {
                return contactsFilterArray.size();
            } else {
                return BaseActivity.mContactUsers.size();
            }
        }

        @Override
        public Object getItem(int position) {

            if (contactsFilterArray == null) {
                return BaseActivity.mContactUsers.get(position);
            } else {
                return contactsFilterArray.get(position);
            }
        }

        @Override
        public long getItemId(int position) {

            if (contactsFilterArray == null) {
                return BaseActivity.mContactUsers.get(position).getUserID();
            } else {
                return contactsFilterArray.get(position).getUserID();
            }
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            RelativeLayout layoutItem;
            if (convertView != null) {
                layoutItem = (RelativeLayout) convertView;
            } else {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                layoutItem = (RelativeLayout) inflater.inflate(R.layout.layout_friend_list_item, null);
            }

            UserInfo user;
            if (contactsFilterArray == null) {
                user = BaseActivity.mContactUsers.get(position);
            } else {
                user = contactsFilterArray.get(position);
            }

            FBProfilePictureView profileView = (FBProfilePictureView) layoutItem.findViewById(R.id.friend_profile);
            profileView.setProfileId(user.getFacebookID());

            TextView textViewName = (TextView) layoutItem.findViewById(R.id.friend_username);
            textViewName.setText(user.getUserName());

            ImageButton buttonCall = (ImageButton) layoutItem.findViewById(R.id.friend_call_btn);
            ImageButton buttonMessage = (ImageButton) layoutItem.findViewById(R.id.friend_message_conversation_btn);
            buttonCall.setTag(Integer.valueOf(position));
            buttonMessage.setTag(Integer.valueOf(position));
            buttonCall.setOnClickListener(callClickListener);
            buttonMessage.setOnClickListener(callClickListener);
            return layoutItem;
        }
    }

    private class FriendsLoadedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            ContactsFragment.this.mFriendsAdapter.notifyDataSetChanged();
        }
    }

    private class ContactsLoadedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            ContactsFragment.this.mContactsAdapter.notifyDataSetChanged();
        }
    }

    private class ServiceBoundReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            MainActivity activity = (MainActivity) getActivity();
            if (BaseActivity.mFriendUsers.size() == 0) {

                activity.service.requestFriendsFacebook();
            } else {

                mFriendsAdapter.notifyDataSetChanged();
            }

            if (BaseActivity.mContactUsers.size() == 0) {

                activity.service.requestUsersFromContacts();
            } else {

                mContactsAdapter.notifyDataSetChanged();
            }
        }
    }
}
