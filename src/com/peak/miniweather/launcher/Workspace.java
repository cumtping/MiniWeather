/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peak.miniweather.launcher;

import com.peak.miniweather.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Scroller;

/**
 * The workspace is a wide area with a wallpaper and a finite number of screens.
 * Each screen contains a number of icons, folders or widgets the user can
 * interact with. A workspace is meant to be used with a fixed width only.
 */
public class Workspace extends ViewGroup implements DragSource, DragScroller {
	private static final String TAG = Workspace.class.getSimpleName();

	private static final int INVALID_SCREEN = -1;

	/**
	 * The velocity at which a fling gesture will cause us to snap to the next
	 * screen
	 */
	private static final int SNAP_VELOCITY = 1000;

	private int mDefaultScreen;

	private Paint mPaint;

	// private Bitmap mWallpaper;

	private int mWallpaperWidth;

	// private int mWallpaperHeight;

	private float mWallpaperOffset;

	// private boolean mWallpaperLoaded;

	private boolean mFirstLayout = true;

	private int mCurrentScreen;

	private int mNextScreen = INVALID_SCREEN;

	private Scroller mScroller;

	private VelocityTracker mVelocityTracker;

	/**
	 * CellInfo for the cell that is currently being dragged
	 */
	// private CellLayout.CellInfo mDragInfo;

	private float mLastMotionX;

	private float mLastMotionY;

	private final static int TOUCH_STATE_REST = 0;

	private final static int TOUCH_STATE_SCROLLING = 1;

	private int mTouchState = TOUCH_STATE_REST;

	private OnLongClickListener mLongClickListener;

	private Activity mAct;

	private DragController mDragger;

	private boolean mAllowLongPress;

	private boolean mLocked;

	private int mTouchSlop;

	final Rect mDrawerBounds = new Rect();

	final Rect mClipBounds = new Rect();

	int mDrawerContentHeight;

	int mDrawerContentWidth;

	public boolean setmove = false;

	public int xGrid = 0;

	public int yGrid = 0;

	public int xDrop = 0;

	public int yDrop = 0;

	private int mTotalScreen;

	private OnScreenChangedListener mOnScreenChangedListener = null;

	/**
	 * 
	 * Screen changed listener.
	 * 
	 * @author ping
	 * 
	 */
	public interface OnScreenChangedListener {
		public void onScreenChanded(int index);
	}

	public void setOnScreenChangedListener(OnScreenChangedListener l) {
		mOnScreenChangedListener = l;
	}

	public void setTotalScreen(int total) {
		mTotalScreen = total;
	}

	public int getTotalScreen() {
		return mTotalScreen;
	}

