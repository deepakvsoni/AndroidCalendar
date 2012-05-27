package com.deepak.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
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
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DayCalendarFragment extends CalendarFragmentBase implements
		GenericAsyncTaskRun {
	private GestureDetector gestureDetector;

	public static int MIN_HEIGHT = 2;
	public static int ID = 2;
	private Mode _mode = Mode.Day;
	private Calendar _calendar;
	private RelativeLayout rlContent;
	private ProgressBar pbActivity;
	private HashMap<String, ArrayList<Schedule>> _mapping = new HashMap<String, ArrayList<Schedule>>();
	private ScheduleLoader _scheduleTask;
	private ScheduleObjectContainer _container = new ScheduleObjectContainer();

	// Day View Controls
	private TextView tvTitle;
	private View timeRuler;
	private RelativeLayout rlHours;
	private DatePicker dtDate;
	private List<Schedule> _dayItems = new ArrayList<Schedule>();
	private ListView lvSchedules;
	private ScheduleAdapter _dayAdapter;

	private final String[] MONTH_NAMES = { "January", "February", "March",
			"April", "May", "June", "July", "August", "September", "October",
			"November", "December" };

	final private String[] DAYS = { "Monday", "Tuesday", "Wednesday",
			"Thursday", "Friday", "Saturday", "Sunday" };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.schedule_calendar, container, false);
		_calendar = Calendar.getInstance();
		pbActivity = (ProgressBar) v.findViewById(R.id.pbActivity);
		rlContent = (RelativeLayout) v.findViewById(R.id.rlContent);
		showDayView();
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
		_calendar.add(Calendar.DAY_OF_MONTH, -1);
		refreshDayView();
		animateViewLeft(rlHours);
		reloadSchedules();
	}

	@Override
	public void rightSwipe() {
		_calendar.add(Calendar.DAY_OF_MONTH, 1);
		refreshDayView();
		animateViewRight(rlHours);
		reloadSchedules();
	}

}
