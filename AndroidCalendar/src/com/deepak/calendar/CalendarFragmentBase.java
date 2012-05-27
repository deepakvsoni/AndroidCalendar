package com.deepak.calendar;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;

public abstract class CalendarFragmentBase extends Fragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	private void replaceFragment(Fragment fragment, int id,
			boolean blnAddToBackStack) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(id, fragment, fragment.getClass().getName());
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		// if (blnAddToBackStack) {
		// ft.addToBackStack(null);
		// }
		ft.commit();
	}

	protected void showFragment(Fragment fragment, int id,
			boolean addToBackStack, boolean popBackStack) {
		Fragment details = getFragmentManager().findFragmentById(id);
		if (null == fragment) {
			if (null != details && details.isAdded()) {
				FragmentTransaction ft = getFragmentManager()
						.beginTransaction();
				ft.remove(details);
				getFragmentManager().popBackStack();
				ft.commit();
			}
			return;
		}
		Class<?> type = fragment.getClass();
		if (null == details) {
			replaceFragment(fragment, id, addToBackStack);
			return;
		}
		if (type.isInstance(details) && details.isAdded()
				&& details.isVisible()) {
			return;
		}
		if (type.isInstance(details) && !details.isAdded()) {
			replaceFragment(fragment, id, addToBackStack);
			return;
		}
		if (popBackStack && !type.isInstance(details) && details.isAdded()) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.remove(details);
			getFragmentManager().popBackStack();
			ft.commit();
		}
		replaceFragment(fragment, id, addToBackStack);
	}

	// Common
	protected void animateViewRight(View viewToBeAnimated) {

		ObjectAnimator mover = ObjectAnimator.ofFloat(viewToBeAnimated,
				"translationX", -viewToBeAnimated.getWidth(), 0f);
		mover.setDuration(0);
		mover.start();
		mover = ObjectAnimator.ofFloat(viewToBeAnimated, "translationX",
				viewToBeAnimated.getWidth(), 0f);
		mover.setDuration(150);
		mover.start();
	}

	protected void animateViewLeft(View viewToBeAnimated) {
		ObjectAnimator mover = ObjectAnimator.ofFloat(viewToBeAnimated,
				"translationX", viewToBeAnimated.getWidth(), 0f);
		mover.setDuration(0);
		mover.start();
		mover = ObjectAnimator.ofFloat(viewToBeAnimated, "translationX",
				-viewToBeAnimated.getWidth(), 0f);
		mover.setDuration(150);
		mover.start();
	}

	public abstract void leftSwipe();

	public abstract void rightSwipe();
}