	/**
	 * Used to inflate the Workspace from XML.
	 * 
	 * @param context
	 *            The application's context.
	 * @param attrs
	 *            The attribtues set containing the Workspace's customization
	 *            values.
	 */
	public Workspace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * Used to inflate the Workspace from XML.
	 * 
	 * @param context
	 *            The application's context.
	 * @param attrs
	 *            The attribtues set containing the Workspace's customization
	 *            values.
	 * @param defStyle
	 *            Unused.
	 */
	public Workspace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.Workspace, defStyle, 0);
		mDefaultScreen = a.getInt(R.styleable.Workspace_defaultScreen, 0);
		a.recycle();

		initWorkspace();
	}

	/**
	 * Initializes various states for this workspace.
	 */
	private void initWorkspace() {
		mScroller = new Scroller(getContext());
		mCurrentScreen = mDefaultScreen;

		mPaint = new Paint();
		mPaint.setDither(false);

		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	@Override
	public void addView(View child, int index, LayoutParams params) {
		super.addView(child, index, params);
	}

	@Override
	public void addView(View child) {

		super.addView(child);
	}

	@Override
	public void addView(View child, int index) {
		super.addView(child, index);
	}

	@Override
	public void addView(View child, int width, int height) {
		super.addView(child, width, height);
	}

	@Override
	public void addView(View child, LayoutParams params) {
		super.addView(child, params);
	}

	public boolean isDefaultScreenShowing() {
		return mCurrentScreen == mDefaultScreen;
	}

	/**
	 * Returns the index of the currently displayed screen.
	 * 
	 * @return The index of the currently displayed screen.
	 */
	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	/**
	 * Sets the current screen.
	 * 
	 * @param currentScreen
	 */
	public void setCurrentScreen(int currentScreen) {
		mCurrentScreen = Math.max(0,
				Math.min(currentScreen, getChildCount() - 1));
		scrollTo(mCurrentScreen * getWidth(), 0);
		invalidate();
	}

	/**
	 * Shows the default screen (defined by the firstScreen attribute in XML.)
	 */
	void showDefaultScreen() {
		setCurrentScreen(mDefaultScreen);
	}

	public int getDefaultScreen() {
		return mDefaultScreen;
	}

	/**
	 * Registers the specified listener on each screen contained in this
	 * workspace.
	 * 
	 * @param l
	 *            The listener used to respond to long clicks.
	 */
	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mLongClickListener = l;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).setOnLongClickListener(l);
		}
	}

	@Override
	public void computeScroll() {

		if (mScroller.computeScrollOffset()) {
			this.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		} else if (mNextScreen != INVALID_SCREEN) {
			mCurrentScreen = Math.max(0,
					Math.min(mNextScreen, getChildCount() - 1));
			// if (mLauncherMode == MODE_WEATHER_ACTIVITY) {
			// mLauncher.setScreen(mCurrentScreen);
			// } else {
			// mLauncher2.setScreen(mCurrentScreen);
			// }
			mNextScreen = INVALID_SCREEN;
		}
		// if (mLauncherMode == MODE_WEATHER_ACTIVITY) {
		// if (mLauncher.dotPosition != this.getCurrentScreen())
		// mLauncher.setDotImages(this.getCurrentScreen());
		// } else {
		// }
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		boolean restore = false;

		final int mScrollX = this.getScrollX();
		final int mRight = this.getRight();
		final int mLeft = this.getLeft();
		// final int mBottom = this.getBottom();
		// final int mTop = this.getTop();

		float x = mScrollX * mWallpaperOffset;
		if (x + mWallpaperWidth < mRight - mLeft) {
			x = mRight - mLeft - mWallpaperWidth;
		}

		boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING
				&& mNextScreen == INVALID_SCREEN;

		if (fastDraw) {
			drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());
		} else {
			final long drawingTime = getDrawingTime();

			if (mNextScreen >= 0 && mNextScreen < getChildCount()
					&& Math.abs(mCurrentScreen - mNextScreen) == 1) {
				drawChild(canvas, getChildAt(mCurrentScreen), drawingTime);
				drawChild(canvas, getChildAt(mNextScreen), drawingTime);
			} else {

				final int count = getChildCount();
				for (int i = 0; i < count; i++) {
					drawChild(canvas, getChildAt(i), drawingTime);
				}
			}
		}

		if (restore) {
			canvas.restore();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY) {
			throw new IllegalStateException(
					"Workspace can only be used in EXACTLY mode.");
		}

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (heightMode != MeasureSpec.EXACTLY) {
		}

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		final int wallpaperWidth = mWallpaperWidth;
		mWallpaperOffset = wallpaperWidth > width ? (count * width - wallpaperWidth)
				/ ((count - 1) * (float) width)
				: 1.0f;

		if (mFirstLayout) {
			scrollTo(mCurrentScreen * width, 0);
			mFirstLayout = false;
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		int childLeft = 0;

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				final int childWidth = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + childWidth,
						child.getMeasuredHeight());
				childLeft += childWidth;
			}
		}
	}

	@Override
	public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
			boolean immediate) {
		int screen = indexOfChild(child);
		if (screen != mCurrentScreen || !mScroller.isFinished()) {
			// if (mLauncherMode == MODE_WEATHER_ACTIVITY) {
			// if (!mLauncher.isWorkspaceLocked()) {
			snapToScreen(screen);
			// }
			// } else if (mLauncherMode == MODE_CHOOSE_CITY_ACTIVITY) {
			// if (!mLauncher2.isWorkspaceLocked()) {
			// snapToScreen(screen);
			// }
			// }
			return true;
		}
		return false;
	}

	@Override
	protected boolean onRequestFocusInDescendants(int direction,
			Rect previouslyFocusedRect) {

		if (true) {

			int focusableScreen;
			if (mNextScreen != INVALID_SCREEN) {
				focusableScreen = mNextScreen;
			} else {
				focusableScreen = mCurrentScreen;
			}
			getChildAt(focusableScreen).requestFocus(direction,
					previouslyFocusedRect);

		}
		return false;
	}

	@Override
	public boolean dispatchUnhandledMove(View focused, int direction) {
		if (direction == View.FOCUS_LEFT) {
			if (getCurrentScreen() > 0) {
				snapToScreen(getCurrentScreen() - 1);
				return true;
			}
		} else if (direction == View.FOCUS_RIGHT) {
			if (getCurrentScreen() < getChildCount() - 1) {
				snapToScreen(getCurrentScreen() + 1);
				return true;
			}
		}
		return super.dispatchUnhandledMove(focused, direction);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		if (mLocked) {
			return true;
		}
		
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return false;
		}

		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_MOVE:

			final int xDiff = (int) Math.abs(x - mLastMotionX);
			final int yDiff = (int) Math.abs(y - mLastMotionY);

			final int touchSlop = mTouchSlop;
			boolean xMoved = xDiff > touchSlop;
			boolean yMoved = yDiff > touchSlop;

			if (xMoved || yMoved) {

				if (xMoved) {
					mTouchState = TOUCH_STATE_SCROLLING;
				}

				if (mAllowLongPress) {
					mAllowLongPress = false;
					final View currentScreen = getChildAt(mCurrentScreen);
					currentScreen.cancelLongPress();
				}
			}
			break;

		case MotionEvent.ACTION_DOWN:

			mLastMotionX = x;
			mLastMotionY = y;
			mAllowLongPress = true;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;
			break;

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			mAllowLongPress = false;
			break;
		}

