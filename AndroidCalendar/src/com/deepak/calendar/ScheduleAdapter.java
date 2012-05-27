package com.deepak.calendar;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ScheduleAdapter extends ArrayAdapter<Schedule> {
	public ScheduleAdapter(Context context, List<Schedule> lstItems) {
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
			vi.inflate(R.layout.schedule_item, itemView, true);
		} else {
			itemView = (RelativeLayout) convertView;
		}
		Schedule item = getItem(position);
		TextView tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
		TextView tvTime = (TextView) itemView.findViewById(R.id.tvTime);
		TextView tvNotes = (TextView) itemView.findViewById(R.id.tvNotes);
		TextView tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);

		tvTime.setText(String.format("%s - %s",
				item.StartTime.replace('.', ':'),
				item.EndTime.replace('.', ':')));
		tvTitle.setText(item.Title);
		tvNotes.setText(item.Notes);
		tvLocation.setText(item.Venue);

		return itemView;
	}
}
