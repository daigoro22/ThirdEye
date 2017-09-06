package com.example.abedaigorou.thirdeye;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES10.GL_CULL_FACE;
import static android.opengl.GLES20.GL_BACK;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by abedaigorou on 2017/07/20.
 */

public class Sphere
{
    private int slices;//経度
    private int stacks;//緯度
    private float radius;
    private float[] coordinates;
    private float[] texCoordinates;
    /*private float[] rotate=new float[16];
    private float[] camera=new float[16];
    private float[] frustum=new float[16];
    private int[] cubeTextureID=new int[1];
    private float CAMERA_Z=2.5f;
    private float CAMERA_X=0f;
    private float CAMERA_Y=0f;*/
    private short[] index;
    private String TAG="sphere";
    int coordinatesCount=0,indexCount=0,texCoordCount=0,verticesNum,indexNum,screenWidth,screenHeight;

    /*
    //シェーダ
    //vPositionは絶対右辺
    public final String vertexShaderCode=
            "attribute vec4 vPosition;"+
            "attribute vec2 A_texture_uv;"+
            "uniform mat4 vpMatrix;"+
            "varying vec2 V_texture_uv;"+
                    "void main(){"
                        + "gl_Position=vpMatrix*vPosition;"+
                        "  V_texture_uv=A_texture_uv;"+
                    "}";

    public final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform sampler2D texture0;"+
            "varying vec2 V_texture_uv;"+
                    "void main() {" +
                    "  gl_FragColor =(texture2D(texture0,V_texture_uv));" +
                    "}";

    private static int loadShader(int type,String shaderCode) {
        int shader=GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        // コンパイルチェック
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(" loadShader", "Failed in Compilation");
            Log.e(" loadShader", GLES20.glGetShaderInfoLog(shader));
            return -1;
        }
        return shader;
    }*/

    private int shaderProgram;

    public Sphere(int slices, int stacks, float radius/*, int screenWidth, int screenHeight*/){
        this.slices=slices;
        this.stacks=stacks;
        this.radius=radius;
        this.screenWidth=screenWidth;
        this.screenHeight=screenHeight;

        verticesNum=(slices+1)*(stacks+1);
        indexNum=6*slices*stacks;

        coordinates=new float[3*verticesNum];
        texCoordinates=new float[2*verticesNum];

        index=new short[indexNum];

        /*int vertexShader=loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderCode);
        int fragmentShader=loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderCode);

        shaderProgram=GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram,vertexShader);
        GLES20.glAttachShader(shaderProgram,fragmentShader);
        GLES20.glLinkProgram(shaderProgram);

        //テクスチャの生成
        GLES20.glGenTextures(1,cubeTextureID,0);

        //テクスチャアクティブ
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //テクスチャのバインド
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cubeTextureID[0]);

        // 縮小時の補間設定
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        // 拡大時の補間設定
        GLES20.glTexParameteri(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);


        Matrix.setLookAtM(camera,0,CAMERA_X,CAMERA_Y,CAMERA_Z,0,0,0,0,1.0f,0);
        float ratio=(float)screenWidth/screenHeight;
        Matrix.frustumM(frustum,0,-ratio,ratio,-1,1,1,10);
        Matrix.multiplyMM(camera,0,frustum,0,camera,0);

        checkGLError("コンストラクタA");

    }*/
/*
    public void draw(Bitmap imageBitmap){
        GLES20.glUseProgram(shaderProgram);
        //GLES20.glEnable(GL_CULL_FACE);
        GLES20.glEnable(GL_CULL_FACE);
        GLES20.glCullFace(GL_BACK);


        int positionAttrib=GLES20.glGetAttribLocation(shaderProgram,"vPosition");
        int vpUnif=GLES20.glGetUniformLocation(shaderProgram,"vpMatrix");

        int textureVertexAttrib=GLES20.glGetAttribLocation(shaderProgram,"A_texture_uv");
        int texture0Unif=GLES20.glGetUniformLocation(shaderProgram,"texture0");

        GLES20.glEnableVertexAttribArray(positionAttrib);
        GLES20.glEnableVertexAttribArray(textureVertexAttrib);

        /*faces=new float[]{
                0.0f, 0.5f, 0.0f,//三角形の点A(x,y,z)
                -0.5f, -0.5f, 0.0f,//三角形の点B(x,y,z)
                0.5f, -0.5f, 0.0f//三角形の点C(x,y,z)
        };
        FloatBuffer coordBuffer=VRUtil.convert(coordinates);

        ShortBuffer indexBuffer=VRUtil.convert(index);

        FloatBuffer texCoordBuffer=VRUtil.convert(texCoordinates);

        //テクスチャアクティブ
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //テクスチャのバインド
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,cubeTextureID[0]);
        //texture0に0を代入
        GLES20.glUniform1i(texture0Unif,0);

        //テクスチャ画像更新
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,imageBitmap,0);

        GLES20.glVertexAttribPointer(positionAttrib,3, GLES20.GL_FLOAT, false, 0, coordBuffer);
        GLES20.glVertexAttribPointer(textureVertexAttrib,2,GLES20.GL_FLOAT,false,0,texCoordBuffer);

        Matrix.setIdentityM(rotate,0);
        Matrix.rotateM(rotate,0,1f,0,1,0);
        Matrix.multiplyMM(camera,0,camera,0,rotate,0);

        GLES20.glUniformMatrix4fv(vpUnif,1,false,camera,0);
        GLES20.glUniform1f(texture0Unif,0);

        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,coordinates.length/3);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,indexCount,GLES20.GL_UNSIGNED_SHORT,indexBuffer);//UNSIGNED_SHORTじゃないとerror1280

        GLES20.glDisableVertexAttribArray(positionAttrib);
        GLES20.glDisableVertexAttribArray(textureVertexAttrib);*/
    }


