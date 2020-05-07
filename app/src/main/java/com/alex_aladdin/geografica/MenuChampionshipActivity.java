package com.alex_aladdin.geografica;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class MenuChampionshipActivity extends AppCompatActivity {

    private MenuActivity.Menu[] mMenu; //Массив всех пунктов меню (карт)
    private int mCurrentItem; //Номер пункта меню, карта которого запущена в данный момент
    private MapImageView.Level mLevel; //Текущий выбранный уровень сложности
    private long mTime; //Суммарное время по всем пройденным картам
    private HashMap mTestMistakes; // Суммарное количество ошибок по всем пройденным картам

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_championship);

        //Если приложение запущено впервые
        if (savedInstanceState == null) {
            mMenu = MenuActivity.Menu.values();
            setButtonEnabled();
        }
    }

    //Анимируем данную картинку в галочку, а предыдущую картинку из галочки в исходную
    private void animate(final ImageView image) {
        //Создаем набор анимаций
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.setDuration(200);

        /* --- Анимация для текущей картинки --- */
        ObjectAnimator rotationAnimator1 = ObjectAnimator.ofFloat(image, View.ROTATION_Y, 0, 90);
        rotationAnimator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                //После первой половины анимации меняем картинку на другую
                image.setImageResource(R.drawable.tick);
            }
        });
        ObjectAnimator rotationAnimator2 = ObjectAnimator.ofFloat(image, View.ROTATION_Y, -90, 0);

        animatorSet.play(rotationAnimator2).after(rotationAnimator1);

        /* --- Анимация для предыдущей картинки --- */
        final int resId;
        final ImageView image_prev;

        //mLevel ещё не инициализирован
        if (mLevel == null) {
            animatorSet.start();
            return;
        }

        switch (mLevel) {
            case EASY:
                resId = getResources().getIdentifier("level_easy", "drawable", getPackageName());
                image_prev = (ImageView) findViewById(R.id.image_easy);
                break;
            case NORMAL:
                resId = getResources().getIdentifier("level_normal", "drawable", getPackageName());
                image_prev = (ImageView) findViewById(R.id.image_normal);
                break;
            case HARD:
                resId = getResources().getIdentifier("level_hard", "drawable", getPackageName());
                image_prev = (ImageView) findViewById(R.id.image_hard);
                break;
            default:
                return;
        }

        ObjectAnimator rotationAnimator3 = ObjectAnimator.ofFloat(image_prev, View.ROTATION_Y, 0, 90);
        rotationAnimator3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                //После первой половины анимации меняем картинку на другую
                image_prev.setImageResource(resId);
            }
        });
        ObjectAnimator rotationAnimator4 = ObjectAnimator.ofFloat(image_prev, View.ROTATION_Y, -90, 0);

        animatorSet.play(rotationAnimator4).after(rotationAnimator3);

        animatorSet.start();
    }

    //Делаем кнопку НАЧАТЬ доступной/недоступной
    private void setButtonEnabled() {
        Button buttonGo = (Button) findViewById(R.id.button_go);

        if (mLevel == null) {
            buttonGo.setClickable(false);
            buttonGo.setPressed(true);
        }
        else {
            buttonGo.setClickable(true);
            buttonGo.setPressed(false);
        }
    }

    public void onLayoutEasyClick(View view) {
        ImageView imageEasy = (ImageView) findViewById(R.id.image_easy);
        animate(imageEasy);
        mLevel = (mLevel == MapImageView.Level.EASY) ? null : MapImageView.Level.EASY;
        setButtonEnabled();
    }

    public void onLayoutNormalClick(View view) {
        ImageView imageNormal = (ImageView) findViewById(R.id.image_normal);
        animate(imageNormal);
        mLevel = (mLevel == MapImageView.Level.NORMAL) ? null : MapImageView.Level.NORMAL;
        setButtonEnabled();
    }

    public void onLayoutHardClick(View view) {
        ImageView imageHard = (ImageView) findViewById(R.id.image_hard);
        animate(imageHard);
        mLevel = (mLevel == MapImageView.Level.HARD) ? null : MapImageView.Level.HARD;
        setButtonEnabled();
    }

    //Кнопка НАЧАТЬ
    public void onButtonGoClick(View view) {
        //Перемешиваем последовательность пунктов меню
        Collections.shuffle(Arrays.asList(mMenu));
        //Задаем текущий пункт
        mCurrentItem = -1;
        //Сбрасываем время
        mTime = 0;
        // Создаем новый объект для хранения ошибок
        mTestMistakes = new HashMap<>();

        //Запускаем карту всей России
        Intent intent = new Intent(MenuChampionshipActivity.this, MainActivity.class);
        intent.putExtra("LEVEL", mLevel);
        intent.putExtra("MAP_NAME", "russia");
        intent.putExtra("MAP_CAPTION", getString(R.string.finish_districts_displacement));
        intent.putExtra("TIME", mTime);
        intent.putExtra("SHOW_TIMER", true);
        intent.putExtra("SHOW_BUTTON_INFO", false);
        intent.putExtra("FRAGMENT_START", true);
        intent.putExtra("FRAGMENT_FINISH_TRAINING", false);
        intent.putExtra("FRAGMENT_FINISH_CHECK", true);
        intent.putExtra("FRAGMENT_FINISH_CHAMPIONSHIP", false);
        intent.putExtra("SHOW_TESTS_IMMEDIATELY", false);
        intent.putExtra("TEST_MISTAKES", mTestMistakes);

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            mCurrentItem++;

            //Обновляем суммарное время
            mTime += data.getLongExtra("TIME", 0);
            // Обновляем количество ошибок
            mTestMistakes = (HashMap) data.getSerializableExtra("TEST_MISTAKES");
        }
        else
            return;

        Intent intent = new Intent(MenuChampionshipActivity.this, MainActivity.class);

        //Все пункты меню пройдены
        if (mCurrentItem == mMenu.length) {
            return;
        }
        //Остался один
        else if (mCurrentItem == mMenu.length - 1) {
            intent.putExtra("LEVEL", mLevel);
            intent.putExtra("MAP_NAME", mMenu[mCurrentItem].toString().toLowerCase());
            intent.putExtra("MAP_CAPTION", mMenu[mCurrentItem].getCaption());
            intent.putExtra("TIME", mTime);
            intent.putExtra("SHOW_TIMER", true);
            intent.putExtra("SHOW_BUTTON_INFO", false);
            intent.putExtra("FRAGMENT_START", false);
            intent.putExtra("FRAGMENT_FINISH_TRAINING", false);
            intent.putExtra("FRAGMENT_FINISH_CHECK", false);
            intent.putExtra("FRAGMENT_FINISH_CHAMPIONSHIP", true);
            intent.putExtra("SHOW_TESTS_IMMEDIATELY", false);
            intent.putExtra("TEST_MISTAKES", mTestMistakes);
        }
        //Осталось больше одного
        else {
            intent.putExtra("LEVEL", mLevel);
            intent.putExtra("MAP_NAME", mMenu[mCurrentItem].toString().toLowerCase());
            intent.putExtra("MAP_CAPTION", mMenu[mCurrentItem].getCaption());
            intent.putExtra("TIME", mTime);
            intent.putExtra("SHOW_TIMER", true);
            intent.putExtra("SHOW_BUTTON_INFO", false);
            intent.putExtra("FRAGMENT_START", false);
            intent.putExtra("FRAGMENT_FINISH_TRAINING", false);
            intent.putExtra("FRAGMENT_FINISH_CHECK", true);
            intent.putExtra("FRAGMENT_FINISH_CHAMPIONSHIP", false);
            intent.putExtra("SHOW_TESTS_IMMEDIATELY", false);
            intent.putExtra("TEST_MISTAKES", mTestMistakes);
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
        saveInstanceState.putLong("TIME", mTime);
        saveInstanceState.putSerializable("TEST_MISTAKES", mTestMistakes);
    }

    //Восстанавливаем данные
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mMenu = (MenuActivity.Menu[]) savedInstanceState.getSerializable("MENU");
        mCurrentItem = savedInstanceState.getInt("CURRENT_ITEM");
        mLevel = (MapImageView.Level) savedInstanceState.getSerializable("LEVEL");
        mTime = savedInstanceState.getLong("TIME");
        mTestMistakes = (HashMap) savedInstanceState.getSerializable("TEST_MISTAKES");

        //Выставляем доступность кнопки
        setButtonEnabled();

        //Выставляем галочку
        final ImageView image;

        if (mLevel == null) return;

        switch (mLevel) {
            case EASY:
                image = (ImageView) findViewById(R.id.image_easy);
                break;
            case NORMAL:
                image = (ImageView) findViewById(R.id.image_normal);
                break;
            case HARD:
                image = (ImageView) findViewById(R.id.image_hard);
                break;
            default:
                return;
        }

        image.setImageResource(R.drawable.tick);
    }
}