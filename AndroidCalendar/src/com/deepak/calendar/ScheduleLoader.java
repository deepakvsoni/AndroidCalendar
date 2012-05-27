package com.deepak.calendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.AsyncTask;

public class ScheduleLoader  extends AsyncTask<Void, Void, Void> {
	private int _token;
	private GenericAsyncTaskRun _run;
	private TaskStatus _status;
	public ArrayList<Schedule> _mondaySchedule, _tuesdaySchedule,
			_wednesdaySchedule, _thursdaySchedule, _fridaySchedule,
			_saturdaySchedule, _sundaySchedule;

	public HashMap<String, ArrayList<Schedule>> _mapping = new HashMap<String, ArrayList<Schedule>>();
	public List<Schedule> _dayItems = new ArrayList<Schedule>();
	
	public int getToken() {
		return _token;
	}

	public TaskStatus getState() {
		return _status;
	}

	public GenericAsyncTaskRun getRun() {
		return _run;
	}

	public void setRun(GenericAsyncTaskRun activity) {
		this._run = activity;
		if (_status == TaskStatus.Completed) {
			notifyCompleted();
		}
	}

	private void notifyCompleted() {
		if (null != _run) {
			_run.onRunCompleted(_token);
		}
	}

	public ScheduleLoader(GenericAsyncTaskRun activity, int token) {
		_run = activity;
		_token = token;
		_status = TaskStatus.Instantiated;
	}

	@Override
	protected void onPreExecute() {
		if (null != _run) {
			_run.onPreExecute(_token);
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		_status = TaskStatus.Running;
		if (null != _run) {
			_run.run(_token);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		_status = TaskStatus.Completed;
		notifyCompleted();
	}
}