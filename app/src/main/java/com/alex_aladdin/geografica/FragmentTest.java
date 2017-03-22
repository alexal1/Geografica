package com.alex_aladdin.geografica;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentTest extends Fragment {

    private RelativeLayout mLayout;
    private ListView mListVariants;
    private ArrayAdapter<String> mAdapter;
    private int mCorrectVariant;
    private Timer mTimer = null;
    private PieceImageView mCurrentPiece;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test, container, false);
        mLayout = (RelativeLayout) rootView.findViewById(R.id.layout_test);
        mListVariants = (ListView) rootView.findViewById(R.id.list_variants);

        // Обработчик нажатий на элементы ListView
        mListVariants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Сбрасываем таймер
                if (mTimer != null)
                    mTimer.cancel();
                mTimer = new Timer();
                // Запускаем проверку с задержкой
                DelayedChecker delayedChecker = new DelayedChecker(position);
                mTimer.schedule(delayedChecker, 500, 1000);
                // Убираем подсветку с остальных элементов
                for (int i = 0, count = mListVariants.getCount(); i < count; i++) {
                    if (i != position)
                        mListVariants.getChildAt(i).setBackgroundResource(R.drawable.list_test_item);
                }
            }
        });

        return rootView;
    }

    // Инициализация фрагмента в тот момент, когда кусок начали тащить
    public void init(PieceImageView piece, List<PieceImageView> fakes) {
        mCurrentPiece = piece;
        fakes.add(piece);
        Collections.shuffle(fakes);
        mCorrectVariant = fakes.indexOf(piece);
        final String[] variants = new String[4];
        for (int i = 0; i < fakes.size(); i++)
            variants[i] = fakes.get(i).getCaption();

        // Создаем адаптер для ListView
        mAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_test, variants);
        mListVariants.setAdapter(mAdapter);

        // Задаем ширину
        mListVariants.getLayoutParams().width = getWidestView();
    }

    // Возвращает ширину самого длинного элемента списка
    private int getWidestView() {
        int maxWidth = 0, width;
        View view = null;
        RelativeLayout parent = (RelativeLayout) getActivity().findViewById(R.id.layout_root);
        for (int i = 0, count = mAdapter.getCount(); i < count; i++) {
            view = mAdapter.getView(i, view, parent);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            width = view.getMeasuredWidth();
            if (width > maxWidth)
                maxWidth = width;
        }

        return maxWidth;
    }

    // Проверяем, правильный ли выбран пункт списка ListView, через промежуток времени
    private class DelayedChecker extends TimerTask {

        private int variant;
        private Boolean isChecked = false;

        private DelayedChecker(int variant) {
            this.variant = variant;
        }

        @Override
        public void run() {
            final View chosenView = mListVariants.getChildAt(variant);

            // Работаем с UI-потоком
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isChecked) {
                        // Возвращаем всё как было
                        chosenView.setBackgroundResource(R.drawable.list_test_item);
                        mListVariants.clearChoices();
                        mListVariants.requestLayout();
                        mAdapter.notifyDataSetChanged();
                        // Разрешаем нажатия
                        mListVariants.setEnabled(true);
                        // Отменяем таймер
                        cancel();
                        return;
                    }

                    // Проверка правильности ответа
                    if (variant == mCorrectVariant) {
                        chosenView.setBackgroundResource(R.color.right_choice);
                    }
                    else {
                        chosenView.setBackgroundResource(R.color.wrong_choice);
                    }

                    // Запрещаем нажатия
                    mListVariants.setEnabled(false);

                    isChecked = true;
                }
            });
        }

    }

    // Устанавливаем фрагмент над данным куском паззла
    public void set() {
        float fragment_width = mLayout.getWidth();
        float fragment_height = mLayout.getHeight();

        // Вычисляем координаты верхнего левого угла нашего фрагмента
        float fragment_x = mCurrentPiece.getTargetX() - fragment_width / 2;
        float fragment_y = mCurrentPiece.getTargetY() - (float) mCurrentPiece.getHeight() / 2 - fragment_height;

        mLayout.setX(fragment_x);
        mLayout.setY(fragment_y);

        // Делаем видимым фрагмент
        mLayout.setVisibility(View.VISIBLE);

        /* --- Используем отдельный View-элемент в качестве фона для FragmentTest --- */

        final View background = getActivity().findViewById(R.id.fragment_test_background);
        final ZoomableRelativeLayout layoutZoom = (ZoomableRelativeLayout) getActivity().findViewById(R.id.layout_zoom);

        // Делаем фон видимым
        background.setVisibility(View.VISIBLE);

        // При касании фона убираем фрагмент, убираем сам фон и возвращаем экран в исходное состояне
        background.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mLayout.setVisibility(View.GONE);
                background.setVisibility(View.GONE);
                layoutZoom.centerDefault();

                // Снимаем обработчик
                background.setOnTouchListener(null);

                return true;
            }
        });
    }

    public RelativeLayout getLayout() {
        return mLayout;
    }
}