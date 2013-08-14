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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuff;
import android.os.Vibrator;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * A ViewGroup that coordinated dragging across its dscendants
 */
public class DragLayer extends FrameLayout implements DragController {
	private final static String TAG = " -----------DragLayer--------";
    private static final int SCROLL_DELAY = 600;
    private static final int SCROLL_ZONE = 20;
    private static final int VIBRATE_DURATION = 35;
    private static final int ANIMATION_SCALE_UP_DURATION = 110;

    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;

    // Number of pixels to add to the dragged item for scaling
    private static final float DRAG_SCALE = 24.0f;

    private boolean mDragging = false;
    private boolean mShouldDrop;
    private float mLastMotionX;
    private float mLastMotionY;
    private boolean isStart = true;
    
    /**
     * The bitmap that is currently being dragged
     */
    private Bitmap mDragBitmap = null;
    private View mOriginator;
    private CellLayout originalCellLayout;

    private int mBitmapOffsetX;
    private int mBitmapOffsetY;

    /**
     * X offset from where we touched on the cell to its upper-left corner
     */
    private float mTouchOffsetX;
    
    /**
     * Y offset from where we touched on the cell to its upper-left corner
     */
    private float mTouchOffsetY;
    
    /**
     * Utility rectangle
     */
    private Rect mDragRect = new Rect();
    
    /**
     * Where the drag originated
     */
    private DragSource mDragSource;
    
    /**
     * The data associated with the object being dragged
     */
    private Object mDragInfo;

    private final Rect mRect = new Rect();    
    private final int[] mDropCoordinates = new int[2];

//    private final Vibrator mVibrator = new Vibrator(1);
    
    private DragListener mListener;

    private DragScroller mDragScroller;
    
    private static final int SCROLL_OUTSIDE_ZONE = 0;
    private static final int SCROLL_WAITING_IN_ZONE = 1;

    private static final int SCROLL_LEFT = 0;
    private static final int SCROLL_RIGHT = 1;
    
    private int mScrollState = SCROLL_OUTSIDE_ZONE;

    private ScrollRunnable mScrollRunnable = new ScrollRunnable();
    private View mIgnoredDropTarget;

    private RectF mDragRegion;
    private boolean mEnteredRegion;
//    private DropTarget mLast/DropTarget;

    private final Paint mTrashPaint = new Paint();
    private Paint mDragPaint;

    private static final int ANIMATION_STATE_STARTING = 1;
    private static final int ANIMATION_STATE_RUNNING = 2;
    private static final int ANIMATION_STATE_DONE = 3;

    private static final int ANIMATION_TYPE_SCALE = 1;

    private float mAnimationFrom;
    private float mAnimationTo;
    private int mAnimationDuration;
    private long mAnimationStartTime;
    private int mAnimationType;
    private int mAnimationState = ANIMATION_STATE_DONE;

    private InputMethodManager mInputMethodManager;
    //add by Dra_zj
    private int[] lastCellXY = new int[2]; 
    private int[] positions = new int[5];  
    private boolean isMember ;        
    private Activity mLauncher;
    private int originalPosition;    
    private int headPosition;           
    private int addNums = 0;             
    String[] groupArr;                        
    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        Log.d(TAG, "constructor");
        
