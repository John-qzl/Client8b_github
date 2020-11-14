package com.example.navigationdrawertest.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.example.navigationdrawertest.MainActivity;
import com.example.navigationdrawertest.R;
import com.example.navigationdrawertest.adapter.NavDrawerListAdapter;
import com.example.navigationdrawertest.application.MyApplication;
import com.example.navigationdrawertest.application.OrientApplication;
import com.example.navigationdrawertest.entity.NavDrawerItem;
import com.example.navigationdrawertest.fragment.HomeFragment;
import com.example.navigationdrawertest.internet.HttpClientHelper;
import com.example.navigationdrawertest.internet.SyncWorkThread;
import com.example.navigationdrawertest.login.Login;
import com.example.navigationdrawertest.model.BCRelation;
import com.example.navigationdrawertest.model.Cell;
import com.example.navigationdrawertest.model.Diagram;
import com.example.navigationdrawertest.model.Mmc;
import com.example.navigationdrawertest.model.Operation;
import com.example.navigationdrawertest.model.Post;
import com.example.navigationdrawertest.model.Product;
import com.example.navigationdrawertest.model.Rw;
import com.example.navigationdrawertest.model.RwRelation;
import com.example.navigationdrawertest.model.Scene;
import com.example.navigationdrawertest.model.Signature;
import com.example.navigationdrawertest.model.Task;
import com.example.navigationdrawertest.model.UploadFileRecord;
import com.example.navigationdrawertest.model.User;
import com.example.navigationdrawertest.tree.DepartmentNode;
import com.example.navigationdrawertest.tree.TreeNode;
import com.example.navigationdrawertest.tree.UserNode;
import com.example.navigationdrawertest.utils.ActivityCollector;
import com.example.navigationdrawertest.utils.CommonTools;
import com.example.navigationdrawertest.utils.CommonUtil;
import com.example.navigationdrawertest.utils.Config;
import com.example.navigationdrawertest.utils.ConverXML;
import com.example.navigationdrawertest.utils.FileOperation;
import com.example.navigationdrawertest.utils.FontSize;
import com.example.navigationdrawertest.utils.ListStyle;
import com.example.navigationdrawertest.utils.NetCheckTool;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2018/1/16.
 */

public class MainActivity1 extends FragmentActivity implements OnItemClickListener {

    private DrawerLayout mDrawerLayout;
    private ImageView leftMenu, mDoneSync;
    private LinearLayout mSyn, mMedia, mQuit, mSign, mHome;
    private TextView mtitle, mLoginName;
    private ListView mDrawerList;
    private NavDrawerListAdapter mAdapter;
    private List<RwRelation> projectList;
    private List<BCRelation> BCprojectList;
    private List<NavDrawerItem> mNavDrawerItems;
    private TypedArray mNavMenuIconsTypeArray;
    private static int localPosition = 0;
    private AlertDialog.Builder dialog;
    private TreeNode rootNode;
    private String nowProductId;
    private String fieldType = "";  //1产品验收  2武器所检  3靶场试验