    private void addVertex(float x,float y,float z){
        coordinates[coordinatesCount++]=x;
        coordinates[coordinatesCount++]=y;
        coordinates[coordinatesCount++]=z;
    }

    private void calcIndex(boolean isInsideOut){
        for(short i=0;i<stacks;i++){
            for(short j=0;j<slices;j++){
                short count=(short)((slices+1)*i+j);
                if(isInsideOut){
                    index[indexCount++]=count;
                    index[indexCount++]=(short)(count+slices+1);
                    index[indexCount++]=(short)(count+slices+2);

                    //上半分
                    index[indexCount++]=count;
                    index[indexCount++]=(short)(count+slices+2);
                    index[indexCount++]=(short)(count+1);
                }else {
                    //下半分
                    index[indexCount++] = count;
                    index[indexCount++] = (short) (count + slices + 2);
                    index[indexCount++] = (short) (count + slices + 1);

                    //上半分
                    index[indexCount++] = count;
                    index[indexCount++] = (short) (count + 1);
                    index[indexCount++] = (short) (count + slices + 2);
                }
            }
        }
        showLog(index,"index",3);
    }

    private void addTexCoord(float u,float v){
        texCoordinates[texCoordCount++]=u;
        texCoordinates[texCoordCount++]=v;
        //Log.i(TAG,String.valueOf(u)+":"+String.valueOf(v));
    }

    private void calcTextureCoords(boolean isRotate){
        float u,v=0,radU,radV;
        int count;
        for(int i=0;i<=stacks;i++) {
            for (int j = 0; j <=slices; j++) {
                count = 3 * ((slices+1) * i + j);
                radU=(float)atan2(coordinates[count + 2],coordinates[count]);//((coordinates[count]==0&&coordinates[count+2]==0)? 0:atan(coordinates[count + 2]/coordinates[count]));
                radV=(float)asin(coordinates[count+1]);
                //radV= (float)atan2(coordinates[count + 1] ,sqrt(pow(coordinates[count], 2) + pow(coordinates[count + 2], 2)));
                //((coordinates[count]==0&&coordinates[count+1]==0&&coordinates[count+2]==0)?0:atan(coordinates[count + 1] /sqrt(pow(coordinates[count], 2) + pow(coordinates[count + 2], 2))));

                u =1-(float) ((1/(2 * PI))*radU+0.5);//1/2だとintで0になってしまう
                v =1-(float) ((1/ PI) *radV+0.5);
                addTexCoord((isRotate)?v:u,(isRotate)?u:v);
                /*if(j==slices&&!(i==0||i==stacks)){
                    addTexCoord(0,v);
                }else{
                    addTexCoord(1 - u, 1 - v);
                }*/
            }
            //addTexCoord(0,(float)i/stacks);
        }

        showLog(texCoordinates,"texCoordinates",2);
    }

    public void build(boolean isInsideOut,boolean isRotate){
        float xBuffer=0f,yBuffer=0f,zBuffer=0f,r,ph,th;
        for(int i=0;i<=stacks;i++){
            ph=(float)PI*i/stacks;
            yBuffer=(float)cos(ph);
            r=(float)sin(ph);
            for(int j=0;j<=slices;j++){
                th=2*(float)PI*j/slices;
                xBuffer=r*(float)cos(th);
                zBuffer=r*(float)sin(th);
                addVertex(xBuffer, yBuffer, zBuffer);
            }
        }
        showLog(coordinates,"coordinates",3);
        Log.i(TAG,"coordinates:"+String.valueOf(coordinatesCount));
        calcIndex(isInsideOut);
        calcTextureCoords(isRotate);
    }
    
    public float[] getCoordinates(){
        return coordinates;
    }

    public float[] getTexCoordinates(){
        return texCoordinates;
    }

    public short[] getIndex(){
        return index;
    }

    private void showLog(float[] val,String tag,int split){
        Log.i(TAG,tag);
        for(int c=0;c<val.length;c++){
            if(c%split==0){
                Log.i(TAG,"--------------------------------");
            }
            Log.i(TAG,String.valueOf(val[c]));
        }
    }

    private void showLog(short[] val,String tag,int split){
        Log.i(TAG,tag);
        for(int c=0;c<val.length;c++){
            if(c%split==0){
                Log.i(TAG,"--------------------------------");
            }
            Log.i(TAG,String.valueOf(val[c]));
        }
    }

    private void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }
}
