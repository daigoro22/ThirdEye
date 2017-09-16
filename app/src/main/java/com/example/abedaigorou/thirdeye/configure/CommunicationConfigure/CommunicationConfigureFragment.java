package com.example.abedaigorou.thirdeye.configure.CommunicationConfigure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.abedaigorou.thirdeye.R;
import com.example.abedaigorou.thirdeye.configure.ConfigureUtils;

/**
 * Created by abedaigorou on 2017/09/15.
 */

public class CommunicationConfigureFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private EditTextPreference receiveImageWidth,receiveImageHeight,ipAddr,port;
    private final String TAG="ComConfigureFragment";
    private CommunicationConfigureEventListener listener;
    private Preference.OnPreferenceChangeListener isEvenListener,isIpAddrListenter,isPortListener;
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
        receiveImageWidth=(EditTextPreference)findPreference(getString(R.string.key_receiveimagewidth_preference));
        receiveImageHeight=(EditTextPreference)findPreference(getString(R.string.key_receiveimageheight_preference));
        ipAddr=(EditTextPreference)findPreference(getString(R.string.key_ipaddr_preference));
        port=(EditTextPreference)findPreference(getString(R.string.key_port_preference));


        isEvenListener=ConfigureUtils.getIsEvenListener(getContext());

        isIpAddrListenter=ConfigureUtils.getIsIpAddrListener(getContext());

        isPortListener=ConfigureUtils.getIsPortListener(getContext());

        receiveImageWidth.setOnPreferenceChangeListener(isEvenListener);
        receiveImageHeight.setOnPreferenceChangeListener(isEvenListener);
        ipAddr.setOnPreferenceChangeListener(isIpAddrListenter);
        port.setOnPreferenceChangeListener(isPortListener);
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
        Log.i(TAG,receiveImageWidth.getText());
        if(key.equals(getString(R.string.key_receiveimagewidth_preference))) {
            listener.onReceiveImageWidthConfigured(Integer.parseInt(receiveImageWidth.getText()));
            receiveImageWidth.setSummary(receiveImageWidth.getText());
        }

        else if(key.equals(getString(R.string.key_receiveimageheight_preference))) {
            listener.onReceiveImageHeightConfigured(Integer.parseInt(receiveImageHeight.getText()));
            receiveImageHeight.setSummary(receiveImageHeight.getText());
        }

        else if(key.equals(getString(R.string.key_isServer_preference))){
            listener.onIsServerConfigured();
        }
        else if(key.equals(getString(R.string.key_ipaddr_preference))){
            listener.onIpAddrConfigured(ipAddr.getText());
            ipAddr.setSummary(ipAddr.getText());
        }
        else{
            listener.onPortConfigured(Integer.parseInt(port.getText()));
            port.setSummary(port.getText());
        }
    }

    public interface CommunicationConfigureEventListener{
        void onCommunicationConfigureViewCreated();
        void onReceiveImageWidthConfigured(int width);
        void onReceiveImageHeightConfigured(int height);
        void onIpAddrConfigured(String addr);
        void onPortConfigured(int port);
        void onIsServerConfigured();
    }
}
