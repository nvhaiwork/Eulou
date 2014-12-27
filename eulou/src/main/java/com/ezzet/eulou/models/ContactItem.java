package com.ezzet.eulou.models;

/**
 * Created by apple on 12/27/14.
 */
public class ContactItem {

	private boolean isHeader;
	private String header;
	private UserInfo userInfo;

	public boolean isHeader() {
		return isHeader;
	}

	public void setHeader(boolean isHeader) {
		this.isHeader = isHeader;
	}

	public String getHeaderText() {
		return header;
	}

	public void setHeaderText(String header) {
		this.header = header;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}
}
