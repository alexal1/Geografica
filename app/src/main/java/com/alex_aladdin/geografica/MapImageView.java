package com.alex_aladdin.geografica;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

// Этот класс объявляется публичым, т.к на него ссылается файл разметки activity_main.xml
public class MapImageView extends ImageView {

    public static float K; //Отношение ширины реального изображения к ширине оригинального
    public static float RATIO; //Пропорции картинки
    private float mOriginalWidth; //Длина оригинального изображения
    private int mType = 2; //Текущий тип информационности карты (с надписями, без напдписей, и т.д.)

    public MapImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        //В метод loadMap передаем название файла с картой и тип (с надписями, без надписей и т.д.)
        loadMap(context, "sfo", mType);
    }

    //Метод, загружающий нужную карту
    private void loadMap(Context context, String name, int type) {
        //Получаем id ресурса по строке name и числу type
        int resId = getResources().getIdentifier(name + type, "drawable", context.getPackageName());

        //Получаем размеры экрана
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final float screen_w = size.x;
        final float screen_h = size.y;

        //Загружаем из ресурсов картинку, сразу уменьшенную в число раз, кратное двум, но чтобы была не меньше размеров экрана
        Bitmap bmMap = decodeSampledBitmapFromResource(getResources(), resId, Math.round(screen_w), Math.round(screen_h));
        //Получаем размеры загруженной картинки
        float bitmap_w = bmMap.getWidth();
        float bitmap_h = bmMap.getHeight();
        //Считаем пропорции экрана и картинки
        float screen_ratio = screen_w/screen_h;
        float bitmap_ratio = bitmap_w/bitmap_h;
        //Если у картинки ratio больше, надо уменьшить её до ширины экрана
        if (bitmap_ratio > screen_ratio) {
            Bitmap bmScaledMap = Bitmap.createScaledBitmap(bmMap, Math.round(screen_w),
                    Math.round(screen_w*bitmap_h/bitmap_w), true);
            bmMap.recycle();
            this.setImageBitmap(bmScaledMap);
            //Сохраняем коэффициент сжатия
            K = (float)bmScaledMap.getWidth()/mOriginalWidth;
        }
        //Если у картинки ratio меньше, надо уменьшить её до высоты экрана
        else {
            Bitmap bmScaledMap = Bitmap.createScaledBitmap(bmMap, Math.round(screen_h*bitmap_w/bitmap_h),
                    Math.round(screen_h), true);
            bmMap.recycle();
            this.setImageBitmap(bmScaledMap);
            //Сохраняем коэффициент сжатия
            K = (float)bmScaledMap.getWidth()/mOriginalWidth;
        }

        //Сохраняем пропорции картинки
        RATIO = bitmap_ratio;
    }

    //Метод, получающий из ресурсов сразу уменьшенное изображение (в кратное двум число раз), чтобы оно было не меньше
    //заданных размеров
    private Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        //Сначала декодируем с inJustDecodeBounds = true, чтобы узнать размеры оригинала
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        //Рассчитываем inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        //Теперь декодируем изображение, сразу уменьшенное в inSampleSize раз
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    //Вспомогательный метод для decodeSampledBitmapFromResource, рассчитывающий значение коэффициента inSampleSize
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        //Высота и ширина оригинального изображения
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            //Вычисляем наибольшее значение inSampleSize, являющееся степенью двойки, такое чтобы при этом одновременно
            //высота и ширина итоговой картинки были больше, чем reqHeight и reqWidth
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        //Сохраняем длину оригинального изображения
        mOriginalWidth = width;

        return inSampleSize;
    }

    //Метод, меняющий картинку карты
    public void changeMapInfo(Context context) {
        mType++;
        mType = mType % 4;
        if (mType == 0)
            this.setVisibility(INVISIBLE);
        else {
            this.setVisibility(VISIBLE);
            loadMap(context, "sfo", mType);
        }
    }
}