    public static void actionStart1(Context context, String fieldType) {
        Intent intent = new Intent(context, MainActivity1.class);
        if (fieldType.equals("1")) {
            intent.putExtra("fieldType", "1");
        } else if (fieldType.equals("2")) {
            intent.putExtra("fieldType", "2");
        } else {
            intent.putExtra("fieldType", "3");
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        getActionBar().hide();
        ActivityCollector.addActivity(this);
        initUserInformation();
        initview();
        fieldType = getIntent().getStringExtra("fieldType");
        localPosition = 0;
        selectItem(localPosition);
        mtitle.setText(R.string.app_name);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    @SuppressLint("NewApi")
    private void initview() {
        leftMenu = (ImageView) findViewById(R.id.leftmenu);
        mtitle = (TextView) findViewById(R.id.title);
        mSyn = (LinearLayout) findViewById(R.id.data_syn);
        mSign = (LinearLayout) findViewById(R.id.signPhoto_collect);
        mHome = (LinearLayout) findViewById(R.id.home);
//        mDoneSync = (ImageView) findViewById(R.id.done_data_syn);
        mMedia = (LinearLayout) findViewById(R.id.media);
        mQuit = (LinearLayout) findViewById(R.id.quit);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        mDrawerList = (ListView) findViewById(R.id.left_listview);
        mLoginName = (TextView) findViewById(R.id.loginName);
        mLoginName.setText(OrientApplication.getApplication().loginUser.getUsername() + "");
        leftMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        mQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity1.this);
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
                        Intent intent1 = new Intent(MainActivity1.this, LoginActivity.class);
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
        mMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity1.this, DocActivity.class);
                intent.putExtra("nowProductId", nowProductId);
                intent.putExtra("fieldType", fieldType);
                startActivity(intent);
            }
        });
        mSyn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(NetCheckTool.check(MainActivity1.this)){
                    ProgressDialog prodlg = ProgressDialog.show(MainActivity1.this, "同步", "正在同步数据");
                    prodlg.setIcon(MainActivity1.this.getResources().getDrawable(R.drawable.logo_title));
                    SyncWorkThread syncThread = new SyncWorkThread(MainActivity1.this, handler);
                    syncThread.start();
                }else{
                    Toast.makeText(MainActivity1.this, "网络连接异常", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        mDoneSync.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (NetCheckTool.check(MainActivity1.this)) {
//                    dialog = new AlertDialog.Builder(MainActivity1.this);
//                    dialog.setIcon(R.drawable.logo_title).setTitle(R.string.app_name);
//                    dialog.setMessage("是否要进行已完成状态的数据同步？（注：此操作仅限管理员！）");
//                    dialog.setCancelable(false);
//                    dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            OrientApplication.getApplication().setFlag(1);
//                            ProgressDialog prodlg = ProgressDialog.show(MainActivity1.this, "同步", "正在同步数据");
//                            prodlg.setIcon(MainActivity1.this.getResources().getDrawable(R.drawable.logo_title));
//                            SyncWorkThread syncThread = new SyncWorkThread(MainActivity1.this, handler);
//                            syncThread.start();
////                            uploadDoneTable();
////                            prodlg.dismiss();
//                        }
//                    });
//                    dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//                    dialog.show();
//                }else{
//                    Toast.makeText(MainActivity1.this, "网络连接异常", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        mSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity1.this, SignPhotoCollectActivity.class);
                startActivity(intent);
            }
        });
        mHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity1.this, GateActivity.class);
                startActivity(intent);
            }
        });
        mNavMenuIconsTypeArray = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        mNavDrawerItems = new ArrayList<NavDrawerItem>();
        //左侧项目树展现方法2，根据RwRelation表格展现----数据表采用
        List<RwRelation> proList = DataSupport.where("userid = ?", OrientApplication.getApplication().loginUser.getUserid()).find(RwRelation.class);
        List<BCRelation> BCList = DataSupport.where("syfzrID = ?", OrientApplication.getApplication().loginUser.getUserid()).find(BCRelation.class);
        //20200610 暂时放开PAD上人员查看数据权限
