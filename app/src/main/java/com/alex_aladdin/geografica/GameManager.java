package com.alex_aladdin.geografica;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import gr.antoniom.chronometer.Chronometer;

class GameManager {

    private MapImageView mCurrentMap;
    private ArrayList<PieceImageView> mArrayPieces = new ArrayList<>();

    private Chronometer mChronometer;
    private long mCurrentTime;
    private Boolean mStarted = false;

    GameManager(Context context, String map_name) {
        //Загружаем карту
        mCurrentMap = (MapImageView)((Activity)context).findViewById(R.id.image_map);
        mCurrentMap.loadMap(context, map_name);

        //Загружаем навигатор
        ImageView imageNav = (ImageView)((Activity)context).findViewById(R.id.image_nav);
        int resId = context.getResources().getIdentifier("nav_" + map_name, "drawable", context.getPackageName());
        imageNav.setImageResource(resId);

        //Получаем экземпляр хронометра
        mChronometer = (Chronometer)((Activity)context).findViewById(R.id.chronometer);

        /* --- Создаем все PieceImageView --- */
        ExcelParser parser = new ExcelParser(context, map_name);
        //Объявяем отображение HashMap, в которое parser'ом будет вкладываться требуемая информация о каждом PieceImageView
        HashMap<String, String> map;
        //Вспомогательные объекты для программного создания View
        RelativeLayout layout = (RelativeLayout)((Activity)context).findViewById(R.id.layout_zoom);

        while ((map = parser.getNextMap()) != null) {
            //Создаем PieceImageView
            PieceImageView view = new PieceImageView(context);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //Выравнивание по центру
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

            view.setLayoutParams(layoutParams);
            view.loadPiece(map);
            layout.addView(view);
            view.setVisibility(View.INVISIBLE);
            //Передаем в массив
            mArrayPieces.add(view);
        }
    }

    //Есть ли кусочки паззла, ещё не установленные на свои места, но уже видимые
    boolean hasVisiblePieces() {
        for (PieceImageView piece : mArrayPieces) {
            if (!piece.isSettled() && (piece.getVisibility() == View.VISIBLE))
                return true;
        }
        return false;
    }

    /* --- Геттеры --- */

    //Возвращаем произвольный кусок паззла
    // НЕ стоящий на своем месте, и НЕ видимый пользователю
    PieceImageView getPiece() {
        //Клонируем массив
        ArrayList<PieceImageView> array_random = new ArrayList<>(mArrayPieces);
        //Перемешиваем
        Collections.shuffle(array_random);

        for (PieceImageView piece : array_random) {
            if (!piece.isSettled() && (piece.getVisibility() == View.INVISIBLE)) return piece;
        }
        return null;
    }

    //Возвращает либо кусок с нужным индексом, либо null
    PieceImageView getPiece(int number) {
        if (number < mArrayPieces.size())
            return mArrayPieces.get(number);
        else
            return null;
    }
    MapImageView getMap() { return mCurrentMap; }

    //Возвращает массив номеров кусочков паззла, которые уже стоят на своих местах
    ArrayList<Integer> getListOfSettledPieces() {
        ArrayList<Integer> array = new ArrayList<>();
        for (PieceImageView piece : mArrayPieces) {
            if (piece.isSettled())
                array.add(mArrayPieces.indexOf(piece));
        }
        return array;
    }

    /* --- Работаем с хронометром --- */

    void startTimer() {
        //Обнуляем и запускаем
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        mStarted = true;
    }

    void resumeTimer() {
        //Восстанавливаем и запускаем
        setTime(mCurrentTime);
        mChronometer.start();
        mStarted = true;
    }

    void stopTimer() {
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
        //Устанавливаем время
        mCurrentTime = time;
        mChronometer.setBase(SystemClock.elapsedRealtime() - mCurrentTime);
    }
}