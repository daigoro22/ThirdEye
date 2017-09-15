package com.example.abedaigorou.thirdeye.configure.CommunicationConfigure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.View;
import android.widget.Toast;

import com.example.abedaigorou.thirdeye.R;
import com.example.abedaigorou.thirdeye.configure.ConfigureUtils;

/**
 * Created by abedaigorou on 2017/09/15.
 */

public class CommunicationConfigureFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private EditTextPreference receiveImageWidth;
    private EditTextPreference receiveImageHeight;
    private SwitchPreference isServer;

    private CommunicationConfigureEventListener listener;
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof CommunicationConfigureEventListener){
            listener=(CommunicationConfigureEventListener)context;
        }
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.pref_communication);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listener.onCommunicationConfigureViewCreated();
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
        receiveImageWidth=(EditTextPreference)findPreference(getString(R.string.key_receiveimagewidth_preference));
        receiveImageHeight=(EditTextPreference)findPreference(getString(R.string.key_receiveimageheight_preference));
        isServer=(SwitchPreference)findPreference(getString(R.string.key_isServer_preference));

        if(key.equals(getString(R.string.key_receiveimagewidth_preference))) {
            if (ConfigureUtils.isNumber(receiveImageWidth.getText())) {
                int parse;
                if (ConfigureUtils.isEven(parse = Integer.parseInt(receiveImageWidth.getText()))) {
                    listener.onReceiveImageWidthConfigured(parse);
                    receiveImageWidth.setSummary(receiveImageWidth.getText());
                } else {
                    Toast.makeText(getContext(), "奇数は設定できません", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "文字は設定できません", Toast.LENGTH_SHORT).show();
            }
        }

        else if(key.equals(getString(R.string.key_receiveimageheight_preference))) {
            if (ConfigureUtils.isNumber(receiveImageHeight.getText())) {
                int parse;
                if (ConfigureUtils.isEven(parse = Integer.parseInt(receiveImageHeight.getText()))) {
                    listener.onReceiveImageHeightConfigured(parse);
                    receiveImageHeight.setSummary(receiveImageHeight.getText());
                } else {
                    Toast.makeText(getContext(), "奇数は設定できません", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "文字は設定できません", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            listener.onIsServerConfigured();
        }
    }

    public interface CommunicationConfigureEventListener{
        void onCommunicationConfigureViewCreated();
        void onReceiveImageWidthConfigured(int width);
        void onReceiveImageHeightConfigured(int height);
        void onIsServerConfigured();
    }
}