//        List<RwRelation> proList = DataSupport.findAll(RwRelation.class);
        projectList = proList;
        BCprojectList = BCList;
        if (fieldType.equals("1")) {
            for(int i=0; i<proList.size(); i++){
                mNavDrawerItems.add(new NavDrawerItem(proList.get(i).getRwname(), mNavMenuIconsTypeArray
                        .getResourceId(0, -1)));
            }
        } else {
            for(int i=0; i<BCList.size(); i++){
                mNavDrawerItems.add(new NavDrawerItem(BCList.get(i).getXhdh(), mNavMenuIconsTypeArray
                        .getResourceId(0, -1)));
            }
        }

        // Recycle the typed array
        mNavMenuIconsTypeArray.recycle();
        // setting the nav drawer list adapter
        mAdapter = new NavDrawerListAdapter(getApplicationContext(),
                mNavDrawerItems);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     * */
    public void selectItem(final int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        OrientApplication.getApplication().setCommander(false);
        if((projectList.size() > 0 && projectList != null) || (BCprojectList.size() > 0 && BCprojectList != null)){
            RwRelation proEntity = new RwRelation();
            BCRelation BCproEntity = new BCRelation();
            if (fieldType.equals("1")) {
                proEntity = projectList.get(position);
                nowProductId = proEntity.getRwid();
            } else {
                BCproEntity = BCprojectList.get(position);
                nowProductId = BCproEntity.getSsxh();
            }
            if (fieldType.equals("1")) {
                if (proEntity != null) {
                    OrientApplication.getApplication().rw = proEntity;
                    Log.i("项目名称", proEntity.getRwname());
                }
            } else {
                if (BCproEntity != null) {
                    RwRelation rwRelation = new RwRelation();
                    rwRelation.setRwid(BCproEntity.getSsxh());
                    rwRelation.setRwname(BCproEntity.getXhdh());
                    rwRelation.setFieldType(fieldType);
                    OrientApplication.getApplication().rw = rwRelation;
                    Log.i("项目名称", rwRelation.getRwname());
                }
            }
            localPosition = position;
            if (fieldType == null) {
                fieldType = "1";
            }
            fragment = new HomeFragment(proEntity, fieldType, BCproEntity);
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment).commit();

            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
//            setTitle(projectList.get(position).getRwname());
            if (fieldType.equals("1")) {
                mtitle.setText(projectList.get(position).getRwname());
            } else {
                mtitle.setText(BCprojectList.get(position).getChname());
            }
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    protected void initUserInformation(){
        if (DataSupport.findAll(User.class).size() > 0) {
            List<User> userList = DataSupport.where("userid = ?", OrientApplication.getApplication().loginUser.getUserid()).find(User.class);
            if (userList.size() > 0) {
                OrientApplication.getApplication().loginUser = userList.get(0);
            } else {
                Toast.makeText(this, "用户信息异常", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "用户信息异常", Toast.LENGTH_SHORT).show();
        }
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
            }
            return;
        }
    };

    public void getSyncInformation() {
        Builder builder = new AlertDialog.Builder(this);
//        Resources res = this.getResources();
//        builder.setIcon(res.getDrawable(R.drawable.logo_title)).setTitle(res.getString(R.string.listsyncmessage));
        LayoutInflater li = LayoutInflater.from(this);
        View v = li.inflate(R.layout.listsyncmessage, null);
        ListView listsyncmessage = (ListView) v.findViewById(R.id.listsyncmessage);
        listsyncmessage.setAdapter(new MainActivity1.ListSyncAdapter(this, (ArrayList<String>) OrientApplication.getApplication().uploadDownloadList));

        builder.setView(v)
                .setPositiveButton("请点击进入",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity1.actionStart1(MainActivity1.this, fieldType);
                                dialog.cancel();
                            }
                        }).setCancelable(false).show();
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
            return new MainActivity1.ListSyncView(context, tableName);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        OrientApplication.getApplication().setCommander(false);
        ActivityCollector.finishAll();
    }



    /**
     * @Description: 判断用户是否为该节点的负责人
     * @author qiaozhili
     * @date 2019/2/19 16:44
     * @param
     * @return
     */
//    private void isCommander(String nodeIds) {
//        String userId = OrientApplication.getApplication().loginUser.getUserid();
//        List<User> userList = DataSupport.where("userid=?",userId ).find(User.class);
//        if (!nodeIds.equals("")) {
//            String[] strNodeId = nodeIds.split("\\、");
//            for (int i = 0; i < strNodeId.length; i++) {
//                String nodeID = strNodeId[i];
//                if (userList.size() > 0) {
//                    String commanderId = userList.get(0).getCommanderId();
//                    if (!commanderId.equals("")) {
//                        String[] strCommanderId = commanderId.split("\\,");
//                        for (int j = 0; j < strCommanderId.length; j++) {
//                            if (nodeID.equals(strCommanderId[j])) {
//                                OrientApplication.getApplication().setCommander(true);
//                            }
//                        }
//                    } else {
//                        OrientApplication.getApplication().setCommander(false);
//                    }
//                } else {
//                    OrientApplication.getApplication().setCommander(false);
//                }
//            }
//        } else {
//            OrientApplication.getApplication().setCommander(false);
//        }
//    }
}
