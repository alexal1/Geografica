package com.alex_aladdin.geografica;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragmentStart extends Fragment {

    private TextView mTextView;
    //Определяем слушатель типа нашего интерфейса. Это будет сама активность
    private FragmentStart.OnCompleteListener mListener;

    //Определяем событие, которое фрагмент будет использовать для связи с активностью
    interface OnCompleteListener {
        void onFragmentStartComplete();
    }

    //Наполняем объект mListener нашей активностью в момент присоединения фрагмента к активности
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragmentStart.OnCompleteListener) {
            mListener = (FragmentStart.OnCompleteListener) context;
        }
        else
            throw new ClassCastException(context.toString() +
                    " должен реализовывать интерфейс FragmentStart.OnCompleteListener");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof FragmentStart.OnCompleteListener) {
            mListener = (FragmentStart.OnCompleteListener) activity;
        }
        else
            throw new ClassCastException(activity.toString() +
                    " должен реализовывать интерфейс FragmentStart.OnCompleteListener");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_start, container, false);
        mTextView = (TextView) rootView.findViewById(R.id.text_start);
        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.layout_start);

        //Обработчик клика на layout
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentStartComplete();
            }
        });

        //Запускаем
        show();

        return rootView;
    }

    private void show() {
        //Включаем обратный отсчет
        new CountDownTimer(4000, 1000) {

            public void onTick(final long millisUntilFinished) {
                mTextView.setText(String.valueOf(millisUntilFinished/1000));
                mTextView.setAlpha(1.0f);
                //Затухание
                ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mTextView, "alpha", 0);
                alphaAnimator.setDuration(1200);
                //Поторапливаем onFinish()
                if (millisUntilFinished/1000 == 1) alphaAnimator.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        onFinish();
                    }
                });
                alphaAnimator.start();
            }

            public void onFinish() {
                if (FragmentStart.this.isAdded()) mListener.onFragmentStartComplete();
            }
        }.start();
    }
}