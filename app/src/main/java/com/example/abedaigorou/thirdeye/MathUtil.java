package com.example.abedaigorou.thirdeye;

/**
 * Created by abedaigorou on 2017/09/27.
 */

public class MathUtil
{
    public static float map(float val,float fromHigh,float fromLow,float toHigh,float toLow){
        return (val-fromLow)*(toHigh-toLow)/(fromHigh-fromLow)+toLow;
    }
}
