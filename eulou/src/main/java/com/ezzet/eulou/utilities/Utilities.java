package com.ezzet.eulou.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import com.ezzet.eulou.R;
import com.ezzet.eulou.views.CustomAlertDialog;
import com.ezzet.eulou.views.CustomAlertDialog.OnNegativeButtonClick;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Utilities {

	/**
	 * Do hide keyboard
	 * 
	 * @param context
	 *            {@link Context}
	 * @param view
	 *            Curernt forcus view
	 * 
	 * */
	public static void doHideKeyboard(Context context, View view) {

		if (view != null) {

			InputMethodManager inputManager = (InputMethodManager) context
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	/**
	 * Sort list message by time
	 * 
	 * @param map
	 *            Message list
	 * */
	public static Map<String, Map<String, Object>> sortMessage(
			Map<String, Map<String, Object>> map) {

		List<Map.Entry<String, Map<String, Object>>> list = new LinkedList<Map.Entry<String, Map<String, Object>>>(
				map.entrySet());
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		Collections.sort(list,
				new Comparator<Map.Entry<String, Map<String, Object>>>() {

					@Override
					public int compare(Entry<String, Map<String, Object>> lhs,
							Entry<String, Map<String, Object>> rhs) {
						// TODO Auto-generated method stub

						Date leftDate = null;
						Date rightDate = null;
						try {

							leftDate = dateFormat.parse((String) lhs.getValue()
									.get("LastTime"));
						} catch (Exception ex) {

							leftDate = new Date();
						}

						try {

							rightDate = dateFormat.parse((String) rhs
									.getValue().get("LastTime"));
						} catch (Exception ex) {

							rightDate = new Date();
						}

						return rightDate.compareTo(leftDate);
					}
				});

		Map<String, Map<String, Object>> returnMap = new LinkedHashMap<String, Map<String, Object>>();
		for (Map.Entry<String, Map<String, Object>> entry : list) {

			returnMap.put(entry.getKey(), entry.getValue());
		}

		return returnMap;
	}

	public static String calcMessageTime(String dateStr) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm",
				Locale.ENGLISH);
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date startDate = new Date();
		try {

			startDate = dateFormat.parse(dateStr);
		} catch (ParseException e) {

			LogUtil.e("calcMessageTime", e.getMessage());
		}

		String startDateString = dateFormat1.format(startDate);
		Date yesterdayDate = new Date(new Date().getTime()
				- (1000 * 60 * 60 * 24));
		if (dateFormat1.format(new Date()).equals(startDateString)) {

			return timeFormat.format(startDate);
		} else if (dateFormat1.format(yesterdayDate).equals(startDateString)) {

			return "Yesterday";
		} else {

			return startDateString;
		}
	}

	public static boolean isToday(String timeStr) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date startDate = new Date();
		try {

			startDate = dateFormat.parse(timeStr);
		} catch (ParseException e) {

			LogUtil.e("calcMessageTime", e.getMessage());
		}

		String startDateString = dateFormat1.format(startDate);
		if (dateFormat1.format(new Date()).equals(startDateString)) {

			return true;
		}

		return false;
	}

	public static boolean isYesterday(String timeStr) {

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		dateFormat1.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date startDate = new Date();
		try {

			startDate = dateFormat.parse(timeStr);
		} catch (ParseException e) {

			LogUtil.e("calcMessageTime", e.getMessage());
		}

		String startDateString = dateFormat1.format(startDate);
		Date yesterdayDate = new Date(new Date().getTime()
				- (1000 * 60 * 60 * 24));
		if (dateFormat1.format(yesterdayDate).equals(startDateString)) {

			return true;
		}

		return false;
	}

	public static boolean isToday(Date date) {

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String startDateString = dateFormat.format(date);
		if (dateFormat.format(new Date()).equals(startDateString)) {

			return true;
		}

		return false;
	}

	/**
	 * show alert message
	 * 
	 * @param message
	 *            Alert message
	 * @param title
	 *            alert title
	 * */
	public static void showAlertMessage(Context context, String message,
			String title, OnNegativeButtonClick buttonClick) {

		final CustomAlertDialog dialog = new CustomAlertDialog(context);
		dialog.setCancelableFlag(false);
		dialog.setTitle(title);
		dialog.setMessage(message);
		if (buttonClick == null) {

			dialog.setNegativeButton(context.getString(R.string.ok),
					new OnNegativeButtonClick() {

						@Override
						public void onButtonClick(final View view) {
							// TODO Auto-generated method stub

							dialog.dismiss();
						}
					});
		} else {

			dialog.setNegativeButton(context.getString(R.string.ok),
					buttonClick);
		}

		dialog.show();
	}
}
