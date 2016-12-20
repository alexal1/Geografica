package com.alex_aladdin.geografica;

import android.app.Activity;
import android.content.Context;

import gr.antoniom.chronometer.Chronometer;

class GameTimer {

    private Chronometer mChronometer;

    GameTimer(Context context) {
        mChronometer = (Chronometer)((Activity)context).findViewById(R.id.chronometer);
    }

    void start() {
        mChronometer.start();
    }
}