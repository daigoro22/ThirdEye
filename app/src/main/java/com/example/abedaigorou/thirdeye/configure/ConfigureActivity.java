package com.example.abedaigorou.thirdeye.configure;

/**
 * Created by abedaigorou on 2017/08/29.
 */

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppLaunchChecker;
import android.util.Log;

import com.example.abedaigorou.thirdeye.CaptureManager;
import com.example.abedaigorou.thirdeye.ImageUtils;
import com.example.abedaigorou.thirdeye.MainActivity;
import com.example.abedaigorou.thirdeye.R;
import com.example.abedaigorou.thirdeye.configure.CameraConfigure.CameraConfigureFragment;
import com.example.abedaigorou.thirdeye.configure.CameraConfigure.PreviewFragment;
import com.example.abedaigorou.thirdeye.configure.CommunicationConfigure.CommunicationConfigureFragment;
import com.example.abedaigorou.thirdeye.configure.VRConfigure.GLPreviewFragment;
import com.example.abedaigorou.thirdeye.configure.VRConfigure.VRConfigureFragment;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.HashSet;
import java.util.Set;

public class ConfigureActivity extends Activity implements VRConfigureFragment.VRConfigureEventListener,CameraConfigureFragment.CameraConfigureEventListener,
        PreviewFragment.PreviewFragmentEventListener,CommunicationConfigureFragment.CommunicationConfigureEventListener
{
    private Bitmap bitmap;
    private int width,height,afMode;
    private Mat rgbaMatOut,bgrMat,mYuvMat;
    PreviewFragment previewFragment;
    CameraConfigureFragment cameraConfigureFragment;
    GLPreviewFragment glPreviewFragment;
    VRConfigureFragment vrConfigureFragment;
    CommunicationConfigureFragment communicationConfigureFragment;
    private CaptureManager manager;
    private CaptureManager.CaptureEventListener listener;
    private final String TAG="ConfigureActivity";
    public final static String INTENTTAG="Intent_ConfigureActivity";
    public final static String BUNDLETAG="Bundle_ConfigureActivity";
    public final static int REQUEST_CODE_CAMERA=0;
    public final static int REQUEST_CODE_VR=1;
    public final static int REQUEST_CODE_FIRSTTIME=2;
    public final static int REQUEST_CODE_COMMUNICATION=3;
    private SharedPreferences sharedPreferences;
    private int requestCode;
    private static ConfigureActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setContentView(R.layout.activity_configure);
        Bundle request=getIntent().getExtras();

        requestCode=request.getInt(INTENTTAG);

        bitmap=null;
        instance=this;

        listener=new CaptureManager.CaptureEventListener() {
            @Override
            public void onTakeImage(final byte[] data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mYuvMat = ImageUtils.ByteToMat(data);
                        Imgproc.cvtColor(mYuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);
                        Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA, 0);
                        bitmap = Bitmap.createBitmap(bgrMat.cols(), bgrMat.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(rgbaMatOut, bitmap);
                        previewFragment.setImageData(bitmap);
                    }
                });
            }

            @Override
            public void onConfigured() {

            }

            @Override
            public void onOpened() {

            }
        };
    }

    public static ConfigureActivity getInstance(){
        return instance;
    }

    private void init(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Fragmentの追加や削除といった変更を行う際は、Transactionを利用します
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                // 新しく追加を行うのでaddを使用します
                // 他にも、メソッドにはreplace removeがあります
                // メソッドの1つ目の引数は対象のViewGroupのID、2つ目の引数は追加するfragment
                Fragment fragment1 = getFragmentManager().findFragmentById(R.id.container1);
                Fragment fragment2=  getFragmentManager().findFragmentById(R.id.container2);

                switch (requestCode){
                    case REQUEST_CODE_FIRSTTIME:
                    case REQUEST_CODE_CAMERA:
                        if(fragment1!=null&&fragment1 instanceof PreviewFragment){
                            previewFragment=(PreviewFragment)fragment1;
                        }else{
                            previewFragment=PreviewFragment.createInstance(bitmap);
                            transaction.add(R.id.container1,previewFragment);
                        }

                        if(fragment2!=null&&fragment2 instanceof CameraConfigureFragment){
                            cameraConfigureFragment=(CameraConfigureFragment)fragment2;
                        }else{
                            cameraConfigureFragment=CameraConfigureFragment.createInstance(1920,1080);
                            transaction.add(R.id.container2,cameraConfigureFragment);
                        }
                        break;

                    case REQUEST_CODE_VR:
                        if(fragment1!=null&&fragment1 instanceof GLPreviewFragment){
                            glPreviewFragment=(GLPreviewFragment)fragment1;
                        }else{
                            glPreviewFragment=new GLPreviewFragment();
                            transaction.add(R.id.container1,glPreviewFragment);
                        }

                        if(fragment2!=null&&fragment2 instanceof VRConfigureFragment){
                            vrConfigureFragment=(VRConfigureFragment) fragment2;
                        }else{
                            vrConfigureFragment=new VRConfigureFragment();
                            transaction.add(R.id.container2,vrConfigureFragment);
                        }
                        break;
                    
                    case REQUEST_CODE_COMMUNICATION:
                        if(fragment1!=null&&fragment1 instanceof CommunicationConfigureFragment){
                            communicationConfigureFragment=(CommunicationConfigureFragment)fragment1;
                        }else{
                            communicationConfigureFragment=new CommunicationConfigureFragment();
                            transaction.add(R.id.container1,communicationConfigureFragment);
                        }
                        break;
                        
                }
                transaction.commit();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG,"onResume");
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, new LoaderCallbackInterface() {
            @Override
            public void onManagerConnected(int status) {
                if(status== LoaderCallbackInterface.SUCCESS) {
                    Log.i("a", "success");
                    rgbaMatOut = new Mat();
                    bgrMat = new Mat(height, width, CvType.CV_8UC4);
                    ImageUtils.setWidthAndHeight(width,height);

                    if(requestCode==REQUEST_CODE_FIRSTTIME){
                        CaptureManager.checkPermission(instance);
                    }
                    else if(requestCode==REQUEST_CODE_CAMERA){
                        Log.i(TAG,"二回目以降");
                        //二回目以降
                        fetchImageDatafromPreference();
                        if((manager=CaptureManager.getInstance())==null){
                            manager=CaptureManager.newInstance(instance,listener);
                        }
                        manager.setListener(listener);
                        manager.start("0",width,height,afMode);
                        init();
                    }else{
                        init();
                    }
                }
            }

            @Override
            public void onPackageInstall(int operation, InstallCallbackInterface callback) {
                Log.i("a","install");
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG,"onPause");
        if(manager!=null) {
            manager.stop();
        }
    }

    @Override
    public void onImageSizeConfigured(String imageSize) {
        Log.i(TAG,"onImageSizeConfigured");

        int [] sizes= ConfigureUtils.getSplitedInt(imageSize,"x");
        if(sizes.length<2){
            return;
        }
        width=sizes[0];
        height=sizes[1];
        manager.setImageSize(imageSize);
        bgrMat=new Mat(sizes[1],sizes[0], CvType.CV_8UC4);
        ImageUtils.setWidthAndHeight(width,height);
    }

    @Override
    public void onAutoFocusConfigured(int afMode) {
        if(manager.getAFMODE()!=afMode)
            manager.setAFMode(afMode);
    }

    @Override
    public void onFocusDistanceConfigured(float focusdist) {
        if(focusdist!=manager.getLENSDIST()) {
            manager.setFocusDistance(focusdist);
        }
    }

    @Override
    public void onPreviewViewCreated() {
        Log.i(TAG,"onPreviewViewCreated");
    }

    @Override
    public void onCameraConfigureViewCreated() {
        Log.i(TAG,"onCameraConfigureViewCreated");
        cameraConfigureFragment.setImageSizes(manager.getAvailableImageSize());
        cameraConfigureFragment.setMaxFocus(manager.getMaxFocus());
        if(manager.getHardwareLebel()==2){
            cameraConfigureFragment.setEnabled(false,R.string.key_focus_preference);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // パーミッションが必要な処理
            Log.i(TAG,"permissionAccepted");
            manager=CaptureManager.newInstance(instance,listener);
            setResult(RESULT_OK);
            initPreference();
            fetchImageDatafromPreference();
            init();
            manager.start("0",width,height,afMode);
        } else {
            // パーミッションが得られなかった時
            finish();
        }
    }


    private void initPreference(){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        Set<String> sizesSet=new HashSet<>();
        String[] stsize=manager.getAvailableImageSize();

        for(String e:stsize)
            sizesSet.add(e);

        //デフォルト画像サイズ(最小)、デフォルトAFモード(AFOFF)、利用可能画像サイズ
        editor.putString(getString(R.string.key_size_preference),stsize[stsize.length-1]);
        editor.putString(getString(R.string.key_autofocus_preference),"0");
        editor.putStringSet(getString(R.string.key_availableimagesizes_preference),sizesSet);

        editor.putString(getString(R.string.key_receiveimagewidth_preference),getString(R.string.defReceiveWidth));
        editor.putString(getString(R.string.key_receiveimageheight_preference),getString(R.string.defReceiveHeight));
        editor.apply();
    }

    //画像情報をSharedPreferenceから取得
    private void fetchImageDatafromPreference(){
        int[] isize=ConfigureUtils.getConfiguredSize(getApplicationContext());
        width=isize[0];
        height=isize[1];
        afMode=ConfigureUtils.getConfiguredIntValue(getApplicationContext(),R.string.key_autofocus_preference,"0");
    }

    @Override
    public void onVRConfigureViewCreated() {

    }

    @Override
    public void onCameraPositionConfigured(int progress) {

    }

    @Override
    public void onDevideLongitudeConfigured(int devide) {

    }

    @Override
    public void onDevideLatitudeConfigured(int devide) {

    }


    @Override
    public void onCommunicationConfigureViewCreated() {
        
    }

    @Override
    public void onReceiveImageWidthConfigured(int width) {

    }

    @Override
    public void onReceiveImageHeightConfigured(int height) {

    }

    @Override
    public void onIpAddrConfigured(String addr) {

    }

    @Override
    public void onPortConfigured(int port) {

    }

    @Override
    public void onIsServerConfigured() {

    }
}

