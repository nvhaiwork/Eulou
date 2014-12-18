package com.ezzet.eulou.fragments;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.ezzet.eulou.activities.BaseActivity;
import com.ezzet.eulou.activities.ChatActivity;
import com.ezzet.eulou.R;
import com.ezzet.eulou.adapters.MessageListAdapter;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.extra.HelperFunction;
import com.ezzet.eulou.models.UserInfo;

public class MessagesFragment extends Fragment
		implements
			OnItemClickListener,
			OnCloseListener,
			OnQueryTextListener,
			AdapterView.OnItemLongClickListener {

	private MessageListAdapter mMessagesAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.fragment_messages, container, false);

		SearchView searchView = (SearchView) rootView
				.findViewById(R.id.messages_search_view);
		ListView messageList = (ListView) rootView
				.findViewById(R.id.messages_message_list);
		mMessagesAdapter = new MessageListAdapter(getActivity());
		messageList.setAdapter(mMessagesAdapter);
		searchView.setIconified(false);
		searchView.onActionViewExpanded();
		searchView.clearFocus();

		// Listener
		searchView.setOnCloseListener(this);
		searchView.setOnQueryTextListener(this);
		messageList.setOnItemClickListener(this);
		messageList.setOnItemLongClickListener(this);
		return rootView;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onItemClick(AdapterView<?> adapterView, View view, int pos,
			long id) {
		// TODO Auto-generated method stub

		Map<String, Object> historyItem = (Map<String, Object>) adapterView
				.getAdapter().getItem(pos);
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

		mMessagesAdapter.setData(BaseActivity.mMessages);
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

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view,
			int i, long l) {

		deleteOptionsDialog(getActivity(), i);
		return true;
	}

	public void deleteOptionsDialog(final Activity act, final int pos) {

		final Dialog dialog = new Dialog(act);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.dialog_delete_options);
		dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		Window window = dialog.getWindow();
		WindowManager.LayoutParams wlp = window.getAttributes();
		wlp.gravity = Gravity.CENTER;
		wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
		wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
		wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
		window.setAttributes(wlp);
		TextView option1 = (TextView) dialog
				.findViewById(R.id.delete_options_1);
		TextView option2 = (TextView) dialog
				.findViewById(R.id.delete_options_2);
		option1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				new DeleteMessageHistory().execute(pos);
				dialog.dismiss();
			}
		});

		option2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				new DeleteMessageHistory().execute(-1);
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private class DeleteMessageHistory extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... pos) {

			HelperFunction helper = new HelperFunction(getActivity());
			String userId = String.valueOf(BaseActivity.mUserInfo.getUserID());
			int selectedPos = pos[0];
			if (selectedPos < 0) {

				helper.deleteAllMessageHistory(userId);
				BaseActivity.mMessages.clear();
			} else {

				String key = (String) (new ArrayList(
						BaseActivity.mMessages.keySet())).get(selectedPos);
				Map<String, Object> message = BaseActivity.mMessages.get(key);
				UserInfo userInfo = (UserInfo) message.get("UserInfo");
				String partnerId = String.valueOf(userInfo.getUserID());
				helper.deleteMessageHistory(userId, partnerId);
				BaseActivity.mMessages.remove(key);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			mMessagesAdapter.setData(BaseActivity.mMessages);
			mMessagesAdapter.notifyDataSetChanged();
		}
	}
}
