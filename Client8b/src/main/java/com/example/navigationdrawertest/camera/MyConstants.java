package com.example.navigationdrawertest.camera;

import android.os.Environment;

public class MyConstants {
    public static final String CHAPTER_2_PATH = Environment
            .getExternalStorageDirectory().getPath()
            + "/singwhatiwanna/chapter_2/";

    public static final String CACHE_FILE_PATH = CHAPTER_2_PATH + "usercache";

    public static final int MSG_FROM_CLIENT = 0;
    public static final int MSG_FROM_SERVICE = 1;
    public static final String LOCAL_FILE_PREFIX = "file://";
    public static final int MAX_IMG_COUNT = 9;//最多9张图片
    public static final int SUCCESS_CODE = 0;
}
