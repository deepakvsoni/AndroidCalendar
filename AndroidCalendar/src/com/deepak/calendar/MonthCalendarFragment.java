package com.deepak.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MonthCalendarFragment extends CalendarFragmentBase implements
		GenericAsyncTaskRun {
	private GestureDetector gestureDetector;

	public static int MIN_HEIGHT = 2;
	public static int ID = 2;
	private Mode _mode = Mode.Month;
	private Calendar _calendar;
	private Button btnYearDecrement, btnYearIncrement;
	private RelativeLayout rlContent;
	private TextView tvMonthYear;
	private ProgressBar pbActivity;
	private HashMap<String, ArrayList<Schedule>> _mapping = new HashMap<String, ArrayList<Schedule>>();
	private ScheduleLoader _scheduleTask;
	private ScheduleObjectContainer _container = new ScheduleObjectContainer();

	// Month View
	private GridView gvDays;

	private final String[] MONTH_NAMES = { "January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October",
			"November", "December" };

	final private String[] DAYS = { "Monday", "Tuesday", "Wednesday",
			"Thursday", "Friday", "Saturday", "Sunday" };

	private MonthGridAdapter _monthGridAdapter;
	private HashMap<Integer, Integer> _buttonMonths = new HashMap<Integer, Integer>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.schedule_calendar, container, false);
		_calendar = Calendar.getInstance();
		pbActivity = (ProgressBar) v.findViewById(R.id.pbActivity);
		rlContent = (RelativeLayout) v.findViewById(R.id.rlContent);

		_buttonMonths.put(Integer.valueOf(R.id.btnJan), Calendar.JANUARY);
		_buttonMonths.put(Integer.valueOf(R.id.btnFeb), Calendar.FEBRUARY);
		_buttonMonths.put(Integer.valueOf(R.id.btnMar), Calendar.MARCH);
		_buttonMonths.put(Integer.valueOf(R.id.btnApr), Calendar.APRIL);
		_buttonMonths.put(Integer.valueOf(R.id.btnMay), Calendar.MAY);
		_buttonMonths.put(Integer.valueOf(R.id.btnJune), Calendar.JUNE);
		_buttonMonths.put(Integer.valueOf(R.id.btnJuly), Calendar.JULY);
		_buttonMonths.put(Integer.valueOf(R.id.btnAug), Calendar.AUGUST);
		_buttonMonths.put(Integer.valueOf(R.id.btnSep), Calendar.SEPTEMBER);
		_buttonMonths.put(Integer.valueOf(R.id.btnOct), Calendar.OCTOBER);
		_buttonMonths.put(Integer.valueOf(R.id.btnNov), Calendar.NOVEMBER);
		_buttonMonths.put(Integer.valueOf(R.id.btnDec), Calendar.DECEMBER);

		showMonthView();

		if (null != _scheduleTask) {
			_scheduleTask.setRun(this);
		} else {
			_scheduleTask = new ScheduleLoader(this, Token.GET_SCHEDULE);
			_scheduleTask.execute((Void[]) null);
		}
		gestureDetector = new GestureDetector(new SwipeGestureDetector(this));

		return v;
	}

	public void onStart() {
		super.onStart();
	}

	public Mode getMode() {
		return _mode;
	}

	public void setMode(Mode _mode) {
		this._mode = _mode;
		showMonthView();
	}

	// Month View
	private void showMonthView() {
		_calendar.set(Calendar.DATE, 1);
		_calendar.set(Calendar.HOUR_OF_DAY, 0);
		_calendar.set(Calendar.SECOND, 0);
		_calendar.set(Calendar.MINUTE, 0);
		_calendar.set(Calendar.MILLISECOND, 0);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		rlContent.removeAllViews();
		View v = inflater.inflate(R.layout.calender_month, rlContent);
		gvDays = (GridView) v.findViewById(R.id.gvDays);
		_monthGridAdapter = new MonthGridAdapter();
		gvDays.setAdapter(_monthGridAdapter);

		gvDays.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				TextView tvDay = (TextView) arg1.findViewById(R.id.tvDay);
				String text = tvDay.getText().toString();
				if (!U.isNullOrEmpty(text)) {
					if (_mapping.containsKey(text)) {
						ArrayList<Schedule> s = _mapping.get(text);
						ScheduleListFragment sl = new ScheduleListFragment();
						sl.setSchedule(s);
						// showDialog(sl, "dialog2");
					}
				}
			}
		});

		v.findViewById(R.id.gvDays).setOnTouchListener(
				new View.OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return null == gestureDetector
								|| gestureDetector.onTouchEvent(event);
					}
				});

		tvMonthYear = (TextView) v.findViewById(R.id.tvMonthYear);
		tvMonthYear.setText(String.format("%s, %d",
				MONTH_NAMES[_calendar.get(Calendar.MONTH)],
				_calendar.get(Calendar.YEAR)));

		((Button) v.findViewById(R.id.btnJan))
				.setOnClickListener(changeMonthClickListener);
		((Button) v.findViewById(R.id.btnFeb))
				.setOnClickListener(changeMonthClickListener);
		((Button) v.findViewById(R.id.btnMar))
				.setOnClickListener(changeMonthClickListener);
		((Button) v.findViewById(R.id.btnApr))
				.setOnClickListener(changeMonthClickListener);
		((Button) v.findViewById(R.id.btnMay))
				.setOnClickListener(changeMonthClickListener);
		((Button) v.findViewById(R.id.btnJune))
				.setOnClickListener(changeMonthClickListener);
		((Button) v.findViewById(R.id.btnJuly))
				.setOnClickListener(changeMonthClickListener);
		((Button) v.findViewById(R.id.btnAug))
				.setOnClickListener(changeMonthClickListener);
		((Button) v.findViewById(R.id.btnSep))
				.setOnClickListener(changeMonthClickListener);
		((Button) v.findViewById(R.id.btnOct))
				.setOnClickListener(changeMonthClickListener);
		((Button) v.findViewById(R.id.btnNov))
				.setOnClickListener(changeMonthClickListener);
		((Button) v.findViewById(R.id.btnDec))
				.setOnClickListener(changeMonthClickListener);

		btnYearDecrement = ((Button) v.findViewById(R.id.btnYearDecrement));
		btnYearDecrement.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				_calendar.add(Calendar.YEAR, -1);
				refreshMonthView();
				reloadSchedules();
			}
		});

		btnYearIncrement = ((Button) v.findViewById(R.id.btnYearIncrement));
		btnYearIncrement.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				_calendar.add(Calendar.YEAR, 1);
				refreshMonthView();
				reloadSchedules();
			}
		});

		btnYearDecrement.setText(String.format("%d",
				_calendar.get(Calendar.YEAR) - 1));
		btnYearIncrement.setText(String.format("%d",
				_calendar.get(Calendar.YEAR) + 1));
		// if (null != _refreshTask) {
		// _refreshTask.setRun(null);
		// }
		// _refreshTask = new GenericAsyncTask(this, Token.REFRESH_SCHEDULE);
		// _refreshTask.execute((Void[]) null);
		refreshMonthView();
		reloadSchedules();
	}

	private void refreshMonthView() {
		if (null != tvMonthYear) {
			tvMonthYear.setText(String.format("%s, %d",
					MONTH_NAMES[_calendar.get(Calendar.MONTH)],
					_calendar.get(Calendar.YEAR)));
			btnYearDecrement.setText(String.format("%d",
					_calendar.get(Calendar.YEAR) - 1));
			btnYearIncrement.setText(String.format("%d",
					_calendar.get(Calendar.YEAR) + 1));
		}
	}

	private void loadMonthSchedule() {
		if (null != _monthGridAdapter) {
			_monthGridAdapter.notifyDataSetChanged();
		}
	}

	private View.OnClickListener changeMonthClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Integer key = Integer.valueOf(v.getId());
			if (_buttonMonths.containsKey(key)) {
				_calendar.set(Calendar.MONTH, _buttonMonths.get(key));
				refreshMonthView();
				reloadSchedules();
			}
		}
	};

	public class MonthGridAdapter extends BaseAdapter {
		public int getCount() {
			int count = 7;
			int dayOfWeek = _calendar.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == Calendar.SUNDAY) {
				dayOfWeek = 7;
			} else {
				dayOfWeek--;
			}
			count += _calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			count += dayOfWeek - 1;
			if (count % 7 != 0)
				count += (7 - (count % 7));
			return count;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = null;
			if (null != convertView) {
				v = convertView;
			} else {
				v = getActivity().getLayoutInflater().inflate(
						R.layout.month_view_item, null);
			}
			TextView tv = (TextView) v.findViewById(R.id.tvDay);
			TextView tvFirst = (TextView) v.findViewById(R.id.tvFirst);
			TextView tvSecond = (TextView) v.findViewById(R.id.tvSecond);
			TextView tvThird = (TextView) v.findViewById(R.id.tvThird);
			if (position < 7) {
				tv.setGravity(Gravity.CENTER);
				tv.setText(DAYS[position]);
				tvFirst.setText(" ");
				tvSecond.setText(" ");
				tvThird.setText(" ");
			} else {
				tv.setGravity(Gravity.RIGHT);

				int dayOfWeek = _calendar.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek == Calendar.SUNDAY) {
					dayOfWeek = 7;
				} else {
					dayOfWeek--;
				}
				int day = position - (7 + dayOfWeek) + 2;
				if (position < (7 + dayOfWeek - 1)
						|| day > _calendar.getActualMaximum(Calendar.DATE)) {
					tv.setText("  ");
					tvFirst.setText(" ");
					tvSecond.setText(" ");
					tvThird.setText(" ");
				} else {
					String ke = U.s(day);
					tv.setText(String.format("%d", day));
					tvFirst.setText(" ");
					tvSecond.setText(" ");
					tvThird.setText(" ");
					if (_mapping.containsKey(ke)) {
						ArrayList<Schedule> s = _mapping.get(ke);
						if (s.size() > 0) {
							tvFirst.setText(s.get(0).Title);
						}
						if (s.size() > 1) {
							tvSecond.setText(s.get(1).Title);
						}
						if (s.size() > 2) {
							tvThird.setText(s.get(2).Title);
						}
					}

				}
			}
			return v;
		}
	}

	@Override
	public void run(int token) {
		if (Token.GET_SCHEDULE == token) {
			_container.Schedules.clear();

			Calendar cal = Calendar.getInstance();
			cal.set(_calendar.get(Calendar.YEAR),
					_calendar.get(Calendar.MONTH),
					_calendar.get(Calendar.DATE), 0, 0, 0);
			cal.set(Calendar.MILLISECOND, 0);
			for (int i = 0; i < cal.getActualMaximum(Calendar.DATE); ++i) {
				int day = cal.get(Calendar.DATE);
				List<Schedule> schedule = AndroidCalendar.getEvents(
						getActivity().getContentResolver(), cal);

				_scheduleTask._mapping.put(U.s(day),
						(ArrayList<Schedule>) schedule);

				cal.add(Calendar.DATE, 1);
			}
		}
	}

	@Override
	public void onRunCompleted(int token) {
		pbActivity.setVisibility(View.INVISIBLE);

		for (String key : _scheduleTask._mapping.keySet()) {
			_mapping.put(key, _scheduleTask._mapping.get(key));
		}
		loadMonthSchedule();
	}

	@Override
	public void onPreExecute(int token) {
		pbActivity.setVisibility(View.VISIBLE);
	}

	private void reloadSchedules() {
		if (null != _scheduleTask) {
			_scheduleTask.setRun(null);
		}
		_scheduleTask = new ScheduleLoader(this, Token.GET_SCHEDULE);
		_scheduleTask.execute((Void[]) null);
	}

	@Override
	public void leftSwipe() {
		_calendar.add(Calendar.MONTH, -1);
		refreshMonthView();
		animateViewLeft(gvDays);
		reloadSchedules();
	}

	@Override
	public void rightSwipe() {
		_calendar.add(Calendar.MONTH, 1);
		refreshMonthView();
		animateViewRight(gvDays);
		reloadSchedules();
	}

}
