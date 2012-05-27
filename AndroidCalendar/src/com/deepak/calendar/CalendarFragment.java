package com.deepak.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CalendarFragment extends CalendarFragmentBase implements
		GenericAsyncTaskRun {
	private GestureDetector gestureDetector;

	public static int MIN_HEIGHT = 2;
	public static int ID = 2;
	private Mode _mode = Mode.Day;
	private Calendar _calendar;
	private Button btnYearDecrement, btnYearIncrement;
	private RelativeLayout rlContent;
	private TextView tvMonthYear;
	private ProgressBar pbActivity;
	private HashMap<String, ArrayList<Schedule>> _mapping = new HashMap<String, ArrayList<Schedule>>();
	private ScheduleLoader _scheduleTask;
	private ScheduleObjectContainer _container = new ScheduleObjectContainer();

	// Week View Controls
	private TextView tvWeekMonthYear;
	private ListView lvMonday, lvTuesday, lvWednesday, lvThursday, lvFriday,
			lvSaturday, lvSunday;
	private TextView tvMonday, tvTuesday, tvWednesday, tvThursday, tvFriday,
			tvSaturday, tvSunday;
	private Button btnWeek1, btnWeek2, btnWeek3, btnWeek4, btnWeek5,
			btnIncrementMonth, btnDecrementMonth;
	private View llWeekViewParent;
	private ArrayList<Schedule> _mondaySchedule, _tuesdaySchedule,
			_wednesdaySchedule, _thursdaySchedule, _fridaySchedule,
			_saturdaySchedule, _sundaySchedule;

	// Day View Controls
	private TextView tvTitle;
	private View timeRuler;
	private RelativeLayout rlHours;
	private DatePicker dtDate;
	private List<Schedule> _dayItems = new ArrayList<Schedule>();
	private ListView lvSchedules;
	private ScheduleAdapter _dayAdapter;

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
		if (_mode == Mode.Day) {
			showDayView();
		} else if (_mode == Mode.Month) {
			showMonthView();
		} else if (_mode == Mode.Week) {
			showWeekView();
		}
	}

	// Day View
	private void showDayView() {
		_calendar = Calendar.getInstance();
		_calendar.set(Calendar.HOUR_OF_DAY, 0);
		_calendar.set(Calendar.SECOND, 0);
		_calendar.set(Calendar.MINUTE, 0);
		_calendar.set(Calendar.MILLISECOND, 0);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		rlContent.removeAllViews();
		View v = inflater.inflate(R.layout.calendar_day, rlContent);
		tvTitle = ((TextView) v.findViewById(R.id.tvTitle));
		rlHours = (RelativeLayout) v.findViewById(R.id.rlHours);
		timeRuler = (View) v.findViewById(R.id.timeRuler);
		dtDate = ((DatePicker) v.findViewById(R.id.dtDate));
		lvSchedules = (ListView) v.findViewById(R.id.lvSchedules);
		lvSchedules
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						Schedule s = _dayAdapter.getItem(arg2);
						showInternalEvent(s.Id);
					}
				});
		v.findViewById(R.id.scHours).setOnTouchListener(
				new View.OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return null == gestureDetector
								|| gestureDetector.onTouchEvent(event);
					}
				});

		dtDate.init(_calendar.get(Calendar.YEAR),
				_calendar.get(Calendar.MONTH), _calendar.get(Calendar.DATE),
				dayPickerListener);
		_dayAdapter = new ScheduleAdapter(getActivity(), _dayItems);
		lvSchedules.setAdapter(_dayAdapter);

		refreshDayView();
		reloadSchedules();
	}

	private boolean loading;

	private DatePicker.OnDateChangedListener dayPickerListener = new DatePicker.OnDateChangedListener() {

		@Override
		public void onDateChanged(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			if (loading) {
				return;
			}
			_calendar.set(year, monthOfYear, dayOfMonth);
			refreshDayView();
			reloadSchedules();
		}
	};

	private Button getScheduleButton(Schedule schedule) {
		Calendar startTime = U.getTime(schedule.StartTime);
		Calendar endTime = U.getTime(schedule.EndTime);
		int height = (int) ((endTime.getTimeInMillis() - startTime
				.getTimeInMillis()) / (60 * 1000));
		int topMargin = startTime.get(Calendar.HOUR_OF_DAY) * 60
				+ startTime.get(Calendar.MINUTE);
		Button btn = new Button(getActivity());

		btn.setText(schedule.Title);

		btn.setOnClickListener(inAppScheduleClickListener);
		btn.setTag(R.id.schedule_id, schedule.Id);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				(RelativeLayout.LayoutParams) tvTitle.getLayoutParams());

		params.setMargins(70, topMargin, 0, 0);
		btn.setLayoutParams(params);

		btn.setHeight(height == 0 ? MIN_HEIGHT : height);
		btn.setTag(height == 0 ? MIN_HEIGHT : height);
		return btn;
	}

	private void loadDaySchedules() {
		rlHours.removeAllViews();
		rlHours.addView(timeRuler);
		for (Schedule schedule : _dayItems) {
			Calendar sDate = U.getFormattedDate(schedule.Date);
			sDate.set(Calendar.HOUR_OF_DAY, 0);
			sDate.set(Calendar.MINUTE, 0);
			sDate.set(Calendar.SECOND, 0);
			sDate.set(Calendar.MILLISECOND, 0);
			rlHours.addView(getScheduleButton(schedule));
		}

		List<ViewHolder> lstArrangement = new ArrayList<ViewHolder>();
		for (int i = 0; i < rlHours.getChildCount(); ++i) {
			View v = rlHours.getChildAt(i);
			if (!(v instanceof Button)) {
				continue;
			}
			Button btn = (Button) v;
			ViewHolder vh = null;
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btn
					.getLayoutParams();
			Object tag = btn.getTag();
			if (null == tag) {
				continue;
			}
			int height = Integer.parseInt(tag.toString());
			int btnTop = params.topMargin, btnBottom = params.topMargin
					+ height;
			for (ViewHolder tvh : lstArrangement) {
				if ((btnTop >= tvh.Top && btnTop <= tvh.Bottom)
						|| (btnBottom >= tvh.Top && btnBottom <= tvh.Bottom)
						|| (tvh.Top >= btnTop && tvh.Bottom <= btnBottom)) {
					vh = tvh;
					if (vh.Top > btnTop) {
						vh.Top = btnTop;
					}
					if (vh.Bottom < btnBottom) {
						vh.Bottom = btnBottom;
					}
					vh.Items.add(btn);
				}
			}
			if (null == vh) {
				vh = new ViewHolder();
				vh.Top = btnTop;
				vh.Bottom = btnBottom;
				lstArrangement.add(vh);
				vh.Items.add(btn);
			}

		}

		for (ViewHolder vh : lstArrangement) {
			if (vh.Items.size() > 0) {
				int maxWidth = (rlContent.getWidth() - dtDate.getWidth())
						/ vh.Items.size();
				int left = 70;
				for (Button btn : vh.Items) {
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
							(RelativeLayout.LayoutParams) btn.getLayoutParams());

					params.setMargins(left, params.topMargin, 0, 0);
					btn.setLayoutParams(params);
					btn.setWidth(maxWidth);
					left += maxWidth;
				}
			}
		}
		if (null != _dayAdapter) {
			_dayAdapter.notifyDataSetChanged();
		}
	}

	private void refreshDayView() {
		if (null == rlHours || null == tvTitle) {
			return;
		}
		rlHours.removeAllViews();
		rlHours.addView(timeRuler);
		int dayOfWeek = _calendar.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SUNDAY) {
			dayOfWeek = 6;
		} else {
			dayOfWeek -= 2;
		}
		loading = true;
		dtDate.init(_calendar.get(Calendar.YEAR),
				_calendar.get(Calendar.MONTH),
				_calendar.get(Calendar.DAY_OF_MONTH), dayPickerListener);
		tvTitle.setText(String.format("%d %s, %s %d",
				_calendar.get(Calendar.DATE), DAYS[dayOfWeek],
				MONTH_NAMES[_calendar.get(Calendar.MONTH)],
				_calendar.get(Calendar.YEAR)));
		dayOfWeek = _calendar.get(Calendar.DAY_OF_WEEK);

		loading = false;
	}

	// Week View
	AdapterView.OnItemClickListener weekViewItemClick = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Schedule s = (Schedule) ((ListView) arg0).getAdapter()
					.getItem(arg2);
			showInternalEvent(s.Id);
		}
	};

	private void showWeekView() {
		_calendar = Calendar.getInstance();
		_calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		_calendar.set(Calendar.HOUR_OF_DAY, 0);
		_calendar.set(Calendar.SECOND, 0);
		_calendar.set(Calendar.MINUTE, 0);
		_calendar.set(Calendar.MILLISECOND, 0);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		rlContent.removeAllViews();
		View v = inflater.inflate(R.layout.calendar_week, rlContent);
		llWeekViewParent = v.findViewById(R.id.llParent);
		v.findViewById(R.id.llParent).setOnTouchListener(
				new View.OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return null == gestureDetector
								|| gestureDetector.onTouchEvent(event);
					}
				});
		View vMonday = ((ViewStub) v.findViewById(R.id.vsMonday)).inflate();
		View vTuesday = ((ViewStub) v.findViewById(R.id.vsTuesday)).inflate();
		View vWednesday = ((ViewStub) v.findViewById(R.id.vsWednesday))
				.inflate();
		View vThursday = ((ViewStub) v.findViewById(R.id.vsThursday)).inflate();
		View vFriday = ((ViewStub) v.findViewById(R.id.vsFriday)).inflate();
		View vSaturday = ((ViewStub) v.findViewById(R.id.vsSaturday)).inflate();
		View vSunday = ((ViewStub) v.findViewById(R.id.vsSunday)).inflate();

		lvMonday = (ListView) vMonday.findViewById(R.id.lvSchedule);
		lvTuesday = (ListView) vTuesday.findViewById(R.id.lvSchedule);
		lvWednesday = (ListView) vWednesday.findViewById(R.id.lvSchedule);
		lvThursday = (ListView) vThursday.findViewById(R.id.lvSchedule);
		lvFriday = (ListView) vFriday.findViewById(R.id.lvSchedule);
		lvSaturday = (ListView) vSaturday.findViewById(R.id.lvSchedule);
		lvSunday = (ListView) vSunday.findViewById(R.id.lvSchedule);

		lvMonday.setOnItemClickListener(weekViewItemClick);
		lvTuesday.setOnItemClickListener(weekViewItemClick);
		lvWednesday.setOnItemClickListener(weekViewItemClick);
		lvThursday.setOnItemClickListener(weekViewItemClick);
		lvFriday.setOnItemClickListener(weekViewItemClick);
		lvSaturday.setOnItemClickListener(weekViewItemClick);
		lvSunday.setOnItemClickListener(weekViewItemClick);

		_mondaySchedule = new ArrayList<Schedule>();
		_tuesdaySchedule = new ArrayList<Schedule>();
		_wednesdaySchedule = new ArrayList<Schedule>();
		_thursdaySchedule = new ArrayList<Schedule>();
		_fridaySchedule = new ArrayList<Schedule>();
		_saturdaySchedule = new ArrayList<Schedule>();
		_sundaySchedule = new ArrayList<Schedule>();

		lvMonday.setAdapter(new ScheduleAdapter(getActivity(), _mondaySchedule));
		lvTuesday.setAdapter(new ScheduleAdapter(getActivity(),
				_tuesdaySchedule));
		lvWednesday.setAdapter(new ScheduleAdapter(getActivity(),
				_wednesdaySchedule));
		lvThursday.setAdapter(new ScheduleAdapter(getActivity(),
				_thursdaySchedule));
		lvFriday.setAdapter(new ScheduleAdapter(getActivity(), _fridaySchedule));
		lvSaturday.setAdapter(new ScheduleAdapter(getActivity(),
				_saturdaySchedule));
		lvSunday.setAdapter(new ScheduleAdapter(getActivity(), _sundaySchedule));

		tvMonday = (TextView) vMonday.findViewById(R.id.tvTitle);
		tvTuesday = (TextView) vTuesday.findViewById(R.id.tvTitle);
		tvWednesday = (TextView) vWednesday.findViewById(R.id.tvTitle);
		tvThursday = (TextView) vThursday.findViewById(R.id.tvTitle);
		tvFriday = (TextView) vFriday.findViewById(R.id.tvTitle);
		tvSaturday = (TextView) vSaturday.findViewById(R.id.tvTitle);
		tvSunday = (TextView) vSunday.findViewById(R.id.tvTitle);

		tvWeekMonthYear = (TextView) v.findViewById(R.id.tvWeekMonthYear);

		btnWeek1 = ((Button) v.findViewById(R.id.btnWeek1));
		btnWeek1.setOnClickListener(weekClickListener);
		btnWeek2 = ((Button) v.findViewById(R.id.btnWeek2));
		btnWeek2.setOnClickListener(weekClickListener);
		btnWeek3 = ((Button) v.findViewById(R.id.btnWeek3));
		btnWeek3.setOnClickListener(weekClickListener);
		btnWeek4 = ((Button) v.findViewById(R.id.btnWeek4));
		btnWeek4.setOnClickListener(weekClickListener);
		btnWeek5 = ((Button) v.findViewById(R.id.btnWeek5));
		btnWeek5.setOnClickListener(weekClickListener);

		btnDecrementMonth = ((Button) v.findViewById(R.id.btnDecrementMonth));
		btnDecrementMonth.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				_calendar.add(Calendar.MONTH, -1);
				_calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				refreshWeekView();
				reloadSchedules();
			}
		});

		btnIncrementMonth = ((Button) v.findViewById(R.id.btnIncrementMonth));
		btnIncrementMonth.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				_calendar.add(Calendar.MONTH, 1);
				_calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				refreshWeekView();
				reloadSchedules();
			}
		});
		refreshWeekView();
		reloadSchedules();
	}

	private View.OnClickListener weekClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnWeek1:
				_calendar.add(Calendar.WEEK_OF_YEAR, -2);
				refreshWeekView();
				reloadSchedules();
				break;
			case R.id.btnWeek2:
				_calendar.add(Calendar.WEEK_OF_YEAR, -1);
				refreshWeekView();
				reloadSchedules();
				break;
			case R.id.btnWeek4:
				_calendar.add(Calendar.WEEK_OF_YEAR, 1);
				refreshWeekView();
				reloadSchedules();
				break;
			case R.id.btnWeek5:
				_calendar.add(Calendar.WEEK_OF_YEAR, 2);
				refreshWeekView();
				reloadSchedules();
				break;
			}
		}
	};

	private void refreshWeekView() {
		if (null != tvWeekMonthYear) {
			Calendar cal = Calendar.getInstance();
			cal.set(_calendar.get(Calendar.YEAR),
					_calendar.get(Calendar.MONTH), _calendar.get(Calendar.DATE));
			int day = cal.get(Calendar.DATE);
			cal.add(Calendar.WEEK_OF_YEAR, 1);
			int day7 = cal.get(Calendar.DATE);
			tvWeekMonthYear.setText(String.format("%d-%d, %s %d", day, day7,
					MONTH_NAMES[_calendar.get(Calendar.MONTH)],
					cal.get(Calendar.YEAR)));
			int month = cal.get(Calendar.MONTH);
			btnWeek1.setText("");
			if (month == 0) {
				btnDecrementMonth.setText(MONTH_NAMES[11]);
			} else {
				btnDecrementMonth.setText(MONTH_NAMES[month - 1]);
			}
			if (month == 1) {
				btnIncrementMonth.setText(MONTH_NAMES[0]);
			} else {
				btnIncrementMonth.setText(MONTH_NAMES[month + 1]);
			}

			cal.set(_calendar.get(Calendar.YEAR),
					_calendar.get(Calendar.MONTH), _calendar.get(Calendar.DATE));
			// Week 1
			cal.add(Calendar.WEEK_OF_YEAR, -2);
			day = cal.get(Calendar.DATE);
			cal.add(Calendar.DATE, 6);
			day7 = cal.get(Calendar.DATE);
			btnWeek1.setText(String.format("%d - %d %s", day, day7,
					MONTH_NAMES[cal.get(Calendar.MONTH)]));
			cal.add(Calendar.DATE, 1);
			// Week 2
			day = cal.get(Calendar.DATE);
			cal.add(Calendar.DATE, 6);
			day7 = cal.get(Calendar.DATE);
			btnWeek2.setText(String.format("%d - %d %s", day, day7,
					MONTH_NAMES[cal.get(Calendar.MONTH)]));
			cal.add(Calendar.DATE, 1);
			// Week 3
			day = cal.get(Calendar.DATE);
			cal.add(Calendar.DATE, 6);
			day7 = cal.get(Calendar.DATE);
			btnWeek3.setText(String.format("%d - %d %s", day, day7,
					MONTH_NAMES[cal.get(Calendar.MONTH)]));
			cal.add(Calendar.DATE, 1);
			// Week 4
			day = cal.get(Calendar.DATE);
			cal.add(Calendar.DATE, 6);
			day7 = cal.get(Calendar.DATE);
			btnWeek4.setText(String.format("%d - %d %s", day, day7,
					MONTH_NAMES[cal.get(Calendar.MONTH)]));
			cal.add(Calendar.DATE, 1);
			// Week 5
			day = cal.get(Calendar.DATE);
			cal.add(Calendar.DATE, 6);
			day7 = cal.get(Calendar.DATE);
			btnWeek5.setText(String.format("%d - %d %s", day, day7,
					MONTH_NAMES[cal.get(Calendar.MONTH)]));
		}
	}

	private void loadWeekSchedule() {
		Calendar cal = Calendar.getInstance();
		cal.set(_calendar.get(Calendar.YEAR), _calendar.get(Calendar.MONTH),
				_calendar.get(Calendar.DATE));
		int day = cal.get(Calendar.DATE);
		// filterWeek();

		day = cal.get(Calendar.DATE);
		tvMonday.setText(String.format("Mon %d", day));
		cal.add(Calendar.DATE, 1);
		day = cal.get(Calendar.DATE);
		tvTuesday.setText(String.format("Tue %d", day));
		cal.add(Calendar.DATE, 1);
		day = cal.get(Calendar.DATE);
		tvWednesday.setText(String.format("Wed %d", day));
		cal.add(Calendar.DATE, 1);
		day = cal.get(Calendar.DATE);
		tvThursday.setText(String.format("Thu %d", day));
		cal.add(Calendar.DATE, 1);
		day = cal.get(Calendar.DATE);
		tvFriday.setText(String.format("Fri %d", day));
		cal.add(Calendar.DATE, 1);
		day = cal.get(Calendar.DATE);
		tvSaturday.setText(String.format("Sat %d", day));
		cal.add(Calendar.DATE, 1);
		day = cal.get(Calendar.DATE);
		tvSunday.setText(String.format("Sun %d", day));

		ScheduleAdapter adapter = (ScheduleAdapter) lvMonday.getAdapter();
		adapter.notifyDataSetChanged();
		adapter = (ScheduleAdapter) lvTuesday.getAdapter();
		adapter.notifyDataSetChanged();
		adapter = (ScheduleAdapter) lvWednesday.getAdapter();
		adapter.notifyDataSetChanged();
		adapter = (ScheduleAdapter) lvThursday.getAdapter();
		adapter.notifyDataSetChanged();
		adapter = (ScheduleAdapter) lvFriday.getAdapter();
		adapter.notifyDataSetChanged();
		adapter = (ScheduleAdapter) lvSaturday.getAdapter();
		adapter.notifyDataSetChanged();
		adapter = (ScheduleAdapter) lvSunday.getAdapter();
		adapter.notifyDataSetChanged();
	}

	public class WeekScheduleAdapter extends ArrayAdapter<Schedule> {
		public WeekScheduleAdapter(Context context, ArrayList<Schedule> lstItems) {
			super(context, R.id.tvTitle, lstItems);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RelativeLayout itemView;

			// Inflate the view
			if (convertView == null) {
				itemView = new RelativeLayout(getContext());
				String inflater = Context.LAYOUT_INFLATER_SERVICE;
				LayoutInflater vi;
				vi = (LayoutInflater) getContext().getSystemService(inflater);
				vi.inflate(R.layout.week_row, itemView, true);
			} else {
				itemView = (RelativeLayout) convertView;
			}
			Schedule sc = getItem(position);
			TextView tvTitle = (TextView) itemView.findViewById(R.id.text1);
			tvTitle.setText(sc.Title + " " + sc.StartTime);

			return itemView;
		}
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
						//showDialog(sl, "dialog2");
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

	private View.OnClickListener inAppScheduleClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag(R.id.schedule_id) != null) {
				String eid = (String) v.getTag(R.id.schedule_id);
				showInternalEvent(eid);
			}
		}
	};

	private void showInternalEvent(String eid) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(AndroidCalendar.getViewEventUri(eid));
		getActivity().startActivity(intent);
	}

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
			if (_mode == Mode.Day) {
				_scheduleTask._dayItems = AndroidCalendar.getEvents(
						getActivity().getContentResolver(), _calendar);
			} else if (_mode == Mode.Week) {
				Calendar cal = Calendar.getInstance();
				cal.set(_calendar.get(Calendar.YEAR),
						_calendar.get(Calendar.MONTH),
						_calendar.get(Calendar.DATE), 0, 0, 0);
				cal.set(Calendar.MILLISECOND, 0);
				for (int i = 0; i < 7; ++i) {
					ArrayList<Schedule> schedule = AndroidCalendar.getEvents(
							getActivity().getContentResolver(), cal);
					switch (i) {
					case 0:
						_scheduleTask._mondaySchedule = schedule;
						break;
					case 1:
						_scheduleTask._tuesdaySchedule = schedule;
						break;
					case 2:
						_scheduleTask._wednesdaySchedule = schedule;
						break;
					case 3:
						_scheduleTask._thursdaySchedule = schedule;
						break;
					case 4:
						_scheduleTask._fridaySchedule = schedule;
						break;
					case 5:
						_scheduleTask._saturdaySchedule = schedule;
						break;
					case 6:
						_scheduleTask._sundaySchedule = schedule;
						break;
					}
					cal.add(Calendar.DATE, 1);
				}
			} else if (_mode == Mode.Month) {
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
	}

	@Override
	public void onRunCompleted(int token) {
		pbActivity.setVisibility(View.INVISIBLE);
		if (_mode == Mode.Day) {
			_dayItems.clear();
			_dayItems.addAll(_scheduleTask._dayItems);
			loadDaySchedules();
		} else if (_mode == Mode.Week) {
			_mondaySchedule.clear();
			_mondaySchedule.addAll(_scheduleTask._mondaySchedule);
			_tuesdaySchedule.clear();
			_tuesdaySchedule.addAll(_scheduleTask._tuesdaySchedule);
			_wednesdaySchedule.clear();
			_wednesdaySchedule.addAll(_scheduleTask._wednesdaySchedule);
			_thursdaySchedule.clear();
			_thursdaySchedule.addAll(_scheduleTask._thursdaySchedule);
			_fridaySchedule.clear();
			_fridaySchedule.addAll(_scheduleTask._fridaySchedule);
			_saturdaySchedule.clear();
			_saturdaySchedule.addAll(_scheduleTask._saturdaySchedule);
			_sundaySchedule.clear();
			_sundaySchedule.addAll(_scheduleTask._sundaySchedule);
			loadWeekSchedule();
		} else if (_mode == Mode.Month) {
			for (String key : _scheduleTask._mapping.keySet()) {
				_mapping.put(key, _scheduleTask._mapping.get(key));
			}
			loadMonthSchedule();
		}
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
		if (_mode == Mode.Day) {
			_calendar.add(Calendar.DAY_OF_MONTH, -1);
			refreshDayView();
			animateViewLeft(rlHours);
		} else if (_mode == Mode.Week) {
			_calendar.add(Calendar.WEEK_OF_YEAR, -1);
			refreshWeekView();
			animateViewLeft(llWeekViewParent);
		} else if (_mode == Mode.Month) {
			_calendar.add(Calendar.MONTH, -1);
			refreshMonthView();
			animateViewLeft(gvDays);
		}
		reloadSchedules();
	}

	@Override
	public void rightSwipe() {
		if (_mode == Mode.Day) {
			_calendar.add(Calendar.DAY_OF_MONTH, 1);
			refreshDayView();
			animateViewRight(rlHours);
		} else if (_mode == Mode.Week) {
			_calendar.add(Calendar.WEEK_OF_YEAR, 1);
			refreshWeekView();
			animateViewRight(llWeekViewParent);
		} else if (_mode == Mode.Month) {
			_calendar.add(Calendar.MONTH, 1);
			refreshMonthView();
			animateViewRight(gvDays);
		}
		reloadSchedules();
	}

}
