package com.serenegiant.simpletexture.ui;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.serenegiant.simpletexture.R;

public class PhycialSettingActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout mLlSnapshotButton;
    LinearLayout mLlPlayButton;
    LinearLayout mLlPreviousButton;
    LinearLayout mLlNextSongButton;
    LinearLayout mLlShootNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phycial_setting);

        mLlSnapshotButton = (LinearLayout) findViewById(R.id.ll_snapshot_button);
        mLlPlayButton = (LinearLayout) findViewById(R.id.ll_play_button);
        mLlPreviousButton = (LinearLayout) findViewById(R.id.ll_previous_button);
        mLlNextSongButton = (LinearLayout) findViewById(R.id.ll_next_song_button);
        mLlShootNumber = (LinearLayout) findViewById(R.id.ll_shoot_number);

        mLlSnapshotButton.setOnClickListener(this);
        mLlPlayButton.setOnClickListener(this);
        mLlPreviousButton.setOnClickListener(this);
        mLlNextSongButton.setOnClickListener(this);
        mLlShootNumber.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_snapshot_button:
                showSnapshotDialog();
                break;

            case R.id.ll_play_button:
                showPlayDialog();
                break;

            case R.id.ll_previous_button:
                showPreviousDialog();
                break;

            case R.id.ll_next_song_button:
                showNextSongDialog();
                break;

            case R.id.ll_shoot_number:
                showShotNumberDialog();
                break;
        }
    }

    /**
     * 快照按钮控制的对话框
     */
    private void showSnapshotDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PhycialSettingActivity.this);
        builder.setTitle("用摄像头的快照按钮控制");
        final String[] state = {"禁用", "录像", "连拍",};
        builder.setSingleChoiceItems(state, 1, new DialogInterface.OnClickListener() {
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

    /**
     * 耳机播放键控制的对话框
     */
    private void showPlayDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PhycialSettingActivity.this);
        builder.setTitle("用耳机的播放键控制");
        final String[] state = {"禁用", "录像", "连拍",};
        builder.setSingleChoiceItems(state, 1, new DialogInterface.OnClickListener() {
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

    /**
     * 耳机上一曲键控制的对话框
     */
    private void showPreviousDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PhycialSettingActivity.this);
        builder.setTitle("用耳机的上一曲键控制");
        final String[] state = {"禁用", "录像", "连拍",};
        builder.setSingleChoiceItems(state, 1, new DialogInterface.OnClickListener() {
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

    /**
     * 耳机下一曲键控制的对话框
     */
    private void showNextSongDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(PhycialSettingActivity.this);
        builder.setTitle("用耳机的下一曲键控制");
        final String[] state = {"禁用", "录像", "连拍",};
        builder.setSingleChoiceItems(state, 1, new DialogInterface.OnClickListener() {
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

    /**
     * 连拍数量的对话框
     */
    private void showShotNumberDialog(){
        final EditText editText = new EditText(PhycialSettingActivity.this);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(PhycialSettingActivity.this);
        inputDialog.setTitle("连拍数量（1~20）");
        inputDialog.setView(editText);
        inputDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        inputDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        inputDialog.show();
    }


}
