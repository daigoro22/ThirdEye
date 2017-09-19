package com.example.abedaigorou.thirdeye;

import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;

/**
 * Created by abedaigorou on 2017/07/09.
 */

public class ImageUtils {

    /**
     * Takes an Android Image in the YUV_420_888 format and returns an OpenCV Mat.
     *
     * @param image Image in the YUV_420_888 format.
     * @return OpenCV Mat.
     */
    private static String TAG="ImageUtils";
    private static float bytesPerPixel=ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8f;
    private static int width,height;
    private static  byte[] data;
    private static byte[] rowData;
    private static Mat mat;

    public static void setWidthAndHeight(int _width,int _height){
        width=_width;
        height=_height;
        data=new byte[(int)(width*height*bytesPerPixel)];
        rowData= new byte[width];
        mat = new Mat(height + height / 2, width, CvType.CV_8UC1);//y成分+uv成分
    }

    public static Mat imageToMat(Image image) {
        ByteBuffer buffer;
        int rowStride;
        int pixelStride;
        int width = image.getWidth();
        int height = image.getHeight();
        int offset = 0;

        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[image.getWidth() * image.getHeight() * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];

        for (int i = 0; i < planes.length; i++) {
            buffer = planes[i].getBuffer();
            rowStride = planes[i].getRowStride();
            pixelStride = planes[i].getPixelStride();

            //yのとき大きさ等倍,u,vのとき大きさ1/2
            int w = (i == 0) ? width : width / 2;
            int h = (i == 0) ? height : height / 2;
            for (int row = 0; row < h; row++) {
                int bytesPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8;
                if (pixelStride == bytesPerPixel) {//Yのとき
                    int length = w * bytesPerPixel;
                    buffer.get(data, offset, length);

                    // Advance buffer the remainder of the row stride, unless on the last row.
                    // Otherwise, this will throw an IllegalArgumentException because the buffer
                    // doesn't include the last padding.
                    if (h - row != 1) {
                        buffer.position(buffer.position() + rowStride - length);
                    }
                    offset += length;
                } else {//U,Vのとき

                    // On the last row only read the width of the image minus the pixel stride
                    // plus one. Otherwise, this will throw a BufferUnderflowException because the
                    // buffer doesn't include the last padding.
                    if (h - row == 1) {//299999だから
                        buffer.get(rowData, 0, width - pixelStride + 1);
                    } else {
                        buffer.get(rowData, 0, rowStride);
                    }

                    for (int col = 0; col < w; col++) {
                        data[offset++] = rowData[col * pixelStride];//YUV420だったら２つまたいでサプサンプリング
                    }
                }
            }
        }

        // Finally, create the Mat.
        Mat mat = new Mat(height + height / 2, width, CvType.CV_8UC1);//y成分+uv成分
        mat.put(0, 0, data);

        return mat;
    }

    public static byte[] ImageToByte(Image image){
        ByteBuffer buffer;
        int rowStride;
        int pixelStride;
        int offset = 0;

        Image.Plane[] planes = image.getPlanes();

        for (int i = 0; i < planes.length; i++) {
            buffer = planes[i].getBuffer();
            rowStride = planes[i].getRowStride();
            pixelStride = planes[i].getPixelStride();

            //yのとき大きさ等倍,u,vのとき大きさ1/2
            int w = (i == 0) ? width : width / 2;
            int h = (i == 0) ? height : height / 2;
            for (int row = 0; row < h; row++) {
                if (pixelStride == bytesPerPixel) {//Yのとき
                    int length =(int)( w * bytesPerPixel);
                    buffer.get(data, offset, length);

                    // Advance buffer the remainder of the row stride, unless on the last row.
                    // Otherwise, this will throw an IllegalArgumentException because the buffer
                    // doesn't include the last padding.
                    if (h - row != 1) {
                        buffer.position(buffer.position() + rowStride - length);
                    }
                    offset += length;
                } else {//U,Vのとき

                    // On the last row only read the width of the image minus the pixel stride
                    // plus one. Otherwise, this will throw a BufferUnderflowException because the
                    // buffer doesn't include the last padding.
                    if (h - row == 1) {//299999だから
                        buffer.get(rowData, 0, width - pixelStride + 1);
                    } else {
                        buffer.get(rowData, 0, rowStride);
                    }

                    for (int col = 0; col < w; col++) {
                        data[offset++] = rowData[col * pixelStride];//YUV420だったら２つまたいでサプサンプリング
                    }
                }
            }
        }
        return data;
    }

    public static Mat ByteToMat(byte[] image) {
        // Finally, create the Mat.
        mat.put(0, 0, image);
        return mat;
    }
}