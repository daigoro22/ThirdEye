package com.example.abedaigorou.thirdeye.configure;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.example.abedaigorou.thirdeye.R;

import java.util.regex.Pattern;

/**
 * Created by abedaigorou on 2017/08/31.
 */

public class ConfigureUtils
{
    public static Preference.OnPreferenceChangeListener getIsUDPPacketSizeListener(final Context context){
        return new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue instanceof String) {
                    String stNewValue=(String)newValue;
                    if (ConfigureUtils.isNumber(stNewValue)) {
                        if (Integer.parseInt(stNewValue)<=64000) {
                            preference.setSummary(stNewValue);
                            return true;
                        } else {
                            Toast.makeText(context, "64000byte以上は設定できません", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    } else {
                        Toast.makeText(context, "文字は設定できません", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else{
                    return false;
                }
            }
        };
    }


    public static Preference.OnPreferenceChangeListener getIsEvenListener(final Context context){
        return new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue instanceof String) {
                    String stNewValue=(String)newValue;
                    if (ConfigureUtils.isNumber(stNewValue)) {
                        int parse;
                        if (ConfigureUtils.isEven(parse = Integer.parseInt(stNewValue))) {
                            preference.setSummary(stNewValue);
                            return true;
                        } else {
                            Toast.makeText(context, "奇数は設定できません", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    } else {
                        Toast.makeText(context, "文字は設定できません", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else{
                    return false;
                }
            }
        };
    }

    public static Preference.OnPreferenceChangeListener getIsIpAddrListener(final Context context){
        return new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue instanceof String) {
                    String stNewValue=(String)newValue;
                    if (ConfigureUtils.isIpAddr(stNewValue)) {
                        preference.setSummary(stNewValue);
                        return true;
                    } else {
                        Toast.makeText(context, "IPアドレスのみ設定できます", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else{
                    return false;
                }
            }
        };
    }

    public static Preference.OnPreferenceChangeListener getIsPortListener(final Context context){
        return new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue instanceof String) {
                    String stNewValue=(String)newValue;
                    if (ConfigureUtils.isNumber(stNewValue)) {
                        preference.setSummary(stNewValue);
                        return true;
                    } else {
                        Toast.makeText(context, "0~65535の整数のみ設定できます", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else{
                    return false;
                }
            }
        };
    }

    public static int[] getSplitedInt(String split,String devide){
        int[] values=new int[2];
        String[] splited=split.split(devide,2);
        if(split.length()<2){
            return new int[1];
        }
        values[0]=Integer.parseInt(splited[0]);
        values[1]=Integer.parseInt(splited[1]);
        return values;
    }

    public static int[] getConfiguredSize(Context context){
        SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(context);
        String stsize=sp.getString(context.getString(R.string.key_size_preference),"");
        return getSplitedInt(stsize,"x");
    }

    public static boolean getConfiguredIsServer(Context context){
        SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.key_isServer_preference),false);
    }

    public static int getConfiguredIntValue(Context context,int key,String defValue){
        SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(context);
        if(key==R.string.key_size_preference){
            return -1;
        }
        return Integer.parseInt(sp.getString(context.getString(key),defValue));
    }

    public static String getConfiguredStringValue(Context context,int key,String defValue){
        SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getString(key),defValue);
    }

    public static boolean isNumber(String num){
        String regex="\\d";
        Pattern p= Pattern.compile(regex);
        return p.matcher(num).find();
    }

    public static boolean isIpAddr(String addr){
        String regex="^(\\d){1,3}.(\\d){1,3}.(\\d){1,3}.(\\d){1,3}$";
        Pattern p=Pattern.compile(regex);
        return p.matcher(addr).find();
    }


    public static boolean isEven(int num){
        if(num%2==0){
            return true;
        }else{
            return false;
        }
    }
}
