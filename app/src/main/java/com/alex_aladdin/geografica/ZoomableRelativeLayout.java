package com.alex_aladdin.geografica;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.Stack;

//Зуммируемый RelativeLayout
public class ZoomableRelativeLayout extends RelativeLayout {
    public static final float MAX_ZOOM = 2.0f;
    private float mX0, mY0;
    private boolean mZoomed = false;
    private Stack<PieceImageView> mStack = new Stack<>(); //Стек из ссылок на PieceImageView, на верхнюю будем центрировать зум

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
        //Получаем координаты для центрирования зума
        //Для этого берем верхний кусок из стека
        PieceImageView imagePiece = getCurrentPiece();
        //Эмпирическая формула
        float x = (imagePiece.getX() + (float) imagePiece.getWidth() / 2) * 2 - (float) getWidth() / 2;
        float y = (imagePiece.getY() + (float) imagePiece.getHeight() / 2) * 2 - (float) getHeight() / 2;
        setPivotX(x);
        setPivotY(y);

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
        //В неувеличенном состоянии не обрабатываем событие
        if (!isZoomed()) return false;

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

    /* --- Работаем со стеком --- */

    //Новый кусок стал актуальным (касание либо появление)
    //Добавляем его в стек
    public void setCurrentPiece(PieceImageView imagePiece) {
        mStack.push(imagePiece);
    }

    //Извлекаем верхний кусок из стека
    //Установленные куски выкидываем из стека
    private PieceImageView getCurrentPiece() {
        while (true) {
            PieceImageView imagePiece = mStack.peek();
            //Если установлен, выкидываем
            if (imagePiece.isSettled()) {
                mStack.pop();
                continue;
            }
            return imagePiece;
        }
    }
}