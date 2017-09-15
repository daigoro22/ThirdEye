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
    EditText editText,editText2;
    CaptureManager captureManager;
    CaptureManager.CaptureEventListener listener;
    public Handler mainHandler;
    final static String HOST_DEFAULT = "192.168.1.2";
    final static int PORT_DEFAULT = 8001;   // 待受ポート番号
    private String HOST="";
    private int PORT=0;
    private int width=176;//176;
    private int height=144;//144;
    private int size=176*(144+144/2);
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
        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);

        editText.setText(HOST_DEFAULT);
        editText2.setText(String.valueOf(PORT_DEFAULT));
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
                        mYuvMat = ImageUtils.ByteToMat(getter, width, height);
                        Imgproc.cvtColor(mYuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);
                        Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA, 0);
                        bitmap = Bitmap.createBitmap(bgrMat.cols(), bgrMat.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(rgbaMatOut, bitmap);
                        if ((vrActivity = VRActivity.getInstance()) != null) {
                            vrActivity.setImageBitmap(bitmap);
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
                    initCameraInfo();
                    if (!AppLaunchChecker.hasStartedFromLauncher(instance)) {
                        //初回起動
                        AppLaunchChecker.onActivityCreate(instance);
                        Intent intent=new Intent(getApplicationContext(),ConfigureActivity.class);
                        intent.putExtra(ConfigureActivity.INTENTTAG,ConfigureActivity.REQUEST_CODE_FIRSTTIME);
                        startActivity(intent);
                    }else{
                        //二回目以降、起動時のみ
                        if(captureManager==null) {
                            captureManager = CaptureManager.newInstance(instance, listener);
                            captureManager.start("0", width, height, afMode);
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

    private void initCameraInfo(){
        //preferenceからサイズ,AFMODE取得
        int[] isize = ConfigureUtils.getConfiguredSize(instance, prefs);

        if(isize.length<2)
            return;

        width = isize[0];
        height = isize[1];
        afMode = ConfigureUtils.getConfiguredAFMode(instance, prefs);
        size = width * (height + height / 2);
        imageData = new byte[size];
        rgbaMatOut = new Mat();
        bgrMat = new Mat(height, width, CvType.CV_8UC4);
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

    public void onClientConnect(View v){
        HOST=editText.getText().toString();
        PORT=Integer.valueOf(editText2.getText().toString());
        udpManager.ClientConnect(HOST,PORT);
        captureManager.start("0",width,height,0);
    }

    public void onServerConnect(View v){
        HOST=editText.getText().toString();
        PORT=Integer.valueOf(editText2.getText().toString());

        udpManager.ServerConnect(PORT);
    }

    public void onDisconnectClick(View v){
        udpManager.Disconnect();
        captureManager.stop();
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