//		return mTouchState != TOUCH_STATE_REST;
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Log.d(TAG, "onTouchEvent");
		if (mLocked) {
			return true;
		}

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();
		final int mScrollX = this.getScrollX();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG, "onTouchEvent ACTION_DOWN");

			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d(TAG, "onTouchEvent ACTION_MOVE");

			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final int deltaX = (int) (mLastMotionX - x);
				mLastMotionX = x;

				if (deltaX < 0) {
					if (mScrollX > 0) {

						scrollBy(Math.max(-mScrollX, deltaX), 0);
					}
				} else if (deltaX > 0) {
					if (mCurrentScreen < mTotalScreen - 1) {
						final int availableToScroll = getChildAt(
								getChildCount() - 1).getRight()
								- mScrollX - getWidth();
						if (availableToScroll > 0) {

							scrollBy(Math.min(availableToScroll, deltaX), 0);
						}
					} else {
					}
				}
			}

			mTouchState = TOUCH_STATE_SCROLLING;

			break;
		case MotionEvent.ACTION_UP:
			Log.d(TAG, "onTouchEvent ACTION_UP");

			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				int velocityX = (int) velocityTracker.getXVelocity();

				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurrentScreen < getChildCount() - 1) {
					snapToScreen(mCurrentScreen + 1);
				} else {
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			}
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
		}

		return true;
	}

	private void snapToDestination() {

		Log.d(TAG, "screen snapToDestination");

		final int screenWidth = getWidth();
		final int mScrollX = this.getScrollX();
		final int whichScreen = (mScrollX + (screenWidth / 2)) / screenWidth;

		snapToScreen(whichScreen);
	}

	public void snapToScreen(int whichScreen) {

		if (whichScreen > mTotalScreen - 1 || whichScreen < 0) {
			Log.d(TAG, "wwp Can not screen to " + whichScreen
					+ " total screen =" + mTotalScreen);
			return;
		}

		Log.d(TAG, "wwp snapToScreen " + whichScreen);

		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		boolean changingScreens = whichScreen != mCurrentScreen;

		mNextScreen = whichScreen;

		View focusedChild = getFocusedChild();
		if (focusedChild != null && changingScreens
				&& focusedChild == getChildAt(mCurrentScreen)) {
			focusedChild.clearFocus();
		}

		final int newX = whichScreen * getWidth();
		final int mScrollX = this.getScrollX();
		final int delta = newX - mScrollX;

		mScroller.startScroll(mScrollX, 0, delta, 0, Math.abs(delta) * 2);
		invalidate();
		
		if(mOnScreenChangedListener != null){
			mOnScreenChangedListener.onScreenChanded(whichScreen);
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final SavedState state = new SavedState(super.onSaveInstanceState());
		state.currentScreen = mCurrentScreen;
		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		if (savedState.currentScreen != -1) {
			mCurrentScreen = savedState.currentScreen;
			// if (mLauncherMode == MODE_WEATHER_ACTIVITY) {
			// mLauncher.setScreen(mCurrentScreen);
			// } else if (mLauncherMode == MODE_CHOOSE_CITY_ACTIVITY) {
			// mLauncher2.setScreen(mCurrentScreen);
			// }
		}
	}

	public void setLauncher(Activity launcher) {
		mAct = launcher;
		// mLauncher.setScreen(mCurrentScreen);
	}

	public void setDragger(DragController dragger) {
		mDragger = dragger;
	}

	public void scrollLeft() {
		Log.d(TAG, "screen scrollLeft");
		if (mNextScreen == INVALID_SCREEN && mCurrentScreen > 0
				&& mScroller.isFinished()) {
			snapToScreen(mCurrentScreen - 1);
		}
	}

	public void scrollRight() {
		Log.d(TAG, "screen scrolRight");
		if (mNextScreen == INVALID_SCREEN
				&& mCurrentScreen < getChildCount() - 1
				&& mScroller.isFinished()) {
			snapToScreen(mCurrentScreen + 1);
		}
	}

	public int getScreenForView(View v) {
		int result = -1;
		if (v != null) {
			ViewParent vp = v.getParent();
			int count = getChildCount();
			for (int i = 0; i < count; i++) {
				if (vp == getChildAt(i)) {
					return i;
				}
			}
		}
		return result;
	}

	public View getViewForTag(Object tag) {
		int screenCount = getChildCount();
		for (int screen = 0; screen < screenCount; screen++) {
			CellLayout currentScreen = ((CellLayout) getChildAt(screen));
			int count = currentScreen.getChildCount();
			for (int i = 0; i < count; i++) {
				View child = currentScreen.getChildAt(i);
				if (child.getTag() == tag) {
					return child;
				}
			}
		}
		return null;
	}

	/**
	 * Unlocks the SlidingDrawer so that touch events are processed.
	 * 
	 * @see #lock()
	 */
	public void unlock() {
		mLocked = false;
	}

	/**
	 * Locks the SlidingDrawer so that touch events are ignores.
	 * 
	 * @see #unlock()
	 */
	public void lock() {
		mLocked = true;
	}

	/**
	 * @return True is long presses are still allowed for the current touch
	 */
	public boolean allowLongPress() {
		return mAllowLongPress;
	}

	/**
	 * Set true to allow long-press events to be triggered, usually checked by
	 * {@link Launcher} to accept or block dpad-initiated long-presses.
	 */
	public void setAllowLongPress(boolean allowLongPress) {
		mAllowLongPress = allowLongPress;
	}

	public void moveToDefaultScreen() {
		snapToScreen(mDefaultScreen);
		getChildAt(mDefaultScreen).requestFocus();
	}

	public static class SavedState extends BaseSavedState {
		int currentScreen = -1;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentScreen = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(currentScreen);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	@Override
	public void onDropCompleted(View target, boolean success) {

	}
}
