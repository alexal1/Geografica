package com.alex_aladdin.geografica;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Display;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

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

        //Принимаем в качестве параметра название карты, переданное нам из меню
        String map_name = getIntent().getExtras().getString("MAP_NAME");

        mManager = new GameManager(this, map_name);
        mImageMap = mManager.getMap();

        mLayoutZoom = (ZoomableRelativeLayout)findViewById(R.id.layout_zoom);
        mLayoutZoom.setOnDragListener(new MyDragListener()); //Теперь мы можем перетаскивать кусочки паззла

        //Если приложение запущено впервые
        if (savedInstanceState == null) {
            mState = State.START;
            showStartLayout();
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
                mState = State.FINISH;
                mManager.stopTimer();
                showFinishLayout();
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
                //Показываем стартовый экран
                showStartLayout();
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
                //Показываем финишный экран
                showFinishLayout();
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
        mManager.startTimer();
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

    //Показываем финишный экран
    private void showFinishLayout() {
        final LinearLayout layoutFinish = (LinearLayout)findViewById(R.id.layout_finish);
        layoutFinish.setVisibility(View.VISIBLE);
        //Делаем кнопки недоступными
        final ImageButton buttonAdd = (ImageButton)findViewById(R.id.button_add_piece);
        final ImageButton buttonInfo = (ImageButton)findViewById(R.id.button_info);
        final ImageButton buttonZoom = (ImageButton)findViewById(R.id.button_zoom);
        buttonAdd.setEnabled(false);
        buttonInfo.setEnabled(false);
        buttonZoom.setEnabled(false);
        //Если были в зуме, возвращаемся
        if (mLayoutZoom.isZoomed()) mLayoutZoom.zoomOut();

        //Показываем заголовок
        final TextView textCaption = (TextView)findViewById(R.id.text_finish_caption);
        String caption = getIntent().getStringExtra("MAP_CAPTION").toUpperCase();
        caption += " " + getString(R.string.finish_federal_district);
        textCaption.setText(caption);

        //Показываем результат
        final TextView textResult = (TextView)findViewById(R.id.text_finish_result);
        long time = mManager.getTime();
        DecimalFormat df = new DecimalFormat("00");
        String text = getString(R.string.finish_result) + "\n";

        int hours = (int)(time / (3600 * 1000));
        int remaining = (int)(time % (3600 * 1000));

        int minutes = remaining / (60 * 1000);
        remaining = remaining % (60 * 1000);

        int seconds = remaining / 1000;
        int milliseconds = ((int)time % 1000) / 10;

        if (hours > 0) {
            text += df.format(hours) + ":";
        }

        text += df.format(minutes) + ":";
        text += df.format(seconds) + ":";
        text += df.format(milliseconds);

        textResult.setText(text);
    }

    //Клик на кнопку МЕНЮ финишного экрана
    public void onButtonMenuClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    //Клик на кнопку РЕСТАРТ финишного экрана
    public void onButtonRestartClick(View view) {
        mState = State.START;

        String map_name = getIntent().getExtras().getString("MAP_NAME");
        mManager = new GameManager(this, map_name);

        this.recreate();
    }

    //Клик на кнопку ДАЛЕЕ финишного экрана
    public void onButtonNextClick(View view) {
        setResult(RESULT_OK);
        finish();
    }
}