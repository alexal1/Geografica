package com.alex_aladdin.geografica;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import gr.antoniom.chronometer.Chronometer;

class GameTimer {

    private Chronometer mChronometer;
    private TextView mTextCaption;

    GameTimer(Context context) {
        mChronometer = (Chronometer)((Activity)context).findViewById(R.id.chronometer);
        mTextCaption = (TextView)((Activity)context).findViewById(R.id.text_caption);
    }

    void start() {
        mChronometer.start();
    }

    long getBase() {
        return mChronometer.getBase();
    }

    void setBase(long base) {
        mChronometer.setBase(base);
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