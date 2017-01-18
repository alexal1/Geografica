package com.alex_aladdin.geografica;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MenuActivity extends AppCompatActivity {

    private int mCurrentItem; //Номер пункта меню, карта которого запущена в данный момент

    private enum Menu {

        DVO("Дальневосточный"),
        SFO("Сибирский"),
        UFO("Уральский"),
        PRFO("Приволжский"),
        SZFO("Северо-Западный"),
        YUFO("Южный"),
        SKFO("Северо-Кавказский");

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

        //Добавляем кнопки
        addButtons();
    }

    //Метод, добавляющий кнопки, аналогичные по внешнему виду кнопке buttonSample
    private void addButtons() {
        LinearLayout layoutMenu = (LinearLayout)findViewById(R.id.menu);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (final Menu item : Menu.values()) {
            Button buttonSample = (Button)inflater.inflate(R.layout.menu_button, layoutMenu, false);
            buttonSample.setText(item.getCaption());
            layoutMenu.addView(buttonSample);

            //Вешаем обработчик нажатия, который вызывает MainActivity
            buttonSample.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MenuActivity.this, MainActivity.class);
                    intent.putExtra("MAP_NAME", item.toString().toLowerCase());
                    intent.putExtra("MAP_CAPTION", item.getCaption());

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

        //Была выбрана кнопка ДАЛЕЕ
        if (resultCode == Activity.RESULT_OK) {
            Menu[] menu = Menu.values();

            //Если это уже был последний пункт меню, прерываем
            if (mCurrentItem == menu.length - 1) return;

            mCurrentItem++;
            Menu item = menu[mCurrentItem];

            //Снова запускаем активность
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            intent.putExtra("MAP_NAME", item.toString().toLowerCase());
            intent.putExtra("MAP_CAPTION", item.getCaption());

            startActivityForResult(intent, 0);
        }
    }
}