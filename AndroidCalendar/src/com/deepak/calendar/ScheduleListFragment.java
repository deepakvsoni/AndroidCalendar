package com.deepak.calendar;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class ScheduleListFragment extends DialogFragmentBase {
	private List<Schedule> _schedule = null;
	private ScheduleAdapter _adapter;

	public List<Schedule> getSchedule() {
		return _schedule;
	}

	public void setSchedule(List<Schedule> schedule) {
		this._schedule = schedule;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.list, container, false);
		((TextView) v.findViewById(R.id.tvTitle))
				.setText(getString(R.string.schedule));
		ListView lv = (ListView) v.findViewById(R.id.lvItems);
		if (null != _schedule) {
			_adapter = new ScheduleAdapter(getActivity(), _schedule);
			lv.setAdapter(_adapter);
		}
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Schedule s = _schedule.get(arg2);
				showSchedule(s);
			}
		});

		return v;
	}

	private void showSchedule(Schedule s) {
		if (s._id == -1) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(AndroidCalendar.getViewEventUri(s.Id));
			getActivity().startActivity(intent);
		}
	}

}
