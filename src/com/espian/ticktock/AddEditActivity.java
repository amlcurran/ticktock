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
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.espian.ticktock.oss.LoadHideHelper;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Author: Alex Curran
 * Date: 22/03/2013
 */
public class AddEditActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private EditText mTitle;
	private CalendarPickerView mDatePicker;
	private Calendar mMaxDate;
	private boolean isEdit = false;
	private String editId;
	private LoadHideHelper mHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addedit);

		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowCustomEnabled(true);
		getActionBar().setCustomView(R.layout.title_edit_text);
		mTitle = (EditText) getActionBar().getCustomView();

		mMaxDate = Calendar.getInstance();
		mMaxDate.add(Calendar.YEAR, 2);

		mDatePicker = (CalendarPickerView) findViewById(R.id.calendarPicker);
		mDatePicker.init(new Date(), new Date(), mMaxDate.getTime());
		//mTitle = (EditText) findViewById(R.id.labelEdit);

		editId = getIntent().getStringExtra(BaseColumns._ID);
		if (isEdit = (editId != null)) {
			// Only want the fade-in loader if it's an edit, else we can load everything instantly
			mHelper = new LoadHideHelper(findViewById(R.id.loadingitems));
			getLoaderManager().initLoader(TickTockProvider.LOADER_SINGLE_ITEM, getIntent().getExtras(), this);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.addedit, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.menu_add:
				ContentValues cvs = new ContentValues();
				String label = mTitle.getText().toString();
				if (label == null || label.isEmpty()) {
					Toast.makeText(this, R.string.no_empty_label, Toast.LENGTH_SHORT).show();
					return true;
				}
				cvs.put("label", label);
				cvs.put("date", DateFormat.getDateInstance(DateFormat.LONG).format(mDatePicker.getSelectedDate()));
				if (isEdit) {

					int updateResult = getContentResolver().update(TickTockProvider.countdownUri, cvs,
							BaseColumns._ID + "=?", new String[]{editId + ""});
					if (updateResult == 1) {
						setResult(RESULT_OK);
						finish();
					} else {
						Toast.makeText(this, getString(R.string.failed_update, mTitle.getText().toString()), Toast.LENGTH_SHORT).show();
					}

				} else {
					Uri result = getContentResolver().insert(TickTockProvider.countdownUri, cvs);
					if (result != null) {
						setResult(RESULT_OK);
						finish();
					} else {
						Toast.makeText(this, getString(R.string.failed_add, mTitle.getText().toString()), Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case R.id.menu_discard:
				setResult(RESULT_CANCELED);
				finish();
				break;

		}
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, TickTockProvider.countdownUri, null, BaseColumns._ID + "=?", new String[]{editId}, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

		try {
			data.moveToFirst();
			Date editableDate = DateFormat.getDateInstance(DateFormat.LONG).parse(data.getString(2));
			mDatePicker.init(editableDate, new Date(), mMaxDate.getTime());
			mTitle.setText(data.getString(1));
			mHelper.show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, R.string.load_date_exception, Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {


	}
}
