package com.alex_aladdin.geografica;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.HashMap;

public class FragmentFinishChampionship extends Fragment {

    private TextView mTextLevel, mTextTime, mTextMistakes, mTextRepeat;
    long mTime;
    String mLevel;
    private HashMap<String, Integer> mMistakes;
    //Определяем слушатель типа нашего интерфейса. Это будет сама активность
    private FragmentFinishChampionship.OnCompleteListener mListener;

    //Определяем событие, которое фрагмент будет использовать для связи с активностью
    interface OnCompleteListener {
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
    public static FragmentFinishChampionship newInstance(long time, String level, HashMap testMistakes) {
        FragmentFinishChampionship fragmentFinishChampionship = new FragmentFinishChampionship();
        Bundle args = new Bundle();
        args.putLong("TIME", time);
        args.putString("LEVEL", level);
        args.putSerializable("TEST_MISTAKES", testMistakes);
        fragmentFinishChampionship.setArguments(args);
        return fragmentFinishChampionship;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Получаем аргументы обратно
        mTime = getArguments().getLong("TIME");
        mLevel = getArguments().getString("LEVEL");
        @SuppressWarnings("unchecked")
        HashMap<String, Integer> testMistakes = (HashMap<String, Integer>) getArguments().getSerializable("TEST_MISTAKES");
        mMistakes = testMistakes;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_finish_championship, container, false);
        mTextLevel = (TextView) rootView.findViewById(R.id.text_finish_level);
        mTextTime = (TextView) rootView.findViewById(R.id.text_finish_time);
        mTextMistakes = (TextView) rootView.findViewById(R.id.text_finish_mistakes);
        mTextRepeat = (TextView) rootView.findViewById(R.id.text_finish_repeat);

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

        // Дожидаемся, пока табличка с результатами установит свой размер, и превращаем её в одну колонку, если не помещается
        final GridLayout layoutResults = (GridLayout) rootView.findViewById(R.id.layout_finish_results);
        final LinearLayout layoutWindow = (LinearLayout) rootView.findViewById(R.id.layout_window);
        ViewTreeObserver observer = layoutResults.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    layoutResults.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    //noinspection deprecation
                    layoutResults.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                int innerWidth = layoutResults.getWidth();
                int outerWidth = layoutWindow.getWidth();

                if (innerWidth == outerWidth) {
                    final int viewsCount = layoutResults.getChildCount();
                    for (int i = 0; i < viewsCount; i++) {
                        View view = layoutResults.getChildAt(i);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.setGravity(Gravity.CENTER_HORIZONTAL);
                        view.setLayoutParams(params);
                    }
                    layoutResults.setColumnCount(1);
                }
            }
        });

        return rootView;
    }

    public void show() {
        //Показываем уровень сложности
        mTextLevel.setText(mLevel);

        //Показываем время
        DecimalFormat df = new DecimalFormat("00");
        String text = "";

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

        mTextTime.setText(text);

        // Находим округ с наибольшим числом ошибок и считаем общее число ошибок
        int numberOfMistakes = 0;
        int valueMax = 0;
        String keyMax = "";

        for (String key : mMistakes.keySet()) {
            int value = mMistakes.get(key);
            if (value > valueMax) {
                valueMax = value;
                keyMax = key;
            }
            numberOfMistakes += value;
        }

        mTextMistakes.setText(String.valueOf(numberOfMistakes));
        if (mMistakes.isEmpty()) {
            // Увеличиваем текст
            mTextRepeat.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);

            mTextRepeat.setText(getString(R.string.finish_perfect));
        }
        else if (keyMax.equals(getString(R.string.districts_displacement))) {
            String textRepeat = getString(R.string.finish_repeat) + " " + keyMax;
            mTextRepeat.setText(textRepeat);
        }
        else {
            String textRepeat = getString(R.string.finish_repeat) + " " + keyMax + " " +
                    getString(R.string.finish_federal_district);
            mTextRepeat.setText(textRepeat);
        }
    }

}