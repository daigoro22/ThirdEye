package com.example.abedaigorou.thirdeye.configure.CameraConfigure;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.CheckResult;
import android.util.Log;
import android.view.View;

import com.example.abedaigorou.thirdeye.R;
import com.example.abedaigorou.thirdeye.configure.ConfigureUtils;
import com.example.abedaigorou.thirdeye.configure.SeekbarPreference;

import java.util.ArrayList;

/**
 * Created by abedaigorou on 2017/08/29.
 */

public class CameraConfigureFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public  static String KEY_SIZE = "KEY_SIZE";
    private static String KEY_MAX_WIDTH="KEY_MAX_WIDTH";
    private static String KEY_MAX_HEIGHT="KEY_MAX_HEIGHT";
    private String[] sizes;
    private float maxFocus;
    private int maxWidth,maxHeight,defaultWidth,defaultHeight;
    private final String TAG = "CameraConfigureFragment";
    private CameraConfigureEventListener listener;
    private SharedPreferences sharedPreferences;

    @CheckResult
    public static CameraConfigureFragment createInstance(int maxWidth,int maxHeight) {
        CameraConfigureFragment fragment = new CameraConfigureFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_MAX_WIDTH,maxWidth);
        bundle.putInt(KEY_MAX_HEIGHT,maxHeight);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_camera);

        Bundle args = getArguments();

        if (args != null) {
            sizes = args.getStringArray(KEY_SIZE);
            maxWidth=args.getInt(KEY_MAX_WIDTH);
            maxHeight=args.getInt(KEY_MAX_HEIGHT);
        }
    }

    public void setImageSizes(String[] sizes){
        if(sizes!=null) {
            this.sizes=sizes;
            ListPreference list=(ListPreference)findPreference(getString(R.string.key_size_preference));
            int[] defsize= ConfigureUtils.getSplitedInt(sizes[sizes.length-1],"x");
            defaultWidth=defsize[0];
            defaultHeight=defsize[1];
            list.setDefaultValue(sizes[sizes.length-1]);
            list.setEntries(getAdjSizes());
            list.setEntryValues(getAdjSizes());
        }
    }

    public void setMaxFocus(float focus){
        if(focus!=-1f){
            this.maxFocus=focus;
            SeekbarPreference seek=(SeekbarPreference)findPreference(getString(R.string.key_focus_preference));
            seek.setMAX_PROGRESS((int)(focus*100));
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof CameraConfigureEventListener){
            this.listener=(CameraConfigureEventListener)context;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listener.onCameraConfigureViewCreated();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        sp.registerOnSharedPreferenceChangeListener(this);
        setSummaries(sp);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setSummaries(final SharedPreferences sp) {
        sharedPreferences=sp;
        ListPreference sizeList = (ListPreference) findPreference(getString(R.string.key_size_preference));
        ListPreference afList=(ListPreference)findPreference(getString(R.string.key_autofocus_preference));
        SeekbarPreference focusSeekbar=(SeekbarPreference)findPreference(getString(R.string.key_focus_preference));

        // 取得方法
        final String text = sp.getString(getString(R.string.key_text_preference),"");
        final String val=sp.getString(getString(R.string.key_autofocus_preference),"");

        findPreference(getString(R.string.key_text_preference)).setSummary(text+val);

        afList.setSummary(afList.getEntry());
        sizeList.setSummary(sizeList.getValue());
        focusSeekbar.setSummary("");

        listener.onAutoFocusConfigured(Integer.parseInt(afList.getValue()));
        listener.onFocusDistanceConfigured((float)focusSeekbar.getValue()/100);
        listener.onImageSizeConfigured(sizeList.getValue());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummaries(sharedPreferences);
    }

    @Override
    public void onSaveInstanceState(Bundle args){
        args.putStringArray(KEY_SIZE,sizes);
        args.putInt(KEY_MAX_WIDTH,maxWidth);
        args.putInt(KEY_MAX_HEIGHT,maxHeight);
        super.onSaveInstanceState(args);
    }

    private String[] getAdjSizes(){
        ArrayList<String> list=new ArrayList<>();
        int[] size;
        for(int count=0;count<sizes.length;count++){
            size=ConfigureUtils.getSplitedInt(sizes[count],"x");
            if(!(size[0]>maxWidth||size[1]>maxHeight)){
                list.add(sizes[count]);
            }
        }
        return list.toArray(new String[0]);
    }

    public void setEnabled(boolean enabled,int key){
        findPreference(getString(key)).setEnabled(enabled);
    }

    public void setEnabled(boolean enabled,String key){
        findPreference(key).setEnabled(enabled);
    }

    public int getAfMode(){
        return Integer.parseInt(((ListPreference)findPreference(getString(R.string.key_autofocus_preference))).getValue());
    }

    public int getWidth(){
        String val=((ListPreference)findPreference(getString(R.string.key_size_preference))).getValue();
        if(val==null){
            return defaultWidth;
        }
        int[] size=ConfigureUtils.getSplitedInt(val,"x");
        return size[0];
    }

    public int getHeight(){
        String val=((ListPreference)findPreference(getString(R.string.key_size_preference))).getValue();
        if(val==null){
            return defaultHeight;
        }
        int[] size=ConfigureUtils.getSplitedInt(val,"x");
        return size[1];
    }

    public interface CameraConfigureEventListener
    {
        void onCameraConfigureViewCreated();
        void onImageSizeConfigured(String imageSize);
        void onAutoFocusConfigured(int afMode);
        void onFocusDistanceConfigured(float focusdist);
    }
}
