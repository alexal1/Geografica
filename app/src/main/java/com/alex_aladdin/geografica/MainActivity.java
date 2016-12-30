package com.alex_aladdin.geografica;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final float DELTA_MM = 5.0f; //Дельта прилипания в миллиметрах

    private GameManager mManager;
    private MapImageView mImageMap;
    private boolean mPiecesEnabled = true; //Разрешено ли перетаскивание кусочков (запрещается при зуммировании)
    private GameTimer mTimer;
    private State mState; //Состояние игры

    private enum State {
        START, RUN, FINISH
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Принимаем в качестве параметра название карты, переданное нам из меню
        String map_name = getIntent().getExtras().getString("MAP_NAME");

        mManager = new GameManager(this, map_name);
        mImageMap = mManager.getMap();

        mTimer = new GameTimer(this);

        RelativeLayout rootLayout = (RelativeLayout)findViewById(R.id.root);
        rootLayout.setOnDragListener(new MyDragListener()); //Теперь мы можем перетаскивать кусочки паззла
        rootLayout.setOnTouchListener(new MyZoomTouchListener()); //Теперь мы можем зуммировать layout

        //Если приложение запущено впервые
        if (savedInstanceState == null) {
            mState = State.START;
            showStartLayout();
        }
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
                    //Показываем название вместо таймера
                    mTimer.showCaption(view);
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
                        //Показываем его позади всех остальных
                        view.toBack();
                        //Теперь можно показать следующий кусочек, но только если нет других доступных
                        if (!mManager.hasVisiblePieces())
                            showNewPiece();
                        //А с этого уже можно снять обработчик
                        view.setOnTouchListener(null);
                    }
                    else {
                        //Включаем подсветку
                        view.setBackgroundResource(R.drawable.backlight);
                    }

                    view.setX(x - (float)view.getWidth()/2);
                    view.setY(y - (float)view.getHeight()/2);
                    view.setVisibility(View.VISIBLE);

                    //Показываем таймер вместо названия
                    mTimer.showTimer();
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
        //Поднимаем этот кусок над остальными
        imagePiece.toFront();

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
        //Берем max, чтобы не получить отрицательных значений
        int range_x = Math.max(screen_w - piece_w, 1);
        int range_y = Math.max(screen_h - piece_h - buttons_h, 1);
        //Берем случайные значения из диапазона
        Random random = new Random();
        int x = random.nextInt(range_x);
        int y = random.nextInt(range_y);
        imagePiece.setX(x);
        imagePiece.setY(y);
    }

    //Сохраняем промежуточное состоние активности (какие куски паззла уже на своих местах)
    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        //Сохраняем состояние игры
        saveInstanceState.putString("STATE", mState.toString());

        //Получаем массив индексов кусочков паззла, которые уже стоят на своих местах
        ArrayList<Integer> array = mManager.getListOfSettledPieces();
        saveInstanceState.putIntegerArrayList("SETTLED_PIECES", array);

        //Сохраняем текущее время таймера
        saveInstanceState.putLong("TIMER", mTimer.getBase());
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
        final float screen_w = size.x;
        final float screen_h = size.y;
        //Пропорции экрана и картинки
        float screen_ratio = screen_w/screen_h;
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
            //Делаем его видимым
            view.setVisibility(View.VISIBLE);
            //Отодвигаем его на задний план, чтоб он не мог перекрыть элементы интерфейса
            view.toBack();
        }

        //Далее выполняем действия в зависимости от состояния игры
        mState = State.valueOf(savedInstanceState.getString("STATE"));
        switch (mState){
            case START:
                //Показываем стартовый экран
                showStartLayout();
                break;
            case RUN:
                //Показываем новый кусок паззла
                showNewPiece();
                //Выставляем нужное время таймера
                long base = savedInstanceState.getLong("TIMER");
                mTimer.start();
                mTimer.setBase(base);
                break;
            case FINISH:
                break;
        }
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
        ImageButton buttonInfo = (ImageButton)findViewById(R.id.button_info);
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
            buttonInfo.setEnabled(true);
        }
        //Если false, снимаем слушатели
        else {
            while ((piece = mManager.getPiece(i)) != null) {
                piece.setOnTouchListener(null);
                i++;
            }

            //Блокируем кнопки
            buttonAdd.setEnabled(false);
            buttonInfo.setEnabled(false);
        }
    }

    //Клик на кнопку, добавляющую новый кусок паззла
    public void onButtonAddClick(View view) {
        showNewPiece();
    }

    //Клик на кнопку, меняющую картинку карты
    public void onButtonInfoClick(View view) {
        mImageMap.changeMapInfo(this);
    }

    //Клик на стартовый экран
    public void onLayoutStartClick(View view) {
        //Обновляем состояние
        mState = State.RUN;
        //Делаем невидимым темный экран вместе с кругом
        RelativeLayout layoutStart = (RelativeLayout)findViewById(R.id.layout_start);
        layoutStart.setVisibility(View.GONE);
        //Показываем кусок паззла
        showNewPiece();
        //Включаем таймер
        mTimer.start();
    }

    //Показываем стартовый экран
    private void showStartLayout() {
        final RelativeLayout layoutStart = (RelativeLayout)findViewById(R.id.layout_start);
        layoutStart.setVisibility(View.VISIBLE);
        //Включаем обратный отсчет
        final TextView textStart = (TextView)findViewById(R.id.text_start);
        new CountDownTimer(4000, 1000) {

            public void onTick(final long millisUntilFinished) {
                textStart.setText(String.valueOf(millisUntilFinished/1000));
                textStart.setAlpha(1.0f);
                //Затухание
                ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(textStart, "alpha", 0);
                alphaAnimator.setDuration(1200);
                //Поторапливаем onFinish()
                if (millisUntilFinished/1000 == 1) alphaAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        onFinish();
                    }
                });
                alphaAnimator.start();
            }

            public void onFinish() {
                //Делаем проверку на то, не было ли ещё нажатия на экран
                if (mState == State.START) onLayoutStartClick(layoutStart);
            }
        }.start();
    }
}