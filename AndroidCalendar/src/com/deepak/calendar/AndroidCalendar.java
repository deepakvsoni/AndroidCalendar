package com.deepak.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.format.DateUtils;

public class AndroidCalendar {
	public static final int DAILY = 0, WEEKLY = 1, MONTHLY = 4,
			FORTNIGHTLY = 2, NEVER = 8;

	public static Uri getCalendarsUri() {
		Uri calendarUri;
		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			calendarUri = Uri.parse("content://com.android.calendar/calendars");
		} else {
			calendarUri = Uri.parse("content://calendar/calendars");
		}
		return calendarUri;
	}

	public static Uri getInstanceUri() {
		Uri instancesUri;
		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			instancesUri = Uri
					.parse("content://com.android.calendar/instances/when");
		} else {
			instancesUri = Uri.parse("content://calendar/instances/when");
		}
		return instancesUri;
	}

	public static Uri getAttendeeUri() {
		//
		Uri instancesUri;
		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			instancesUri = Uri
					.parse("content://com.android.calendar/attendees");
		} else {
			instancesUri = Uri.parse("content://calendar/instances/when");
		}
		return instancesUri;
	}

	public static Uri getEventUri() {
		Uri viewEventUri;
		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			viewEventUri = Uri.parse("content://com.android.calendar/events");
		} else {
			viewEventUri = Uri.parse("content://calendar/events");
		}
		return viewEventUri;
	}

	public static Uri getViewEventUri(String eventId) {
		Uri viewEventUri;
		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			viewEventUri = Uri.parse("content://com.android.calendar/events/"
					+ eventId);
		} else {
			viewEventUri = Uri.parse("content://calendar/events/" + eventId);
		}
		return viewEventUri;
	}

	public static String createCalendarEvent(ContentResolver resolver,
			String title, String description, String location, long startDate,
			long endDate, int recurrance, String timeZoneId) {
		String eventId = null;
		ContentValues event = new ContentValues();
		event.put("calendar_id", getCalendarId(resolver));
		event.put("dtstart", startDate);
		event.put("dtend", endDate);
		event.put("title", title);
		event.put("description", description);
		event.put("eventLocation", location);
		event.put("eventTimezone", timeZoneId);
		switch (recurrance) {
		case DAILY:
			event.put("rrule", "FREQ=DAILY");
			break;
		case MONTHLY:
			event.put("rrule", "FREQ=MONTHLY");
			break;
		case WEEKLY:
			event.put("rrule", "FREQ=WEEKLY");
			break;
		case FORTNIGHTLY:
			event.put("rrule", "FREQ=WEEKLY;INTERVAL=2");
			break;
		}

		Uri lastRow = resolver.insert(getEventUri(), event);
		if (null != lastRow) {
			eventId = lastRow.getLastPathSegment();
		}
		return eventId;
	}

	public static int getCalendarId(ContentResolver contentResolver) {
		int calendarId = 1;

		final Cursor cursor = contentResolver.query(getCalendarsUri(), null,
				null, null, null);

		while (cursor.moveToNext()) {
			calendarId = cursor.getInt(0);
			break;
		}
		cursor.close();
		return calendarId;
	}

	public static ArrayList<Schedule> getEvents(ContentResolver contentResolver,
			Calendar calendar) {
		ArrayList<Schedule> schedules = new ArrayList<Schedule>();
		Uri calendarUri = getCalendarsUri();
		Cursor cursor = contentResolver.query(calendarUri, (new String[] {
				"_id", "displayName", "selected" }), null, null, null);

		List<String> calendarIds = new ArrayList<String>();

		try {
			System.out.println("Count=" + cursor.getCount());
			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					String _id = cursor.getString(0);
					calendarIds.add(_id);
				}
			}
			cursor.close();
		} catch (AssertionError ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// For each calendar, display all the events from the previous week to
		// the end of next week.
		for (String id : calendarIds) {
			Uri instancesUri = null;
			if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
				instancesUri = Uri
						.parse("content://com.android.calendar/instances/when");
			} else {
				instancesUri = Uri.parse("content://calendar/instances/when");
			}

			Uri.Builder builder = instancesUri.buildUpon();
			long now = calendar.getTimeInMillis();

			ContentUris.appendId(builder, now);
			ContentUris.appendId(builder, now + DateUtils.DAY_IN_MILLIS);

			Cursor eventCursor = contentResolver.query(builder.build(),
					new String[] { "title", "begin", "end", "event_id" },
					"calendar_id=" + id, null, "startDay ASC, startMinute ASC");

			System.out.println("eventCursor count=" + eventCursor.getCount());
			if (eventCursor.getCount() > 0) {
				while (eventCursor.moveToNext()) {
					final String title = eventCursor.getString(0);
					final Date begin = new Date(eventCursor.getLong(1));
					final Date end = new Date(eventCursor.getLong(2));
					final String eid = eventCursor.getString(3);

					Calendar d = Calendar.getInstance();
					d.setTimeInMillis(begin.getTime());

					Schedule schedule = new Schedule();
					schedule._id = -1;
					schedule.Date = U.getUTCDate(d);
					schedule.StartTime = U.getUTCDate(d);
					d.setTimeInMillis(end.getTime());

					schedule.EndTime = U.getFormattedTime(d);
					schedule.Title = title;
					schedule.Id = eid;
					schedules.add(schedule);
				}
			}
			eventCursor.close();
			break;
		}
		return schedules;
	}

	public static int deleteInternalEvent(ContentResolver contentResolver,
			String eventId) {
		Uri deleteUri = getViewEventUri(eventId);
		int rows = contentResolver.delete(deleteUri, null, null);
		return rows;
	}
}
