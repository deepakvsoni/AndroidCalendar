package com.deepak.calendar;

public interface GenericAsyncTaskRun {
	public void run(int token);
	public void onRunCompleted(int token);
	public void onPreExecute(int token);

}
