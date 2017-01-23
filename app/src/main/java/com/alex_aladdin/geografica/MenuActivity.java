package com.alex_aladdin.geografica;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
    }

    //Запускаем чемпионат
    public void onButtonChampionshipClick(View view) {
        //Сначала запускаем карту всей России
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.putExtra("LEVEL", MapImageView.Level.NORMAL);
        intent.putExtra("MAP_NAME", "russia");
        intent.putExtra("MAP_CAPTION", "Российская Федерация");
        intent.putExtra("SHOW_TIMER", false);
        intent.putExtra("SHOW_BUTTON_INFO", true);
        intent.putExtra("FRAGMENT_START", true);
        intent.putExtra("FRAGMENT_FINISH_TRAINING", false);
        intent.putExtra("FRAGMENT_FINISH_CHECK", true);

        startActivityForResult(intent, 0);
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