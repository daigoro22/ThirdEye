package com.example.abedaigorou.thirdeye;

/**
 * Created by abedaigorou on 2017/05/28.
 */

public class objectDatas
{
    //キューブの座標
    public final static float cubeVertices[] = {
            // Front face
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,

            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,

           /* // Right face
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,

            // Back face
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,

            // Left face
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,

            // Top face
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,

            // Bottom face
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,*/
    };

    //テクスチャ座標
    public final static float cubeTexture[]={
            0f,1f,
            1f,1f,
            0f,0f,
            1f,1f,
            1f,0f,
            0f,0f
            /*0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0f,


            /*0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0f,


            0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0f,


            0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0f,


            0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0f,


            0f,0f,
            0f,1f,
            1f,0f,
            0f,1f,
            1f,1f,
            1f,0f*/
    };

    //インデックス座標
    public final static short[] cubeIndices = new short[] {
            0, 1, 2,
            3, 4, 5,
            6,7,8,
            9,10,11,
            12,13,14,
    };

}
