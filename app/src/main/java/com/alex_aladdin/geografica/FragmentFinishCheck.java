package com.alex_aladdin.geografica;

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

public class FragmentFinishCheck extends Fragment {

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
        //Через секунду выключаем
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {}

            @Override
            public void onFinish() {
                if (FragmentFinishCheck.this.isAdded()) mListener.onFragmentFinishComplete(Activity.RESULT_OK);
            }
        }.start();
    }
}