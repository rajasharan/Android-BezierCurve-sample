package com.rajasharan.curvepaths;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by rajasharan on 7/26/15.
 */
public class SpringView extends View implements Animator.AnimatorListener {
    private static final String TAG = "QuadBezierView";
    private static final int MAX_COUNT = 3;

    private SparseArray<Point> mTouches;
    private int mCurrentTouchIndex;
    private float mRadius;
    private float[] mAnimatedRadius;
    private Path mPath;
    private Paint mFillPaint;
    private Paint mCurvePaint;
    private ViewConfiguration mViewConfigs;
    private ObjectAnimator mRippleAnimator;
    private ObjectAnimator mSpringAnimator;
    private boolean mResetSpringAnim;
    private boolean mMultiTouchMode;

    public SpringView(Context context) {
        this(context, null);
    }

    public SpringView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpringView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        mTouches = new SparseArray<>(MAX_COUNT);
        mRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
        mAnimatedRadius = new float[] {0f, 0f, 0f, 0f};
        mCurrentTouchIndex = -1;
        mViewConfigs = ViewConfiguration.get(context);
        mResetSpringAnim = true;
        mMultiTouchMode = false;

        mPath = new Path();
        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(Color.GRAY);

        mCurvePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCurvePaint.setStyle(Paint.Style.STROKE);
        mCurvePaint.setARGB(128, 128, 128, 128);

        PropertyValuesHolder pvhAlpha = PropertyValuesHolder.ofInt("paintAlpha", 255, 0);
        PropertyValuesHolder pvhRadius = PropertyValuesHolder.ofFloat("radius", 0f, 4f);
        mRippleAnimator = ObjectAnimator.ofPropertyValuesHolder(this, pvhAlpha, pvhRadius);
        mRippleAnimator.setDuration(500);
        mRippleAnimator.setInterpolator(new DecelerateInterpolator(2f));
        mRippleAnimator.addListener(this);

