package com.example.navigationdrawertest.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.navigationdrawertest.R;
import com.example.navigationdrawertest.model.SignPhoto;
import com.example.navigationdrawertest.model.Signature;
import com.example.navigationdrawertest.utils.ActivityUtil;
import com.example.navigationdrawertest.utils.BitmapUtil;
import com.example.navigationdrawertest.utils.CommonTools;
import com.example.navigationdrawertest.write.WriteButtonClick;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by user on 2020/8/8.
 */

public class SignPhotoAdapter  extends BaseAdapter {
    private Context context;
    /** 布局填充器 */
    private LayoutInflater layoutInflater;
    private List<SignPhoto> signPhotoList;
    private String activityName;
    int windowHeight;
    int windowWidth;

    public SignPhotoAdapter(Context context){
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        initData();
    }

    private void initData(){
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        windowWidth = dm.widthPixels;// 获取屏幕分辨率宽度
        windowHeight = dm.heightPixels;
        signPhotoList = DataSupport.findAll(SignPhoto.class);
    }

    private WriteButtonClick myWbc;
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (myWbc != null) {
                myWbc.click(v);
            }
        }
    };

    @Override
    public int getCount() {
        return signPhotoList.size();
    }

    @Override
    public Object getItem(int position) {
        return signPhotoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SignPhotoAdapter.ViewHolder viewHolder;
        final SignPhoto signPhoto = (SignPhoto) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_signphoto, null);
            viewHolder = new SignPhotoAdapter.ViewHolder();
            viewHolder.signname = (TextView) convertView.findViewById(R.id.signphotoname);
            viewHolder.signimage = (ImageButton) convertView.findViewById(R.id.signphotoimage);
            viewHolder.signID = (TextView) convertView.findViewById(R.id.signphotoID);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (SignPhotoAdapter.ViewHolder) convertView.getTag();
        }
        viewHolder.signname.setText(signPhoto.getSigntoryName());
        viewHolder.signID.setText(signPhoto.getSigntoryId());
        String _path = CommonTools.null2String(signPhoto.getBitmapPath());
        Bitmap bitmap = BitmapUtil.getLoacalBitmap(_path);
        viewHolder.signimage.setImageBitmap(bitmap);
        return convertView;
    }

    class ViewHolder {
        public TextView signname;
        public ImageButton signimage;
        public TextView signID;
    }
}
