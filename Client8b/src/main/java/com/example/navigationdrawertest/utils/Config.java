package com.example.navigationdrawertest.utils;

import org.xmlpull.v1.XmlPullParser;

import com.example.navigationdrawertest.R;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

public class Config{
	
	public String ip=null;
	public String port=null;
	public static String rwphotoPath = "/8b/files/rwphotopath";		//任务级别拍照路径
	public static String tablephotoPath = "/8b/files/tablephotopath";				//检查项级别拍照路径
	public static String tableallphotoPath = "/8b/files/tableallphotoPath";		//检查表格总级别拍照路径
	public static String htmlPath = "/8b/files/tablehtml";					//检查表格HTML保存路径
	public static String packagePath = "/data/com.example.navigationdrawer8bu";			//包路径
//	public static String packagePath = "/activity";			//包路径
	public static String signphotoPath = "/8b/files/signphoto";			//检查表格签署照片路径
	public static String opphotoPath = "/8b/files/opphoto";					//操作项示意图照片路径
	
	public static String v2photoPath = "/8b/files/v2p";					//操作项拍照文件夹
	public static String mmcPath = "/mmccopy";					//操作项拍照文件夹
	public static String rootPath = "/8b/files";					//操作项拍照文件夹

	public static String requireval = "requireval";//要求值
	public static String upper = "upper";//上偏差
	public static String lower = "lower";//下偏差
	public static String actualval = "actualval";//实测值
	public static String compliance = "compliance";//符合度
	public static String fuhe = "合格";//符合度
	public static String bufuhe = "不合格";//符合度

	public String getIpAndPort(Context context)
	{
		try {
			Resources r = context.getResources();
			int i = R.xml.config;
			/*
			 * The XML parsing interface returned for an XML resource. This is a standard XmlPullParser interface, 
			 * as well as an extended AttributeSet interface and an additional close() method on this interface for the client to indicate when it is done reading the resource.
			 */
			XmlResourceParser xrp = r.getXml(i);
			while(true)
			{
				/*
				 * Returns the type of the current event (START_TAG, END_TAG, TEXT, etc.)
				 */
				if(xrp.getEventType() == XmlPullParser.START_TAG)//遇到开始标签
				{
					String targName = xrp.getName();//获取标签名
					System.out.println(targName);
					if(targName.equals("user"))//所有的用户标签都是user
					{
						ip = xrp.getAttributeValue(null,"ipAddress").toString();//获得第一属性：IP地址
						System.out.println(ip);
						port = xrp.getAttributeValue(null,"port").toString();//获得第二属性:端口号
						System.out.println(port);
						break;
					}
				}
				xrp.next();//匹配IP和Port不成功，则查看下一个标签信息
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return ip+":"+port;
	}
	
	//用来设置系统当前时间，暂时没有用到
	public static boolean  setSystemTime(String time)
	{
		return false;
	}
}
