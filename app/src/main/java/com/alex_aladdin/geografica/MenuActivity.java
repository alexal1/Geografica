package com.alex_aladdin.geografica;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.alex_aladdin.geografica.di.ServiceLocator;
import com.alex_aladdin.geografica.helpers.SharedPreferencesHelper;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;
import static com.alex_aladdin.geografica.helpers.SharedPreferencesHelper.PREFS_CONTACT_FORM_WAS_CLOSED;
import static com.alex_aladdin.geografica.helpers.SharedPreferencesHelper.PREFS_CONTACT_FORM_WAS_SENT;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "GeoMenuActivity";

    private final Analytics analytics = ServiceLocator.get(Analytics.class);
    private final SharedPreferencesHelper sharedPrefsHelper = ServiceLocator.get(SharedPreferencesHelper.class);
    private final Calendar calendar = GregorianCalendar.getInstance();

    public enum Menu {

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

        final LinearLayout layoutInner = findViewById(R.id.layout_inner);
        final ImageView imageLogo = findViewById(R.id.image_logo);
        final TextView textLogo = findViewById(R.id.text_logo);

        //Отслеживаем момент, когда layoutInner полностью загружается
        ViewTreeObserver viewTreeObserver = layoutInner.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Снимаем слушатель
                removeOnGlobalLayoutListener(layoutInner, this);

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

    @Override
    protected void onResume() {
        super.onResume();

        ContactFormFragment existingContactFormFragment = (ContactFormFragment) getSupportFragmentManager().findFragmentByTag(ContactFormFragment.TAG);
        if (existingContactFormFragment != null) {
            existingContactFormFragment.setOnCloseClick(this::closeContactFormFragment);
            if (existingContactFormFragment.isHidden()) {
                showContactFormFragment(existingContactFormFragment);
            }
        } else if (isOldUser() && !wasContactFormClosed() && !wasContactFormSent()) {
            ContactFormFragment contactFormFragment = ContactFormFragment.Companion.create();
            contactFormFragment.setOnCloseClick(this::closeContactFormFragment);

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(0, 0, 0, R.anim.go_out_down)
                    .add(R.id.root_layout, contactFormFragment, ContactFormFragment.TAG)
                    .addToBackStack(ContactFormFragment.TAG)
                    .hide(contactFormFragment)
                    .commit();

            showContactFormFragment(contactFormFragment);
        }
    }

    private void showContactFormFragment(ContactFormFragment fragment) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (fragment.isResumed()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.go_in_up, 0, 0, 0)
                        .show(fragment)
                        .commit();
            }
        }, 500);
    }

    private void closeContactFormFragment() {
        sharedPrefsHelper.setBool(PREFS_CONTACT_FORM_WAS_CLOSED, true);
        getSupportFragmentManager().popBackStack(ContactFormFragment.TAG, POP_BACK_STACK_INCLUSIVE);
    }

    private boolean isOldUser() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            long firstInstallTime = packageInfo.firstInstallTime;
            calendar.set(2020, 4, 8);
            return firstInstallTime <= calendar.getTimeInMillis();
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot retrieve package info", e);
            return true;
        }
    }

    private boolean wasContactFormClosed() {
        return sharedPrefsHelper.getBool(PREFS_CONTACT_FORM_WAS_CLOSED, false);
    }

    private boolean wasContactFormSent() {
        return sharedPrefsHelper.getBool(PREFS_CONTACT_FORM_WAS_SENT, false);
    }

    private void removeOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
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
        analytics.rateAppClick();
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException exception) {
            //На случай если не установлен Play Store
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="
                    + appPackageName)));
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag(ContactFormFragment.TAG) != null) {
            return;
        }
        super.onBackPressed();
    }
}