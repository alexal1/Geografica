package com.alex_aladdin.geografica;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import java.util.HashMap;

// Этот класс объявляется публичым, т.к на него ссылается файл разметки activity_main.xml
public class PieceImageView extends ImageView {

    private Boolean mSettled = false; //Кусочек установлен на предназначенное для него место
    private float mTargetX, mTargetY;
    private String mCaption;
    private int mReqWidth = 0, mReqHeight = 0; //Ширина и высота, которые должны получиться исходя из масштабирования к карте
    private Context mContext;

    public PieceImageView(Context context) {
        super(context);
        mContext = context;
    }

    //При касании
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //Если этот кусок уже установлен, ничего не делаем
            if (mSettled) return false;

            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new MyDragShadowBuilder(this);
            //Выключаем подсветку
            setBackgroundResource(R.color.transparent);
            //Запускаем DragAndDrop
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newDragAndDrop(this, data, shadowBuilder, this, 0);
            }
            else {
                oldDragAndDrop(this, data, shadowBuilder, this, 0);
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

    //Делаем кастомный DragShadowBuilder
    //Он увеличивает "тень" во время перетаскивания в зуме
    private class MyDragShadowBuilder extends View.DragShadowBuilder {
        private final float mZoom = ZoomableRelativeLayout.MAX_ZOOM;
        private ZoomableRelativeLayout mLayoutZoom;
        private Point mScaleFactor;

        MyDragShadowBuilder(View view) {
            super(view);

            mLayoutZoom = (ZoomableRelativeLayout) ((Activity)mContext).findViewById(R.id.layout_zoom);
        }

        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {
            //Если нет увеличения, запускаем метод по умолчанию
            if (!mLayoutZoom.isZoomed()) {
                super.onProvideShadowMetrics(size, touch);
                return;
            }

            int width, height;

            width = Math.round(getView().getWidth() * mZoom);
            height = Math.round(getView().getHeight() * mZoom);

            size.set(width, height);
            mScaleFactor = size;

            touch.set(width / 2, height / 2);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            //Если нет увеличения, запускаем метод по умолчанию
            if (!mLayoutZoom.isZoomed()) {
                super.onDrawShadow(canvas);
                return;
            }

            canvas.scale(mScaleFactor.x / (float) getView().getWidth(), mScaleFactor.y / (float) getView().getHeight());
            getView().draw(canvas);
        }
    }

    //Метод, загружающий нужную часть паззла
    public void loadPiece(HashMap<String, String> map) {
        //Берем значения из map
        String name = map.get("name");
        float picture_x = Float.parseFloat(map.get("x"));
        float picture_y = Float.parseFloat(map.get("y"));
        mCaption = map.get("caption");

        //Вычисляем целевые координаты относительно экрана
        calculateTargetXY(picture_x, picture_y);

        //Получаем id ресурса по строке name
        int resId = getResources().getIdentifier(name, "drawable", mContext.getPackageName());

        //Загружаем из ресурсов картинку, сразу уменьшенную в число раз, кратное двум
        //но чтобы была не меньше размеров mReqWidth и mReqHeight, которые вычисляются пропорционально уменьшению карты
        Bitmap bmPiece = decodeSampledBitmapFromResource(getResources(), resId);
        //Теперь преобразуем её точно к размерам mReqWidth и mReqHeight
        Bitmap bmScaledPiece = Bitmap.createScaledBitmap(bmPiece, mReqWidth, mReqHeight, true);
        bmPiece.recycle();
        this.setImageBitmap(bmScaledPiece);
    }

    //Метод, получающий на вход координаты относительно картинки, и вычисляющий координаты относительно экрана
    private void calculateTargetXY(float picture_x, float picture_y) {
        //Необходимо вычислить координаты верхнего левого угла карты относительно экрана
        //Мы не можем просто взять эти координаты из объекта MapImageView, потому что у него они пока ещё нулевые
        //Получаем размеры экрана
        WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
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

        //Вычисляем координаты относительно экрана
        mTargetX = picture_x*MapImageView.K + map_x;
        mTargetY = picture_y*MapImageView.K + map_y;
    }

    //Метод, получающий из ресурсов сразу уменьшенное изображение (в кратное двум число раз), чтобы оно было не меньше
    //заданных размеров
    private Bitmap decodeSampledBitmapFromResource(Resources res, int resId) {
        //Сначала декодируем с inJustDecodeBounds = true, чтобы узнать размеры оригинала
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        //Рассчитываем inSampleSize
        options.inSampleSize = calculateInSampleSize(options);

        //Теперь декодируем изображение, сразу уменьшенное в inSampleSize раз
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    //Вспомогательный метод для decodeSampledBitmapFromResource, рассчитывающий значение коэффициента inSampleSize
    private int calculateInSampleSize(BitmapFactory.Options options) {
        //Высота и ширина оригинального изображения
        final int height = options.outHeight;
        final int width = options.outWidth;
        //Вычисляем mReqWidth и mReqHeight
        mReqWidth = Math.round(width*MapImageView.K);
        mReqHeight = Math.round(height*MapImageView.K);

        int inSampleSize = 1;

        if (height > mReqHeight || width > mReqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            //Вычисляем наибольшее значение inSampleSize, являющееся степенью двойки, такое чтобы при этом одновременно
            //высота и ширина итоговой картинки были больше, чем mReqHeight и mReqWidth
            while ((halfHeight / inSampleSize) >= mReqHeight && (halfWidth / inSampleSize) >= mReqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    //Метод, поднимающий этот View над всеми остальными
    public void toFront() {
        View parent = (View)this.getParent();
        this.bringToFront();
        parent.requestLayout();
        parent.invalidate();
    }

    //Метод, опускающий этот View под все остальные
    public void toBack() {
        ViewGroup parent = (ViewGroup) this.getParent();
        parent.removeView(this);
        parent.addView(this, 1);
    }

    //Устанавливаем кусок паззла на предназначенное ему место
    public void settle() {
        //Получаем размеры экрана
        WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final float screen_w = size.x;
        final float screen_h = size.y;

        //Устанавливаем
        if (getWidth() == 0 && getHeight() == 0) {
            setX(mTargetX - screen_w/2);
            setY(mTargetY - screen_h/2);
        }
        else {
            setX(mTargetX - (float)getWidth()/2);
            setY(mTargetY - (float)getHeight()/2);
        }

        mSettled = true;
    }

    //То же самое, но с анимацией
    public void settle(float x, float y) {
        //Получаем размеры экрана
        WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final float screen_w = size.x;
        final float screen_h = size.y;

        AnimatorSet animSetXY = new AnimatorSet();
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(this, View.TRANSLATION_X, (x - screen_w/2), (mTargetX - screen_w/2));
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, (y - screen_h/2), (mTargetY - screen_h/2));

        animSetXY.playTogether(animatorX, animatorY);
        animSetXY.setInterpolator(new DecelerateInterpolator());
        animSetXY.setDuration(200);
        animSetXY.start();

        mSettled = true;
    }

    //Геттеры
    public float getTargetX() { return mTargetX; }
    public float getTargetY() { return mTargetY; }
    public boolean isSettled() { return mSettled; }
    public String getCaption() { return mCaption; }
}