package com.example.navigationdrawertest.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navigationdrawertest.R;
import com.example.navigationdrawertest.application.OrientApplication;
import com.example.navigationdrawertest.internet.SyncWorkThread;
import com.example.navigationdrawertest.internet.UpdataVersionThread;
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
import com.example.navigationdrawertest.utils.FontSize;
import com.example.navigationdrawertest.utils.ListStyle;
import com.example.navigationdrawertest.utils.NetCheckTool;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;

import static com.example.navigationdrawertest.customCamera.album.view.FilterImageView.TAG;

/**
 * Created by user on 2020/8/18.
 */

public class AdminActivity extends BaseActivity {
    private AlertDialog.Builder dialog;
    private ProgressDialog prodlg;
    private LinearLayout mQuit, mSyn, mDelAll, mSignCollect, mUpdate;
    private TextView mLoginName;
    private Context context;

    public static void actionStart1(Context context) {
        Intent intent = new Intent(context, AdminActivity.class);
        context.startActivity(intent);
    }

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
        context = this;
        mQuit = (LinearLayout) findViewById(R.id.quit);
        mLoginName = (TextView) findViewById(R.id.loginName);
        mLoginName.setText(OrientApplication.getApplication().loginUser.getUsername() + "");
        mDelAll = (LinearLayout) findViewById(R.id.line_admin_delAll);
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
        mSignCollect = (LinearLayout) findViewById(R.id.line_admin_signcollect);
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
        mSyn = (LinearLayout) findViewById(R.id.data_syn);
        mSyn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetCheckTool.check(AdminActivity.this)){
                    ProgressDialog prodlg = ProgressDialog.show(AdminActivity.this, "同步", "正在同步数据");
                    prodlg.setIcon(AdminActivity.this.getResources().getDrawable(R.drawable.logo_title));
                    SyncWorkThread syncThread = new SyncWorkThread(AdminActivity.this, handler);
                    syncThread.start();
                }else{
                    Toast.makeText(AdminActivity.this, "网络连接异常", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mUpdate = (LinearLayout) findViewById(R.id.line_update);
        mUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInternet();
            }
        });
    }

    // 根据消息更新界面
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            // 响应读取本地文件或者同步结束
            String readResult = (String) bundle.get("localread");
            if (readResult != null && readResult.equalsIgnoreCase("Failure"))	// 读本地出错
            {
                String reason = (String) bundle.get("REASON");
            } else if (readResult != null
                    && readResult.equalsIgnoreCase("ERROR"))// 同步出错
            {
                // 错误处理
                String errorTitle = (String) bundle.get("ERRORTITLE");
                if (errorTitle != null && !errorTitle.isEmpty()) {
                    String errorInfo = (String) bundle.get("ERRORINFORMATION");
                }
            } else if (readResult != null && readResult.equalsIgnoreCase("oksync"))	// 同步成功
            {
                getSyncInformation();
            } else if (readResult != null && readResult.equalsIgnoreCase("OK"))// 读本地成功
            {
            }else if (readResult != null && readResult.equalsIgnoreCase("okupdata")) {
                prodlg.dismiss();
                String apkpath = (String) bundle.get("apkpath");
                String apkVersion = (String) bundle.get("apkVersion");
                updataWarn(apkpath, apkVersion);
            }else if (readResult != null && readResult.equalsIgnoreCase("noNewVersion")) {
                prodlg.dismiss();
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setIcon(R.drawable.logo_title).setTitle("提示");
                dialog.setMessage("没有检测到新版本，请检查新版本是否存在！");
                dialog.setCancelable(false);
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
            return;
        }
    };

    public void getSyncInformation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        Resources res = this.getResources();
