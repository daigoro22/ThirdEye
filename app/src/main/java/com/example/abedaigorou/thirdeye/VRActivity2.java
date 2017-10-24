package com.example.abedaigorou.thirdeye;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;

import static android.opengl.GLES10.GL_CULL_FACE;
import static android.opengl.GLES10.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_TEXTURE_2D;

public class VRActivity2 extends GvrActivity implements GvrView.StereoRenderer{
    private static final String TAG="VRRobot";
    public final static String INTENTTAG_WIDTH="Intent_VRActivity_WIDTH";
    public final static String INTENTTAG_HEIGHT="Intent_VRActivity_HEIGHT";

    private static final float CAMERA_Z = -14f;//10;
    private static VRActivity2 instance;
    public static final String sVertexShaderSource =
                    //"uniform mat4 wMatrix;" +
                    "uniform mat4 sMatrix;"+
                    "uniform mat4 vpMatrix;" +
                    "attribute vec3 position;" +
                    "attribute vec2 A_texture_uv;"+
                    "varying vec2 V_texture_uv;"+
                    "void main() {" +
                    "  gl_Position = vpMatrix * sMatrix * vec4(position, 1.0);" +
                    "  V_texture_uv=A_texture_uv;"+
                    "}";
    public static final String sFragmentShaderSource =
            //"#extension GL_OES_EGL_image_external : require \n"+
            "precision mediump float;" +
            //"uniform samplerExternalOES texture0;"+
            "uniform sampler2D textureY;"+
            "uniform sampler2D textureU;"+
            "uniform sampler2D textureV;"+
            "varying vec2 V_texture_uv;"+
            "const mat3 convert = mat3( 1.164, 1.164, 1.164, 0.0, -0.213, 2.112, 1.793, -0.533, 0.0 ); "+
            "vec3 yuv;"+
            "vec3 rgb;"+
            "void main() {" +
                "yuv.x = (texture2D(textureY,V_texture_uv).x - (16.0 / 255.0));"+
                "yuv.y = (texture2D(textureU,V_texture_uv).x - 0.5);"+
                "yuv.z = (texture2D(textureV,V_texture_uv).x - 0.5);"+
                "rgb=convert*yuv;"+
                "gl_FragColor = vec4(rgb,1.0);"+
            "}";

    private final float Z_NEAR=0.1f;
    private final float Z_FAR=100.0f;
    private int width,height,imageSize;
    private int cubeProgram;

    private int drawPositionParam;
    private int vpPositionParam;
    private int scaleParam;
    private int cubeTextureVertexParam;
    private int textureYParam,textureUParam,textureVParam,vboParam,iboParam,texboParam;

    private int[] cubeTextureID=new int[3];

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
    float[] eularAngle=new float[3];
    float[] headAngle=new float[3];
    byte[] dataY,dataU,dataV;
    private int frameCount=0;
    private Bitmap imageBitmap;
    private ServoController sc;
    private InputStream inYUV;
    private ByteBuffer texYBuffer,texUBuffer,texVBuffer;
    private Bitmap reinokao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*inYUV=getResources().openRawResource(R.raw.data_yuv);
        try {
            inYUV.read(dataY,0,YUV_SIZE);
            inYUV.read(dataU,0,YUV_SIZE/4);
            inYUV.read(dataV,0,YUV_SIZE/4);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        super.onCreate(savedInstanceState);
        Bundle imageSizeBundle=getIntent().getExtras();
        width=imageSizeBundle.getInt(INTENTTAG_WIDTH);
        height=imageSizeBundle.getInt(INTENTTAG_HEIGHT);
        imageSize=width*height;

        dataY=new byte[imageSize];
        dataU=new byte[imageSize/4];
        dataV=new byte[imageSize/4];

        setContentView(R.layout.common_ui);
        GvrView gvrView=(GvrView)findViewById(R.id.gvr_view);
        sc=new ServoController(180,10,5,24,48000);
        sc.start();

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
        texYBuffer=ByteBuffer.wrap(dataY);
        texUBuffer=ByteBuffer.wrap(dataU);
        texVBuffer=ByteBuffer.wrap(dataV);

        /*texYBuffer.put(dataY);
        texUBuffer.put(dataU);
        texVBuffer.put(dataV);*/

        texYBuffer.position(0);
        texVBuffer.position(0);
        texUBuffer.position(0);

        setGvrView(gvrView);
    }


