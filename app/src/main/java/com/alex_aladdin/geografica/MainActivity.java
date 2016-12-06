package com.alex_aladdin.geografica;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final float STICK = 1/8f; //Помноженное на высоту экрана, дает дельту прилипания

    private MapImageView mImageMap;
    private PieceImageView mImagePiece;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GameManager manager = new GameManager(this);

        mImageMap = manager.getCurrentMap();
        mImagePiece = manager.getCurrentPiece();
        mImagePiece.setOnTouchListener(new MyTouchListener());
        mImagePiece.getRootView().setOnDragListener(new MyDragListener());
    }

    //Класс MyTouchListener
    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                //Выключаем подсветку
                mImagePiece.setBackgroundResource(R.color.transparent);
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

    //Класс MyDragListener
    private class MyDragListener implements View.OnDragListener {
        public boolean onDrag(View v, DragEvent event) {
            float x, y; //Текущие координаты DragAndDrop'a

            PieceImageView view = (PieceImageView) event.getLocalState();
            TextView textAccuracy = (TextView)findViewById(R.id.text_accuracy);

            //Координаты цели
            final float target_x = mImagePiece.getTargetX()*MapImageView.K + mImageMap.getX();
            final float target_y = mImagePiece.getTargetY()*MapImageView.K + mImageMap.getY();

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    view.setVisibility(View.INVISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    x = event.getX();
                    y = event.getY();
                    float picture_x = (x - mImageMap.getX())/MapImageView.K;
                    float picture_y = (y - mImageMap.getY())/MapImageView.K;
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
                    }
                    else {
                        //Включаем подсветку
                        mImagePiece.setBackgroundResource(R.drawable.backlight);
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
}