package com.example.navigationdrawertest.model;

import org.litepal.crud.DataSupport;

/**
 * Created by user on 2020/9/21.  靶场策划表
 */

public class BCRelation extends DataSupport {
    private int id;
    private String chid;			//策划ID
    private String chname;			//策划名称
    private String ssxh;			//所属型号ID
    private String xhdh;			//所属型号代号
    private String syfzrID;           //试验负责人

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChid() {
        return chid;
    }

    public void setChid(String chid) {
        this.chid = chid;
    }

    public String getChname() {
        return chname;
    }

    public void setChname(String chname) {
        this.chname = chname;
    }

    public String getSsxh() {
        return ssxh;
    }

    public void setSsxh(String ssxh) {
        this.ssxh = ssxh;
    }

    public String getXhdh() {
        return xhdh;
    }

    public void setXhdh(String xhdh) {
        this.xhdh = xhdh;
    }

    public String getSyfzrID() {
        return syfzrID;
    }

    public void setSyfzrID(String syfzrID) {
        this.syfzrID = syfzrID;
    }
}
