package com.example.abedaigorou.thirdeye.configure.VRConfigure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.abedaigorou.thirdeye.R;
import com.example.abedaigorou.thirdeye.configure.SeekbarPreference;

import java.util.regex.Pattern;

/**
 * Created by abedaigorou on 2017/09/09.
 */

public class VRConfigureFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private String TAG="VRConfigureActivity";
    private VRConfigureEventListener listener;
    private EditTextPreference devideLongitude,devideLatitude;
    private SeekbarPreference cameraPosition;

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.pref_vr);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof VRConfigureEventListener){
            this.listener=(VRConfigureEventListener) context;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listener.onVRConfigureViewCreated();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        devideLatitude=(EditTextPreference)findPreference(getString(R.string.key_devidelatitude_preference));
        devideLongitude=(EditTextPreference)findPreference(getString(R.string.key_devidelongitude_preference));
        cameraPosition=(SeekbarPreference)findPreference(getString(R.string.key_cameraposition_preference));

        if(key.equals(getString(R.string.key_devidelatitude_preference))) {
            if (isNumber(devideLatitude.getText())) {
                int parse;
                if (isEven(parse = Integer.parseInt(devideLatitude.getText()))) {
                    listener.onDevideLatitudeConfigured(parse);
                    devideLatitude.setSummary(devideLatitude.getText());
                } else {
                    Toast.makeText(getContext(), "奇数は設定できません", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "文字は設定できません", Toast.LENGTH_SHORT).show();
            }
        }

        if(key.equals(getString(R.string.key_devidelongitude_preference))) {
            if (isNumber(devideLongitude.getText())) {
                int parse;
                if (isEven(parse = Integer.parseInt(devideLongitude.getText()))) {
                    listener.onDevideLongitudeConfigured(parse);
                    devideLongitude.setSummary(devideLongitude.getText());
                } else {
                    Toast.makeText(getContext(), "奇数は設定できません", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "文字は設定できません", Toast.LENGTH_SHORT).show();
            }
        }

        listener.onCameraPositionConfigured(cameraPosition.getValue());
    }

    private boolean isNumber(String num){
        String regex="\\d";
        Pattern p= Pattern.compile(regex);
        return p.matcher(num).find();
    }

    private boolean isEven(int num){
        if(num%2==0){
            return true;
        }else{
            return false;
        }
    }

    public interface VRConfigureEventListener{
        void onVRConfigureViewCreated();
        void onCameraPositionConfigured(int progress);
        void onDevideLongitudeConfigured(int devide);
        void onDevideLatitudeConfigured(int devide);
    }
}
