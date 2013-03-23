/*
 * Copyright (C) 2013 Alex Curran.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.espian.ticktock;

import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import com.espian.ticktock.oss.CursorPagerAdapter;

/**
 * Author: Alex Curran
 * Date: 23/03/2013
 */
public class TitleCursorPagerAdapter extends CursorPagerAdapter<CountdownFragment> {

	private final SparseArray<String> mTitles;
	private String noItemString;

	public TitleCursorPagerAdapter(FragmentActivity act) {
		super(act.getSupportFragmentManager(), CountdownFragment.class, EmptyFragment.class, null, null);
		noItemString = act.getString(R.string.no_items);
		mTitles = new SparseArray<String>();
	}

	@Override
	public void swapCursor(Cursor c) {
		mTitles.clear();
		while (c.moveToNext()) {
			mTitles.put(c.getPosition(), c.getString(1));
		}
		c.moveToFirst();
		super.swapCursor(c);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (mTitles.size() == 0) return noItemString.toUpperCase();
		return mTitles.get(position).toUpperCase();
	}

	@Override
	public int getItemPosition(Object object) {
		return PagerAdapter.POSITION_NONE;
	}

}
