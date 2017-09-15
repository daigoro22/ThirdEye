package com.example.abedaigorou.thirdeye.configure;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.abedaigorou.thirdeye.R;


/**
 * Created by abedaigorou on 2017/09/14.
 */

public class ConfigureSelectActivity extends Activity
{
    private int reqCode=0;
    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.activity_configureselect);

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        adapter.add("VR設定");
        adapter.add("カメラ設定");
        adapter.add("通信設定");
        ListView listView=(ListView)findViewById(R.id.confugureSelectList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),ConfigureActivity.class);
                switch (position){
                    case 0:
                        reqCode=ConfigureActivity.REQUEST_CODE_VR;
                        break;
                    case 1:
                        reqCode=ConfigureActivity.REQUEST_CODE_CAMERA;
                        break;
                    case 2:
                        reqCode=ConfigureActivity.REQUEST_CODE_COMMUNICATION;
                        break;
                }
                intent.putExtra(ConfigureActivity.INTENTTAG,reqCode);
                startActivity(intent);
            }
        });
    }
}
