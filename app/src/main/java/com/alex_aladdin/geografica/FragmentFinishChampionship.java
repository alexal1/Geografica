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
import android.widget.TextView;

import java.text.DecimalFormat;

public class FragmentFinishChampionship extends Fragment {

    private TextView mTextResult, mTextLevel;
    long mTime;
    String mLevel;
    //Определяем слушатель типа нашего интерфейса. Это будет сама активность
    private FragmentFinishChampionship.OnCompleteListener mListener;

    //Определяем событие, которое фрагмент будет использовать для связи с активностью
    public interface OnCompleteListener {
        void onFragmentFinishComplete(int resultCode);
    }

    //Наполняем объект mListener нашей активностью в момент присоединения фрагмента к активности
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragmentFinishChampionship.OnCompleteListener) {
            mListener = (FragmentFinishChampionship.OnCompleteListener) context;
        }
        else
            throw new ClassCastException(context.toString() +
                    " должен реализовывать интерфейс FragmentFinishChampionship.OnCompleteListener");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof FragmentFinishChampionship.OnCompleteListener) {
            mListener = (FragmentFinishChampionship.OnCompleteListener) activity;
        }
        else
            throw new ClassCastException(activity.toString() +
                    " должен реализовывать интерфейс FragmentFinishChampionship.OnCompleteListener");
    }

    //Метод для получения аргументов из активности
    public static FragmentFinishChampionship newInstance(long time, String level) {
        FragmentFinishChampionship fragmentFinishChampionship = new FragmentFinishChampionship();
        Bundle args = new Bundle();
        args.putLong("TIME", time);
        args.putString("LEVEL", level);
        fragmentFinishChampionship.setArguments(args);
        return fragmentFinishChampionship;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Получаем аргументы обратно
        mTime = getArguments().getLong("TIME");
        mLevel = getArguments().getString("LEVEL");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_finish_championship, container, false);
        mTextResult = (TextView) rootView.findViewById(R.id.text_finish_result);
        mTextLevel = (TextView) rootView.findViewById(R.id.text_finish_level);

        //Делаем кнопки активности недоступными
        final ImageButton buttonAdd = (ImageButton) getActivity().findViewById(R.id.button_add_piece);
        final ImageButton buttonInfo = (ImageButton) getActivity().findViewById(R.id.button_info);
        final ImageButton buttonZoom = (ImageButton) getActivity().findViewById(R.id.button_zoom);
        buttonAdd.setEnabled(false);
        buttonInfo.setEnabled(false);
        buttonZoom.setEnabled(false);

        //Кнопка с флагом
        ImageButton buttonFlag = (ImageButton) rootView.findViewById(R.id.button_finish_flag);
        buttonFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentFinishComplete(Activity.RESULT_OK);
            }
        });

        //Запускаем
        show();

        return rootView;
    }

    public void show() {
        //Показываем уровень сложности
        mTextLevel.setText(mLevel);

        //Показываем результат
        DecimalFormat df = new DecimalFormat("00");
        String text = getString(R.string.finish_result) + "\n";

        int hours = (int)(mTime / (3600 * 1000));
        int remaining = (int)(mTime % (3600 * 1000));

        int minutes = remaining / (60 * 1000);
        remaining = remaining % (60 * 1000);

        int seconds = remaining / 1000;
        int milliseconds = ((int)mTime % 1000) / 10;

        if (hours > 0) {
            text += df.format(hours) + ":";
        }

        text += df.format(minutes) + ":";
        text += df.format(seconds) + ":";
        text += df.format(milliseconds);

        mTextResult.setText(text);
    }
}