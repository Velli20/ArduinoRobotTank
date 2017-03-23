/*
 * MIT License
 *
 * Copyright (c) [2017] [velli20]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.velli.commander.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;



public class JoystickView extends View {
    private static final int DEFAULT_INNER_CIRCLE_RADIUS_IN_DP = 30;
    private static final int DEFAULT_PROGRESS_Y_MAX = 255;
    private static final int DEFAULT_PROGRESS_Y_MIN = -255;
    private static final int DEFAULT_PROGRESS_X_MAX = 255;
    private static final int DEFAULT_PROGRESS_X_MIN = -255;

    private float mScale;
    private float mWidth;
    private float mHeight;
    private float mInnerCircleRadius;
    private float mOuterCircleRadius;

    private float mProgressYmax = DEFAULT_PROGRESS_Y_MAX;
    private float mProgressXmax = DEFAULT_PROGRESS_X_MAX;
    private float mProgressYmin = DEFAULT_PROGRESS_Y_MIN;
    private float mProgressXmin = DEFAULT_PROGRESS_X_MIN;

    private float mProgressY = 0;
    private float mProgressX = 0;

    private Paint mOuterCirclePaint = new Paint();
    private Paint mThumbCirclePaint = new Paint();

    private PointF mTouchingPoint = new PointF();

    private boolean mTouching = false;

    private OnJoyStickProgressChangedListener mListener;

    public interface OnJoyStickProgressChangedListener {
        void onProgressChanged(JoystickView view, int progressY, int progressX);
    }

    public JoystickView(Context context) {
        this(context, null, -1);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScale = getResources().getDisplayMetrics().density;

        mOuterCirclePaint.setColor(Color.BLACK);
        mOuterCirclePaint.setStrokeWidth(getDpValue(1));
        mOuterCirclePaint.setStyle(Paint.Style.STROKE);
        mOuterCirclePaint.setAlpha(31);
        mOuterCirclePaint.setAntiAlias(true);

        mThumbCirclePaint.setColor(Color.BLACK);
        mThumbCirclePaint.setStyle(Paint.Style.FILL);
        mThumbCirclePaint.setAlpha(143);
        mThumbCirclePaint.setAntiAlias(true);

        mInnerCircleRadius = getDpValue(DEFAULT_INNER_CIRCLE_RADIUS_IN_DP);

        setWillNotDraw(false);
    }

    private float getDpValue(float value) {
        return mScale * value;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getWidth();
        mHeight = getHeight();


        /* Draw outer bound circle */
        float cx = mWidth / 2;
        float cy = mHeight / 2;
        float outerRadius = mOuterCircleRadius - mInnerCircleRadius;

        if(outerRadius > 0) {
            canvas.drawCircle(cx, cy, outerRadius, mOuterCirclePaint);
        }

        /* Draw thumb circle */
        if(!mTouching) {
            canvas.drawCircle(cx, cy, mInnerCircleRadius, mThumbCirclePaint);
        } else {

            float innerCx = mTouchingPoint.x;
            float innerCy = mTouchingPoint.y;

            /* Calculate touching radius */
            float touchingRadius = (float) Math.sqrt((Math.pow(mTouchingPoint.x -cx, 2) + Math.pow(mTouchingPoint.y -cy, 2)));

            if(touchingRadius < 0) {
                touchingRadius = outerRadius * -1;
            }
            /* User thumb is touching outside of the outer circle.
             * Calculate equivalent coordinates with outer circle radius
             */
            if(touchingRadius > outerRadius) {
                float angle = (float) Math.atan2((innerCy - cy),(innerCx - cx));

                innerCy = ((float) Math.sin(angle) * outerRadius) + cy;
                innerCx = ((float) Math.cos(angle) * outerRadius) + cx ;

            }

            canvas.drawCircle(innerCx, innerCy, mInnerCircleRadius, mThumbCirclePaint);

            calculateCurrentProgress(innerCy, innerCx);

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouching = true;
                mTouchingPoint.x = event.getX();
                mTouchingPoint.y = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchingPoint.x = event.getX();
                mTouchingPoint.y = event.getY();
                break;
            default:
            case MotionEvent.ACTION_UP:
                mTouching = false;
                mProgressY = 0;
                mProgressX = 0;
                if(mListener != null) {
                    mListener.onProgressChanged(this, (int)mProgressY, (int)mProgressX);
                }
                break;

        }
        invalidate();
        return true;
    }

    private void calculateCurrentProgress(float thumbCy, float thumbCx) {
        float cx = mWidth  / 2;
        float cy = mHeight / 2;
        float oldProgressY = mProgressY;
        float oldProgressX = mProgressX;

        float yAxisInMin = cy + (mOuterCircleRadius ) - mInnerCircleRadius;
        float yAxisInMax = cy - (mOuterCircleRadius ) + mInnerCircleRadius;

        float xAxisInMin = cx + (mOuterCircleRadius ) - mInnerCircleRadius;
        float xAxisInMax = cx - (mOuterCircleRadius ) + mInnerCircleRadius;


        mProgressY = (thumbCy - yAxisInMin) * (mProgressYmax - mProgressYmin) / (yAxisInMax - yAxisInMin) + mProgressYmin;
        mProgressX = (thumbCx - xAxisInMin) * (mProgressXmax - mProgressXmin) / (xAxisInMax - xAxisInMin) + mProgressXmin;

        if(mListener != null && ((mProgressY != oldProgressY) || (mProgressX != oldProgressX))) {
            mListener.onProgressChanged(this, (int)mProgressY, (int)mProgressX);
        }
    }

    public void setOnJoyStickProgressChangedListener(OnJoyStickProgressChangedListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);


        if(widthSize > heightSize && heightSize != 0) {
            mOuterCircleRadius = heightSize / 2;
        } else if(heightSize > widthSize && widthSize != 0) {
            mOuterCircleRadius = widthSize / 2;
        }


        setMeasuredDimension(widthSize, heightSize);
    }
}
