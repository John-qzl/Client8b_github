package com.example.navigationdrawertest.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.navigationdrawertest.CustomUI.ObservableScrollView;
import com.example.navigationdrawertest.R;
import com.example.navigationdrawertest.adapter.SignPhotoAdapter;
import com.example.navigationdrawertest.application.OrientApplication;
import com.example.navigationdrawertest.data.AerospaceDB;
import com.example.navigationdrawertest.model.SignPhoto;
import com.example.navigationdrawertest.model.Signature;
import com.example.navigationdrawertest.model.Task;
import com.example.navigationdrawertest.utils.Config;
import com.example.navigationdrawertest.utils.DateUtil;
import com.example.navigationdrawertest.write.DialogListener;
import com.example.navigationdrawertest.write.WritePadDialog;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by user on 2020/8/8.
 */

public class SignPhotoCollectActivity extends BaseActivity  implements ObservableScrollView.Callbacks{
    private LinearLayout mBack, mPhotoAdd;
    private SignPhotoAdapter signPhotoAdapter = null;
    private ListView listView;
    private AerospaceDB aerospacedb;
    private List<SignPhoto> signlists = new ArrayList<SignPhoto>();
    private Bitmap mSignBitmap;
    private String signPath;
    int windowHeight;
    int windowWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signphoto_collect);
        getActionBar().hide();
        initView();
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        windowWidth = dm.widthPixels;// 获取屏幕分辨率宽度
        windowHeight = dm.heightPixels;
        aerospacedb = new AerospaceDB();
//        loadSignAdapter(OrientApplication.getApplication().loginUser.getUserid());
        signPhotoAdapter = new SignPhotoAdapter(SignPhotoCollectActivity.this);
        listView.setAdapter(signPhotoAdapter);
    }

    private void initView() {
        mBack = (LinearLayout) findViewById(R.id.signcollect_back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPhotoAdd = (LinearLayout) findViewById(R.id.signcollect_add);
        mPhotoAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSignPhoto();
//                loadSignAdapter(OrientApplication.getApplication().loginUser.getUserid());
                signPhotoAdapter.notifyDataSetChanged();
                listView.setAdapter(signPhotoAdapter);
            }
        });
        listView = (ListView) findViewById(R.id.signCollect_list);
    }

    public void addSignPhoto() {
        LayoutInflater factory = LayoutInflater.from(this);//提示框
        final View view = factory.inflate(R.layout.layout_addsignphoto, null);//这里必须是final的
        final EditText edit = (EditText) view.findViewById(R.id.et_signPhotoName);//签署姓名
        final EditText id = (EditText) view.findViewById(R.id.et_signPhotoID);//签署人ID
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        final String timeString = formatter.format(curDate);
        final String creatorID = OrientApplication.getApplication().loginUser.getUserid();
        final SignPhoto signPhoto = new SignPhoto();
        final Signature signnature = new Signature();
        new AlertDialog.Builder(this)
                .setTitle("请输入签名人信息！")//提示框标题
                .setView(view)
                .setPositiveButton("确定",//提示框的两个按钮
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                String signtoryID = String.valueOf(id.getText());
                                String signtoryName = String.valueOf(edit.getText());
                                signPhoto.setSigntoryId(signtoryID);
                                signPhoto.setSigntoryName(signtoryName);
                                signPhoto.setTime(timeString);
                                signPhoto.setCreateUserId(creatorID);
                                signDialog(signPhoto,signnature);
                                signPhotoAdapter.notifyDataSetChanged();
                                listView.setAdapter(signPhotoAdapter);
                            }
                        })
                .setNegativeButton("取消", null).create().show();
    }

    public void signDialog(final SignPhoto signPhoto, Signature signnature) {
        WritePadDialog writeTabletDialog = new WritePadDialog(this, new DialogListener() {
            @Override
            public void refreshActivity(Object object,
                                        Signature sign) {

                mSignBitmap = (Bitmap) object;
                signPath = createFile(signPhoto.getSigntoryId());
                Bitmap bmp = getBitmapByOpt(signPath);
                if (bmp != null) {
                    String time = DateUtil.getCurrentDate();
                    signPhoto.setBitmapPath(signPath);
                    signPhoto.save();
                }
//                mPhotoAdd.setEnabled(false);
                signPhotoAdapter.notifyDataSetChanged();
                listView.setAdapter(signPhotoAdapter);
            }
        }, signnature);
        writeTabletDialog.show();
    }

    /**
     * 创建手写签名文件
     *
     * @return
     */
    private String createFile(String signId) {
        ByteArrayOutputStream baos = null;
        String _path = null;
        try {
            String signphotoPath = Environment.getExternalStorageDirectory()+ Config.personalsignphoto + "/" ;
            _path = signphotoPath + signId + ".jpg";
            File path = new File(signphotoPath);
            if (!path.exists()) {// 目录存在返回false
                path.mkdirs();// 创建一个目录
            }
            baos = new ByteArrayOutputStream();
            mSignBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] photoBytes = baos.toByteArray();
            if (photoBytes != null) {
                new FileOutputStream(new File(_path)).write(photoBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null)
                    baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return _path;
    }

    public Bitmap getBitmapByOpt(String picturePath) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, opt);
        int imgHeight = opt.outHeight;
        int imgWidth = opt.outWidth;
        int scaleX = imgWidth / windowWidth;
        int scaleY = imgHeight / windowHeight;
        int scale = 1;
        if (scaleX > scaleY & scaleY >= 1) {
            scale = scaleX;
        }
        if (scaleY > scaleX & scaleX >= 1) {
            scale = scaleY;
        }
        opt.inJustDecodeBounds = false;
        opt.inSampleSize = scale;
        return BitmapFactory.decodeFile(picturePath, opt);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
//
//    //加载Sign数据
//    private void loadSignAdapter(String userid){
//        List<SignPhoto> signs = DataSupport.findAll(SignPhoto.class);
//        signlists = signs;
//    }

    @Override
    public void onScrollChanged(int scrollY) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent() {

    }
}
