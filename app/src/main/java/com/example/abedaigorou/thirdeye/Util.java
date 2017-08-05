package com.example.abedaigorou.thirdeye;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by abedaigorou on 2017/07/13.
 */

public class Util
{
    public static Handler GetNewHandler(String name){
        HandlerThread thread=new HandlerThread(name);
        thread.start();
        return new Handler(thread.getLooper());
    }
}
