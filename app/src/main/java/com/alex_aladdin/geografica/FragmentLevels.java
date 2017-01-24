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
import android.widget.Button;
import android.widget.ImageButton;

public class FragmentLevels extends Fragment {

    //Определяем слушатель типа нашего интерфейса. Это будет сама активность
    private FragmentLevels.OnChooseListener mListener;

    //Определяем событие, которое фрагмент будет использовать для связи с активностью
    public interface OnChooseListener {
        void onLevelChoose(MapImageView.Level level);
    }

    //Наполняем объект mListener нашей активностью в момент присоединения фрагмента к активности
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragmentLevels.OnChooseListener) {
            mListener = (FragmentLevels.OnChooseListener) context;
        }
        else
            throw new ClassCastException(context.toString() +
                    " должен реализовывать интерфейс FragmentLevels.OnChooseListener");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof FragmentLevels.OnChooseListener) {
            mListener = (FragmentLevels.OnChooseListener) activity;
        }
        else
            throw new ClassCastException(activity.toString() +
                    " должен реализовывать интерфейс FragmentLevels.OnChooseListener");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_levels, container, false);
        ImageButton buttonEasy = (ImageButton) rootView.findViewById(R.id.button_level_easy);
        ImageButton buttonNormal = (ImageButton) rootView.findViewById(R.id.button_level_normal);
        ImageButton buttonHard = (ImageButton) rootView.findViewById(R.id.button_level_hard);

        setActivityEnabled(false);

        //Вешаем обработчики кликов
        buttonEasy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLevelChoose(MapImageView.Level.EASY);
            }
        });

        buttonNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLevelChoose(MapImageView.Level.NORMAL);
            }
        });

        buttonHard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onLevelChoose(MapImageView.Level.HARD);
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        setActivityEnabled(true);
    }

    private void setActivityEnabled(Boolean enabled) {
        //Делаем кнопки активности доступными/недоступными
        final Button buttonChampionship = (Button) getActivity().findViewById(R.id.button_championship);
        final Button buttonTraining = (Button) getActivity().findViewById(R.id.button_training);
        final Button buttonRate = (Button) getActivity().findViewById(R.id.button_rate);

        buttonChampionship.setEnabled(enabled);
        buttonTraining.setEnabled(enabled);
        buttonRate.setEnabled(enabled);
    }
}