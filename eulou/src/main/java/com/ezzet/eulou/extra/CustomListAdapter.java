package com.ezzet.eulou.extra;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ezzet.eulou.R;
import com.ezzet.eulou.models.Code;

public class CustomListAdapter extends BaseAdapter{
	static class ViewHolder {
		TextView tvCountryName,tvCountryCode;
	}

	Context context;
	ArrayList<Code> codeList;
	
	public CustomListAdapter(Context context, ArrayList<Code> codeList) {
		this.context = context;
		this.codeList = codeList;
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return codeList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return codeList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.country_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.tvCountryName = (TextView) convertView.findViewById(R.id.txtCountryName);
			viewHolder.tvCountryCode = (TextView) convertView.findViewById(R.id.txtCountryCode);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.tvCountryName.setText(codeList.get(position).getName());
		viewHolder.tvCountryCode.setText("+"+codeList.get(position).getCallingCodes());
		return convertView;
	}

}