//        builder.setIcon(res.getDrawable(R.drawable.logo_title)).setTitle(res.getString(R.string.listsyncmessage));
        LayoutInflater li = LayoutInflater.from(this);
        View v = li.inflate(R.layout.listsyncmessage, null);
        ListView listsyncmessage = (ListView) v.findViewById(R.id.listsyncmessage);
        listsyncmessage.setAdapter(new AdminActivity.ListSyncAdapter(this, (ArrayList<String>) OrientApplication.getApplication().uploadDownloadList));

        builder.setView(v)
                .setPositiveButton("请点击进入",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                AdminActivity.actionStart1(AdminActivity.this);
                                dialog.cancel();
                            }
                        }).setCancelable(false).show();
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

    public class ListSyncAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<String> uploadDownloadList;

        public ListSyncAdapter(Context context,
                               ArrayList<String> uploadDownloadList) {
            this.context = context;
            this.uploadDownloadList = uploadDownloadList;
        }

        @Override
        public int getCount() {
            return uploadDownloadList.size();
        }

        @Override
        public Object getItem(int position) {
            return uploadDownloadList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String tableName = uploadDownloadList.get(position);
            return new AdminActivity.ListSyncView(context, tableName);
        }

    }

    public class ListSyncView extends LinearLayout {
        private final Context context;
        private final String tableName;
        private TextView taskName;

        public ListSyncView(Context context, String tableName) {
            super(context);
            this.context = context;
            this.tableName = tableName;
            // 文字
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    ListStyle.listTaskNameWidth + 500,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.setMargins(5, 0, 5, 0);
            textParams.gravity = Gravity.CENTER;
            taskName = new TextView(this.context);
            taskName.setTextColor(Color.BLACK);
            taskName.setText(this.tableName);
            if (tableName.equals("无")) {
                taskName.setTextColor(Color.RED);
            }
            taskName.setTextSize(FontSize.listMidleSize1);
            taskName.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            this.addView(taskName, textParams);
        }
    }

    /**
     * @Description: 检查网络连接
     * @author qiaozhili
     * @date 2020/3/20 17:12
     * @param
     * @return
     */
    private void checkInternet() {
        this.prodlg = ProgressDialog.show(this, "检测网络连接中", "请稍侯...");
        prodlg.setIcon(this.getResources().getDrawable(
                R.drawable.logo_title));
        boolean bConnected = NetCheckTool.check(this);// 检测本地网络是否可连接服务
        if (bConnected == true)	// 联网登录
        {
            prodlg.dismiss();
            starUpdata();
        }else{		//离线登录
            alert("无网络连接！");
            prodlg.cancel();
        }
    }

    /**
     * @param
     * @return
     * @Description: 下载apk
     * @author qiaozhili
     * @date 2020/3/20 17:40
     */

    private void starUpdata() {
        this.prodlg = ProgressDialog.show(this, "版本更新", "正在更新版本");
        prodlg.setIcon(this.getResources().getDrawable(R.drawable.logo_title));
        UpdataVersionThread uptataThread = new UpdataVersionThread(this, handler);
        uptataThread.start();
    }

    private void updataWarn(final String apkPath, String apkVersion) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setIcon(R.drawable.logo_title).setTitle("检测到新版本，是否进行升级？");
//		dialog.setMessage("version:" + apkVersion);
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (apkPath != "") {
//					installAPK(apkPath);
                    install(apkPath);
                } else {
                    Toast.makeText(AdminActivity.this, "没查询apk文件，请检查服务端数据！", Toast.LENGTH_SHORT).show();
                }
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

    private void install(String filePath) {
        Log.i(TAG, "开始执行安装: " + filePath);
        try {
            File apkFile = new File(filePath);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= 24) {
                Log.w(TAG, "版本大于 N ，开始使用 fileProvider 进行安装");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(
                        context
                        , "com.example.navigationdrawer8bu.fileprovider"
                        , apkFile);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            } else {
                Log.w(TAG, "正常进行安装");
                intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            }
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void alert(String error) {
        new AlertDialog.Builder(this).setMessage(error)
                .setIcon(R.drawable.logo_title).setTitle(R.string.app_name)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        prodlg.cancel();
                    }
                }).show();
    }
}
