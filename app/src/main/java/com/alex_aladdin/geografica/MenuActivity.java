package com.alex_aladdin.geografica;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MenuActivity extends AppCompatActivity {

    private enum Menu {

        DVO("Дальневосточный"),
        SFO("Сибирский"),
        UFO("Уральский");

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
                    startActivity(intent);
                }
            });
        }
    }
}