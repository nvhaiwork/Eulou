package com.ezzet.eulou.fragments;

import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import com.ezzet.eulou.activities.BaseActivity;
import com.ezzet.eulou.activities.ChatActivity;
import com.ezzet.eulou.R;
import com.ezzet.eulou.adapters.MessageListAdapter;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.models.UserInfo;

public class MessagesFragment extends Fragment implements OnItemClickListener, OnCloseListener, OnQueryTextListener {

    private ListView mMessageList;
    private SearchView mSearchView;
    private MessageListAdapter mMessagesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_messages, container, false);

        mSearchView = (SearchView) rootView.findViewById(R.id.messages_search_view);
        mMessageList = (ListView) rootView.findViewById(R.id.messages_message_list);
        mMessagesAdapter = new MessageListAdapter(getActivity());
        mMessageList.setAdapter(mMessagesAdapter);
        mSearchView.setIconified(false);
        mSearchView.onActionViewExpanded();
        mSearchView.clearFocus();

        // Listener
        mSearchView.setOnCloseListener(this);
        mSearchView.setOnQueryTextListener(this);
        mMessageList.setOnItemClickListener(this);
        return rootView;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
        // TODO Auto-generated method stub

        Map<String, Object> historyItem = (Map<String, Object>) adapterView.getAdapter().getItem(pos);
        UserInfo userInfo = (UserInfo) historyItem.get("UserInfo");
        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
        chatIntent.putExtra(Constants.INTENT_MESAGE_USER_ID, userInfo);
        startActivity(chatIntent);
    }

    @Override
    public boolean onQueryTextChange(String queryText) {
        // TODO Auto-generated method stub

        mMessagesAdapter.getFilter().filter(queryText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onClose() {
        // TODO Auto-generated method stub

        mMessagesAdapter.getFilter().filter("");
        return false;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }

    public void updateMessageHistory() {

        mMessagesAdapter.setData(BaseActivity.mMessegas);
        mMessagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // TODO Auto-generated method stub
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {

            updateMessageHistory();
        }
    }
}
