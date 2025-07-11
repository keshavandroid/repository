package com.android.reloop.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class InfiniteViewPager extends ViewPager {
	
	private boolean infinitePagesEnabled = true;

	public InfiniteViewPager(Context context) {
		super(context);
	}

	public InfiniteViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setAdapter(PagerAdapter adapter) {
		super.setAdapter(adapter);
		// offset first element so that we can scroll to the left
		setCurrentItem(0);
	}

	@Override
	public void setCurrentItem(int item) {
		if (infinitePagesEnabled) {
			// offset the current item to ensure there is space to scroll
			item = getOffsetAmount() + (item % getAdapter().getCount());
		}
		super.setCurrentItem(item);

	}
	
	public void enableInfinitePages(boolean enable) {
		infinitePagesEnabled = enable;
		if (getAdapter() instanceof InfinitePagerAdapter) {
			InfinitePagerAdapter infAdapter = (InfinitePagerAdapter) getAdapter();
			infAdapter.enableInfinitePages(enable);
		}
	}

	private int getOffsetAmount() {
		if (getAdapter() instanceof InfinitePagerAdapter) {
			InfinitePagerAdapter infAdapter = (InfinitePagerAdapter) getAdapter();
			// allow for 100 back cycles from the beginning
			// should be enough to create an illusion of infinity
			// warning: scrolling to very high values (1,000,000+) results in
			// strange drawing behaviour
			return infAdapter.getRealCount() * 100;
		} else {
			return 0;
		}
	}

}