        final int srcColor = context.getResources().getColor(R.color.delete_color_filter);
        mTrashPaint.setColorFilter(new PorterDuffColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP));
    }

    public void startDrag(CellLayout originalCellLayout,View v, DragSource source, 
    		Object dragInfo, int dragAction, int[] positions, Activity mLauncher, 
    		int position) {

    	Log.d(TAG, "startDrag position=" + position);
    	if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }
        
        this.positions = positions;
        this.isMember = isMember;
        this.mLauncher = mLauncher;
        this.originalPosition = position;

        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v.getLayoutParams();
        lastCellXY[0] = lp.cellX;
        lastCellXY[1] = lp.cellY;

        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);

        if (mListener != null) {
            mListener.onDragStart(v, source, dragInfo, dragAction);
        }

        Rect r = mDragRect;
        r.set(v.getScrollX(), v.getScrollY(), 0, 0);

        offsetDescendantRectToMyCoords(v, r);
        mTouchOffsetX = mLastMotionX - r.left;
        mTouchOffsetY = mLastMotionY - r.top;

        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);
        v.buildDrawingCache();

        Bitmap viewBitmap = v.getDrawingCache();
        int width = viewBitmap.getWidth();
        int height = viewBitmap.getHeight();

        Matrix scale = new Matrix();
        float scaleFactor = v.getWidth();
        scaleFactor = (scaleFactor + DRAG_SCALE) /scaleFactor;
        scale.setScale(scaleFactor, scaleFactor);

        mAnimationTo = 1.0f;
        mAnimationFrom = 1.0f / scaleFactor;
        mAnimationDuration = ANIMATION_SCALE_UP_DURATION;
        mAnimationState = ANIMATION_STATE_STARTING;
        mAnimationType = ANIMATION_TYPE_SCALE;

        mDragBitmap = Bitmap.createBitmap(viewBitmap, 0, 0, width, height, scale, true);
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);

        final Bitmap dragBitmap = mDragBitmap;
        mBitmapOffsetX = (dragBitmap.getWidth() - width) / 2;
        mBitmapOffsetY = (dragBitmap.getHeight() - height) / 2;

        if (dragAction == DRAG_ACTION_MOVE) {
            v.setVisibility(GONE);
        }
        SharedPreferences settings = mLauncher.getSharedPreferences("groups",0);
		String groups = settings.getString("group", "");
		groupArr = groups.split(";;");
		addNums = groupArr[0]==null||"".equals(groupArr[0])?0 : groupArr.length;
		headPosition = 7 + addNums;

        mDragPaint = null;
        mDragging = true;
        mShouldDrop = true;
        mOriginator = v;
        this.originalCellLayout = originalCellLayout;
        mDragSource = source;
        mDragInfo = dragInfo;

//        mVibrator.vibrate(VIBRATE_DURATION);

        mEnteredRegion = false;

        invalidate();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging || super.dispatchKeyEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mDragging && mDragBitmap != null) {
            if (mAnimationState == ANIMATION_STATE_STARTING) {
                mAnimationStartTime = SystemClock.uptimeMillis();
                mAnimationState = ANIMATION_STATE_RUNNING;
            }

            if (mAnimationState == ANIMATION_STATE_RUNNING) {
                float normalized = (float) (SystemClock.uptimeMillis() - mAnimationStartTime) /
                        mAnimationDuration;
                if (normalized >= 1.0f) {
                    mAnimationState = ANIMATION_STATE_DONE;
                }
                normalized = Math.min(normalized, 1.0f);
                final float value = mAnimationFrom  + (mAnimationTo - mAnimationFrom) * normalized;

                switch (mAnimationType) {
                    case ANIMATION_TYPE_SCALE:
                        final Bitmap dragBitmap = mDragBitmap;
                        canvas.save();
//                        canvas.translate(mScrollX + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
//                                mScrollY + mLastMotionY - mTouchOffsetY - mBitmapOffsetY);
                        canvas.translate(this.getScrollX() + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
                                this.getScrollY() + mLastMotionY - mTouchOffsetY - mBitmapOffsetY);

                        canvas.translate((dragBitmap.getWidth() * (1.0f - value)) / 2,
                                (dragBitmap.getHeight() * (1.0f - value)) / 2);
                        canvas.scale(value, value);
                        canvas.drawBitmap(dragBitmap, 0.0f, 0.0f, mDragPaint);
                        canvas.restore();
                        break;
                }
            } else {
//                canvas.drawBitmap(mDragBitmap,
//                        mScrollX + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
//                        mScrollY + mLastMotionY - mTouchOffsetY - mBitmapOffsetY, mDragPaint);
                canvas.drawBitmap(mDragBitmap,
                        this.getScrollX() + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
                        this.getScrollY() + mLastMotionY - mTouchOffsetY - mBitmapOffsetY, mDragPaint);

            }
        }
    }

    private void endDrag() {
    	Log.i(TAG, "end drag-----------");
        if (mDragging) {
            mDragging = false;
            if (mDragBitmap != null) {
                mDragBitmap.recycle();
            }
            if (mOriginator != null) {
                mOriginator.setVisibility(VISIBLE);
            }
            if (mListener != null) {
                mListener.onDragEnd();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                break;

            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;
//                mLastDropTarget = null;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
//                if (mShouldDrop && drop(x, y)) {
//                    mShouldDrop = false;
//                }
                endDrag();
                break;
        }

        return mDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            return false;
        }
        
        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();
//        Log.i(TAG, x+"=================="+y);
        switch (action) {
        case MotionEvent.ACTION_DOWN:
//        	Log.i(TAG, "=====ACTION_DOWN"+mLastMotionY);
            // Remember where the motion event started
        	
            mLastMotionX = x;
            mLastMotionY = y;
            
            if ((x < SCROLL_ZONE) || (x > getWidth() - SCROLL_ZONE)) {
                mScrollState = SCROLL_WAITING_IN_ZONE;
                postDelayed(mScrollRunnable, SCROLL_DELAY);
            } else {
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }

            break;
        case MotionEvent.ACTION_MOVE:
//            final int scrollX = mScrollX;
//            final int scrollY = mScrollY;
            final int scrollX = this.getScrollX();
            final int scrollY = this.getScrollY();

            final float touchX = mTouchOffsetX;
            final float touchY = mTouchOffsetY;

            final int offsetX = mBitmapOffsetX;
            final int offsetY = mBitmapOffsetY;

            int left = (int) (scrollX + mLastMotionX - touchX - offsetX);
            int top = (int) (scrollY + mLastMotionY - touchY - offsetY);

            final Bitmap dragBitmap = mDragBitmap;
            final int width = dragBitmap.getWidth();
            final int height = dragBitmap.getHeight();

            final Rect rect = mRect;
            rect.set(left - 1, top - 1, left + width + 1, top + height + 1);

            mLastMotionX = x;
            mLastMotionY = y;

            left = (int) (scrollX + x - touchX - offsetX);
            top = (int) (scrollY + y - touchY - offsetY);

            rect.union(left - 1, top - 1, left + width + 1, top + height + 1);
            invalidate(rect);

            final int[] coordinates = mDropCoordinates;

            boolean inDragRegion = false;
            if (mDragRegion != null) {
                final RectF region = mDragRegion;
                final boolean inRegion = region.contains(ev.getRawX(), ev.getRawY());
                if (!mEnteredRegion && inRegion) {
                    mDragPaint = mTrashPaint;
                    mEnteredRegion = true;
                    inDragRegion = true;
                } else if (mEnteredRegion && !inRegion) {
                    mDragPaint = null;
                    mEnteredRegion = false;
                }
            }

            if (!inDragRegion && x < SCROLL_ZONE) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_LEFT);
                    postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            } else if (!inDragRegion && x > getWidth() - SCROLL_ZONE) {
                if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                    mScrollState = SCROLL_WAITING_IN_ZONE;
                    mScrollRunnable.setDirection(SCROLL_RIGHT);
                    postDelayed(mScrollRunnable, SCROLL_DELAY);
                }
            } else {
                if (mScrollState == SCROLL_WAITING_IN_ZONE) {
                    mScrollState = SCROLL_OUTSIDE_ZONE;
                    mScrollRunnable.setDirection(SCROLL_RIGHT);
                    removeCallbacks(mScrollRunnable);
                }
            }
            
            break;
        case MotionEvent.ACTION_UP:
