package com.alex_aladdin.geografica;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class FragmentExit extends Fragment {

    //Определяем слушатель типа нашего интерфейса. Это будет сама активность
    private FragmentExit.OnCompleteListener mListener;

    //Определяем событие, которое фрагмент будет использовать для связи с активностью
    public interface OnCompleteListener {
        void onExitAttempt(Boolean exit);
    }

    //Наполняем объект mListener нашей активностью в момент присоединения фрагмента к активности
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragmentExit.OnCompleteListener) {
            mListener = (FragmentExit.OnCompleteListener) context;
        }
        else
            throw new ClassCastException(context.toString() +
                    " должен реализовывать интерфейс FragmentExit.OnCompleteListener");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof FragmentExit.OnCompleteListener) {
            mListener = (FragmentExit.OnCompleteListener) activity;
        }
        else
            throw new ClassCastException(activity.toString() +
                    " должен реализовывать интерфейс FragmentExit.OnCompleteListener");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exit, container, false);

        //Кнопка ДА
        ImageButton buttonCheck = (ImageButton) rootView.findViewById(R.id.button_exit_check);
        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onExitAttempt(true);
            }
        });

        //Кнопка НЕТ
        ImageButton buttonCross = (ImageButton) rootView.findViewById(R.id.button_exit_cross);
        buttonCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onExitAttempt(false);
            }
        });

        //Клик по экрану
        LinearLayout layoutExit = (LinearLayout) rootView.findViewById(R.id.layout_exit);
        layoutExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onExitAttempt(false);
            }
        });

        return rootView;
    }
}