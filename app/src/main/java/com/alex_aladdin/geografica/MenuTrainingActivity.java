package com.alex_aladdin.geografica;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MenuTrainingActivity extends AppCompatActivity {

    private int mCurrentItem; //Номер пункта меню, карта которого запущена в данный момент

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_training);

        //Добавляем кнопки
        addButtons();
    }

    //Метод, добавляющий кнопки, аналогичные по внешнему виду кнопке buttonSample
    private void addButtons() {
        LinearLayout layoutMenu = (LinearLayout)findViewById(R.id.layout_menu);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (final MenuActivity.Menu item : MenuActivity.Menu.values()) {
            Button buttonSample = (Button)inflater.inflate(R.layout.menu_button, layoutMenu, false);
            buttonSample.setText(item.getCaption());
            layoutMenu.addView(buttonSample);

            //Вешаем обработчик нажатия, который вызывает MainActivity
            buttonSample.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MenuTrainingActivity.this, MainActivity.class);
                    intent.putExtra("LEVEL", MapImageView.Level.NORMAL);
                    intent.putExtra("MAP_NAME", item.toString().toLowerCase());
                    intent.putExtra("MAP_CAPTION", item.getCaption());
                    intent.putExtra("SHOW_TIMER", false);
                    intent.putExtra("SHOW_BUTTON_INFO", true);
                    intent.putExtra("FRAGMENT_START", true);
                    intent.putExtra("FRAGMENT_FINISH_TRAINING", false);
                    intent.putExtra("FRAGMENT_FINISH_CHECK", true);

                    startActivityForResult(intent, 0);
                    mCurrentItem = item.ordinal();
                }
            });
        }
    }

    //Ожидаем ответ от MainActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        MenuActivity.Menu[] menu = MenuActivity.Menu.values();

        switch (resultCode) {
            //Была выбрана кнопка ДАЛЕЕ
            case Activity.RESULT_OK:
                //Если это уже был последний пункт меню, прерываем
                if (mCurrentItem == menu.length - 1) return;

                mCurrentItem++;

                break;

            //Была выбрана кнопка МЕНЮ
            case Activity.RESULT_CANCELED:
                return;

            //Была выбрана кнопка РЕСТАРТ
            case Activity.RESULT_FIRST_USER:
                break;
        }

        MenuActivity.Menu item = menu[mCurrentItem];

        //Снова запускаем активность
        Intent intent = new Intent(MenuTrainingActivity.this, MainActivity.class);
        intent.putExtra("LEVEL", MapImageView.Level.NORMAL);
        intent.putExtra("MAP_NAME", item.toString().toLowerCase());
        intent.putExtra("MAP_CAPTION", item.getCaption());
        intent.putExtra("SHOW_TIMER", false);
        intent.putExtra("SHOW_BUTTON_INFO", true);
        intent.putExtra("FRAGMENT_START", true);
        intent.putExtra("FRAGMENT_FINISH_TRAINING", false);
        intent.putExtra("FRAGMENT_FINISH_CHECK", true);

        startActivityForResult(intent, 0);
    }

    //Сохраняем значение переменной mCurrentItem при повороте экрана
    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        saveInstanceState.putInt("CURRENT_ITEM", mCurrentItem);
    }

    //Восстанавливаем значение переменной mCurrentItem после поворота экрана
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mCurrentItem = savedInstanceState.getInt("CURRENT_ITEM");
    }
}