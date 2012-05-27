package com.deepak.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.util.Log;
import android.widget.TimePicker;

public class U {
	public static int MAX_SIZE = 250 * 1024;

	public static String PARENT_DATA_FOLDER = "PTCentralPro";

	private static SimpleDateFormat FORMAT = new SimpleDateFormat(
			"yyyyMMdd");

	private static String[] MONTHS = new String[] { "January", "February",
			"March", "April", "May", "June", "July", "August", "September",
			"October", "November", "December" };

	public static String getFormattedDate(Calendar date) {
		return FORMAT.format(new Date(date.getTimeInMillis()));
	}

	public static Calendar getFormattedDate(String date) {
		Calendar cal = Calendar.getInstance();
		Date d;
		try {
			d = FORMAT.parse(date);
			cal.setTimeInMillis(d.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cal;
	}

	public static Calendar getDate(String date) {
		Calendar cal = Calendar.getInstance();

		String[] split = date.split(" ");
		if (split.length > 2) {
			int day = Integer.parseInt(split[0]);
			int year = Integer.parseInt(split[2]);
			int month = Calendar.JANUARY;
			if (split[1].compareTo("January") == 0) {
				month = Calendar.JANUARY;
			} else if (split[1].compareTo("February") == 0) {
				month = Calendar.FEBRUARY;
			} else if (split[1].compareTo("March") == 0) {
				month = Calendar.MARCH;
			} else if (split[1].compareTo("April") == 0) {
				month = Calendar.APRIL;
			} else if (split[1].compareTo("May") == 0) {
				month = Calendar.MAY;
			} else if (split[1].compareTo("June") == 0) {
				month = Calendar.JUNE;
			} else if (split[1].compareTo("July") == 0) {
				month = Calendar.JULY;
			} else if (split[1].compareTo("August") == 0) {
				month = Calendar.AUGUST;
			} else if (split[1].compareTo("September") == 0) {
				month = Calendar.SEPTEMBER;
			} else if (split[1].compareTo("October") == 0) {
				month = Calendar.OCTOBER;
			} else if (split[1].compareTo("November") == 0) {
				month = Calendar.NOVEMBER;
			} else if (split[1].compareTo("December") == 0) {
				month = Calendar.DECEMBER;
			}
			cal.set(year, month, day);
		}
		return cal;
	}

	public static Calendar getTime(String time) {
		Calendar cal = Calendar.getInstance();

		String[] split = time.split("\\.");
		if (split.length > 1) {
			int hour = Integer.parseInt(split[0]);

			split = split[1].split(" ");
			if (split.length > 1) {
				int min = Integer.parseInt(split[0]);
				if (split[1].compareTo("PM") == 0) {
					hour += 12;
				}
				cal.set(Calendar.HOUR_OF_DAY, hour);
				cal.set(Calendar.MINUTE, min);
			}
		}
		return cal;
	}

	public static Calendar stringToDate(String dtString) {
		Calendar dt = Calendar.getInstance();
		String[] split = dtString.split("/");
		try {
			if (split.length == 3) {
				dt.set(Calendar.DATE, Integer.parseInt(split[0]));
				dt.set(Calendar.MONTH, Integer.parseInt(split[1]));
				String[] splitYear = split[2].split(" ");
				if (splitYear.length > 1) {
					dt.set(Calendar.YEAR, Integer.parseInt(splitYear[0]));
				}
			}
		} catch (Exception e) {
			Log.e("Fitness App", "Could not parse date string.");
		}
		return dt;
	}

	public static String getFormattedTime(TimePicker tpTime) {
		int hour = tpTime.getCurrentHour();
		String am = hour > 12 ? "PM" : "AM";
		return String.format("%d.%02d %s", hour % 12,
				tpTime.getCurrentMinute(), am);
	}

	public static String getFormattedTime(Calendar tpTime) {
		int hour = tpTime.get(Calendar.HOUR_OF_DAY); // tpTime.getCurrentHour();
		String am = hour >= 12 ? "PM" : "AM";
		return String.format("%d.%02d %s", hour % 12,
				tpTime.get(Calendar.MINUTE), am);
	}

	public static String getStringFromBundle(Bundle bundle, String key,
			String defaultValue) {
		if (bundle.containsKey(key)) {
			return bundle.getString(key);
		}
		return defaultValue;
	}

	public static String s(int value) {
		return String.format("%d", value);
	}

	public static String dt() {
		Calendar cal = Calendar.getInstance();
		return String.format("%d %s %d", cal.get(Calendar.DATE),
				MONTHS[cal.get(Calendar.MONTH)], cal.get(Calendar.YEAR));
	}

	public static boolean isNullOrEmpty(String string) {
		return null == string || string.length() == 0
				|| string.compareTo("null") == 0;
	}

	public static String defaultIfNullOrEmpty(String string, String sDefault) {
		return null == string || string.length() == 0
				|| string.compareTo("null") == 0 ? sDefault : string;
	}

	public static String getUTCDate(Calendar calendar) {
		return String.format("%4d%2d%2d", calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH));
	}
}
