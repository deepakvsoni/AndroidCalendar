package com.deepak.calendar;

import android.os.AsyncTask;

public class GenericAsyncTask extends AsyncTask<Void, Void, Void> {
	private int _token;
	private GenericAsyncTaskRun _run;
	private TaskStatus _status;

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

	public GenericAsyncTask(GenericAsyncTaskRun activity, int token) {
		_run = activity;
		_token = token;
		_status = TaskStatus.Instantiated;
	}
	
	@Override
	protected void onPreExecute () {
		if(null != _run) {
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