package com.alex_aladdin.geografica;

import android.annotation.TargetApi;
import android.app.Activity;
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

import java.util.Arrays;
import java.util.Collections;

public class MenuActivity extends AppCompatActivity implements FragmentLevels.OnChooseListener {

    private SubMenuActivity.Menu[] mMenu; //Перечисление всех пунктов меню из SubMenuActivity
    private int mCurrentItem; //Номер пункта меню, карта которого запущена в данный момент
    private MapImageView.Level mLevel; //Выбранный уровень сложности

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

        //Если приложение запущено впервые
        if (savedInstanceState == null)
            mMenu = SubMenuActivity.Menu.values();
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
        //Запускаем фрагмент выбора уровня сложности
        FragmentLevels fragmentLevels = new FragmentLevels();
        getFragmentManager().beginTransaction()
                .add(R.id.layout_menu, fragmentLevels, "LEVELS")
                .addToBackStack("")
                .commit();
    }

    //Пользователь выбрал уровень сложности
    @Override
    public void onLevelChoose(MapImageView.Level level) {
        //Удаляем из активности фрагмент выбора уровня сложности
        FragmentLevels fragmentLevels = (FragmentLevels) getFragmentManager().findFragmentByTag("LEVELS");
        getFragmentManager().beginTransaction()
                .remove(fragmentLevels)
                .commitAllowingStateLoss();

        //Сохраняем выбранный уровень сложности
        mLevel = level;
        //Перемешиваем последовательность пунктов меню
        Collections.shuffle(Arrays.asList(mMenu));
        //Задаем текущий пункт
        mCurrentItem = -1;

        //Запускаем карту всей России
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.putExtra("LEVEL", level);
        intent.putExtra("MAP_NAME", "russia");
        intent.putExtra("MAP_CAPTION", "");
        intent.putExtra("SHOW_TIMER", false);
        intent.putExtra("SHOW_BUTTON_INFO", true);
        intent.putExtra("FRAGMENT_START", true);
        intent.putExtra("FRAGMENT_FINISH_TRAINING", false);
        intent.putExtra("FRAGMENT_FINISH_CHECK", true);

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK)
            mCurrentItem++;
        else
            return;

        Intent intent = new Intent(MenuActivity.this, MainActivity.class);

        //Все пункты меню пройдены
        if (mCurrentItem == mMenu.length) {
            return;
        }
        //Остался один
        else if (mCurrentItem == mMenu.length - 1) {
            intent.putExtra("LEVEL", mLevel);
            intent.putExtra("MAP_NAME", mMenu[mCurrentItem].toString().toLowerCase());
            intent.putExtra("MAP_CAPTION", mMenu[mCurrentItem].getCaption());
            intent.putExtra("SHOW_TIMER", false);
            intent.putExtra("SHOW_BUTTON_INFO", true);
            intent.putExtra("FRAGMENT_START", true);
            intent.putExtra("FRAGMENT_FINISH_TRAINING", false);
            intent.putExtra("FRAGMENT_FINISH_CHECK", true);
        }
        //Осталось больше одного
        else {
            intent.putExtra("LEVEL", mLevel);
            intent.putExtra("MAP_NAME", mMenu[mCurrentItem].toString().toLowerCase());
            intent.putExtra("MAP_CAPTION", mMenu[mCurrentItem].getCaption());
            intent.putExtra("SHOW_TIMER", false);
            intent.putExtra("SHOW_BUTTON_INFO", true);
            intent.putExtra("FRAGMENT_START", true);
            intent.putExtra("FRAGMENT_FINISH_TRAINING", false);
            intent.putExtra("FRAGMENT_FINISH_CHECK", true);
        }

        startActivityForResult(intent, 0);
    }

    //Сохраняем данные
    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        saveInstanceState.putSerializable("MENU", mMenu);
        saveInstanceState.putInt("CURRENT_ITEM", mCurrentItem);
        saveInstanceState.putSerializable("LEVEL", mLevel);
    }

    //Восстанавливаем данные
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mMenu = (SubMenuActivity.Menu[]) savedInstanceState.getSerializable("MENU");
        mCurrentItem = savedInstanceState.getInt("CURRENT_ITEM");
        mLevel = (MapImageView.Level) savedInstanceState.getSerializable("LEVEL");
    }

    //Переходим в подменю
    public void onButtonTrainingClick(View view) {
        Intent intent = new Intent(MenuActivity.this, SubMenuActivity.class);
        startActivity(intent);
    }

    //Открываем Google Play
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