        mSpringAnimator = ObjectAnimator.ofFloat(this, "spring", 0f, 1f);
        mSpringAnimator.setDuration(300);
        mSpringAnimator.setInterpolator(new OvershootInterpolator(10f));
    }

    public void setMultiTouchMode(boolean enable) {
        mMultiTouchMode = enable;
        mTouches.clear();
        mRippleAnimator.cancel();
        mCurrentTouchIndex = -1;
    }

    private void setPaintAlpha(int a) {
        mFillPaint.setAlpha(a);
        /* invalidate not needed because alpha is running simultaneously with radius */
        //invalidateTouch();
    }

    private void setRadius(float r) {
        if (mCurrentTouchIndex != -1) {
            mAnimatedRadius[mCurrentTouchIndex] = mRadius * r;
            invalidateTouchRipple();
        }
    }

    private void setSpring(float t) {
        Log.d(TAG, String.format("Overshoot tension: %f", t));
        Point cp = mTouches.get(2);
        Point start = mTouches.get(0);
        Point end = mTouches.get(1);
        if (cp != null) {
            int x = (start.x + end.x) / 2;
            int y = (start.y + end.y) / 2;

            if (cp.x > x) cp.x -= (cp.x - x)*t;
            else cp.x += (x - cp.x)*t;

            if (cp.y > y) cp.y -= (cp.y - y)*t;
            else cp.y += (y - cp.y)*t;

            mTouches.put(2, cp);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAnimatedTouches(canvas);
        drawPath(canvas);
    }

    private void drawAnimatedTouches(Canvas canvas) {
        for (int i = 0; i < MAX_COUNT; i++) {
            Point p = mTouches.get(i);
            if (p != null) {
                canvas.drawCircle(p.x, p.y, mAnimatedRadius[i], mFillPaint);
                drawCross(p, canvas);
            }
        }
    }

    private void drawCross(Point p, Canvas canvas) {
        int r = (int) mRadius/2;
        canvas.drawLine(p.x-r, p.y-r, p.x+r, p.y+r, mCurvePaint);
        canvas.drawLine(p.x+r, p.y-r, p.x-r, p.y+r, mCurvePaint);
    }

    private void drawPath(Canvas canvas) {
        Point p0, p1, p2, p3;
        p0 = mTouches.get(0);
        p1 = mTouches.get(1);
        p2 = mTouches.get(2);
        p3 = mTouches.get(3);

        if (p3 != null && p2 != null && p1 != null && p0 != null) {
            mPath.rewind();
            mPath.moveTo(p0.x, p0.y);
            mPath.cubicTo(p2.x, p2.y, p3.x, p3.y, p1.x, p1.y);
            canvas.drawPath(mPath, mCurvePaint);
        }
        else if (p2 != null && p1 != null && p0 != null) {
            mPath.rewind();
            mPath.moveTo(p0.x, p0.y);
            mPath.quadTo(p2.x, p2.y, p1.x, p1.y);
            canvas.drawPath(mPath, mCurvePaint);
        }
        else if (p1 != null && p0 != null) {
            mPath.rewind();
            mPath.moveTo(p0.x, p0.y);
            mPath.lineTo(p1.x, p1.y);
            canvas.drawPath(mPath, mCurvePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        int xp = (int) event.getX(pointerIndex);
        int yp = (int) event.getY(pointerIndex);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                registerTouch(xp, yp, pointerId);
                invalidate();
                //invalidateTouch(x, y);
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int i=0; i<event.getPointerCount() && mResetSpringAnim; i++) {
                    pointerIndex = i;
                    pointerId = event.getPointerId(pointerIndex);
                    xp = (int) event.getX(pointerIndex);
                    yp = (int) event.getY(pointerIndex);
                    updateTouch(xp, yp, pointerId);
                    invalidate();

                    if (mCurrentTouchIndex == 2) {
                        Point sp = mTouches.get(0);
                        Point ep = mTouches.get(1);
                        int cx = (sp.x + ep.x)/2;
                        int cy = (sp.y + ep.y)/2;
                        int threshold = Math.min(getHeight()/2, getWidth()/2);
                        if (Math.abs(cx - xp) > threshold || Math.abs(cy - yp) > threshold) {
                            startSpringAnimation(pointerId);
                            mResetSpringAnim = false;
                        }
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                //Log.d(TAG, String.format("pointerId, pointerIndex: %d,%d: (%d,%d)", pointerId, pointerIndex, xp, yp));
                registerTouch(xp, yp, pointerId);
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_POINTER_UP: {
            }
            case MotionEvent.ACTION_UP: {
                startSpringAnimation(pointerId);
                return true;
            }
            case MotionEvent.ACTION_CANCEL: {
            }
        }
        return true;
        //return super.onTouchEvent(event);
    }

    private void registerTouch(int x, int y, int pointerId) {
        if (!mMultiTouchMode) {
            registerTouch(x, y);
            return;
        }
        if (pointerId < MAX_COUNT) {
            mTouches.put(pointerId, new Point(x, y));
            mCurrentTouchIndex = pointerId;
            startTouchAnimation();
        }
    }

    private void registerTouch(int x, int y) {
        mCurrentTouchIndex++;
        mCurrentTouchIndex = mCurrentTouchIndex % MAX_COUNT;
        if (mCurrentTouchIndex == 0) {
            mTouches.clear();
        }
        mResetSpringAnim = true;
        mTouches.put(mCurrentTouchIndex, new Point(x, y));
        startTouchAnimation();
    }

    private void updateTouch(int x, int y, int pointerId) {
        if (!mMultiTouchMode) {
            updateTouch(x, y);
            return;
        }
        mTouches.put(pointerId, new Point(x, y));
    }

    private void updateTouch(int x, int y) {
        mTouches.put(mCurrentTouchIndex, new Point(x, y));
    }

    private void invalidateTouchRipple() {
        int radius = (int) mRadius * 4;
        Point p = mTouches.get(mCurrentTouchIndex);
        if (p == null) {
            return;
        }
        invalidate(p.x - radius, p.y - radius, p.x + radius, p.y + radius);
    }

    private void startTouchAnimation() {
        if (mRippleAnimator.isStarted() || mRippleAnimator.isRunning()) {
            mRippleAnimator.cancel();
        }
        mRippleAnimator.start();
    }

    private void startSpringAnimation(int pointerId) {
        if (mSpringAnimator.isStarted() || mSpringAnimator.isRunning()) {
            mSpringAnimator.cancel();
        }
        if (mCurrentTouchIndex == 2) {
            mSpringAnimator.start();
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }
    @Override
    public void onAnimationEnd(Animator animation) {
        for (int i=0; i<mAnimatedRadius.length; i++) {
            mAnimatedRadius[i] = 0f;
        }
    }
    @Override
    public void onAnimationRepeat(Animator animation) {
    }
    @Override
    public void onAnimationCancel(Animator animation) {
    }
}
