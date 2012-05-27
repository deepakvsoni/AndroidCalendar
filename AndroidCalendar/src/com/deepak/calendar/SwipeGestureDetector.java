package com.deepak.calendar;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public class SwipeGestureDetector extends SimpleOnGestureListener {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	private CalendarFragmentBase _calendarFragment;

	public SwipeGestureDetector(CalendarFragmentBase calendarFragment) {
		_calendarFragment = calendarFragment;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			// right to left swipe
			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				_calendarFragment.rightSwipe();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				_calendarFragment.leftSwipe();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