    @Override
    public void onNewFrame(HeadTransform headTransform) {
        Log.i(TAG,"onNewFrame");
        //ビュー座標変換行列
        Matrix.setLookAtM(camera,0,0f,0f,CAMERA_Z,0f,0f,-100f,0f,1.0f,0f);
        headTransform.getEulerAngles(eularAngle,0);
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
        Matrix.scaleM(scale,0,50,50,50);

        /*//切れ目があるため、回転
        Matrix.setIdentityM(rotate,0);
        Matrix.rotateM(rotate,0,1,0,1,0);
        Matrix.multiplyMM(scale,0,rotate,0,scale,0);*/

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
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //texture0に0を代入
        GLES20.glUniform1i(textureYParam,0);
        //テクスチャ画像更新
        //GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,imageBitmap,0);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D,0,0,0,width,height,GLES20.GL_LUMINANCE,GL_UNSIGNED_BYTE,texYBuffer);
        //GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width,height,0,GLES20.GL_LUMINANCE,GL_UNSIGNED_BYTE,texYBuffer);
        checkGLError("Drawing cube");

        //テクスチャ1
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cubeTextureID[1]);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glUniform1i(textureUParam,1);
        //GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width/2,height/2,0,GLES20.GL_LUMINANCE,GL_UNSIGNED_BYTE,texUBuffer);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D,0,0,0,width/2,height/2,GLES20.GL_LUMINANCE,GL_UNSIGNED_BYTE,texUBuffer);
        checkGLError("Drawing cube");

