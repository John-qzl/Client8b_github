package com.example.navigationdrawertest.model;

import org.litepal.crud.DataSupport;
import org.w3c.dom.Element;

/**
 * Created by user on 2020/8/8.  //电子签章表
 */

public class SignPhoto extends DataSupport {
    private int id;
    private String signtoryId;          //电子签章所属人员ID
    private String signtoryName;		//电子签章所属人员姓名
    private String createUserId;		//创建人ID
    private String time;				//签署时间
    private String bitmapPath;          //电子签章图片保存路径

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSigntoryId() {
        return signtoryId;
    }

    public void setSigntoryId(String signtoryId) {
        this.signtoryId = signtoryId;
    }

    public String getSigntoryName() {
        return signtoryName;
    }

    public void setSigntoryName(String signtoryName) {
        this.signtoryName = signtoryName;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBitmapPath() {
        return bitmapPath;
    }

    public void setBitmapPath(String bitmapPath) {
        this.bitmapPath = bitmapPath;
    }
    /**
     * 805XML同步属性
     * @return
     */
    public static String Tag_signUserId = "signtoryId";
    public static String Attr_signUserName = "signtoryName";
    public static String Attr_createUserId = "createUserId";
    public static String Attr_time = "time";
    public Element setSignPhotoNode(Element signElement, SignPhoto signPhoto)
    {
        signElement.setAttribute(Tag_signUserId, signPhoto.getSigntoryId());
        signElement.setAttribute(Attr_signUserName, signPhoto.getSigntoryName());
        signElement.setAttribute(Attr_createUserId, signPhoto.getCreateUserId());
        signElement.setAttribute(Attr_time, signPhoto.getTime());
        return signElement;
    }

}
