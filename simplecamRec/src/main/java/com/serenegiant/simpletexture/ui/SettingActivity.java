package com.serenegiant.simpletexture.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.serenegiant.simpletexture.R;
import com.serenegiant.simpletexture.utils.ToastUtil;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    TextView mTvGeneralSetup;
    TextView mTvPhycialSetting;
    TextView mTvPreferredResolution;
    LinearLayout mLlVideoAspectRatio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setTitle("USB Camera");

        mTvGeneralSetup = (TextView) findViewById(R.id.tv_general_setup);
        mTvPhycialSetting = (TextView) findViewById(R.id.tv_physical_setting);
        mTvPreferredResolution = (TextView) findViewById(R.id.tv_preferred_resolution);
        mLlVideoAspectRatio = (LinearLayout) findViewById(R.id.ll_video_aspect_ration);

        mTvGeneralSetup.setOnClickListener(this);
        mTvPhycialSetting.setOnClickListener(this);
        mTvPreferredResolution.setOnClickListener(this);
        mLlVideoAspectRatio.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_general_setup:
                Intent intent = new Intent(SettingActivity.this,GeneralSetupActivity.class);
                startActivity(intent);
                break;

            case R.id.tv_physical_setting:
                Intent intent1 = new Intent(SettingActivity.this,PhycialSettingActivity.class);
                startActivity(intent1);
                break;

            case R.id.ll_video_aspect_ration:
                setVideoAspectRatio();
                break;

            case R.id.tv_preferred_resolution:
            Intent intent2 = new Intent(SettingActivity.this,ResolutionActivity.class);
            startActivity(intent2);
            break;

        }

    }

    private void setVideoAspectRatio(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
        builder.setTitle("视频长宽比");
        final String[] ratio = {"默认（推荐）", "16:9", "4:3","1:1"};
        builder.setSingleChoiceItems(ratio, 1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }
}
