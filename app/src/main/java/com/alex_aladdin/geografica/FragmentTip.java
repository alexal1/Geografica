package com.alex_aladdin.geografica;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragmentTip extends Fragment {
    private RelativeLayout mLayout;
    private TextView mTextView;
    private PieceImageView mCurrentPiece;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tip, container, false);
        mLayout = (RelativeLayout) rootView.findViewById(R.id.layout_tip);
        mTextView = (TextView) rootView.findViewById(R.id.text_tip);

        return rootView;
    }

    //Получаем текущий кусочек паззла
    public void init(PieceImageView view) {
        mCurrentPiece = view;
        mTextView.setText(view.getCaption());

        //Поднимаем подсказку
        View parent = (View)mLayout.getParent();
        mLayout.bringToFront();
        parent.requestLayout();
        parent.invalidate();
    }

    //Во время перетаскивания получаем координаты центра кусочка, и задаем координаты нашему фрагменту
    public void set(float x, float y) {
        float fragment_width = mLayout.getWidth();
        float fragment_height = mLayout.getHeight();

        //Вычисляем координаты верхнего левого угла нашего фрагмента
        float fragment_x = x - fragment_width/2;
        float fragment_y = y - (float)mCurrentPiece.getHeight()/2 - fragment_height;

        mLayout.setX(fragment_x);
        mLayout.setY(fragment_y);

        mLayout.setVisibility(View.VISIBLE);
    }

    //Завершаем показ
    public void close() {
        mLayout.setVisibility(View.INVISIBLE);
    }
}