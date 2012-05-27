package com.deepak.calendar;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WeekCalendarFragmentBase extends CalendarFragmentBase implements
		GenericAsyncTaskRun {
	private GestureDetector gestureDetector;

	public static int MIN_HEIGHT = 2;
	public static int ID = 2;
	private Mode _mode = Mode.Week;
	private Calendar _calendar;
	private RelativeLayout rlContent;
	private ProgressBar pbActivity;
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

	private final String[] MONTH_NAMES = { "January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October",
			"November", "December" };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.schedule_calendar, container, false);
		_calendar = Calendar.getInstance();
		pbActivity = (ProgressBar) v.findViewById(R.id.pbActivity);
		rlContent = (RelativeLayout) v.findViewById(R.id.rlContent);
		showWeekView();
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
		showWeekView();
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
		llWeekViewParent.setOnTouchListener(
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
			cal.add(Calendar.DATE, 6);
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

	private void showInternalEvent(String eid) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(AndroidCalendar.getViewEventUri(eid));
		getActivity().startActivity(intent);
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
			ContentResolver resolver = getActivity().getContentResolver(); 
			for (int i = 0; i < 7; ++i) {
				ArrayList<Schedule> schedule = AndroidCalendar.getEvents(
						resolver, cal);
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
		}
	}

	@Override
	public void onRunCompleted(int token) {
		pbActivity.setVisibility(View.INVISIBLE);

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

	public void leftSwipe() {
		_calendar.add(Calendar.WEEK_OF_YEAR, -1);
		refreshWeekView();
		animateViewLeft(llWeekViewParent);
		reloadSchedules();
	}

	public void rightSwipe() {
		_calendar.add(Calendar.WEEK_OF_YEAR, 1);
		refreshWeekView();
		animateViewRight(llWeekViewParent);
		reloadSchedules();
	}
}