package com.alex_aladdin.geografica;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
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

        mLayout.setVisibility(View.VISIBLE);
    }
}