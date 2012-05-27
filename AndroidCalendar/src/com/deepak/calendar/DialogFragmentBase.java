package com.deepak.calendar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class DialogFragmentBase extends DialogFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light_Dialog);
	}
	
	@Override
	public void onDestroyView() {
		Dialog dialog = getDialog();
		if (null != dialog) {
			dialog.setDismissMessage(null);
		}
		super.onDestroyView();
	}
	
	public void showDialog(DialogFragmentBase fragment, String tag) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prev = getFragmentManager().findFragmentByTag(tag);
		if (prev != null) {
			ft.remove(prev);
		}

		// Create and show the dialog.
		fragment.show(ft, tag);
	}
}