package com.example.abedaigorou.thirdeye;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.AppLaunchChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.abedaigorou.thirdeye.configure.ConfigureActivity;
import com.example.abedaigorou.thirdeye.configure.ConfigureSelectActivity;
import com.example.abedaigorou.thirdeye.configure.ConfigureUtils;

import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {
    static MainActivity instance;
    ImageView imageView;
    CaptureManager captureManager;
    CaptureManager.CaptureEventListener listener;
    public Handler mainHandler;
    private String HOST="";
    private int PORT=0;
    private int width=176;//176;
    private int height=144;//144;
    private int size=176*(144+144/2);
    private int receiveWidth=800;
    private int receiveHeight=600;
    private int receiveSize=800*(600+600/2);
    private boolean isServer=false;
    private int afMode;
    private String TAG="MainActivity";
    //final static int size=777600;
    UDPManager udpManager;
    byte[] imageData;
    Mat rgbaMatOut,mYuvMat,bgrMat;
    Bitmap bitmap;
    VRActivity vrActivity;
    SharedPreferences prefs;
    int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        imageView = (ImageView) findViewById(R.id.imageView);
        mainHandler = new Handler(getMainLooper());

        listener = new CaptureManager.CaptureEventListener() {
            @Override
            public void onTakeImage(final byte[] data) {
                udpManager.sendData(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.arraycopy(data, 0, imageData, 0, data.length);
                        mYuvMat = ImageUtils.ByteToMat(data, width, height);
                        Imgproc.cvtColor(mYuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);
                        Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA, 0);
                        bitmap = Bitmap.createBitmap(bgrMat.cols(), bgrMat.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(rgbaMatOut, bitmap);
                    }
                });
                setImage(bitmap);
            }

            @Override
            public void onConfigured() {

            }

            @Override
            public void onOpened() {

            }
        };

        udpManager = new UDPManager(new CommunicationEventListener() {
            @Override
            public void onConnect(String mes) {

            }

            @Override
            public void onConnected(String mes) {
                showToast(mes + "側で接続");
            }

            @Override
            public void onDiconnect(String mes) {
                showToast("切断");
            }

            @Override
            public void onRead(final byte[] getter) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isServer){
                            mYuvMat = ImageUtils.ByteToMat(getter, receiveWidth, receiveHeight);
                            Imgproc.cvtColor(mYuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);
                            Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA, 0);
                            bitmap = Bitmap.createBitmap(bgrMat.cols(), bgrMat.rows(), Bitmap.Config.ARGB_8888);
                            Utils.matToBitmap(rgbaMatOut, bitmap);
                            if ((vrActivity = VRActivity.getInstance()) != null) {
                                vrActivity.setImageBitmap(bitmap);
                        }else{

                        }
                    }
                    }
                });
                if (vrActivity == null)
                    setImage(bitmap);
            }
        }, size);
    }

    @Override
    public void onResume(){
        super.onResume();
        //listener再設定
        captureManager=CaptureManager.getInstance();
        if(captureManager!=null) {
            captureManager.setListener(listener);
        }
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, new LoaderCallbackInterface() {
            @Override
            public void onManagerConnected(int status) {
                if(status== LoaderCallbackInterface.SUCCESS) {
                    Log.i("a", "success");
                    showToast("接続OK");
                    resetPrefInfo();

                    if (!AppLaunchChecker.hasStartedFromLauncher(instance)) {
                        //初回起動
                        Intent intent=new Intent(getApplicationContext(),ConfigureActivity.class);
                        intent.putExtra(ConfigureActivity.INTENTTAG,ConfigureActivity.REQUEST_CODE_FIRSTTIME);
                        startActivityForResult(intent,0);
                    }else{
                        //二回目以降、起動時のみ
                        if(captureManager==null) {
                            captureManager = CaptureManager.newInstance(instance, listener);
                        }
                    }
                }
            }

            @Override
            public void onPackageInstall(int operation, InstallCallbackInterface callback) {
                Log.i("a","install");
                showToast("お待ち下さい");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode==RESULT_OK){
            AppLaunchChecker.onActivityCreate(instance);
        }else{
            finish();
        }
    }

    private void resetPrefInfo(){
        //preferenceからサイズ,AFMODE取得
        int[] isize = ConfigureUtils.getConfiguredSize(instance);

        if(isize.length<2)
            return;

        width = isize[0];
        height = isize[1];
        afMode = ConfigureUtils.getConfiguredIntValue(instance,R.string.key_autofocus_preference,"0");
        size = width * (height + height / 2);
        imageData = new byte[size];
        rgbaMatOut = new Mat();
        bgrMat = new Mat(receiveHeight, receiveWidth, CvType.CV_8UC4);

        receiveWidth=ConfigureUtils.getConfiguredIntValue(instance,R.string.key_receiveimagewidth_preference,"800");
        receiveHeight=ConfigureUtils.getConfiguredIntValue(instance,R.string.key_receiveimageheight_preference,"600");
        receiveSize=receiveWidth*(receiveHeight+receiveHeight/2);
        isServer=ConfigureUtils.getConfiguredIsServer(instance);
        HOST=ConfigureUtils.getConfiguredStringValue(instance,R.string.key_ipaddr_preference,getString(R.string.defIPaddr));
        PORT=ConfigureUtils.getConfiguredIntValue(instance,R.string.key_port_preference,getString(R.string.defPort));
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        udpManager.Disconnect();
    }

    private void showToast(String mes1){
        final String mes=mes1;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.instance,mes,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onConnectClick(View v){
        if(isServer){
            udpManager.setBufferAndPacketSize(receiveSize);
            udpManager.ServerConnect(PORT);
            //udpManager.ClientConnect(HOST,PORT);
        }else{
            udpManager.setBufferAndPacketSize(size);
            udpManager.ClientConnect(HOST,PORT);
            //udpManager.ServerConnect(PORT);
            captureManager.start("0",width,height,0);
        }
    }

    public void onDisconnectClick(View v){
        captureManager.stop();
        udpManager.Disconnect();
    }

    public void onVRClicked(View v){
        Intent intent=new Intent(getApplicationContext(),VRActivity.class);
        //intent.setClassName("com.example.abedaigorou.yuvconverttest","com.example.abedaigorou.yuvconverttest.VRActivity");
        startActivity(intent);
    }

    public void onConfigureClicked(View v){
        startActivity(new Intent(this, ConfigureSelectActivity.class));
    }

    private void setImage(final Bitmap bitmap){
            runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
            });
    }


}
