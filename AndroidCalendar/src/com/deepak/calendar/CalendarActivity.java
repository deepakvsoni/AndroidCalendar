package com.deepak.calendar;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class CalendarActivity extends Activity implements ActionBar.TabListener {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(android.R.style.Theme_Holo_Light);
		setContentView(R.layout.main);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		ActionBar.Tab dayTab = actionBar.newTab();
		dayTab.setText(getString(R.string.day));
		dayTab.setTag(0);
		dayTab.setTabListener(this);

		ActionBar.Tab weekTab = actionBar.newTab();
		weekTab.setText(getString(R.string.week));
		weekTab.setTag(1);
		weekTab.setTabListener(this);

		ActionBar.Tab monthTab = actionBar.newTab();
		monthTab.setText(getString(R.string.month));
		monthTab.setTag(2);
		monthTab.setTabListener(this);

		actionBar.addTab(dayTab);
		actionBar.addTab(weekTab);
		actionBar.addTab(monthTab);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction arg1) {
		
		
	}
	
	private void replaceFragment(Fragment fragment, int id,
			boolean blnAddToBackStack) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(id, fragment, fragment.getClass().getName());
		// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		// if (false && blnAddToBackStack) {
		// ft.addToBackStack(null);
		// }
		ft.commit();
	}

	private void showFragment(Fragment fragment, int id,
			boolean addToBackStack, boolean popBackStack) {
		if (null == fragment) {
			return;
		}
		Class<?> type = fragment.getClass();

		Fragment details = getFragmentManager().findFragmentById(id);
		if (null == details) {
			replaceFragment(fragment, id, addToBackStack);
			return;
		}
		if (type.isInstance(details) && details.isHidden()) {
			return;
		}
		if (type.isInstance(details) && !details.isAdded()) {
			replaceFragment(fragment, id, addToBackStack);
			return;
		}
		if (popBackStack && !type.isInstance(details) && details.isAdded()) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.remove(details);
			getFragmentManager().popBackStack(details.getClass().getName(), 0);
			ft.commit();
		}
		replaceFragment(fragment, id, addToBackStack);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (tab.getTag() != null) {
			int index = Integer.parseInt(tab.getTag().toString());
			switch(index) {
			case 0:
				DayCalendarFragment f = new DayCalendarFragment();
				showFragment(f, R.id.flDetail, false, true);
				break;
			case 1:
				WeekCalendarFragmentBase f3 = new WeekCalendarFragmentBase();
				showFragment(f3, R.id.flDetail, false, true);
				break;
			case 2:
				MonthCalendarFragment f2 = new MonthCalendarFragment();
				showFragment(f2, R.id.flDetail, false, true);
				break;
		
			}
		}
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
}