package com.alex_aladdin.geografica;

import android.app.FragmentManager;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Display;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Random;

import gr.antoniom.chronometer.Chronometer;

public class MainActivity extends AppCompatActivity implements FragmentStart.OnCompleteListener,
        FragmentFinishTraining.OnCompleteListener, FragmentFinishCheck.OnCompleteListener {

    public static final float DELTA_MM = 5.0f; //Дельта прилипания в миллиметрах

    private GameManager mManager;
    private MapImageView mImageMap;
    private ZoomableRelativeLayout mLayoutZoom;
    private State mState; //Состояние игры

    private enum State {
        START, RUN, FINISH
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Видимость таймера и кнопки buttonInfo
        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer);
        ImageButton buttonInfo = (ImageButton) findViewById(R.id.button_info);
        if (getIntent().getBooleanExtra("SHOW_TIMER", false)) chronometer.setVisibility(View.VISIBLE);
        if (getIntent().getBooleanExtra("SHOW_BUTTON_INFO", false)) buttonInfo.setVisibility(View.VISIBLE);

        //Принимаем в качестве параметра название карты, переданное нам из меню
        String map_name = getIntent().getExtras().getString("MAP_NAME");

        mManager = new GameManager(this, map_name);
        mImageMap = mManager.getMap();

        mLayoutZoom = (ZoomableRelativeLayout)findViewById(R.id.layout_zoom);
        mLayoutZoom.setOnDragListener(new MyDragListener()); //Теперь мы можем перетаскивать кусочки паззла

        //Если приложение запущено впервые
        if (savedInstanceState == null) {
            mState = State.START;
            //Подключаем к активности стартовый фрагмент, если стоит соответствующий флаг
            if (getIntent().getBooleanExtra("FRAGMENT_START", false)) {
                FragmentStart fragmentStart = new FragmentStart();
                getFragmentManager().beginTransaction()
                        .add(R.id.layout_root, fragmentStart, "START")
                        .commit();
            }
            else {
                //Обновляем состояние
                mState = State.RUN;
                //Показываем кусок паззла
                showNewPiece();
                //Включаем таймер
                mManager.startTimer();
            }
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

            //Фрагмент-подсказка
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTip fragmentTip = (FragmentTip) fragmentManager.findFragmentById(R.id.fragment_tip);

            //Координаты цели
            final float target_x = view.getTargetX();
            final float target_y = view.getTargetY();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    view.setVisibility(View.INVISIBLE);
                    //Включаем подсказку
                    fragmentTip.init(view);
                    //Поднимаем этот кусок над остальными
                    view.toFront();
                    //Делаем его актуальным для зума
                    mLayoutZoom.setCurrentPiece(view);
                    break;

                case DragEvent.ACTION_DRAG_LOCATION:
                    //Подсказка
                    x = event.getX();
                    y = event.getY();
                    fragmentTip.set(x, y);
                    break;

                case DragEvent.ACTION_DROP:
                    x = event.getX();
                    y = event.getY();

                    //Прилипание
                    if ((Math.abs(x - target_x) < delta) && (Math.abs(y - target_y) < delta)) {
                        //Этот кусочек встал на свое место, ура
                        view.settle(x, y);
                        //Опускаем его вниз, чтобы он не мог загородить собой другой кусок
                        view.toBack();
                        //Теперь можно показать следующий кусочек, но только если нет других доступных
                        if (!mManager.hasVisiblePieces())
                            showNewPiece();
                    }
                    else {
                        //Включаем подсветку
                        view.setBackgroundResource(R.drawable.backlight);
                        //Задаем новые координаты
                        view.setX(x - (float)view.getWidth()/2);
                        view.setY(y - (float)view.getHeight()/2);
                    }

                    //Делаем видимым
                    view.setVisibility(View.VISIBLE);
                    break;

                case DragEvent.ACTION_DRAG_ENDED:
                    //Убираем подсказку
                    fragmentTip.close();
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
        //Новых кусочков не осталось
        if ((imagePiece = mManager.getPiece()) == null) {
            //Не пристыкованных тоже не осталось
            if (!mManager.hasVisiblePieces()) {
                /* --- ФИНИШ --- */
                mState = State.FINISH;
                mManager.stopTimer();

                //Если были в зуме, возвращаемся
                if (mLayoutZoom.isZoomed()) mLayoutZoom.zoomOut();

                //Показываем один из финишных экранов
                if (getIntent().getBooleanExtra("FRAGMENT_FINISH_TRAINING", false)) {
                    String caption = getIntent().getStringExtra("MAP_CAPTION").toUpperCase() + " " +
                            getString(R.string.finish_federal_district);
                    long time = mManager.getTime();
                    FragmentFinishTraining fragmentFinishTraining = FragmentFinishTraining.newInstance(caption, time);
                    getFragmentManager().beginTransaction()
                            .add(R.id.layout_root, fragmentFinishTraining)
                            .commit();
                }
                if (getIntent().getBooleanExtra("FRAGMENT_FINISH_CHECK", false)) {
                    FragmentFinishCheck fragmentFinishCheck = new FragmentFinishCheck();
                    getFragmentManager().beginTransaction()
                            .add(R.id.layout_root, fragmentFinishCheck)
                            .commit();
                }
            }
            return;
        }
        //Делаем видимым и включаем подсветку
        imagePiece.setVisibility(View.VISIBLE);
        imagePiece.setBackgroundResource(R.drawable.backlight);
        //Поднимаем этот кусок над остальными
        imagePiece.toFront();
        //Делаем его актуальным для зума
        mLayoutZoom.setCurrentPiece(imagePiece);

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
        ImageButton buttonAdd = (ImageButton) findViewById(R.id.button_add_piece);
        ImageButton buttonInfo = (ImageButton) findViewById(R.id.button_info);
        int down_h = buttonAdd.getHeight();
        int up_h = buttonInfo.getHeight();
        //Диапазон
        //Берем max, чтобы не получить отрицательных значений
        int range_x = Math.max(screen_w - piece_w, 1);
        int range_y = Math.max(screen_h - piece_h - down_h - up_h, 1);
        //Берем случайные значения из диапазона
        Random random = new Random();
        int x = random.nextInt(range_x);
        int y = up_h + random.nextInt(range_y);
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
        mManager.stopTimer();
        saveInstanceState.putLong("TIMER", mManager.getTime());
    }

    //Восстанавливаем сохраненные значения из метода onSaveInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ArrayList<Integer> array = savedInstanceState.getIntegerArrayList("SETTLED_PIECES");
        if (array == null) return;

        //Берем кусочки паззла с номерами из array и втыкаем их на нужные места
        for (int i : array) {
            //Берем
            PieceImageView view = mManager.getPiece(i);
            //И втыкаем
            view.settle();
            //Делаем его видимым
            view.setVisibility(View.VISIBLE);
        }

        //Далее выполняем действия в зависимости от состояния игры
        mState = State.valueOf(savedInstanceState.getString("STATE"));
        long time = savedInstanceState.getLong("TIMER");
        switch (mState){
            case START:
                //Ничего не делаем
                break;
            case RUN:
                //Выставляем нужное время таймера
                mManager.startTimer();
                mManager.setTime(time);
                //Показываем новый кусок паззла
                showNewPiece();
                break;
            case FINISH:
                //Выставляем нужное время таймера
                mManager.setTime(time);
                break;
        }
    }

    //Запускаем таймер, который был до этого остановлен в методе onSaveInstanceState
    @Override
    public void onResume() {
        super.onResume();

        if (mState == State.RUN) mManager.resumeTimer();
    }

    //Клик на кнопку, добавляющую новый кусок паззла
    public void onButtonAddClick(View view) {
        showNewPiece();
    }

    //Клик на кнопку, меняющую картинку карты
    public void onButtonInfoClick(View view) {
        mImageMap.changeMapInfo(this);
    }

    //Клик на кнопку зуммирования
    public void onButtonZoomClick(View view) {
        if (mLayoutZoom.isZoomed())
            mLayoutZoom.zoomOut();
        else
            mLayoutZoom.zoomIn();
    }

    //Стартовый фрагмент закончил свою работу
    @Override
    public void onFragmentStartComplete() {
        //Обновляем состояние
        mState = State.RUN;
        //Удаляем из активности стартовый фрагмент
        FragmentStart fragmentStart = (FragmentStart) getFragmentManager().findFragmentByTag("START");
        getFragmentManager().beginTransaction()
                .remove(fragmentStart)
                .commitAllowingStateLoss();
        //Показываем кусок паззла
        showNewPiece();
        //Включаем таймер
        mManager.startTimer();
    }

    //Финишный фрагмент закончил свою работу
    @Override
    public void onFragmentFinishComplete(int resultCode) {
        setResult(resultCode);
        finish();
    }
}