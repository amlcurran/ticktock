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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.*;

/**
 * A {@link android.view.View.OnTouchListener} that makes any {@link View} dismissable when the
 * user swipes (drags her finger) horizontally across the view.
 * <p/>
 * <p><em>For {@link android.widget.ListView} list items that don't manage their own touch events
 * (i.e. you're using
 * {@link android.widget.ListView#setOnItemClickListener(android.widget.AdapterView.OnItemClickListener)}
 * or an equivalent listener on {@link android.app.ListActivity} or
 * {@link android.app.ListFragment}, use {@link SwipeDismissListViewTouchListener} instead.</em></p>
 * <p/>
 * <p>Example usage:</p>
 * <p/>
 * <pre>
 * view.setOnTouchListener(new VerticalSwipeDismissTouchListener(
 *         view,
 *         null, // Optional token/cookie object
 *         new VerticalSwipeDismissTouchListener.OnDismissCallback() {
 *             public void onDismiss(View view, Object token) {
 *                 parent.removeView(view);
 *             }
 *         }));
 * </pre>
 * <p/>
 * <p>This class Requires API level 12 or later due to use of {@link
 * android.view.ViewPropertyAnimator}.</p>
 *
 * @see SwipeDismissListViewTouchListener
 */
public class VerticalSwipeDismissTouchListener implements View.OnTouchListener {
	// Cached ViewConfiguration and system-wide constant values
	private int mSlop;
	private int mMinFlingVelocity;
	private int mMaxFlingVelocity;
	private long mAnimationTime;

	// Fixed properties
	private View mView;
	private OnDismissCallback mCallback;
	private int mViewHeight = 1; // 1 and not 0 to prevent dividing by zero

	// Transient properties
	private float mDownY;
	private boolean mSwiping;
	private Object mToken;
	private VelocityTracker mVelocityTracker;
	private float mTranslationY;

	/**
	 * The callback interface used by {@link VerticalSwipeDismissTouchListener} to inform its client
	 * about a successful dismissal of the view for which it was created.
	 */
	public interface OnDismissCallback {
		/**
		 * Called when the user has indicated they she would like to dismiss the view.
		 *
		 * @param view  The originating {@link View} to be dismissed.
		 * @param token The optional token passed to this object's constructor.
		 */
		void onDismiss(View view, Object token);
	}

	/**
	 * Constructs a new swipe-to-dismiss touch listener for the given view.
	 *
	 * @param view     The view to make dismissable.
	 * @param token    An optional token/cookie object to be passed through to the callback.
	 * @param callback The callback to trigger when the user has indicated that she would like to
	 *                 dismiss this view.
	 */
	public VerticalSwipeDismissTouchListener(View view, Object token, OnDismissCallback callback) {
		ViewConfiguration vc = ViewConfiguration.get(view.getContext());
		mSlop = vc.getScaledTouchSlop();
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
		mAnimationTime = view.getContext().getResources().getInteger(
				android.R.integer.config_shortAnimTime);
		mView = view;
		mToken = token;
		mCallback = callback;
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		// offset because the view is translated during swipe
		motionEvent.offsetLocation(0, mTranslationY);

		if (mViewHeight < 2) {
			mViewHeight = mView.getHeight();
		}

		switch (motionEvent.getActionMasked()) {
			case MotionEvent.ACTION_DOWN: {
				// TODO: ensure this is a finger, and set a flag
				mDownY = motionEvent.getRawY();
				mVelocityTracker = VelocityTracker.obtain();
				mVelocityTracker.addMovement(motionEvent);
				view.onTouchEvent(motionEvent);
				return false;
			}

			case MotionEvent.ACTION_UP: {
				if (mVelocityTracker == null) {
					break;
				}

				float deltaY = motionEvent.getRawY() - mDownY;
				mVelocityTracker.addMovement(motionEvent);
				mVelocityTracker.computeCurrentVelocity(1000);
				float velocityX = Math.abs(mVelocityTracker.getXVelocity());
				float velocityY = Math.abs(mVelocityTracker.getYVelocity());
				boolean dismiss = false;
				boolean dismissRight = false;
				if (Math.abs(deltaY) > mViewHeight / 2) {
					dismiss = true;
					dismissRight = deltaY > 0;
				} else if (mMinFlingVelocity <= velocityY && velocityY <= mMaxFlingVelocity
						&& velocityX < velocityY) {
					dismiss = true;
					dismissRight = mVelocityTracker.getYVelocity() > 0;
				}
				if (dismiss) {
					// dismiss
					mView.animate()
							.translationY(dismissRight ? mViewHeight : -mViewHeight)
							.alpha(0)
							.setDuration(mAnimationTime)
							.setListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) {
									performDismiss();
								}
							});
				} else {
					// cancel
					mView.animate()
							.translationY(0)
							.alpha(1)
							.setDuration(mAnimationTime)
							.setListener(null);
				}
				mVelocityTracker = null;
				mTranslationY = 0;
				mDownY = 0;
				mSwiping = false;
				break;
			}

			case MotionEvent.ACTION_MOVE: {
				if (mVelocityTracker == null) {
					break;
				}

				mVelocityTracker.addMovement(motionEvent);
				float deltaY = motionEvent.getRawY() - mDownY;
				if (Math.abs(deltaY) > mSlop) {
					mSwiping = true;
					mView.getParent().requestDisallowInterceptTouchEvent(true);

					// Cancel listview's touch
					MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
					cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
							(motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
					mView.onTouchEvent(cancelEvent);
				}

				if (mSwiping) {
					mTranslationY = deltaY;
					mView.setTranslationY(deltaY);
					// TODO: use an ease-out interpolator or such
					mView.setAlpha(Math.max(0f, Math.min(1f,
							1f - 2f * Math.abs(deltaY) / mViewHeight)));
					return true;
				}
				break;
			}
		}
		return false;
	}

	private void performDismiss() {
		// Animate the dismissed view to zero-height and then fire the dismiss callback.
		// This triggers layout on each animation frame; in the future we may want to do something
		// smarter and more performant.

		final ViewGroup.LayoutParams lp = mView.getLayoutParams();
		final int originalWidth = mView.getWidth();

		ValueAnimator animator = ValueAnimator.ofInt(originalWidth, 1).setDuration(mAnimationTime);

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCallback.onDismiss(mView, mToken);
				// Reset view presentation
				mView.setAlpha(1f);
				mView.setTranslationY(0);
				lp.height = originalWidth;
				mView.setLayoutParams(lp);
			}
		});

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.width = (Integer) valueAnimator.getAnimatedValue();
				mView.setLayoutParams(lp);
			}
		});

		animator.start();
	}
}
