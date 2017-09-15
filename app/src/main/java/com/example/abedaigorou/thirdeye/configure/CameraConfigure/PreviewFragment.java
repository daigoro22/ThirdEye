package com.example.abedaigorou.thirdeye.configure.CameraConfigure;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.abedaigorou.thirdeye.R;

/**
 * Created by abedaigorou on 2017/08/25.
 */

public class PreviewFragment extends Fragment
{
    private ImageView imagePreview;
    private PreviewFragmentEventListener listener;
    private Bitmap imageData;
    private final static String KEY_IMAGE="key_image";
    private final static String TAG="PreviewFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /*Bundle args=getArguments();
        if(args!=null){
            imageData=args.getParcelable(KEY_IMAGE);
        }
        else{
            Log.i(TAG,"imageData is null!");
        }*/
        if(imagePreview!=null){
            imagePreview.setImageBitmap(imageData);
        }
        return inflater.inflate(R.layout.fragment_preview,container,false);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof PreviewFragmentEventListener){
            listener=(PreviewFragmentEventListener)context;
        }
    }

    @CheckResult
    public static PreviewFragment createInstance(Bitmap data){
        PreviewFragment fragment=new PreviewFragment();
        /*Bundle args=new Bundle();
        args.putParcelable(KEY_IMAGE,data);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        imagePreview=(ImageView)view.findViewById(R.id.imagePreview);

        listener.onPreviewViewCreated();
        imagePreview.setImageBitmap(imageData);
    }

    public void setImageData(Bitmap data){
        if(data!=null) {
            imageData = data;
        }
        if(imagePreview!=null){
            imagePreview.setImageBitmap(data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle args){
        //args.putParcelable(KEY_IMAGE,imageData);
        super.onSaveInstanceState(args);
    }

    public interface PreviewFragmentEventListener
    {
        void onPreviewViewCreated();

    }
}
