package com.example.abedaigorou.thirdeye;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by abedaigorou on 2017/06/15.
 */

public class CaptureManager {
    private static CaptureManager instance;
    private int width, height;
    private Context context;
    public final String TAG = "CaptureManager";
    private String CAMERANUM="0";
    final int bufferSize=1048576;
    private byte[][] currentImageData=new byte[3][bufferSize];
    private byte[][] currentImageDataBuffer=new byte[3][bufferSize];
    Bundle currentBundle;
    private int imageSize;
    private boolean isCapturing=false;
    private CaptureEventListener listener;

    CameraDevice.StateCallback cameraDeviceStateCallback;
    ImageReader.OnImageAvailableListener imageAvailableListener;
    CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback;
    CameraCaptureSession.CaptureCallback cameraCaptureSessionCaptureCallback;
    ImageReader imageReader;
    CameraManager cameraManager;
    CameraDevice cameraDevice;
    CameraCaptureSession captureSession;
    CaptureRequest.Builder captureBuilder;
    HandlerThread thread;
    Handler handler;
    ByteBuffer bufferY,bufferU,bufferV;
    Image image;
    Bitmap bitmap;


    public static CaptureManager newInstance(int width, int height, Context context,CaptureEventListener listener) {
        instance = new CaptureManager(width, height, context,listener);
        return instance;
    }

    private CaptureManager(final int width, final int height, Context context, final CaptureEventListener listener) {
        this.width = width;
        this.height = height;
        this.context = context;
        this.listener=listener;

        thread = new HandlerThread("Camera2 background");
        thread.start();
        handler = new Handler(thread.getLooper());
        currentBundle=new Bundle();

        //permissionチェック→してなかったらダイアログ表示
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions((Activity)context, new String[]{
                    Manifest.permission.CAMERA
            }, 1);
        }

        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        cameraDeviceStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.i(TAG, "onOpened");
                try {
                    camera.createCaptureSession(Arrays.asList(imageReader.getSurface()), cameraCaptureSessionStateCallback, null);
                    isCapturing=true;

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                cameraDevice = camera;
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.i(TAG, "onDisconnected");
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                isCapturing=false;
                Log.i(TAG, "onError");
            }
        };

        cameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                Log.i(TAG, "onConfigured");
                captureSession = session;
                captureBuilder = null;
                try {
                    CameraCharacteristics cc=cameraManager.getCameraCharacteristics(CAMERANUM);
                    Range<Integer>[] a=cc.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                    for (Range<Integer> b:a)
                        Log.i("fps",b.toString());
                    StreamConfigurationMap stm=cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Size[] ss=stm.getOutputSizes(ImageFormat.YUV_420_888);
                    for(Size s:ss)
                        Log.i("size",s.toString());
                    //シャッター遅延ゼロでキャプチャ
                    //captureBuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    captureBuilder.addTarget(imageReader.getSurface());
                    captureSession.setRepeatingBurst(Arrays.asList(captureBuilder.build()),null,handler);

                    //captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                    captureBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_OFF);
                    captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,16666666l);
                    captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,new Range<>(120,120));
                    captureBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION,16666666l);

                    /*handler.post(new Runnable() {
                        @Override
                        public void run() {
                            while(isCapturing){
                                try {
                                    captureSession.capture(captureBuilder.build(),cameraCaptureSessionCaptureCallback,null);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    Thread.sleep(interval);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });*/

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Log.i(TAG, "onConfigureFailed");
            }
        };
        cameraCaptureSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                Log.i(TAG, "onCaptureCompleted");
                super.onCaptureCompleted(session, request, result);
            }
        };

        imageReader = ImageReader.newInstance(width, height,ImageFormat.YUV_420_888,1);


        imageAvailableListener = new ImageReader.OnImageAvailableListener() {
            //int Yb,Ub,Vb;
            byte[] data;
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.i(TAG, "onImageAvailable");
                image=reader.acquireNextImage();
                data=ImageUtils.ImageToByte(image);
                listener.onTakeImage(data);

                /*bufferY = image.getPlanes()[0].getBuffer();
                bufferU=image.getPlanes()[1].getBuffer();
                bufferV=image.getPlanes()[2].getBuffer();

                Yb=bufferY.remaining();
                Ub=bufferU.remaining();
                Vb=bufferV.remaining();


                //remaining:最初から最後の要素までの数
                imageSize=bufferY.remaining();
                bufferY.get(currentImageDataBuffer[0],0,Yb);
                bufferU.get(currentImageDataBuffer[1],Yb,Ub);
                bufferV.get(currentImageDataBuffer[2],Yb+Ub,Vb);*/
                //System.arraycopy(currentImageDataBuffer,0,currentImageData,0,imageSize);
                image.close();
            }
        };
        imageReader.setOnImageAvailableListener(imageAvailableListener,handler);
    }

    public void start() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        try {
            cameraManager.openCamera(CAMERANUM, cameraDeviceStateCallback,handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public int getImageSize(){
        return imageSize;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public byte[][] getCurrentImageData(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<3;i++)
                    System.arraycopy(currentImageDataBuffer[i],0,currentImageData[i],0,imageSize);
            }
        });
        return currentImageData;
    }

    /*public Bundle getCurrentBundle(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                System.arraycopy(currentImageDataBuffer,0,currentImageData,0,imageSize);
                currentBundle.putByteArray("data",currentImageData);
                currentBundle.putInt("length",imageSize);
            }
        });
        return currentBundle;
    }*/

    /*public Bitmap getCurrentBmp(){
        return BitmapFactory.decodeByteArray(currentImageData, 0, currentImageData.length);
    }*/
}

