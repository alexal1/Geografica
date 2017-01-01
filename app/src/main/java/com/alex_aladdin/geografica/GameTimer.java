package com.alex_aladdin.geografica;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import gr.antoniom.chronometer.Chronometer;

class GameTimer {

    private Chronometer mChronometer;
    private TextView mTextCaption;
    private long mCurrentTime;
    private Boolean mStarted = false;

    GameTimer(Context context) {
        mChronometer = (Chronometer)((Activity)context).findViewById(R.id.chronometer);
        mTextCaption = (TextView)((Activity)context).findViewById(R.id.text_caption);
    }

    void start() {
        //Обнуляем и запускаем
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        mStarted = true;
    }

    void stop() {
        //Не обновляем показания таймера, если он уже остановлен
        if (!mStarted) return;
        //Останавливаем и обновляем
        mChronometer.stop();
        mCurrentTime = SystemClock.elapsedRealtime() - mChronometer.getBase();
        mStarted = false;
    }

    long getTime() {
        //Синхронизируем и возвращаем значение
        setTime(mCurrentTime);
        return mCurrentTime;
    }

    void setTime(long time) {
        mCurrentTime = time;
        mChronometer.setBase(SystemClock.elapsedRealtime() - mCurrentTime);
    }

    //Метод, показывающий вместо таймера название
    void showCaption(PieceImageView view) {
        String caption = view.getCaption();
        mTextCaption.setText(caption);
        mTextCaption.setVisibility(View.VISIBLE);
        mChronometer.setVisibility(View.INVISIBLE);
    }

    //Метод, показывающий вместо названия таймер
    void showTimer() {
        mTextCaption.setVisibility(View.INVISIBLE);
        mChronometer.setVisibility(View.VISIBLE);
    }
}