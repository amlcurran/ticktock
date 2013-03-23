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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Author: Alex Curran
 * Date: 22/03/2013
 */
public class TickTockProvider extends ContentProvider {

	static final int LOADER_FULL_LIST = 1;
	static final int LOADER_SINGLE_ITEM = 2;

	static final String ENTRY_SINGLE = "vnd.android.cursor.item/ticktock";
	static final String ENTRY_MULTIPLE = "vnd.android.cursor.dir/ticktock";

	static final Uri countdownUri = Uri.parse("content://com.espian.ticktock/entry");

	public static final String TABLE_COUNTDOWNS = "countdowns";

	DatabaseHelper dbHelper;

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext(), 1);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (uri.equals(countdownUri)) {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			return database.query(TABLE_COUNTDOWNS, projection, selection, selectionArgs, null, null, sortOrder);
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if (uri.equals(countdownUri)) {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			long id = database.insert(TABLE_COUNTDOWNS, "label", values);
			return ContentUris.withAppendedId(countdownUri, id);
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (uri.equals(countdownUri)) {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			return database.delete(TABLE_COUNTDOWNS, selection, selectionArgs);
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if (uri.equals(countdownUri)) {
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			return database.update(TABLE_COUNTDOWNS, values, selection, selectionArgs);
		}
		return 0;
	}

	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, int version) {
			super(context, "ticktock.db", null, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE countdowns (" +
					BaseColumns._ID + " INTEGER PRIMARY KEY, " +
					"label TEXT, " +
					"date TEXT, " +
					"notify TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}

}
