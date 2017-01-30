package com.alex_aladdin.geografica;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    enum Menu {

        SKFO("Северо-Кавказский"),
        YUFO("Южный"),
        CFO("Центральный"),
        SZFO("Северо-Западный"),
        PRFO("Приволжский"),
        UFO("Уральский"),
        SFO("Сибирский"),
        DVO("Дальневосточный");

        private String caption;

        Menu(String caption) {
            this.caption = caption;
        }

        String getCaption() {
            return this.caption;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final LinearLayout layoutInner = (LinearLayout) findViewById(R.id.layout_inner);
        final ImageView imageLogo = (ImageView) findViewById(R.id.image_logo);
        final TextView textLogo = (TextView) findViewById(R.id.text_logo);

        //Отслеживаем момент, когда layoutInner полностью загружается
        ViewTreeObserver viewTreeObserver = layoutInner.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Снимаем слушатель
                if (Build.VERSION.SDK_INT < 16)
                    removeOnGlobalLayoutListenerPre16(layoutInner, this);
                else
                    removeOnGlobalLayoutListenerPost16(layoutInner, this);

                //Берем высоту layoutInner
                int inner_h = layoutInner.getHeight();

                //Берем высоту экрана
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int outer_h = size.y;

                Log.i("LoadingMenu", "inner_h = " + inner_h + ", outer_h = " + outer_h);

                //Если layoutInner такой большой, что не умещается в экран, убираем лого
                if (inner_h == outer_h) {
                    imageLogo.setVisibility(View.GONE);
                    textLogo.setVisibility(View.GONE);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void removeOnGlobalLayoutListenerPre16(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        view.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
    }

    @TargetApi(16)
    private void removeOnGlobalLayoutListenerPost16(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        view.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
    }

    //Кнопка ЧЕМПИОНАТ
    public void onButtonChampionshipClick(View view) {
        Intent intent = new Intent(MenuActivity.this, MenuChampionshipActivity.class);
        startActivity(intent);
    }

    //Кнопка ТРЕНИРОВКА
    public void onButtonTrainingClick(View view) {
        Intent intent = new Intent(MenuActivity.this, MenuTrainingActivity.class);
        startActivity(intent);
    }

    //Кнопка ОСТАВИТЬ ОТЗЫВ
    public void onButtonRateClick(View view) {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException exception) {
            //На случай если не установлен Play Store
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="
                    + appPackageName)));
        }
    }
}