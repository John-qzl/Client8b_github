package com.example.navigationdrawertest.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.navigationdrawertest.R;
import com.example.navigationdrawertest.application.OrientApplication;
import com.example.navigationdrawertest.camera.AlbumAty;
import com.example.navigationdrawertest.customCamera.FileOperateUtil;
import com.example.navigationdrawertest.internet.SyncWorkThread;
import com.example.navigationdrawertest.model.BCRelation;
import com.example.navigationdrawertest.model.RwRelation;
import com.example.navigationdrawertest.utils.ActivityCollector;
import com.example.navigationdrawertest.utils.NetCheckTool;

import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.Set;

/**
 * Created by user on 2020/9/16.
 */

public class GateActivity extends BaseActivity {

    private LinearLayout mQuit, mLineCPYS, mLineWQSJ, mLineBCSY;
    private TextView mLoginName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate);
        getActionBar().hide();
        ActivityCollector.addActivity(this);

        mQuit = (LinearLayout) findViewById(R.id.quit);
        mLoginName = (TextView) findViewById(R.id.loginName);
        mLoginName.setText(OrientApplication.getApplication().loginUser.getUsername() + "");
        mLineCPYS = (LinearLayout) findViewById(R.id.line_cpys);
        mLineWQSJ = (LinearLayout) findViewById(R.id.line_wqsj);
        mLineBCSY = (LinearLayout) findViewById(R.id.line_bcsy);

        mQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(GateActivity.this);
                dialog.setIcon(R.drawable.logo_title).setTitle("警告");
                dialog.setMessage("是否退出？");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        boolean bConnected = NetCheckTool.check(getApplicationContext());// 检测本地网络是否可连接服务
                        if (bConnected == true)	// 联网登录
                        {
                            new Thread(new Runnable(){
                                @Override
                                public void run() {
                                    SyncWorkThread.upLoadDeviceInfo("0");
                                }
                            }).start();
                        }
                        Intent intent1 = new Intent(GateActivity.this, LoginActivity.class);
                        startActivity(intent1);
                    }
                });
                dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });
        mLineCPYS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<RwRelation> proList = DataSupport.where("userid = ?", OrientApplication.getApplication().loginUser.getUserid()).find(RwRelation.class);
                if (proList.size() > 0) {
                    Intent intent = new Intent(GateActivity.this, MainActivity1.class);
                    intent.putExtra("fieldType", "1");
                    startActivity(intent);
                } else {
                    showDialog("此领域暂无数据");
                }
            }
        });
        mLineWQSJ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                List<RwRelation> BCList = DataSupport.where("userid = ?", OrientApplication.getApplication().loginUser.getUserid()).find(RwRelation.class);
//                if (BCList.size() > 0) {
//                    Intent intent = new Intent(GateActivity.this, MainActivity1.class);
//                    intent.putExtra("fieldType", "2");
//                    startActivity(intent);
//                } else {
//                    showDialog("此领域暂无数据");
//                }
                showDialog("此领域暂未启用");
            }
        });
        mLineBCSY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                List<BCRelation> BCList = DataSupport.where("syfzrID = ?", OrientApplication.getApplication().loginUser.getUserid()).find(BCRelation.class);
//                if (BCList.size() > 0) {
//                    Intent intent = new Intent(GateActivity.this, MainActivity1.class);
//                    intent.putExtra("fieldType", "3");
//                    startActivity(intent);
//                } else {
//                    showDialog("此领域暂无数据");
//                }
                showDialog("此领域暂未启用");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showDialog(String str) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_title).setTitle("提示");
        builder.setMessage(str)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
