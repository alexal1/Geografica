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
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FragmentFinishCheck extends Fragment {

    private TextView mTextView;
    //Определяем слушатель типа нашего интерфейса. Это будет сама активность
    private FragmentFinishCheck.OnCompleteListener mListener;

    //Определяем событие, которое фрагмент будет использовать для связи с активностью
    public interface OnCompleteListener {
        void onFragmentFinishComplete(int resultCode);
    }

    //Наполняем объект mListener нашей активностью в момент присоединения фрагмента к активности
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragmentFinishCheck.OnCompleteListener) {
            mListener = (FragmentFinishCheck.OnCompleteListener) context;
        }
        else
            throw new ClassCastException(context.toString() +
                    " должен реализовывать интерфейс FragmentStart.OnDoneListener");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof FragmentFinishCheck.OnCompleteListener) {
            mListener = (FragmentFinishCheck.OnCompleteListener) activity;
        }
        else
            throw new ClassCastException(activity.toString() +
                    " должен реализовывать интерфейс FragmentStart.OnDoneListener");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_finish_check, container, false);
        mTextView = (TextView) rootView.findViewById(R.id.text_finish_check);
        RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.layout_finish);

        //Обработчик клика на layout
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentFinishComplete(Activity.RESULT_OK);
            }
        });

        //Запускаем
        show();

        return rootView;
    }

    private void show() {
        //Ждем 2 секунды и делаем затухание
        new CountDownTimer(3000, 1000) {

            public void onTick(final long millisUntilFinished) {
                if (millisUntilFinished/1000 == 1) {
                    //Затухание
                    ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mTextView, "alpha", 0);
                    alphaAnimator.setDuration(1200);
                    //Поторапливаем onFinish()
                    alphaAnimator.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            onFinish();
                        }
                    });
                    alphaAnimator.start();
                }
            }

            public void onFinish() {
                if (FragmentFinishCheck.this.isAdded()) mListener.onFragmentFinishComplete(Activity.RESULT_OK);
            }
        }.start();
    }
}