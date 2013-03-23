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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.espian.ticktock.oss.LoadHideHelper;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Author: Alex Curran
 * Date: 22/03/2013
 */
public class CountdownFragment extends Fragment implements Titleable {

	private String mIdAsString = -1 + "";
	private String mLabel;

	private TextView mLabelView, mDaysToView, mDateView;
	private LoadHideHelper mHelper;

	public CountdownFragment() {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (getArguments() == null) throw new IllegalArgumentException("No args supplied for fragment");

		try {

			final Date date = DateFormat.getDateInstance(DateFormat.LONG).parse(getArguments().getString("date"));
			mIdAsString = getArguments().getString(BaseColumns._ID);
			mLabelView.setText(mLabel = getArguments().getString("label"));
			mDateView.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(date));
			mHelper = new LoadHideHelper(this);

			new Thread(new Runnable() {
				@Override
				public void run() {

					// Requires an ugly fudge because, for some reason, the Days class accesses
					// the disk through random access, which throws errors with StrictMode.
					int days = Days.daysBetween(new DateTime(new Date()), new DateTime(date)).getDays();
					Bundle b = new Bundle();
					b.putString("result", String.valueOf(days + 1));
					Message m = Message.obtain(asyncHandler);
					m.setData(b);
					asyncHandler.sendMessage(m);

				}
			}).start();

		} catch (ParseException e) {
			Toast.makeText(getActivity(), "Malformed date was stored", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		//getActivity().getLoaderManager().initLoader(TickTockProvider.LOADER_SINGLE_ITEM, getArguments(), this);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.countdown, null);
		mLabelView = (TextView) v.findViewById(R.id.label);
		mDateView = (TextView) v.findViewById(R.id.date);
		mDaysToView = (TextView) v.findViewById(R.id.daysto);
		return v;
	}

	@Override
	public String getTitle() {
		if (mLabel == null || mLabel.isEmpty()) return getArguments().getString(BaseColumns._ID);
		return mLabel.toUpperCase();
	}

	public String getCountdownId() {
		return mIdAsString;
	}

	final Handler asyncHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			mDaysToView.setText(msg.getData().getString("result"));
			mHelper.show();
		}
	};

}
