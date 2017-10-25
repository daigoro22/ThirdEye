package com.example.abedaigorou.thirdeye;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by abedaigorou on 2017/09/20.
 */

public class ServoController implements Runnable
{
    private final int PWM_FREQUENCY=50;
    private int samplingRate;
    private int relativePulseFrequency;
    private AudioTrack audioTrack;
    private byte[] playingBuffer;
    private Handler playingThreadHandler;
    private HandlerThread playingThread;
    private int minBufferSize;
    private boolean isPlaying=false;
    private int wait,maxAngle,minAngle;
    private float minDuty,maxDuty;
    private final String TAG="Servo";

    public ServoController(int maxAngle,int minAngle, int wait, float minDuty, float maxDuty, int samplingRate){
        relativePulseFrequency=(samplingRate/PWM_FREQUENCY*2);
        //samplingRate=(int)(PWM_FREQUENCY*(1f/(maxDuty-minDuty))*controlAngle/resolusion)/2;
        this.samplingRate=samplingRate;
        playingBuffer=new byte[relativePulseFrequency];
        this.minDuty=minDuty;
        this.maxDuty=maxDuty;
        this.maxAngle=maxAngle;
        this.minAngle=minAngle;

        audioTrack=new AudioTrack(
                AudioManager.STREAM_MUSIC,//音楽ストリーム
                samplingRate,//サンプリングレート
                AudioFormat.CHANNEL_OUT_STEREO,//ステレオ再生
                AudioFormat.ENCODING_PCM_8BIT,//
                relativePulseFrequency,//バッファサイズ
                AudioTrack.MODE_STREAM);//ストリームモードで再生
        this.wait=wait;
    }

    public synchronized void setPwmDutyRatio(int angleR,int angleL){
        if(angleR>maxAngle || angleL>maxAngle || angleR<minAngle || angleL<minAngle || angleL<0 || angleR<0){
            return;
        }

        //音データ作製
        float tempR=MathUtil.map((float)angleR,180,0,2.4f,0.5f);
        float tempL=MathUtil.map((float)angleL,180,0,2.4f,0.5f);

        float dutyR=MathUtil.map(tempR,20f,0f,relativePulseFrequency,0);
        float dutyL=MathUtil.map(tempL,20f,0f,relativePulseFrequency,0);

        Log.i(TAG,String.valueOf(angleR)+":"+String.valueOf(angleL));
        for(int i=0;i<relativePulseFrequency;i+=2){
            if(i<dutyR){
                playingBuffer[i]=(byte)255;
            }else {
                playingBuffer[i] =(byte)0;
            }
            if(i<dutyL){
                playingBuffer[i+1]=(byte)255;
            }
            else{
                playingBuffer[i+1]=(byte)0;
            }
        }
    }

    private synchronized void writeAndPlay(){
        audioTrack.play();
        audioTrack.write(playingBuffer, 0, playingBuffer.length);
        audioTrack.stop();
    }

    @Override
    public void run() {
        while(isPlaying) {
            writeAndPlay();
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start(){
        if(!isPlaying) {
            isPlaying=true;
            playingThread = new HandlerThread("PlayingThread");
            playingThread.start();
            playingThreadHandler = new Handler(playingThread.getLooper());
            playingThreadHandler.post(this);
        }
    }

    public void stop(){
        if(isPlaying)
        {
            isPlaying=false;
        }
    }
}
