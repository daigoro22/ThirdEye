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
import com.example.abedaigorou.thirdeye.configure.ConfigureUtils;
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
        devideLatitude=(EditTextPreference)findPreference(getString(R.string.key_devidelatitude_preference));
        devideLongitude=(EditTextPreference)findPreference(getString(R.string.key_devidelongitude_preference));
        cameraPosition=(SeekbarPreference)findPreference(getString(R.string.key_cameraposition_preference));

        devideLatitude.setOnPreferenceChangeListener(ConfigureUtils.getIsEvenListener(getContext()));
        devideLongitude.setOnPreferenceChangeListener(ConfigureUtils.getIsEvenListener(getContext()));
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
        Log.i(TAG,"onSharedPreferenceChanged");
        if(key.equals(getString(R.string.key_devidelatitude_preference))) {
            listener.onDevideLatitudeConfigured(Integer.parseInt(devideLatitude.getText()));
            devideLatitude.setSummary(devideLatitude.getText());
        }

        else if(key.equals(getString(R.string.key_devidelongitude_preference))) {
            listener.onDevideLongitudeConfigured(Integer.parseInt(devideLongitude.getText()));
            devideLongitude.setSummary(devideLongitude.getText());
        }

        else {
            listener.onCameraPositionConfigured(cameraPosition.getValue());
        }
    }

    public interface VRConfigureEventListener{
        void onVRConfigureViewCreated();
        void onCameraPositionConfigured(int progress);
        void onDevideLongitudeConfigured(int devide);
        void onDevideLatitudeConfigured(int devide);
    }
}
