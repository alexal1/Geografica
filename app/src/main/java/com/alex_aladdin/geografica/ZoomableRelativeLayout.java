package com.alex_aladdin.geografica;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

//Зуммируемый RelativeLayout
public class ZoomableRelativeLayout extends RelativeLayout {
    public static final float MAX_ZOOM = 2.0f;
    private float mX0, mY0;
    private boolean mZoomed = false;

    public ZoomableRelativeLayout(Context context) {
        super(context);
    }

    public ZoomableRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void zoomIn() {
        AnimatorSet animSetScale = new AnimatorSet();

        ObjectAnimator scaleAnimatorX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1.0f, MAX_ZOOM);
        ObjectAnimator scaleAnimatorY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1.0f, MAX_ZOOM);

        animSetScale.playTogether(scaleAnimatorX, scaleAnimatorY);
        animSetScale.setDuration(300);
        animSetScale.start();

        mZoomed = true;
    }

    public void zoomOut() {
        AnimatorSet animSetScale = new AnimatorSet();

        ObjectAnimator scaleAnimatorX = ObjectAnimator.ofFloat(this, View.SCALE_X, MAX_ZOOM, 1.0f);
        ObjectAnimator scaleAnimatorY = ObjectAnimator.ofFloat(this, View.SCALE_Y, MAX_ZOOM, 1.0f);

        animSetScale.playTogether(scaleAnimatorX, scaleAnimatorY);
        animSetScale.setDuration(300);
        animSetScale.start();

        mZoomed = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Реализуем перемещение
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mX0 = event.getX();
                mY0 = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                //Умножаем на 2 для скорости
                float diff_x = (mX0 - event.getX())*2;
                float diff_y = (mY0 - event.getY())*2;

                setPivotX(getPivotX() + diff_x);
                setPivotY(getPivotY() + diff_y);

                break;
        }
        return true;
    }

    public boolean isZoomed() {
        return mZoomed;
    }
}