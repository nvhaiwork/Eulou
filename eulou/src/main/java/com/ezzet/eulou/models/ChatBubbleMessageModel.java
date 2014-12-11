package com.ezzet.eulou.models;

import java.io.Serializable;
import java.util.Date;

public class ChatBubbleMessageModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fromUser;
	private String toUser;
	private String message;
	private Date messageDate;
	private String messageId;
	private boolean isFromMy;
	private String profileImg;
	private Date seenTime;

	public Date getSeenTime() {
		return seenTime;
	}

	public void setSeenTime(Date seenTime) {
		this.seenTime = seenTime;
	}

	public String getToUser() {
		return toUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getProfileImg() {
		return profileImg;
	}

	public void setProfileImg(String profileImg) {
		this.profileImg = profileImg;
	}

	public String getFromUser() {
		return fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getMessageDate() {
		return messageDate;
	}

	public void setMessageDate(Date messageDate) {
		this.messageDate = messageDate;
	}

	public boolean isFromMy() {
		return isFromMy;
	}

	public void setFromMy(boolean isFromMy) {
		this.isFromMy = isFromMy;
	}

}
