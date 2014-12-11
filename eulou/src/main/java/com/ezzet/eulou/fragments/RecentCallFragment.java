package com.ezzet.eulou.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.adapters.CallsListAdapter;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.models.CallHistoryItem;
import com.ezzet.eulou.models.UserInfo;

public class RecentCallFragment extends Fragment {

	private ListView listViewCalls;
	private CallsListAdapter adapter;
	private ReadCallsReceiver readCallsReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.activity_calls, container, false);

		listViewCalls = (ListView) rootView.findViewById(R.id.listViewCalls);
		adapter = new CallsListAdapter(getActivity());
		listViewCalls.setAdapter(adapter);

		listViewCalls.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				CallHistoryItem item = CallHistoryItem.historyArray
						.get(position);
				UserInfo userInfo = item.userInfo;
				int mainSocial = userInfo.getMainSocial();
				String userIDString = "";
				if (mainSocial == Constants.FACEBOOK)
					userIDString = "fb" + userInfo.getFacebookID();
				else if (mainSocial == Constants.TWITTER)
					userIDString = "tw" + userInfo.getTwitterID();
				else if (mainSocial == Constants.INSTAGRAM)
					userIDString = "in" + userInfo.getInstagramID();
				else
					return;

				Intent intent = new Intent();
				intent.setAction("com.ezzet.eulou.action.CALLBUTTON");
				intent.putExtra("SinchID", userIDString);
				getActivity().sendBroadcast(intent);

			}

		});

		readCallsReceiver = new ReadCallsReceiver();
		IntentFilter filter = new IntentFilter(
				"com.ezzet.eulou.action.READCALLS");
		getActivity().registerReceiver(readCallsReceiver, filter);

		return rootView;
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(readCallsReceiver);
		super.onDestroy();
	}

	private class ReadCallsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			adapter.notifyDataSetChanged();
		}
	}
}
