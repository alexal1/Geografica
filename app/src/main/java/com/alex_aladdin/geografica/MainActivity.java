package com.alex_aladdin.geografica;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import gr.antoniom.chronometer.Chronometer;

public class MainActivity extends AppCompatActivity implements FragmentStart.OnCompleteListener,
        FragmentFinishTraining.OnCompleteListener, FragmentFinishCheck.OnCompleteListener,
        FragmentFinishChampionship.OnCompleteListener, FragmentExit.OnCompleteListener, FragmentTest.EventListener {

    public static final String ERROR_TAG = "GeograficaError";
    public static final float DELTA_MM = 5.0f; //Дельта прилипания в миллиметрах

    private GameManager mManager;
    private MapImageView mImageMap;
    private ZoomableRelativeLayout mLayoutZoom;
    private State mState; //Состояние игры

    private enum State {
        START, RUN, PAUSE, FINISH
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

        //Принимаем в качестве параметров уровень сложности и название карты
        MapImageView.Level level = (MapImageView.Level) getIntent().getSerializableExtra("LEVEL");
        String map_name = getIntent().getExtras().getString("MAP_NAME");

        mManager = new GameManager(this, level, map_name);
        mImageMap = mManager.getMap();

        mLayoutZoom = (ZoomableRelativeLayout)findViewById(R.id.layout_zoom);
        mLayoutZoom.setOnDragListener(new MyDragListener()); //Теперь мы можем перетаскивать кусочки паззла

        //Если приложение запущено впервые
        if (savedInstanceState == null) {
            mState = State.START;
            // Сохраняем ошибки, переданные в качестве параметра. Используется в режиме чемпионата
            HashMap testMistakes = (HashMap) getIntent().getSerializableExtra("TEST_MISTAKES");
            mManager.setTestMistakes(testMistakes);
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
        private final FragmentTip fragmentTip;
        private final FragmentTest fragmentTest;
        private Boolean showTestsImmediately;

        //Конструктор
        MyDragListener() {
            //Переводим величину прилипания из миллиметров в пиксели
            delta = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, DELTA_MM, getResources().getDisplayMetrics());

            FragmentManager fragmentManager = getFragmentManager();
            //Фрагмент-подсказка
            fragmentTip = (FragmentTip) fragmentManager.findFragmentById(R.id.fragment_tip);
            //Фрагмент-тест
            fragmentTest = (FragmentTest) fragmentManager.findFragmentById(R.id.fragment_test);

            showTestsImmediately = getIntent().getBooleanExtra("SHOW_TESTS_IMMEDIATELY", true);
        }

        public boolean onDrag(View v, DragEvent event) {
            float x, y; //Текущие координаты DragAndDrop'a

            PieceImageView view = (PieceImageView) event.getLocalState();

            //Координаты цели
            final float target_x = view.getTargetX();
            final float target_y = view.getTargetY();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    view.setVisibility(View.INVISIBLE);
                    //Включаем подсказку
                    fragmentTip.init(view);
                    // Подготавливаем тест
                    fragmentTest.init(view, mManager.getRandomPieces(view), false);
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
                        // Есть другие доступные куски
                        if (mManager.hasVisiblePieces()) {
                            //Опускаем его вниз, чтобы он не мог загородить собой другой кусок
                            view.toBack();
                        }
                        // Нет других доступных кусков
                        else {
                            // В режиме тренировки сразу показываем тест
                            if (showTestsImmediately) {
                                fragmentTest.set();
                                mLayoutZoom.centerAt(fragmentTest);
                                if (mLayoutZoom.isZoomed())
                                    mLayoutZoom.zoomOut();
                            }
                            // В режиме чемпионата выполняем следующее игровое действие
                            else
                                nextAction();
                        }
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

    /**
     *  Добавляем на экран новый кусок паззла.
     *  Есть два метода: метод по умолчанию и метод, принимающий на вход кусок, который надо добавить.
     */
    private void showNewPiece() {
        showNewPiece(mManager.getNewRandomPiece());
    }

    private void showNewPiece(PieceImageView imagePiece) {
        if (imagePiece == null)
            return;

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
        //Высота верхней и нижней областей интерфейса
        ImageButton buttonAdd = (ImageButton) findViewById(R.id.button_add_piece);
        ImageView imageNav = (ImageView) findViewById(R.id.image_nav);
        int down_h = buttonAdd.getHeight();
        int up_h = imageNav.getHeight();
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

    //Сохраняем промежуточное состоние активности
    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        //Сохраняем состояние игры
        saveInstanceState.putSerializable("STATE", mState);

        //Получаем массив индексов кусочков паззла, которые уже стоят на своих местах
        ArrayList<Integer> arraySettled = mManager.getIndicesOfSettledPieces();
        saveInstanceState.putIntegerArrayList("SETTLED_PIECES", arraySettled);

        //Получаем массив индексов кусочков паззла, для которых уже пройден тест
        ArrayList<Integer> arrayChecked = mManager.getIndicesOfCheckedPieces();
        saveInstanceState.putIntegerArrayList("CHECKED_PIECES", arrayChecked);

        // Сохраняем количество ошибок в тестах для каждой карты
        HashMap<String, Integer> testMistakes = mManager.getTestMistakes();
        saveInstanceState.putSerializable("TEST_MISTAKES", testMistakes);

        //Сохраняем текущее время таймера
        mManager.stopTimer();
        saveInstanceState.putLong("TIMER", mManager.getTime());
    }

    //Восстанавливаем сохраненные значения из метода onSaveInstanceState
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ArrayList<Integer> arraySettled = savedInstanceState.getIntegerArrayList("SETTLED_PIECES");
        ArrayList<Integer> arrayChecked = savedInstanceState.getIntegerArrayList("CHECKED_PIECES");
        if (arraySettled == null || arrayChecked == null) return;

        // Берем кусочки паззла с номерами из arraySettled и втыкаем их на нужные места
        for (int i : arraySettled) {
            //Берем
            PieceImageView view = mManager.getPiece(i);
            //И втыкаем
            view.settle();
            // Если надо, помечаем что для этого кусочка уже был пройден тест
            if (arrayChecked.contains(i))
                view.setChecked();
            //Делаем его видимым
            view.setVisibility(View.VISIBLE);
        }

        // Восстанавливаем количество ошибок в тестах для каждой карты
        HashMap testMistakes = (HashMap) savedInstanceState.getSerializable("TEST_MISTAKES");
        mManager.setTestMistakes(testMistakes);

        //Далее выполняем действия в зависимости от состояния игры
        mState = (State) savedInstanceState.getSerializable("STATE");
        long time = savedInstanceState.getLong("TIMER");
        switch (mState){
            case START:
                //Ничего не делаем
                break;
            case RUN:
                //Выставляем нужное время таймера
                mManager.startTimer();
                mManager.setTime(time);
                // Выполняем следующее действие
                nextAction();
                break;
            case PAUSE:
                mManager.setTime(time);
                // Выполняем следующее действие
                nextAction();
                break;
            case FINISH:
                //Выставляем нужное время таймера
                mManager.setTime(time);
                break;
        }
    }

    // Следующее игровое действие
    private void nextAction() {
        final PieceImageView newPiece;
        if ((newPiece = mManager.getNewRandomPiece()) != null) {
            showNewPiece(newPiece);
        }
        else {
            final PieceImageView uncheckedPiece = mManager.getUncheckedRandomPiece();
            if (uncheckedPiece == null) {
                Log.e(ERROR_TAG, "nextAction() invoked when neither new nor unchecked pieces are left");
                return;
            }

            FragmentManager fragmentManager = getFragmentManager();
            final FragmentTest fragmentTest = (FragmentTest) fragmentManager.findFragmentById(R.id.fragment_test);
            // Инициализируем тестовый фрагмент с параметром autoSet = true
            fragmentTest.init(uncheckedPiece, mManager.getRandomPieces(uncheckedPiece), true);
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
        //Передаем обратно время сборки и количество ошибок, но используется это только в режиме чемпионата
        Intent answerIntent = new Intent();
        answerIntent.putExtra("TIME", mManager.getTime());
        answerIntent.putExtra("TEST_MISTAKES", mManager.getTestMistakes());

        setResult(resultCode, answerIntent);
        finish();
    }

    //Нажатие на аппаратную кнопку НАЗАД
    @Override
    public void onBackPressed() {
        //Если игра в процессе
        if (mState == State.RUN) {
            //Останавливаем
            mState = State.PAUSE;
            mManager.stopTimer();
            mManager.setTime(mManager.getTime()); //синхронизируем (странный баг)
            //Вызываем диалоговый фрагмент
            FragmentExit fragmentExit = new FragmentExit();
            getFragmentManager().beginTransaction()
                    .add(R.id.layout_root, fragmentExit, "EXIT")
                    .addToBackStack("EXIT")
                    .commit();
        }
        //Если игра остановлена (уже вызван диалоговый фрагмент)
        else if (mState == State.PAUSE) {
            mState = State.RUN;
            mManager.resumeTimer();
            super.onBackPressed();
        }
        else
            super.onBackPressed();
    }

    //Получаем ответ от фрагмента
    @Override
    public void onExitAttempt(Boolean exit) {
        if (exit) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        else {
            //Удаляем из активности диалоговый фрагмент
            FragmentExit fragmentExit = (FragmentExit) getFragmentManager().findFragmentByTag("EXIT");
            getFragmentManager().beginTransaction()
                    .remove(fragmentExit)
                    .commitAllowingStateLoss();
            //Запускаем
            mState = State.RUN;
            mManager.resumeTimer();
        }
    }

    // Пользователь выбрал неправильный ответ в тесте
    @Override
    public void onWrongAnswer() {
        String caption = getIntent().getStringExtra("MAP_CAPTION");
        mManager.addTestMistake(caption);
    }

    // Тестовый фрагмент был закрыт
    @Override
    public void onTestClose(Boolean completed) {
        FragmentManager fragmentManager = getFragmentManager();
        final FragmentTest fragmentTest = (FragmentTest) fragmentManager.findFragmentById(R.id.fragment_test);

        // Тест закрыт с положительным результатом
        if (completed) {
            final PieceImageView uncheckedPiece;
            // Есть непройденные тесты
            if ((uncheckedPiece = mManager.getUncheckedRandomPiece()) != null) {
                // Инициализируем тестовый фрагмент с параметром autoSet = true
                fragmentTest.init(uncheckedPiece, mManager.getRandomPieces(uncheckedPiece), true);
            }
            // Непройденных тестов не осталось
            else {
                final PieceImageView newPiece;
                // Есть новые куски
                if ((newPiece = mManager.getNewRandomPiece()) != null) {
                    showNewPiece(newPiece);
                }
                // Новых кусков тоже нет
                else {
                    finishGame();
                }
                mLayoutZoom.centerDefault();
            }
        }
        // Тест закрыт с отрицательным результатом
        else {
            final PieceImageView newPiece;
            // Есть новые куски
            if ((newPiece = mManager.getNewRandomPiece()) != null) {
                showNewPiece(newPiece);
                mLayoutZoom.centerDefault();
            }
            // Больше кусков не осталось => все уже установлены (т.к. мы только что закрыли тест)
            else {
                // Кусок, к которому привязан тест на данный момент
                final PieceImageView currentPiece = fragmentTest.getCurrentPiece();
                // Кусок с непройденным тестом
                final PieceImageView uncheckedPiece = mManager.getUncheckedRandomPiece(currentPiece);
                if (uncheckedPiece == null) {
                    Log.e(ERROR_TAG, "Cannot find any unchecked pieces");
                    return;
                }

                // Инициализируем тестовый фрагмент с параметром autoSet = true
                fragmentTest.init(uncheckedPiece, mManager.getRandomPieces(uncheckedPiece), true);
            }
        }
    }

    // Тестовый фрагмент был инициализирован с параметром autoSet = true и сигнализирует о том, что выполнил set()
    @Override
    public void onAutoSet() {
        FragmentManager fragmentManager = getFragmentManager();
        final FragmentTest fragmentTest = (FragmentTest) fragmentManager.findFragmentById(R.id.fragment_test);
        mLayoutZoom.centerAt(fragmentTest);
        if (mLayoutZoom.isZoomed())
            mLayoutZoom.zoomOut();

        // Поднимаем текущий кусок над остальными, чтобы целиком отображались уголки
        PieceImageView currentPiece = fragmentTest.getCurrentPiece();
        currentPiece.toFront();
    }

    // Завершаем игру
    private void finishGame() {
        mState = State.FINISH;
        mManager.stopTimer();

        //Показываем один из финишных экранов
        if (getIntent().getBooleanExtra("FRAGMENT_FINISH_TRAINING", false)) {
            String caption = getIntent().getStringExtra("MAP_CAPTION").toUpperCase() + " " +
                    getString(R.string.finish_federal_district).toUpperCase();
            long time = mManager.getTime();
            HashMap testMistakes = mManager.getTestMistakes();
            FragmentFinishTraining fragmentFinishTraining = FragmentFinishTraining.newInstance(caption, time, testMistakes);
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
        if (getIntent().getBooleanExtra("FRAGMENT_FINISH_CHAMPIONSHIP", false)) {
            String level = getString(R.string.finish_level) + " " +
                    ((MapImageView.Level) getIntent().getSerializableExtra("LEVEL")).getCaption();
            //Время складывается из времени за эту карту и за предыдущие
            long time = mManager.getTime() + getIntent().getLongExtra("TIME", 0);
            HashMap testMistakes = mManager.getTestMistakes();
            FragmentFinishChampionship fragmentFinishChampionship =
                    FragmentFinishChampionship.newInstance(time, level, testMistakes);
            getFragmentManager().beginTransaction()
                    .add(R.id.layout_root, fragmentFinishChampionship)
                    .commit();
        }
    }

}