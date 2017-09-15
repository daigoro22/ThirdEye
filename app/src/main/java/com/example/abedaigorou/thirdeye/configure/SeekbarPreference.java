package com.example.abedaigorou.thirdeye.configure;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.abedaigorou.thirdeye.R;

/**
 * Created by abedaigorou on 2017/09/01.
 */

public class SeekbarPreference extends Preference implements SeekBar.OnSeekBarChangeListener{
    private final int DEFAULT_PROGRESS=50;
    private int MAX_PROGRESS=100;
    private int currentProgress,oldProgress;
    private TextView prefTextView;
    private SeekBar prefSeekBar;
    private String TAG="SeekbarPreference";

    public SeekbarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, DEFAULT_PROGRESS);
    }

    @Override
    public View onCreateView(ViewGroup parent){
        LinearLayout root = (LinearLayout) super.onCreateView(parent);

        for (int i = 0, size = root.getChildCount(); i < size; i++) {
            View v = root.getChildAt(i);

            if(!(v instanceof RelativeLayout)) continue;

            RelativeLayout r = (RelativeLayout) v;

            LinearLayout seekbarLayout
                    = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pref_seekbar, null);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
                    , RelativeLayout.LayoutParams.WRAP_CONTENT);

            params.addRule(RelativeLayout.BELOW, Resources.getSystem().getIdentifier("summary", "id", "android"));

            float density = getContext().getResources().getDisplayMetrics().density;
            // 10dp
            params.topMargin = (int) (10f / density + 0.5f);

            seekbarLayout.setLayoutParams(params);
            r.addView(seekbarLayout);

            break;
        }

        return root;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            currentProgress = getPersistedInt(currentProgress);
        } else {
            currentProgress = (Integer) defaultValue;
            persistInt(currentProgress);
        }
        oldProgress = currentProgress;
    }

    @Override
    protected void onBindView(View view) {
        prefSeekBar = (SeekBar) view.findViewById(R.id.prefSeekBar);
        prefTextView=(TextView)view.findViewById(R.id.prefTextView);

        if (prefSeekBar != null) {
            prefSeekBar.setProgress(currentProgress);
            prefSeekBar.setMax(MAX_PROGRESS);
            prefSeekBar.setOnSeekBarChangeListener(this);
        }
        if(prefTextView!=null){
            prefTextView.setText(String.valueOf(currentProgress));
        }
        super.onBindView(view);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        prefTextView.setText(String.valueOf(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i(TAG,"onStopTrackingTouch");
        int progress=seekBar.getProgress();
        currentProgress=(callChangeListener(progress))?progress:oldProgress;
        persistInt(currentProgress);
        oldProgress=currentProgress;
    }

    public int getValue(){
        return currentProgress;
    }

    public void setMAX_PROGRESS(int prog){
        if(prefSeekBar!=null) {
            prefSeekBar.setMax(prog);
            MAX_PROGRESS=prog;
        }
    }
}
