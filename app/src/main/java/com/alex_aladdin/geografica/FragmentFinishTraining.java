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

public class FragmentFinishTraining extends Fragment {

    private TextView mTextCaption, mTextResult;
    String mCaption;
    long mTime;
    //Определяем слушатель типа нашего интерфейса. Это будет сама активность
    private FragmentFinishTraining.OnCompleteListener mListener;

    //Определяем событие, которое фрагмент будет использовать для связи с активностью
    public interface OnCompleteListener {
        void onFragmentFinishComplete(int resultCode);
    }

    //Наполняем объект mListener нашей активностью в момент присоединения фрагмента к активности
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FragmentFinishTraining.OnCompleteListener) {
            mListener = (FragmentFinishTraining.OnCompleteListener) context;
        }
        else
            throw new ClassCastException(context.toString() +
                    " должен реализовывать интерфейс FragmentFinishTraining.OnCompleteListener");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof FragmentFinishTraining.OnCompleteListener) {
            mListener = (FragmentFinishTraining.OnCompleteListener) activity;
        }
        else
            throw new ClassCastException(activity.toString() +
                    " должен реализовывать интерфейс FragmentFinishTraining.OnCompleteListener");
    }

    //Метод для получения аргументов из активности
    public static FragmentFinishTraining newInstance(String caption, long time) {
        FragmentFinishTraining fragmentFinishTraining = new FragmentFinishTraining();
        Bundle args = new Bundle();
        args.putString("CAPTION", caption);
        args.putLong("TIME", time);
        fragmentFinishTraining.setArguments(args);
        return fragmentFinishTraining;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Получаем аргументы обратно
        mCaption = getArguments().getString("CAPTION");
        mTime = getArguments().getLong("TIME");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_finish_training, container, false);
        mTextCaption = (TextView) rootView.findViewById(R.id.text_finish_caption);
        mTextResult = (TextView) rootView.findViewById(R.id.text_finish_result);

        //Делаем кнопки активности недоступными
        final ImageButton buttonAdd = (ImageButton) getActivity().findViewById(R.id.button_add_piece);
        final ImageButton buttonInfo = (ImageButton) getActivity().findViewById(R.id.button_info);
        final ImageButton buttonZoom = (ImageButton) getActivity().findViewById(R.id.button_zoom);
        buttonAdd.setEnabled(false);
        buttonInfo.setEnabled(false);
        buttonZoom.setEnabled(false);

        //Кнопка ДАЛЕЕ
        ImageButton buttonNext = (ImageButton) rootView.findViewById(R.id.button_finish_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentFinishComplete(Activity.RESULT_OK);
            }
        });

        //Кнопка МЕНЮ
        ImageButton buttonMenu = (ImageButton) rootView.findViewById(R.id.button_finish_menu);
        buttonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentFinishComplete(Activity.RESULT_CANCELED);
            }
        });

        //Кнопка РЕСТАРТ
        ImageButton buttonRestart = (ImageButton) rootView.findViewById(R.id.button_finish_restart);
        buttonRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onFragmentFinishComplete(Activity.RESULT_FIRST_USER);
            }
        });

        //Запускаем
        show();

        return rootView;
    }

    public void show() {
        //Показываем заголовок
        mTextCaption.setText(mCaption);

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