package com.serenegiant.simpletexture.ui;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.serenegiant.simpletexture.R;

public class ResolutionActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout mLlWidth;
    LinearLayout mLlheight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolution);

        mLlWidth = (LinearLayout) findViewById(R.id.ll_width);
        mLlheight = (LinearLayout) findViewById(R.id.ll_height);

        mLlWidth.setOnClickListener(this);
        mLlheight.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_width:
                showWidthDialog();
                break;

            case R.id.ll_height:
                showHeightDialog();
                break;
        }
    }

    private void showWidthDialog(){
        final EditText editText = new EditText(ResolutionActivity.this);
        AlertDialog.Builder width = new AlertDialog.Builder(ResolutionActivity.this);
        width.setTitle("宽度");
        width.setView(editText);
        width.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        width.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        width.show();
    }

    private void showHeightDialog(){
        final EditText editText = new EditText(ResolutionActivity.this);
        AlertDialog.Builder height = new AlertDialog.Builder(ResolutionActivity.this);
        height.setTitle("宽度");
        height.setView(editText);
        height.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        height.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        height.show();
    }
}
