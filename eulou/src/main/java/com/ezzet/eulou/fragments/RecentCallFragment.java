package com.ezzet.eulou.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.activities.BaseActivity;
import com.ezzet.eulou.adapters.CallsListAdapter;
import com.ezzet.eulou.constants.Constants;
import com.ezzet.eulou.extra.HelperFunction;
import com.ezzet.eulou.models.CallHistoryItem;
import com.ezzet.eulou.models.UserInfo;

import java.util.ArrayList;
import java.util.Map;

public class RecentCallFragment extends Fragment
		implements
			AdapterView.OnItemLongClickListener {

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
				getActivity().sendBroadcast(intent);
			}
		});

		listViewCalls.setOnItemLongClickListener(this);
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

				helper.deleteAllCallHistory(userId);
				CallHistoryItem.historyArray.clear();
			} else {

				CallHistoryItem callItem = CallHistoryItem.historyArray
						.get(selectedPos);
				CallHistoryItem.historyArray.remove(selectedPos);
				helper.deleteCallHistory(userId, callItem.contactID,
						callItem.direction, callItem.groupdate);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			adapter.notifyDataSetChanged();
		}
	}
}
