/*
 *
 */
package com.ezzet.eulou.utilities;

import android.util.Log;

/**
 * Customize environment logger Turn log off when release app
 */
public class LogUtil {
	private static boolean DEBUG = true;

	/**
	 * @Function name: d
	 * @Description Send a DEBUG log message.
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param message
	 *            The message you would like logged.
	 */
	public static void d(String tag, String message) {
		if (DEBUG) {

			Log.d(tag, message);
		}
	}

	/**
	 * @Function name: e
	 * @Description Send an ERROR log message.
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param message
	 *            The message you would like logged.
	 */
	public static void e(String tag, String message) {
		if (DEBUG) {

			Log.e(tag, message);
		}
	}

	/**
	 * @Function name: i
	 * @Description Send an INFO log message.
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param message
	 *            The message you would like logged.
	 */
	public static void i(String tag, String message) {
		if (DEBUG) {

			Log.i(tag, message);
		}
	}

	/**
	 * @Function name: v
	 * @Description Send a VERBOSE log message.
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param message
	 *            The message you would like logged.
	 */
	public static void v(String tag, String message) {
		if (DEBUG) {

			Log.v(tag, message);
		}
	}

	/**
	 * @Function name: w
	 * @Description Send a WARN log message.
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param message
	 *            The message you would like logged.
	 */
	public static void w(String tag, String message) {
		if (DEBUG) {

			Log.w(tag, message);
		}
	}
}
