package com.example.abedaigorou.thirdeye;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.opencv.core.Mat;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import static android.opengl.GLES10.GL_CULL_FACE;
import static android.opengl.GLES20.GL_TEXTURE_2D;

public class VRActivity extends GvrActivity implements GvrView.StereoRenderer{
    private static final String TAG="VRRobot";
    private static final float CAMERA_Z = 25f;
    private static VRActivity instance;
    public static final String sVertexShaderSource =
                    //"uniform mat4 wMatrix;" +
                    "attribute vec2 A_texture_uv;"+
                    "uniform mat4 sMatrix;"+
                    "uniform mat4 vpMatrix;" +
                    "attribute vec3 position;" +
                    "varying vec2 V_texture_uv;"+
                    "void main() {" +
                    "  gl_Position = vpMatrix * sMatrix * vec4(position, 1.0);" +
                    "  V_texture_uv=A_texture_uv;"+
                    "}";
    public static final String sFragmentShaderSource =
            //"#extension GL_OES_EGL_image_external : require \n"+
            "precision mediump float;" +
            //"uniform samplerExternalOES texture0;"+
            "uniform sampler2D texture0;"+
            "varying vec2 V_texture_uv;"+
            "void main() {" +
                "gl_FragColor = (texture2D(texture0,V_texture_uv));"+
                //"gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" +
            "}";

    private final float Z_NEAR=0.1f;
    private final float Z_FAR=100.0f;
    private int cubeProgram;

    private int drawPositionParam;
    private int vpPositionParam;
    private int scaleParam;
    private int cubeTextureVertexParam;
    private int textureZeroParam;

    private int[] cubeTextureID=new int[1];

    FloatBuffer drawVertexBuffer;
    FloatBuffer cubeTextureBuffer;
    ShortBuffer cubeIndexBuffer;


