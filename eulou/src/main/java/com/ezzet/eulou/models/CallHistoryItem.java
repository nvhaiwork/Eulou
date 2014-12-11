package com.ezzet.eulou.models;

import java.util.ArrayList;

public class CallHistoryItem {

	public String contact;
	public String contactID;
	public String startDate;
	public String endDate;
	public String duration;
	public int endCause;
	public String direction;
	public String count;
	public String groupdate;
	public UserInfo userInfo;
	
	public static ArrayList<CallHistoryItem> historyArray = new ArrayList<CallHistoryItem>();

}