        //テクスチャ2
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cubeTextureID[2]);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glUniform1i(textureVParam,2);
        //GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width/2,height/2,0,GLES20.GL_LUMINANCE,GL_UNSIGNED_BYTE,texVBuffer);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D,0,0,0,width/2,height/2,GLES20.GL_LUMINANCE,GL_UNSIGNED_BYTE,texVBuffer);

        //GLES20.glVertexAttribPointer(cubePositionParam,3,GLES20.GL_FLOAT,false,0,cb);

        //vbo
        GLES20.glBindBuffer(GL_ARRAY_BUFFER,vboParam);
        GLES20.glEnableVertexAttribArray(drawPositionParam);
        GLES20.glVertexAttribPointer(drawPositionParam,3,GLES20.GL_FLOAT,false,0,0);
        GLES20.glBindBuffer(GL_ARRAY_BUFFER,0);

        //texbo
        GLES20.glBindBuffer(GL_ARRAY_BUFFER,texboParam);
        GLES20.glEnableVertexAttribArray(cubeTextureVertexParam);
        GLES20.glVertexAttribPointer(cubeTextureVertexParam,2,GLES20.GL_FLOAT,false,0,0);

        //初期化
        GLES20.glBindBuffer(GL_ARRAY_BUFFER,0);

        //GLES20.glVertexAttribPointer(drawPositionParam,3,GLES20.GL_FLOAT,false,0,drawVertexBuffer);
        GLES20.glUniformMatrix4fv(vpPositionParam,1,false,modelViewProjection,0);
        GLES20.glUniformMatrix4fv(scaleParam,1,false,scale,0);
        //GLES20.glVertexAttribPointer(cubeTextureVertexParam,2,GLES20.GL_FLOAT,false,0,cubeTextureBuffer);


        //GLES20.glDrawElements(GLES20.GL_TRIANGLES,cubeIndexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, cubeIndexBuffer);

        //iboバインド
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,iboParam);
        //描画
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,cubeIndexBuffer.capacity(),GLES20.GL_UNSIGNED_SHORT,0);
        //後片付け
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        checkGLError("Drawing cube");

        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,objectDatas.cubeVertices.length/3);


        //無効化
        GLES20.glDisableVertexAttribArray(drawPositionParam);
        GLES20.glDisableVertexAttribArray(cubeTextureVertexParam);
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
        GLES20.glClearColor(0f, 1f, 0f,1f);

        sphere.build(true,true,true);

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
        textureYParam=GLES20.glGetUniformLocation(cubeProgram,"textureY");
        textureUParam=GLES20.glGetUniformLocation(cubeProgram,"textureU");
        textureVParam=GLES20.glGetUniformLocation(cubeProgram,"textureV");

        checkGLError("Cube program params");

        GLES20.glEnable(GL_CULL_FACE);
        //テクスチャの生成
        GLES20.glGenTextures(3,cubeTextureID,0);

        int[] boParam=new int[3];
        //バッファの生成
        GLES20.glGenBuffers(3,boParam,0);
        vboParam=boParam[0];
        iboParam=boParam[1];
        texboParam=boParam[2];

        //バッファをバインド
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER,iboParam);
        //バッファにデータ転送
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER,2*cubeIndexBuffer.capacity(),cubeIndexBuffer,GLES20.GL_STATIC_DRAW);
        //後片付け
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        //vboバインド
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vboParam);
        //バッファにデータ転送
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,4*drawVertexBuffer.capacity(),drawVertexBuffer,GLES20.GL_STATIC_DRAW);
        //後片付け
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

        //texboバインド
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,texboParam);
        //バッファにデータ転送
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,4*cubeTextureBuffer.capacity(),cubeTextureBuffer,GLES20.GL_STATIC_DRAW);
        //後片付け
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0);

        //テクスチャ0
        //テクスチャアクティブ
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //テクスチャのバインド
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cubeTextureID[0]);
        // 縮小時の補間設定
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        // 拡大時の補間設定
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width,height,0,GLES20.GL_LUMINANCE,GL_UNSIGNED_BYTE,texYBuffer);
        checkGLError("Cube program params");

        //テクスチャ1
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cubeTextureID[1]);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width/2,height/2,0,GLES20.GL_LUMINANCE,GL_UNSIGNED_BYTE,texUBuffer);
        //GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,imageBitmap,0);
        checkGLError("Cube program params");

        //テクスチャ2
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cubeTextureID[2]);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_LUMINANCE,width/2,height/2,0,GLES20.GL_LUMINANCE,GL_UNSIGNED_BYTE,texVBuffer);
        checkGLError("Cube program params");

        /*
        imageBitmap.recycle();
        imageBitmap = null;*/
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

    public float[] getHeadAngle(){
        headAngle[0]=MathUtil.map(eularAngle[0],(float)Math.PI/2,-(float)Math.PI/2,180,0);
        headAngle[1]=MathUtil.map(eularAngle[1],(float)Math.PI,-(float)Math.PI,360,0);
        headAngle[2]=MathUtil.map(eularAngle[2],(float)Math.PI,-(float)Math.PI,360,0);
        //Log.i("angle","x:"+String.valueOf(headAngle[0])+"y:"+String.valueOf(headAngle[1])+"z:"+String.valueOf(headAngle[2]));
        return headAngle;
    }

    public void setImageData(byte[] getter){
        if(getter==null||dataY==null||dataU==null||dataV==null)
            return;
        System.arraycopy(getter,0,dataY,0,dataY.length);
        System.arraycopy(getter,dataY.length,dataU,0,dataU.length);
        System.arraycopy(getter,dataY.length+dataU.length,dataV,0,dataV.length);
    }

    public void setImageBitmap(Bitmap bmp){
        this.imageBitmap=bmp;
    }

    public static VRActivity2 getInstance(){
        return instance;
    }
}
