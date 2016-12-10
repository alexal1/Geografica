package com.alex_aladdin.geografica;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final float STICK = 1/50f; //Помноженное на высоту экрана, дает дельту прилипания

    private GameManager mManager;
    private MapImageView mImageMap;
    private boolean mPiecesEnabled = true; //Разрешено ли перетаскивание кусочков (запрещается при зуммировании)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mManager = new GameManager(this);

        mImageMap = mManager.getMap();

        //Если приложение запущено впервые
        if (savedInstanceState == null)
            showNewPiece();

        RelativeLayout rootLayout = (RelativeLayout)findViewById(R.id.root);
        rootLayout.setOnDragListener(new MyDragListener()); //Теперь мы можем перетаскивать кусочки паззла
        rootLayout.setOnTouchListener(new MyZoomTouchListener()); //Теперь мы можем зуммировать layout
    }

    //Класс MyDragTouchListener, вешается на PieceImageView для DragAndDrop'a
    private final class MyDragTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                //Выключаем подсветку
                view.setBackgroundResource(R.color.transparent);
                //Запускаем DragAndDrop
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    newDragAndDrop(view, data, shadowBuilder, view, 0);
                }
                else {
                    oldDragAndDrop(view, data, shadowBuilder, view, 0);
                }
                return true;
            }
            else {
                return false;
            }
        }

        //Метод DragAndDrop для старых и новых API
        @TargetApi(24)
        private void newDragAndDrop(View view, ClipData data, View.DragShadowBuilder shadowBuilder, Object myLocalState, int flags) {
            view.startDragAndDrop(data, shadowBuilder, myLocalState, flags);
        }
        @SuppressWarnings("deprecation")
        private void oldDragAndDrop(View view, ClipData data, View.DragShadowBuilder shadowBuilder, Object myLocalState, int flags) {
            view.startDrag(data, shadowBuilder, myLocalState, flags);
        }
    }

    //Класс MyDragListener, вешается на layout для DragAndDrop'a
    private class MyDragListener implements View.OnDragListener {
        public boolean onDrag(View v, DragEvent event) {
            float x, y; //Текущие координаты DragAndDrop'a

            PieceImageView view = (PieceImageView) event.getLocalState();
            TextView textAccuracy = (TextView)findViewById(R.id.text_accuracy);

            //Координаты цели
            final float target_x = view.getTargetX()*MapImageView.K + mImageMap.getX();
            final float target_y = view.getTargetY()*MapImageView.K + mImageMap.getY();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    view.setVisibility(View.INVISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    x = event.getX();
                    y = event.getY();
                    float picture_x = (x - view.getX())/MapImageView.K;
                    float picture_y = (y - view.getY())/MapImageView.K;
                    textAccuracy.setText("x = " + picture_x + ", y = " + picture_y);
                    break;
                case DragEvent.ACTION_DROP:
                    x = event.getX();
                    y = event.getY();

                    //Прилипание
                    float delta = STICK*v.getHeight();
                    if ((Math.abs(x - target_x) < delta) && (Math.abs(y - target_y) < delta)) {
                        x = target_x;
                        y = target_y;
                        //Этот кусочек встал на свое место, ура
                        view.settle();
                        //Теперь можно открыть новый
                        showNewPiece();
                        //А с этого уже можно снять обработчик
                        view.setOnTouchListener(null);
                    }
                    else {
                        //Включаем подсветку
                        view.setBackgroundResource(R.drawable.backlight);
                    }

                    view.setX(x - view.getWidth()/2);
                    view.setY(y - view.getHeight()/2);
                    view.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    //Добавляем на экран новый кусочек паззла
    private void showNewPiece() {
        PieceImageView imagePiece;
        if ((imagePiece = mManager.getPiece()) == null) return;
        //Вешаем обработчик
        imagePiece.setOnTouchListener(new MyDragTouchListener());
        //Делаем видимым и включаем подсветку
        imagePiece.setVisibility(View.VISIBLE);
        imagePiece.setBackgroundResource(R.drawable.backlight);
    }

    //Сохраняем промежуточное состоние активности (какие куски паззла уже на своих местах)
    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        //Получаем массив индексов кусочков паззла, которые уже стоят на своих местах
        ArrayList<Integer> array_indexes = mManager.getListOfSettledPieces();
        //Получаем массив значений ширины этих кусочков паззла
        ArrayList<Integer> array_width = mManager.getWidthOfSettledPieces();
        //Получаем массив значений высоты этих кусочков паззла
        ArrayList<Integer> array_height = mManager.getHeightOfSettledPieces();

        saveInstanceState.putInt("MAP_WIDTH", mImageMap.getWidth());
        saveInstanceState.putIntegerArrayList("SETTLED_PIECES", array_indexes);
        saveInstanceState.putIntegerArrayList("SETTLED_PIECES_WIDTH", array_width);
        saveInstanceState.putIntegerArrayList("SETTLED_PIECES_HEIGHT", array_height);
    }

    //Восстанавливаем сохраненные значения из метода onSaveInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int map_old_width = savedInstanceState.getInt("MAP_WIDTH");
        ArrayList<Integer> array_indexes = savedInstanceState.getIntegerArrayList("SETTLED_PIECES");
        ArrayList<Integer> array_width = savedInstanceState.getIntegerArrayList("SETTLED_PIECES_WIDTH");
        ArrayList<Integer> array_height = savedInstanceState.getIntegerArrayList("SETTLED_PIECES_HEIGHT");
        if (array_indexes == null || array_width == null || array_height == null) return;

        //Необходимо вычислить координаты верхнего левого угла карты относительно экрана
        //Мы не можем просто взять эти координаты из объекта MapImageView, потому что у него они пока ещё нулевые
        //Получаем размеры экрана
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int screen_w = size.x;
        final int screen_h = size.y;
        //Пропорции экрана и картинки
        float screen_ratio = (float)screen_w/screen_h;
        float bitmap_ratio = MapImageView.RATIO;
        //Считаем по-разному в зависимости от того, какие пропорции у экрана и картинки
        float map_x, map_y, map_new_width;
        if (bitmap_ratio > screen_ratio) {
            map_x = 0;
            map_y = (screen_h - screen_w/bitmap_ratio) / 2;
            map_new_width = (float)screen_w;
        }
        else {
            map_x = (screen_w - screen_h*bitmap_ratio) / 2;
            map_y = 0;
            map_new_width = (float)screen_h*bitmap_ratio;
        }
        //Во сколько раз сжалась/расширилась карта по сравнению с предыдущим состоянием
        //Так мы узнаем, во сколько раз надо изменить размеры кусочков паззла
        float c = map_new_width / (float)map_old_width;

        //Берем кусочки паззла с номерами из array и втыкаем их на нужные места
        for (int i : array_indexes) {
            PieceImageView view = mManager.getPiece(i);

            //Необходимо узнать размеры этого кусочка, зная размеры, которые были до перезагрузки активности
            int piece_old_width = array_width.get(array_indexes.indexOf(i));
            int piece_old_height = array_height.get(array_indexes.indexOf(i));
            float piece_new_width = (float)piece_old_width * c;
            float piece_new_height = (float)piece_old_height * c;

            //Координаты относительно картинки
            float picture_x = view.getTargetX();
            float picture_y = view.getTargetY();
            //Координаты относительно экрана
            final float target_x = picture_x*MapImageView.K + map_x;
            final float target_y = picture_y*MapImageView.K + map_y;
            //Устанавливаем их
            view.setX(target_x - piece_new_width/2);
            view.setY(target_y - piece_new_height/2);
            //И снова подтверждаем, что кусочек на своем месте
            view.settle();
            //И делаем его видимым
            view.setVisibility(View.VISIBLE);

            Log.i("Restore", "map_x = " + map_x + ", map_y = " + map_y);
            Log.i("Restore", String.valueOf(array_indexes));
        }

        //Наконец, показываем новый кусочек паззла
        showNewPiece();
    }

    //Класс MyZoomTouchListener, вешается на layout для зуммирования
    private final class MyZoomTouchListener implements View.OnTouchListener {
        private ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(MainActivity.this, new OnPinchListener());
        private GestureDetector doubleTapGestureDetector = new GestureDetector(MainActivity.this, new GestureDoubleTap());
        private ZoomableRelativeLayout rootLayout = (ZoomableRelativeLayout)findViewById(R.id.root);
        private float x0, y0, x, y;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            scaleGestureDetector.onTouchEvent(event);
            doubleTapGestureDetector.onTouchEvent(event);

            //Реализуем перемещение
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x0 = event.getX();
                    y0 = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    x = event.getX();
                    y = event.getY();

                    rootLayout.move(x0-x, y0-y);

                    x0 = x;
                    y0 = y;
                    break;
            }

            return true;
        }
    }

    //Класс OnPinchListener, нужный для зуммирования
    private class OnPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private ZoomableRelativeLayout rootLayout = (ZoomableRelativeLayout)findViewById(R.id.root);
        private float currentSpan;
        private float startFocusX;
        private float startFocusY;

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            setPiecesEnabled(false); //Делаем невозможным перетаскивание кусочков

            currentSpan = detector.getCurrentSpan();
            startFocusX = detector.getFocusX();
            startFocusY = detector.getFocusY();
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            rootLayout.relativeScale(detector.getCurrentSpan() / currentSpan, startFocusX, startFocusY);
            currentSpan = detector.getCurrentSpan();
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            if (rootLayout.getScaleFactor() <= ZoomableRelativeLayout.MIN_SCALE)
                setPiecesEnabled(true); //Если вернулись в нормальный масштаб, делаем кусочки снова доступными

            rootLayout.release();
        }
    }

    //Класс GestureDoubleTap, нужный для определения двойного клика
    private class GestureDoubleTap extends GestureDetector.SimpleOnGestureListener {
        private ZoomableRelativeLayout rootLayout = (ZoomableRelativeLayout)findViewById(R.id.root);

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            rootLayout.restore(x, y);

            setPiecesEnabled(!mPiecesEnabled); //Делаем кусочки доступными/недоступными в зависимости от состояния
            return true;
        }
    }

    //Метод, снимающий/вешающий слушатели на PieceImageView до/после зуммирования
    private void setPiecesEnabled(boolean enabled) {
        mPiecesEnabled = enabled;

        PieceImageView piece;
        int i = 0;
        //Если true, вешаем слушатели
        if (enabled)
            while ((piece = mManager.getPiece(i)) != null) {
                piece.setOnTouchListener(new MyDragTouchListener());
                i++;
            }
        //Если false, снимаем слушатели
        else
            while ((piece = mManager.getPiece(i)) != null) {
                piece.setOnTouchListener(null);
                i++;
            }
    }
}