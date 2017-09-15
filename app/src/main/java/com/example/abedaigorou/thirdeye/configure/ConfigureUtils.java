package com.example.abedaigorou.thirdeye.configure;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.abedaigorou.thirdeye.R;

/**
 * Created by abedaigorou on 2017/08/31.
 */

public class ConfigureUtils
{
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

    public static int[] getConfiguredSize(Context context,SharedPreferences sp){
        String stsize=sp.getString(context.getString(R.string.key_size_preference),"");
        return getSplitedInt(stsize,"x");
    }

    public static int getConfiguredAFMode(Context context,SharedPreferences sp){
        return Integer.parseInt(sp.getString(context.getString(R.string.key_autofocus_preference),"0"));
    }
}
