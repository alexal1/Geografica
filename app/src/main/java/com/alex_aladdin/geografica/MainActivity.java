package com.alex_aladdin.geografica;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Display;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final float DELTA_MM = 2.0f; //Дельта прилипания в миллиметрах

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

        private final float delta;

        //Конструктор
        MyDragListener() {
            //Переводим величину прилипания из миллиметров в пиксели
            delta = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, DELTA_MM, getResources().getDisplayMetrics());
        }

        public boolean onDrag(View v, DragEvent event) {
            float x, y; //Текущие координаты DragAndDrop'a

            PieceImageView view = (PieceImageView) event.getLocalState();

            //Координаты цели
            final float target_x = view.getTargetX()*MapImageView.K + mImageMap.getX();
            final float target_y = view.getTargetY()*MapImageView.K + mImageMap.getY();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    view.setVisibility(View.INVISIBLE);
                    //Поднимаем этот кусок над остальными
                    view.toFront();
                    break;

                case DragEvent.ACTION_DROP:
                    x = event.getX();
                    y = event.getY();

                    //Прилипание
                    if ((Math.abs(x - target_x) < delta) && (Math.abs(y - target_y) < delta)) {
                        x = target_x;
                        y = target_y;
                        //Этот кусочек встал на свое место, ура
                        view.settle();
                        //Теперь можно открыть новый, НО только если уже нет открытых кусочков
                        if (!mManager.hasVisiblePieces())
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

        //Хотим, чтобы он показывался в случайном месте
        int piece_w = imagePiece.getWidth();
        int piece_h = imagePiece.getHeight();
        if (piece_w == 0 && piece_h == 0) return; //При первоначальной загрузке оставляем как есть
        //Размер экрана
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screen_w = size.x;
        int screen_h = size.y;
        //Высота кнопок
        ImageButton buttonAdd = (ImageButton)findViewById(R.id.button_add_piece);
        int buttons_h = buttonAdd.getHeight();
        //Диапазон
        int range_x = screen_w - piece_w;
        int range_y = screen_h - piece_h - buttons_h;
        //Берем случайные значения из диапазона
        Random random = new Random();
        int x = random.nextInt(range_x);
        int y = random.nextInt(range_y);
        imagePiece.setX(x);
        imagePiece.setY(y);

        //Поднимаем этот кусок над остальными
        imagePiece.toFront();
    }

    //Сохраняем промежуточное состоние активности (какие куски паззла уже на своих местах)
    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        //Получаем массив индексов кусочков паззла, которые уже стоят на своих местах
        ArrayList<Integer> array = mManager.getListOfSettledPieces();

        saveInstanceState.putIntegerArrayList("SETTLED_PIECES", array);
    }

    //Восстанавливаем сохраненные значения из метода onSaveInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ArrayList<Integer> array = savedInstanceState.getIntegerArrayList("SETTLED_PIECES");
        if (array == null) return;

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
        float map_x, map_y;
        if (bitmap_ratio > screen_ratio) {
            map_x = 0;
            map_y = (screen_h - screen_w/bitmap_ratio) / 2;
        }
        else {
            map_x = (screen_w - screen_h*bitmap_ratio) / 2;
            map_y = 0;
        }

        //Берем кусочки паззла с номерами из array и втыкаем их на нужные места
        for (int i : array) {
            PieceImageView view = mManager.getPiece(i);

            //Координаты относительно картинки
            float picture_x = view.getTargetX();
            float picture_y = view.getTargetY();
            //Координаты относительно экрана
            final float target_x = picture_x*MapImageView.K + map_x;
            final float target_y = picture_y*MapImageView.K + map_y;
            //Устанавливаем их
            view.setX(target_x - screen_w/2);
            view.setY(target_y - screen_h/2);
            //И снова подтверждаем, что кусочек на своем месте
            view.settle();
            //И делаем его видимым
            view.setVisibility(View.VISIBLE);
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
    //А также блокирующий все кнопки
    private void setPiecesEnabled(boolean enabled) {
        mPiecesEnabled = enabled;

        ImageButton buttonAdd = (ImageButton)findViewById(R.id.button_add_piece);
        PieceImageView piece;
        int i = 0;
        //Если true, вешаем слушатели
        if (enabled) {
            //При этом НЕ НАДО вешать слушатели на те кусочки, которые уже стоят на своих местах
            ArrayList<Integer> settled_pieces = mManager.getListOfSettledPieces();

            while ((piece = mManager.getPiece(i)) != null) {
                //Если этот кусочек ещё не на своем месте, вешаем слушатель
                if (!settled_pieces.contains(i))
                    piece.setOnTouchListener(new MyDragTouchListener());

                i++;
            }

            //Разблокируем кнопки
            buttonAdd.setEnabled(true);
        }
        //Если false, снимаем слушатели
        else {
            while ((piece = mManager.getPiece(i)) != null) {
                piece.setOnTouchListener(null);
                i++;
            }

            //Блокируем кнопки
            buttonAdd.setEnabled(false);
        }
    }

    //Клик на кнопку, добавляющую новый кусок паззла
    public void onButtonAddClick(View view) {
        showNewPiece();
    }
}