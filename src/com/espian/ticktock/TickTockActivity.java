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

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.espian.ticktock.oss.CursorPagerAdapter;

public class TickTockActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	CursorPagerAdapter<CountdownFragment> mPagerAdapter;
	ViewPager mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (BuildConfig.DEBUG) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyFlashScreen()
					.penaltyLog().build());
		}

		mPagerAdapter = new TitleCursorPagerAdapter(this);
		(mPager = (ViewPager) findViewById(R.id.pager)).setAdapter(mPagerAdapter);

		getLoaderManager().initLoader(0, null, this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean shouldShow = !(mPagerAdapter.getItem(mPager.getCurrentItem()) instanceof EmptyFragment);
		menu.findItem(R.id.menu_edit).setVisible(shouldShow);
		menu.findItem(R.id.menu_delete).setVisible(shouldShow);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.menu_new:
				startActivityForResult(new Intent(this, AddEditActivity.class), 1);
				break;

			case R.id.menu_delete:
				if (mPagerAdapter.getCount() == 0) Toast.makeText(this, R.string.no_delete, Toast.LENGTH_SHORT).show();
				else {
					String idString = ((CountdownFragment) mPagerAdapter.getItem(mPager.getCurrentItem())).getCountdownId();
					int i = getContentResolver().delete(TickTockProvider.countdownUri,
							BaseColumns._ID + "=?", new String[]{idString});
					if (i != 0) getLoaderManager().restartLoader(0, null, this);
					else Toast.makeText(this, R.string.failed_delete, Toast.LENGTH_SHORT).show();
					mPagerAdapter.notifyDataSetChanged();
				}
				return true;

			case R.id.menu_edit:
				if (mPagerAdapter.getCount() == 0) Toast.makeText(this, R.string.no_edit, Toast.LENGTH_SHORT).show();
				else {
					String idString2 = ((CountdownFragment) mPagerAdapter.getItem(mPager.getCurrentItem())).getCountdownId();
					startActivityForResult(new Intent(this, AddEditActivity.class).putExtra(BaseColumns._ID, idString2), 1);
				}
				return true;

		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				getLoaderManager().restartLoader(0, null, this);
			}
		}

		// Invalidate the options menu - if they've gone from 0 to 1 items
		// then we can un-hide delete and edit
		invalidateOptionsMenu();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, TickTockProvider.countdownUri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mPagerAdapter.swapCursor(data);
		// Going from 1 <-> 0 items, hide or show edit/delete
		invalidateOptionsMenu();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mPagerAdapter.swapCursor(null);
		// Going from 1 <-> 0 items, hide or show edit/delete
		invalidateOptionsMenu();
	}

}
