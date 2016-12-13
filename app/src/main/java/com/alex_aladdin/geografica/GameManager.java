package com.alex_aladdin.geografica;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

class GameManager {

    private MapImageView mCurrentMap;
    private ArrayList<PieceImageView> mArrayPieces = new ArrayList<>();

    GameManager(Activity activity) {
        //Загружаем карту
        mCurrentMap = (MapImageView)activity.findViewById(R.id.image_map);

        //Создаем все PieceImageView
        ExcelParser parser = new ExcelParser(activity);
        //Объявяем отображение HashMap, в которое parser'ом будет вкладываться требуемая информация о каждом PieceImageView
        HashMap<String, String> map;
        //Вспомогательные объекты для программного создания View
        RelativeLayout layout = (RelativeLayout)activity.findViewById(R.id.root);

        while ((map = parser.getNextMap()) != null) {
            //Создаем PieceImageView
            PieceImageView view = new PieceImageView(activity);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
    //Возвращает массив значений ширины этих кусочков
    ArrayList<Integer> getWidthOfSettledPieces() {
        ArrayList<Integer> array = new ArrayList<>();
        for (PieceImageView piece : mArrayPieces) {
            if (piece.isSettled())
                array.add(piece.getWidth());
        }
        return array;
    }
    //Возвращает массив значений высоты этих кусочков
    ArrayList<Integer> getHeightOfSettledPieces() {
        ArrayList<Integer> array = new ArrayList<>();
        for (PieceImageView piece : mArrayPieces) {
            if (piece.isSettled())
                array.add(piece.getHeight());
        }
        return array;
    }
}