//        	Log.i(TAG, "???????????????");

        	if(!isStart){
    			isStart = true;
//    			mLauncher.stopAnimation(mLauncher.van);	
    		}
            removeCallbacks(mScrollRunnable);
//            if (mShouldDrop) {
//            	Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!");
//                drop(x, y);
//                mShouldDrop = false;
//            }
            endDrag();

            break;
        case MotionEvent.ACTION_CANCEL:
            endDrag();
        }

        return true;
    }

    public void setDragScoller(DragScroller scroller) {
        mDragScroller = scroller;
    }

    public void setDragListener(DragListener l) {
        mListener = l;
    }

    public void removeDragListener(DragListener l) {
        mListener = null;   
    }

    /**
     * Specifies the view that must be ignored when looking for a drop target.
     *
     * @param view The view that will not be taken into account while looking
     *        for a drop target.
     */
    void setIgnoredDropTarget(View view) {
        mIgnoredDropTarget = view;
    }

    /**
     * Specifies the delete region.
     *
     * @param region The rectangle in screen coordinates of the delete region.
     */
    void setDeleteRegion(RectF region) {
        mDragRegion = region;
    }

    private class ScrollRunnable implements Runnable {
        private int mDirection;

        ScrollRunnable() {
        }

        public void run() {
            if (mDragScroller != null) {
                if (mDirection == SCROLL_LEFT) {
                    mDragScroller.scrollLeft();
                } else {
                    mDragScroller.scrollRight();
                }
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }
        }

        void setDirection(int direction) {
            mDirection = direction;
        }
    }

}
