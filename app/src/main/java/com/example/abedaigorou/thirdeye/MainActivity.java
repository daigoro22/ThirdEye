package com.example.abedaigorou.thirdeye;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
    public Handler mainHandler;
    final static String HOST_DEFAULT = "192.168.1.2";
    final static int PORT_DEFAULT = 8001;   // 待受ポート番号
    private String HOST="";
    private int PORT=0;
    final static int width=176;//176
    final static int height=144;//144
    final static int size=width*(height+height/2);
    //final static int size=777600;
    UDPManager udpManager;
    byte[] imageData=new byte[size];
    Mat rgbaMatOut,mYuvMat,bgrMat;
    Bitmap bitmap;
    VRActivity vrActivity;
    int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance=this;
        setContentView(R.layout.activity_main);
        imageView=(ImageView) findViewById(R.id.imageView);
        editText=(EditText)findViewById(R.id.editText);
        editText2=(EditText)findViewById(R.id.editText2);

        editText.setText(HOST_DEFAULT);
        editText2.setText(String.valueOf(PORT_DEFAULT));
        mainHandler=new Handler(getMainLooper());
        udpManager=new UDPManager(new CommunicationEventListener() {
            @Override
            public void onConnect(String mes) {

            }

            @Override
            public void onConnected(String mes) {
                showToast(mes+"側で接続");
            }

            @Override
            public void onDiconnect(String mes) {
                showToast("切断");
            }

            @Override
            public byte[] onSend() {
                if(imageData!=null)
                    return imageData;
                return new byte[1];
            }

            @Override
            public void onRead(final byte[] getter) {
                throwMain(new Runnable() {
                    @Override
                    public void run() {
                        mYuvMat=ImageUtils.ByteToMat(getter,width,height);
                        Imgproc.cvtColor(mYuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);
                        Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA, 0);
                        bitmap = Bitmap.createBitmap(bgrMat.cols(), bgrMat.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(rgbaMatOut, bitmap);
                        if((vrActivity=VRActivity.getInstance())!=null) {
                            vrActivity.setImageBitmap(bitmap);
                        }
                    }
                });
                if(vrActivity==null)
                    setImage(bitmap);
            }
        },size,1);
    }

    @Override
    public void onResume(){
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, new LoaderCallbackInterface() {
            @Override
            public void onManagerConnected(int status) {
                if(status== LoaderCallbackInterface.SUCCESS) {
                    Log.i("a", "success");
                    showToast("接続OK");
                    rgbaMatOut = new Mat();
                    bgrMat = new Mat(height, width, CvType.CV_8UC4);
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
    public void onDestroy(){
        super.onDestroy();
        udpManager.Disconnect();
    }

    private void showToast(String mes1){
        final String mes=mes1;
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.instance,mes,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClientConnect(View v){
        captureManager=CaptureManager.newInstance(width, height, this, new CaptureEventListener() {
            @Override
            public void onTakeImage(final byte[] data) {
                throwMain(new Runnable() {
                    @Override
                    public void run() {
                    System.arraycopy(data,0,imageData,0,data.length);
                    mYuvMat=ImageUtils.ByteToMat(data,width,height);
                    Imgproc.cvtColor(mYuvMat, bgrMat, Imgproc.COLOR_YUV2BGR_I420);
                    Imgproc.cvtColor(bgrMat, rgbaMatOut, Imgproc.COLOR_BGR2RGBA, 0);
                    bitmap = Bitmap.createBitmap(bgrMat.cols(), bgrMat.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(rgbaMatOut, bitmap);
                    }
                });
                setImage(bitmap);
            }
        });
        captureManager.start();
        HOST=editText.getText().toString();
        PORT=Integer.valueOf(editText2.getText().toString());

        udpManager.ClientConnect(HOST,PORT);
    }

    public void onServerConnect(View v){
        HOST=editText.getText().toString();
        PORT=Integer.valueOf(editText2.getText().toString());

        udpManager.ServerConnect(PORT);
    }

    public void onDisconnectClick(View v){
        udpManager.Disconnect();
    }

    public void onVRClicked(View v){
        Intent intent=new Intent(getApplicationContext(),VRActivity.class);
        //intent.setClassName("com.example.abedaigorou.yuvconverttest","com.example.abedaigorou.yuvconverttest.VRActivity");
        startActivity(intent);
    }

    private void setImage(final Bitmap bitmap){
            throwMain(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
            });
    }

    private void throwMain(Runnable r){
        if(mainHandler!=null)
            mainHandler.post(r);
    }
}