    private float[] sphereVertex;
    private Sphere sphere=new Sphere(14,14,10);
    private float[] camera=new float[16];//カメラ
    private float[] view=new float[16];//ビュー座標変換行列
    private float[] modelView=new float[16];
    private float[] modelViewProjection=new float[16];//透視投影変換行列
    private float[] translate=new float[16];
    float[] scale=new float[16];
    float[] rotate=new float[16];
    private int frameCount=0;
    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_ui);
        GvrView gvrView=(GvrView)findViewById(R.id.gvr_view);
        instance=this;
        gvrView.setEGLConfigChooser(8,8,8,8,16,1);//RGBA、デプスバッファ、ステンシルバッファのサイズ
        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);

        // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
        // Daydream controller input for basic interactions using the existing Cardboard trigger API.
        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }
        setGvrView(gvrView);
    }


    @Override
    public void onNewFrame(HeadTransform headTransform) {
        Log.i(TAG,"onNewFrame");
        //ビュー座標変換行列
        Matrix.setLookAtM(camera,0,0f,0f,CAMERA_Z,0f,0f,0f,0f,1.0f,0f);
        checkGLError("onReadyToDraw");
    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GL_CULL_FACE);

        GLES20.glUseProgram(cubeProgram);
        Log.i(TAG,"onDrawEye");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        float[] perspective=eye.getPerspective(Z_NEAR,Z_FAR);

        Matrix.setIdentityM(scale,0);
        Matrix.setIdentityM(rotate,0);
        Matrix.scaleM(scale,0,50,50,50);
        Matrix.rotateM(rotate,0,90,0,0,1);
        Matrix.multiplyMM(scale,0,rotate,0,scale,0);
        /*Matrix.setIdentityM(translate,0);
        Matrix.rotateM(translate,0,frameCount,0,1,0);
        Matrix.multiplyMM(scale,0,scale,0,translate,0);*/

        //目の動きをカメラに適用
        Matrix.multiplyMM(view,0,eye.getEyeView(),0,camera,0);
        //ビュー座標変換
        //Matrix.multiplyMM(modelView,0,view,0,objectDatas.cubeVertices,0);
        //透視投影変換
        Matrix.multiplyMM(modelViewProjection,0,perspective,0,view,0);

        //オブジェクト座標代入
        //有効化
        GLES20.glEnableVertexAttribArray(drawPositionParam);
        GLES20.glEnableVertexAttribArray(cubeTextureVertexParam);


        //テクスチャアクティブ
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //テクスチャのバインド
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cubeTextureID[0]);
        //texture0に0を代入
        GLES20.glUniform1i(textureZeroParam,0);
        checkGLError("Drawing cube");

        //テクスチャ画像更新
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,imageBitmap,0);

        //GLES20.glVertexAttribPointer(cubePositionParam,3,GLES20.GL_FLOAT,false,0,cb);
        GLES20.glVertexAttribPointer(drawPositionParam,3,GLES20.GL_FLOAT,false,0,drawVertexBuffer);
        GLES20.glUniformMatrix4fv(vpPositionParam,1,false,modelViewProjection,0);
        GLES20.glUniformMatrix4fv(scaleParam,1,false,scale,0);
        GLES20.glVertexAttribPointer(cubeTextureVertexParam,2,GLES20.GL_FLOAT,false,0,cubeTextureBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES,cubeIndexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, cubeIndexBuffer);
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,objectDatas.cubeVertices.length/3);


        //無効化
        GLES20.glDisableVertexAttribArray(drawPositionParam);
        GLES20.glDisableVertexAttribArray(cubeTextureVertexParam);

        checkGLError("Drawing cube");

        /*imageBitmap.recycle();
        imageBitmap = null;*/
        frameCount++;
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
        Log.i(TAG,"onFinishFrame");
    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
        Log.i(TAG,"onSurfaceChanged");
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        Log.i(TAG,"onSurfaceCreated");
        GLES20.glClearColor(0f, 0f, 1f,1f);

        sphere.build(true);

        drawVertexBuffer=VRUtil.convert(sphere.getCoordinates());

        //テクスチャ座標バッファ
        cubeTextureBuffer=VRUtil.convert(sphere.getTexCoordinates());

        cubeIndexBuffer=VRUtil.convert(sphere.getIndex());

        //シェーダ読み込み
        int vertexShader=VRUtil.loadShader(GLES20.GL_VERTEX_SHADER,sVertexShaderSource);
        int fragmentShader=VRUtil.loadShader(GLES20.GL_FRAGMENT_SHADER,sFragmentShaderSource);
        cubeProgram=GLES20.glCreateProgram();
        GLES20.glAttachShader(cubeProgram,vertexShader);
        GLES20.glAttachShader(cubeProgram,fragmentShader);
        GLES20.glLinkProgram(cubeProgram);
        GLES20.glUseProgram(cubeProgram);
        checkGLError("Cube program");

        //ポインタ取得
        drawPositionParam=GLES20.glGetAttribLocation(cubeProgram,"position");
        vpPositionParam=GLES20.glGetUniformLocation(cubeProgram,"vpMatrix");
        scaleParam=GLES20.glGetUniformLocation(cubeProgram,"sMatrix");
        cubeTextureVertexParam=GLES20.glGetAttribLocation(cubeProgram,"A_texture_uv");
        textureZeroParam=GLES20.glGetUniformLocation(cubeProgram,"texture0");
        checkGLError("Cube program params");

        GLES20.glEnable(GL_CULL_FACE);
        //テクスチャの生成
        GLES20.glGenTextures(1,cubeTextureID,0);

        //テクスチャアクティブ
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //テクスチャのバインド
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cubeTextureID[0]);
        //GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,cubeTextureID[0]);

        //ビットマップの生成
        imageBitmap= BitmapFactory.decodeResource(getResources(),R.raw.serval);
        //cubeBmp=Bitmap.createScaledBitmap(cubeBmp,32,32,false);

        /*GLES20.glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GLES20.glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_NEAREST);*/
        // 縮小時の補間設定
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        // 拡大時の補間設定
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //cubeBmpをテクスチャ0に設定
        //GLUtils.texImage2D(GL_TEXTURE_2D,0,cubeBmp,0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,imageBitmap,0);

        imageBitmap.recycle();
        imageBitmap = null;
    }

    @Override
    public void onRendererShutdown() {
        Log.i(TAG,"onRendererShutdown");
    }

    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    public void setImageBitmap(Bitmap bmp){
        this.imageBitmap=bmp;
    }

    public static VRActivity getInstance(){
        return instance;
    }
}
