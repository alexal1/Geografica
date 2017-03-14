package com.alex_aladdin.geografica;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class FragmentTest extends Fragment {

    private RelativeLayout mLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test, container, false);
        mLayout = (RelativeLayout) rootView.findViewById(R.id.layout_test);

        return rootView;
    }

    // Устанавливаем фрагмент над данным куском паззла
    public void set(PieceImageView piece) {
        float fragment_width = mLayout.getWidth();
        float fragment_height = mLayout.getHeight();

        // Вычисляем координаты верхнего левого угла нашего фрагмента
        float fragment_x = piece.getTargetX() - fragment_width / 2;
        float fragment_y = piece.getTargetY() - (float) piece.getHeight() / 2 - fragment_height;

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