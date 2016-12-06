package com.alex_aladdin.geografica;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

class GameManager {

    private MapImageView mCurrentMap;
    private ArrayList<PieceImageView> mArrayPieces = new ArrayList<>();
    private int mCurrentPiece = 0;

    GameManager(Activity activity) {
        //Загружаем карту
        mCurrentMap = (MapImageView)activity.findViewById(R.id.image_map);

        //Создаем все PieceImageView
        ExcelParser parser = new ExcelParser(activity);
        //Объявяем отображение HashMap, в которое parser'ом будет вкладываться требуемая информация о каждом PieceImageView
        HashMap<String, String> map;
        //Вспомогательные объекты для программного создания View
        RelativeLayout layout = (RelativeLayout)activity.findViewById(R.id.activity_main);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        while ((map = parser.getNextMap()) != null) {
            //Создаем PieceImageView
            PieceImageView view = new PieceImageView(activity);
            view.setLayoutParams(layoutParams);
            view.loadPiece(map);
            layout.addView(view);
            //Передаем в массив
            mArrayPieces.add(view);
        }
    }

    PieceImageView getCurrentPiece() { return mArrayPieces.get(mCurrentPiece); }
    MapImageView getCurrentMap() { return mCurrentMap; }
}