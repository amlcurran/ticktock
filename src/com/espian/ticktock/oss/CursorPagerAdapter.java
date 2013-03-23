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

package com.espian.ticktock.oss;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import java.lang.ref.WeakReference;

public class CursorPagerAdapter<F extends Fragment> extends FragmentStatePagerAdapter {

	private final Class<F> fragmentClass;
	private final String[] projection;
	private final Class<? extends Fragment> emptyFragmentClass;
	private final SparseArray<WeakReference<F>> fragmentRefs = new SparseArray<WeakReference<F>>();
	private Cursor cursor;
	private boolean hasEmptyFragment = true;

	public CursorPagerAdapter(FragmentManager fm, Class<F> fragmentClass, Class<? extends Fragment> emptyFragmentClass,
	                          String[] projection, Cursor cursor) {
		super(fm);
		this.fragmentClass = fragmentClass;
		this.emptyFragmentClass = emptyFragmentClass;
		this.projection = projection;
		this.cursor = cursor;
		hasEmptyFragment = true;
	}

	@Override
	public Fragment getItem(int position) {
		if (cursor == null || cursor.getCount() == 0) {
			if (emptyFragmentClass != null) {
				try {
					return emptyFragmentClass.newInstance();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			return null;
		}

		if (fragmentRefs.get(position) != null &&
				fragmentRefs.get(position).get() != null) return fragmentRefs.get(position).get();

		cursor.moveToPosition(position);
		F frag;
		try {
			frag = fragmentClass.newInstance();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		Bundle args = new Bundle();
		if (projection == null || projection.length == 0) {
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				args.putString(cursor.getColumnName(i), cursor.getString(i));
			}
		} else {
			for (int i = 0; i < projection.length; ++i) {
				args.putString(projection[i], cursor.getString(i));
			}
		}
		frag.setArguments(args);
		fragmentRefs.put(position, new WeakReference<F>(frag));
		return frag;
	}

	@Override
	public int getCount() {
		if (hasEmptyFragment) return 1;
		if (cursor == null) return 0;
		else return cursor.getCount();
	}

	public void swapCursor(Cursor c) {
		if (cursor == c)
			return;

		this.cursor = c;
		notifyDataSetChanged();
	}

	public Cursor getCursor() {
		return cursor;
	}


}
