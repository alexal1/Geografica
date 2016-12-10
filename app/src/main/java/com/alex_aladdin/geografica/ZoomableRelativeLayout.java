package com.alex_aladdin.geografica;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

//Зуммируемый RelativeLayout
//Класс взят отсюда:
//http://stackoverflow.com/a/10029320/7271660
//...и немного доработан
public class ZoomableRelativeLayout extends RelativeLayout {
    public static final float MIN_SCALE = 1.0f;
    public static final float MAX_SCALE = 2.0f;

    private float mScaleFactor = 1;
    private float mPivotX;
    private float mPivotY;

    public ZoomableRelativeLayout(Context context) {
        super(context);
    }

    public ZoomableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void dispatchDraw(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.scale(mScaleFactor, mScaleFactor, mPivotX, mPivotY);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    public void scale(float scaleFactor, float pivotX, float pivotY) {
        mScaleFactor = scaleFactor;
        mPivotX = pivotX;
        mPivotY = pivotY;
        this.invalidate();
    }

    //Вызывается по двойному клику. Либо возвращаем в исходное состояние, либо увеличиваем до максимума
    public void restore(final float pivotX, final float pivotY) {
        if(mScaleFactor == MIN_SCALE) {
            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t)
                {
                    scale(MIN_SCALE + (MAX_SCALE - MIN_SCALE)*interpolatedTime, pivotX, pivotY);
                }
            };

            a.setDuration(300);
            startAnimation(a);
        }
        else if(mScaleFactor == MAX_SCALE) {
            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t)
                {
                    scale(MAX_SCALE + (MIN_SCALE - MAX_SCALE)*interpolatedTime, pivotX, pivotY);
                }
            };

            a.setDuration(300);
            startAnimation(a);
        }
    }

    public void relativeScale(float scaleFactor, float pivotX, float pivotY) {
        mScaleFactor *= scaleFactor;

        if(scaleFactor >= 1)
        {
            mPivotX = mPivotX + (pivotX - mPivotX) * (1 - 1 / scaleFactor);
            mPivotY = mPivotY + (pivotY - mPivotY) * (1 - 1 / scaleFactor);
        }
        else
        {
            pivotX = getWidth()/2;
            pivotY = getHeight()/2;

            mPivotX = mPivotX + (pivotX - mPivotX) * (1 - scaleFactor);
            mPivotY = mPivotY + (pivotY - mPivotY) * (1 - scaleFactor);
        }

        this.invalidate();
    }

    public void release() {
        if(mScaleFactor < MIN_SCALE) {
            final float startScaleFactor = mScaleFactor;

            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t)
                {
                    scale(startScaleFactor + (MIN_SCALE - startScaleFactor)*interpolatedTime,mPivotX,mPivotY);
                }
            };

            a.setDuration(300);
            startAnimation(a);
        }
        else if(mScaleFactor > MAX_SCALE) {
            final float startScaleFactor = mScaleFactor;

            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t)
                {
                    scale(startScaleFactor + (MAX_SCALE - startScaleFactor)*interpolatedTime,mPivotX,mPivotY);
                }
            };

            a.setDuration(300);
            startAnimation(a);
        }
    }

    //Перемещение
    public void move(float diff_x, float diff_y) {
        mPivotX += diff_x;
        mPivotY += diff_y;
        this.invalidate();
    }

    //Геттер
    public float getScaleFactor() { return mScaleFactor; }
}