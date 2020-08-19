package com.example.navigationdrawertest.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.navigationdrawertest.R;
import com.example.navigationdrawertest.application.OrientApplication;
import com.example.navigationdrawertest.internet.SyncWorkThread;
import com.example.navigationdrawertest.model.Cell;
import com.example.navigationdrawertest.model.Diagram;
import com.example.navigationdrawertest.model.Mmc;
import com.example.navigationdrawertest.model.Operation;
import com.example.navigationdrawertest.model.Post;
import com.example.navigationdrawertest.model.Product;
import com.example.navigationdrawertest.model.Rw;
import com.example.navigationdrawertest.model.RwRelation;
import com.example.navigationdrawertest.model.Scene;
import com.example.navigationdrawertest.model.SignPhoto;
import com.example.navigationdrawertest.model.Signature;
import com.example.navigationdrawertest.model.Task;
import com.example.navigationdrawertest.model.UploadFileRecord;
import com.example.navigationdrawertest.model.User;
import com.example.navigationdrawertest.utils.Config;
import com.example.navigationdrawertest.utils.NetCheckTool;

import org.litepal.crud.DataSupport;

import java.io.File;

/**
 * Created by user on 2020/8/18.
 */

public class AdminActivity extends BaseActivity {
    private Button mDelAll, mSignCollect;
    private AlertDialog.Builder dialog;
    private ProgressDialog prodlg;
    private LinearLayout mQuit;
    private TextView mLoginName;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            prodlg.dismiss();
            if(msg.what == 1) {
                dialog = new AlertDialog.Builder(AdminActivity.this);
                dialog.setIcon(R.drawable.logo_title).setTitle(R.string.app_name);
                dialog.setMessage("本地数据库已经清空！");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_layout);
        mQuit = (LinearLayout) findViewById(R.id.quit);
        mLoginName = (TextView) findViewById(R.id.loginName);
        mLoginName.setText(OrientApplication.getApplication().loginUser.getUsername() + "");
        mDelAll = (Button) findViewById(R.id.admin_delAll);
        mDelAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new AlertDialog.Builder(AdminActivity.this);
                dialog.setIcon(R.drawable.logo_title).setTitle(R.string.app_name);
                dialog.setMessage("是否清空数据库？");
                dialog.setCancelable(false);
                dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        clearDataBase();
                    }
                });
                dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        mSignCollect = (Button) findViewById(R.id.admin_signcollect);
        mSignCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, SignPhotoCollectActivity.class);
                startActivity(intent);
            }
        });
        mQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(AdminActivity.this);
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
                        Intent intent1 = new Intent(AdminActivity.this, LoginActivity.class);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void clearDataBase() {
        prodlg = ProgressDialog.show(this, "警告", "正在清空数据库，请稍侯...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Message message = new Message();
                    DataSupport.deleteAll(Cell.class);
                    DataSupport.deleteAll(Scene.class);
                    DataSupport.deleteAll(Operation.class);
                    DataSupport.deleteAll(Post.class);
                    DataSupport.deleteAll(Signature.class);
                    DataSupport.deleteAll(Task.class);
                    DataSupport.deleteAll(User.class,"isAdmin = ?", "0");
                    DataSupport.deleteAll(Rw.class);
                    DataSupport.deleteAll(RwRelation.class);
                    DataSupport.deleteAll(Diagram.class);
                    DataSupport.deleteAll(Mmc.class);
                    DataSupport.deleteAll(Product.class);
                    DataSupport.deleteAll(UploadFileRecord.class);
                    DataSupport.deleteAll(SignPhoto.class);
                    File v2pFile = new File(Environment.getExternalStorageDirectory() + Config.rootPath);
                    File mmcFile = new File(Environment.getExternalStorageDirectory() + Config.mmcPath);
                    deleteFiles(v2pFile);
                    deleteFiles(mmcFile);
                    message.what = 1;
                    mHandler.sendMessage(message);
                }catch (Exception e) {
                    Log.e("clear database",e.toString());
                }
            }
        }).start();
    }

    private void deleteFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFiles(f);
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }
}
