package com.example.abedaigorou.thirdeye;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by abedaigorou on 2017/05/27.
 */

public class VRUtil
{
    public static FloatBuffer convert(float[] data){
        ByteBuffer bb=ByteBuffer.allocateDirect(data.length*4);
        bb.order(ByteOrder.nativeOrder());

        FloatBuffer floatbuffer=bb.asFloatBuffer();
        floatbuffer.put(data);
        floatbuffer.position(0);

        return floatbuffer;
    }

    public static ShortBuffer convert(short[] data) {
        ByteBuffer bb = ByteBuffer.allocateDirect(data.length * 2);
        bb.order(ByteOrder.nativeOrder());

        ShortBuffer shortBuffer = bb.asShortBuffer();
        shortBuffer.put(data);
        shortBuffer.position(0);

        return shortBuffer;
    }

    public static int loadShader(int type,String shaderCode) {
        //シェーダのコンパイル
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
