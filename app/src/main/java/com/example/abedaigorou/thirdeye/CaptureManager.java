package com.example.abedaigorou.thirdeye;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Paint;
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

import com.example.abedaigorou.thirdeye.configure.ConfigureUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by abedaigorou on 2017/06/15.
 */

public class CaptureManager {
    private static CaptureManager instance;
    private static int mWidth, mHeight;
    private Context context;
    public final String TAG = "CaptureManager";
    private String CAMERANUM = "0";
    final int bufferSize = 1048576;
    private byte[][] currentImageData = new byte[3][bufferSize];
    private byte[][] currentImageDataBuffer = new byte[3][bufferSize];
    Bundle currentBundle;
    private String[] availableImageSize, availableFpsRange;
    private int hardwareLebel=-1;
    private static float maxFocus=-1f;
    private int imageSize;
    private boolean isCapturing = false;
    private CaptureEventListener listener;

    private int AFMODE=0;
    private float LENSDIST=0;
    private final int fps=30;
    private double tpf=1f/fps;
    private long sensorExposureTime=(long)(tpf*Math.pow(10,9));

    CameraDevice.StateCallback cameraDeviceStateCallback;
    ImageReader.OnImageAvailableListener imageAvailableListener;
    CameraCaptureSession.StateCallback cameraCaptureSessionStateCallback;
    CameraCaptureSession.CaptureCallback cameraCaptureSessionCaptureCallback;
    CameraManager.AvailabilityCallback cameraManagerAvailabilityCallback;

    ImageReader imageReader;
    CameraManager cameraManager;
    CameraDevice cameraDevice;
    CameraCaptureSession captureSession;
    CaptureRequest.Builder captureBuilder;
    HandlerThread thread;
    Handler handler;
    ByteBuffer bufferY, bufferU, bufferV;
    Image image;
    Bitmap bitmap;
    private boolean isreboot=false;


    public static CaptureManager newInstance(Context context,CaptureEventListener listener) {
        instance = new CaptureManager(context,listener);
        return instance;
    }

    public static CaptureManager getInstance(){
        return instance;
    }

    private CaptureManager(Context context, final CaptureEventListener listener) {
        this.context = context;
        this.listener = listener;
        Log.i(TAG,String.valueOf(sensorExposureTime));

        thread = new HandlerThread("Camera2 background");
        thread.start();
        handler = new Handler(thread.getLooper());
        currentBundle = new Bundle();

        //permissionチェック→してなかったらダイアログ表示
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions((Activity) context, new String[]{
                    Manifest.permission.CAMERA
            }, 1);
        }

        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        getAvailableDatas();
        cameraManagerAvailabilityCallback=new CameraManager.AvailabilityCallback() {
            @Override
            public void onCameraAvailable(@NonNull String cameraId) {
                super.onCameraAvailable(cameraId);
                Log.i(TAG,"onCameraAvailable");
                if(isreboot){
                    start(cameraId,mWidth,mHeight,AFMODE);
                    isreboot=false;
                }
            }
        };

        cameraManager.registerAvailabilityCallback(cameraManagerAvailabilityCallback,handler);

        cameraDeviceStateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.i(TAG, "onOpened");
                try {
                    camera.createCaptureSession(Arrays.asList(imageReader.getSurface()), cameraCaptureSessionStateCallback, handler);
                    isCapturing = true;

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                cameraDevice = camera;
                listener.onOpened();
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                Log.i(TAG, "onDisconnected");
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                isCapturing = false;
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
                    //シャッター遅延ゼロでキャプチャ
                    //captureBuilder=cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,AFMODE);
                    if(AFMODE==0&&hardwareLebel==1){
                        captureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE,LENSDIST);
                    }
                    captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                    captureBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_OFF);
                    captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME,sensorExposureTime);
                    captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,new Range<>(fps,fps));
                    captureBuilder.set(CaptureRequest.SENSOR_FRAME_DURATION,sensorExposureTime);
                    captureBuilder.addTarget(imageReader.getSurface());

                    captureSession.setRepeatingBurst(Arrays.asList(captureBuilder.build()), null, handler);
                    //resetBuilder();

                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                listener.onConfigured();
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
        isreboot=false;
    }

    public void start(String cameraId, final int width, final int height, int afMode) {
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
        if(isCapturing)
            return;

        isCapturing=true;
        mWidth=width;
        mHeight=height;
        AFMODE=afMode;

        imageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 1);

        imageAvailableListener = new ImageReader.OnImageAvailableListener() {
            //int Yb,Ub,Vb;
            byte[] data;

            @Override
            public void onImageAvailable(ImageReader reader) {
                //Log.i(TAG, "onImageAvailable");
                image = reader.acquireNextImage();
                data = ImageUtils.ImageToByte(image,width,height);
                listener.onTakeImage(data);
                //listener.onFocusPointTouched();
                image.close();
            }
        };
        imageReader.setOnImageAvailableListener(imageAvailableListener, handler);

        if(CAMERANUM.equals(cameraId)&&cameraDeviceStateCallback!=null) {
            try {
                cameraManager.openCamera(CAMERANUM, cameraDeviceStateCallback, handler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void getAvailableDatas(){
        if (availableFpsRange == null && availableImageSize == null&&maxFocus==-1f&&hardwareLebel==-1) {
            CameraCharacteristics cc = null;
            try {
                cc = cameraManager.getCameraCharacteristics(CAMERANUM);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            Range<Integer>[] a = cc.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            StreamConfigurationMap stm = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] ss = stm.getOutputSizes(ImageFormat.YUV_420_888);

            hardwareLebel=cc.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

            ArrayList<String> temp = new ArrayList<>();

            for (Size s : ss) {
                temp.add(s.toString());
                Log.i(TAG, s.toString());
            }
            availableImageSize = temp.toArray(new String[0]);
            temp.clear();

            for (Range<Integer> b : a) {
                temp.add(b.toString());
                Log.i(TAG, b.toString());
            }
            availableFpsRange = temp.toArray(new String[0]);

            Float lens=cc.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);

            if(lens==null) {
                float[] length = cc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                for (float l : length)
                    Log.i(TAG, String.valueOf(l));
                maxFocus = length[0];
            }else{
                Log.i(TAG,String.valueOf(lens));
                maxFocus=lens;
            }
            temp.clear();
        }
    }

    public int getHardwareLebel(){
        return hardwareLebel;
    }


    public void stop() {
        if(isCapturing) {
            isCapturing = false;
            if (captureSession != null) {
                captureSession.close();
                cameraDevice.close();
            }
        }
    }

    public void reboot(){
        if(isCapturing) {
            isreboot = true;
            stop();
        }
    }

    public void setAFMode(final int afMode){
        handler.post(new Runnable() {
            @Override
            public void run() {
                AFMODE=afMode;
                reboot();
            }
        });
    }

    public void setImageSize(final String imageSize) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (imageSize == null)
                    return;

                int[] sizes = ConfigureUtils.getSplitedInt(imageSize, "x");
                mWidth = sizes[0];
                mHeight = sizes[1];

                reboot();
                if(imageReader!=null) {
                    imageReader.close();
                }
                imageReader = ImageReader.newInstance(mWidth, mHeight, ImageFormat.YUV_420_888, 1);
                imageReader.setOnImageAvailableListener(imageAvailableListener, handler);

            }
        });
    }

    public void setFocusDistance(final float dist){
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(AFMODE==CaptureRequest.CONTROL_AF_MODE_OFF){
                    LENSDIST=dist;
                    reboot();
                }else{
                    Log.i(TAG,"AutoFocus is not OFF");
                }
            }
        });
    }

    public static void checkPermission(Context context){
        //permissionチェック→してなかったらダイアログ表示
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions((Activity) context, new String[]{
                    Manifest.permission.CAMERA
            }, 1);
        }
    }

    public boolean getIsCapturing(){
        return isCapturing;
    }

    public void setListener(CaptureEventListener listener){
        this.listener=listener;
    }

    public int getImageSize(){
        return imageSize;
    }

    public int getAFMODE(){
        return AFMODE;
    }

    public float getLENSDIST(){
        return LENSDIST;
    }

    public static int getWidth(){
        return mWidth;
    }

    public static int getHeight(){
        return mHeight;
    }

    public String[] getAvailableImageSize(){
        return availableImageSize;
    }

    public float getMaxFocus(){return maxFocus;}

    public String[] getAvailableFpsRange(){
        return availableFpsRange;
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


    public interface CaptureEventListener {
        void onTakeImage(byte[] data);
        void onConfigured();
        void onOpened();
    }
}

