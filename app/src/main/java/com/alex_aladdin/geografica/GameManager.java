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
import java.util.List;

import gr.antoniom.chronometer.Chronometer;

class GameManager {

    private MapImageView mCurrentMap;
    private ArrayList<PieceImageView> mArrayPieces = new ArrayList<>();
    private HashMap<String, Integer> mTestMistakes;

    private Chronometer mChronometer;
    private long mCurrentTime;
    private Boolean mStarted = false;

    GameManager(Context context, MapImageView.Level level, String map_name) {
        //Загружаем карту
        mCurrentMap = (MapImageView)((Activity)context).findViewById(R.id.image_map);
        mCurrentMap.loadMap(context, level, map_name);

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

    // Добавляем ошибку к соответствующей карте
    void addTestMistake(String caption) {
        int value = (mTestMistakes.containsKey(caption)) ? mTestMistakes.get(caption) : 0;
        value++;
        mTestMistakes.put(caption, value);
    }

    @SuppressWarnings("unchecked")
    void setTestMistakes(HashMap testMistakes) {
        this.mTestMistakes = (HashMap<String, Integer>) testMistakes;
    }

    /* --- Геттеры --- */

    // Возвращаем произвольный кусок паззла
    // НЕ стоящий на своем месте, и НЕ видимый пользователю
    PieceImageView getNewRandomPiece() {
        //Клонируем массив
        ArrayList<PieceImageView> array_random = new ArrayList<>(mArrayPieces);
        //Перемешиваем
        Collections.shuffle(array_random);

        for (PieceImageView piece : array_random) {
            if (!piece.isSettled() && (piece.getVisibility() == View.INVISIBLE)) return piece;
        }
        return null;
    }

    // Возвращаем произвольный кусок паззла
    // Уже установленный, но для которого ещё не пройден тест
    PieceImageView getUncheckedRandomPiece() {
        return getUncheckedRandomPiece(null);
    }

    // То же самое, но откладываем текущий кусок в конец
    PieceImageView getUncheckedRandomPiece(PieceImageView currentPiece) {
        // Клонируем массив индексов установленных кусков
        ArrayList<Integer> array_indices = new ArrayList<>(getIndicesOfSettledPieces());
        // Вычитаем массив кусков с пройденным тестом
        array_indices.removeAll(getIndicesOfCheckedPieces());

        if (array_indices.isEmpty())
            return null;
        else {
            // Перемешиваем
            Collections.shuffle(array_indices);
            // Если был передан текущий кусок
            if (currentPiece != null) {
                // Ищем его в массиве и откладываем в конец
                int currentPieceIndex = mArrayPieces.indexOf(currentPiece);
                if (array_indices.contains(currentPieceIndex)) {
                    array_indices.remove(Integer.valueOf(currentPieceIndex));
                    array_indices.add(currentPieceIndex);
                }
            }

            return getPiece(array_indices.get(0));
        }
    }

    //Возвращает либо кусок с нужным индексом, либо null
    PieceImageView getPiece(int number) {
        if (number < mArrayPieces.size())
            return mArrayPieces.get(number);
        else
            return null;
    }
    MapImageView getMap() { return mCurrentMap; }

    // Возвращает массив номеров кусочков паззла, которые уже стоят на своих местах
    ArrayList<Integer> getIndicesOfSettledPieces() {
        ArrayList<Integer> array = new ArrayList<>();
        for (PieceImageView piece : mArrayPieces) {
            if (piece.isSettled())
                array.add(mArrayPieces.indexOf(piece));
        }
        return array;
    }

    // Возвращает массив номеров кусочков паззла, для которых уже пройден тест
    ArrayList<Integer> getIndicesOfCheckedPieces() {
        ArrayList<Integer> array = new ArrayList<>();
        for (PieceImageView piece : mArrayPieces) {
            if (piece.isChecked())
                array.add(mArrayPieces.indexOf(piece));
        }
        return array;
    }

    // Возвращает три случайных куска, выбранных из множества всех, исключая данный
    List<PieceImageView> getRandomPieces(PieceImageView excludedPiece) {
        // Клонируем массив
        ArrayList<PieceImageView> array_random = new ArrayList<>(mArrayPieces);
        // Исключаем данный кусок
        array_random.remove(excludedPiece);
        // Перемешиваем
        Collections.shuffle(array_random);

        return array_random.subList(0, 3);
    }

    HashMap<String, Integer> getTestMistakes() {
        return mTestMistakes;
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
        return mCurrentTime;
    }

    void setTime(long time) {
        //Устанавливаем время
        mCurrentTime = time;
        mChronometer.setBase(SystemClock.elapsedRealtime() - mCurrentTime);
    }
}