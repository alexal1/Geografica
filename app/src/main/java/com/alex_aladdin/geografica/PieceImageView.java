package com.alex_aladdin.geografica;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.util.HashMap;

// Этот класс объявляется публичым, т.к на него ссылается файл разметки activity_main.xml
public class PieceImageView extends ImageView {

    private Boolean mSettled = false; //Кусочек установлен на предназначенное для него место
    private float mTargetX, mTargetY;
    private int mReqWidth = 0, mReqHeight = 0; //Ширина и высота, которые должны получиться исходя из масштабирования к карте
    private Context mContext;

    public PieceImageView(Context context) {
        super(context);
        mContext = context;
    }

    //Метод, загружающий нужную часть паззла
    public void loadPiece(HashMap<String, String> map) {
        //Берем значения из map
        String name = map.get("name");
        mTargetX = Float.parseFloat(map.get("x"));
        mTargetY = Float.parseFloat(map.get("y"));
        Log.i("PieceImageView", "name = " + name + ", x = " + mTargetX + ", y = " + mTargetY);
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
        mReqWidth = (int)(width*MapImageView.K);
        mReqHeight = (int)(height*MapImageView.K);

        int inSampleSize = 1;

        if (height > mReqHeight || width > mReqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            //Вычисляем наибольшее значение inSampleSize, являющееся степенью двойки, такое чтобы при этом одновременно
            //высота и ширина итоговой картинки были больше, чем reqHeight и reqWidth
            while ((halfHeight / inSampleSize) > mReqHeight && (halfWidth / inSampleSize) > mReqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    //Геттеры
    public float getTargetX() { return mTargetX; }
    public float getTargetY() { return mTargetY; }
    public boolean isSettled() { return mSettled; }

    //Сеттеры
    public void settle() { mSettled = true; }
}