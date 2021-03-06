package com.example.navigationdrawertest.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.greenrobot.eventbus.Subscribe;
import org.jsoup.nodes.Document;
import org.litepal.crud.DataSupport;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.navigationdrawertest.CustomUI.NumImageButton;
import com.example.navigationdrawertest.CustomUI.ObservableScrollView;
import com.example.navigationdrawertest.CustomUI.SyncHorizontalScrollView;
import com.example.navigationdrawertest.R;
import com.example.navigationdrawertest.adapter.ConditionAdapter1;
import com.example.navigationdrawertest.adapter.Event.LocationEvent;
import com.example.navigationdrawertest.application.OrientApplication;
import com.example.navigationdrawertest.data.AerospaceDB;
import com.example.navigationdrawertest.data.HtmlData;
import com.example.navigationdrawertest.model.Cell;
import com.example.navigationdrawertest.model.Operation;
import com.example.navigationdrawertest.model.Scene;
import com.example.navigationdrawertest.model.Task;
import com.example.navigationdrawertest.sipner.NiceSpinner;
import com.example.navigationdrawertest.sipner.OnSpinnerItemSelectedListener;
import com.example.navigationdrawertest.utils.ActivityCollector;
import com.example.navigationdrawertest.utils.CommonTools;
import com.example.navigationdrawertest.utils.Config;
import com.example.navigationdrawertest.utils.DateUtil;
import com.example.navigationdrawertest.utils.HtmlHelper;
import com.example.navigationdrawertest.utils.ScreenUtils;

import de.greenrobot.event.EventBus;

public class CheckActivity1 extends BaseActivity implements ObservableScrollView.Callbacks {

    private SyncHorizontalScrollView myScrollView, titleHorScr;
    private TableLayout table_headertest;
    private TableLayout table_header;
    private LinearLayout table_header1;
    private TableLayout table_content;
    private Context context;
    private int rowCount = 0;            //行数
    private int cellCount = 0;                //列数
    private List<Cell> cellList;                //本表格所有的CELL集合
    private AerospaceDB aerospacedb;
    private Task currentTask;                //当前表格
    private int width;                            //屏幕总宽度
    private int avewdith;                    //平均宽度
    private List<Cell> headMap = new ArrayList<Cell>();            //head的Cell集合
    public Document htmlDoc = null;
    private static long task_id;                    //表格ID

    private ProgressDialog prodlg;

    //只有返回的时候保存HTML数据(check)
    private List<HtmlData> htmlList;
    //只有返回的时候保存HTML数据(string)
    private List<HtmlData> htmlList_text;

    //表格之外的其他布局
    private com.example.navigationdrawertest.CustomUI.NoScrollListview listView_1;
    private Switch checkSwitch;
    private List<Scene> scenelists = new ArrayList<Scene>();
    //	private ConditionAdapter conditionadapter = null;
    private ConditionAdapter1 conditionadapter;
    public static int picturenumbers = 0;
    //	private ImageView mBack;
    private TextView mTablename, mTotalPhNum;

    private LinearLayout mSumit;
    private RelativeLayout mBottom;
    private Button mProview, mNext, mConfirmBtn, mCancelBtn;
    private ImageView mClose;
    private int rowsnum;
    private int pagetype;

    private ListView mTitlels;
    private QuickCollectAdapter quickCollectAdapter = null;
    private List<Task> quickCollectTaskList = new ArrayList<>();
    private List<Map<String, String>> quickCollectMapList = new ArrayList<Map<String, String>>();
    private PopupWindow popupWindow;
    private int totalPhNumber = 0;
    private LinearLayout mRefresh, mPandu, mWanzhengxing, mBack;

    public static void actionStart(Context context, long taskid, Handler handler, String location) {
        Intent intent = new Intent(context, CheckActivity1.class);
        task_id = Long.valueOf(taskid);
        context.startActivity(intent);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    prodlg.dismiss();
                    EventBus.getDefault().post(new LocationEvent("ok"));
                    finish();
                    break;
                case 2:
//					initParam();
//					initContentUI();
                    break;
                case 3:

                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.checktable1);
        getActionBar().hide();
        EventBus.getDefault().register(this);

        context = this;
        initUI();                        // 初始化UI控件
        initParam();                    // 初始化必要的全局参数

        initHeaderUI();
        initContentUI();
        setTitle(currentTask.getTaskname());
        mTablename.setText(currentTask.getTaskname());
        if (currentTask.getEndTime() != null && !currentTask.getEndTime().equals("")) {
            checkSwitch.setChecked(true);
        }
        checkSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {    //任务完成
                    checkComplete1();
                } else {                    //任务没有完成
                    currentTask.setEndTime("");
                    currentTask.setLocation(1);
                    currentTask.save();
                }
            }
        });
        if (!currentTask.getStartTime().equals("") || currentTask.getStartTime() != null) {
            currentTask.setStartTime(DateUtil.getCurrentDate());
        }
        loadConditionAdapter(task_id, OrientApplication.getApplication().loginUser.getUserid());
//		conditionadapter = new ConditionAdapter(CheckActivity1.this, R.layout.checkconditionitem, scenelists);
        conditionadapter = new ConditionAdapter1(CheckActivity1.this, scenelists);
        listView_1.setAdapter(conditionadapter);

        myScrollView = (SyncHorizontalScrollView) findViewById(R.id.parent_scroll);
        titleHorScr = (SyncHorizontalScrollView) findViewById(R.id.title_horsv);
        titleHorScr.setScrollView(myScrollView);
        myScrollView.setScrollView(titleHorScr);

        // 当布局的状态或者控件的可见性发生改变回调的接口
//		findViewById(R.id.parent_layout).getViewTreeObserver()
//				.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//
//					@Override
//					public void onGlobalLayout() {
//						// 这一步很重要，使得上面的购买布局和下面的购买布局重合
//						onScroll(myScrollView.getScrollY());
//
//						System.out.println(myScrollView.getScrollY());
//					}
//				});
        mTotalPhNum.setText("照片数量：" + String.valueOf(totalPhNumber));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Message message = new Message();
        message.what = 2;
        mHandler.sendMessage(message);
//		initParam();
//		initContentUI();
    }

    private void initUI() {
        table_header = (TableLayout) findViewById(R.id.table_header);
//		table_header1 = (LinearLayout) findViewById(R.id.table_header1);
        table_content = (TableLayout) findViewById(R.id.table_content);
        listView_1 = (com.example.navigationdrawertest.CustomUI.NoScrollListview) findViewById(R.id.check_mylistview_1);
        checkSwitch = (Switch) findViewById(R.id.checkSwitch);
        mTablename = (TextView) findViewById(R.id.table_name);
        mTotalPhNum = (TextView) findViewById(R.id.tv_totalPhNum);
        mClose = (ImageView) findViewById(R.id.check_close);
        mClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new LocationEvent("finish"));
                OrientApplication.app.setPageflage(1);
                CheckActivity1.this.finish();
            }
        });
        mBack = (LinearLayout) findViewById(R.id.lin_back);
        mBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//				(checkComplete() != 0 && currentTask.getLocation() == 1) || checkComplete() == 0
                if (currentTask.getTempType().equals("6")) {
                    if (checkComplete() != 0 && currentTask.getLocation() == 2) {
                        commitWarnInfo();
                    } else {
                        back();
                    }
                } else {
                    back();
                }
            }
        });

        mSumit = (LinearLayout) findViewById(R.id.check_sumit);
        mBottom = (RelativeLayout) findViewById(R.id.check_bottom);
        mProview = (Button) findViewById(R.id.check_proview);
        mNext = (Button) findViewById(R.id.check_next);
        mProview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (OrientApplication.app.getPageflage() > 1) {
                    OrientApplication.app.setPageflage(pagetype - 1);
                }
                finish();
            }
        });
        mNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(CheckActivity1.this, CheckActivity1.class);
                if (pagetype < rowsnum) {
                    OrientApplication.app.setPageflage(pagetype + 1);
                }
                startActivity(intent);

            }
        });
        mRefresh = (LinearLayout) findViewById(R.id.lin_check_refresh);
//		Drawable drawable=getResources().getDrawable(R.drawable.ic_action_refresh);
//		drawable.setBounds(0,0,40,40);
//		mRefresh.setCompoundDrawables(drawable,null,null,null);
        mRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckActivity1.actionStart(CheckActivity1.this, task_id, mHandler, "3");
                finish();
            }
        });
        mPandu = (LinearLayout) findViewById(R.id.lin_check_pandu);
        mPandu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OrientApplication.getApplication().setPanduFlag(1);
                List<Cell> actualvalCellList = DataSupport.where("markup=? and taskid=?", Config.actualval, task_id + "").order("verticalorder asc").find(Cell.class);
                List<String> actualvalVerti = new ArrayList<String>();
                StringBuffer prefixCodes = new StringBuffer();   //不符合标准的要求值
                if (actualvalCellList.size() != 0) {
                    for (Cell cell : actualvalCellList) {
                        String verti = cell.getVerticalorder();
                        if (!actualvalVerti.contains(verti)) {
                            actualvalVerti.add(verti);
                        }
                        if (cell.getMarkup().equals(Config.actualval) && OrientApplication.getApplication().getPageflage() == 1
                                && !cell.getHorizontalorder().equals("1")) {
                            pandu(cell, prefixCodes);
                        }
                    }
                    if (actualvalVerti.size() > 0 && currentTask.getTempType().equals("2")) {
                        for (int i = 0; i < actualvalVerti.size(); i++) {
//							int isBuFuhe = 0;
                            List<String> fuHeDuList = new ArrayList<String>();
                            List<Cell> actVertiCellList = DataSupport.where("markup=? and taskid=? and verticalorder=?", Config.actualval, task_id + "", actualvalVerti.get(i)).order("horizontalorder desc").find(Cell.class);
                            List<Cell> cellList = DataSupport.where("markup=? and taskid=? and verticalorder=? and horizontalorder=?", Config.actualval, task_id + "", actualvalVerti.get(i), String.valueOf(actVertiCellList.size())).find(Cell.class);
                            for (int j = 0; j < actVertiCellList.size(); j++) {
                                if (!actVertiCellList.get(j).getHorizontalorder().equals(String.valueOf(actVertiCellList.size()))) {
                                    String fuhedu = actVertiCellList.get(j).getIsFuhe();
                                    fuHeDuList.add(fuhedu);
                                }
                            }
                            Operation operation = DataSupport.where("cellid=? and taskid=?", cellList.get(0).getCellid(), task_id + "").find(Operation.class).get(0);
                            if (fuHeDuList.contains(Config.buhege)) {
                                judgeResult1(false, cellList.get(0), operation);
                            } else if (!fuHeDuList.contains(Config.buhege) && fuHeDuList.contains(Config.hege)) {
                                judgeResult1(true, cellList.get(0), operation);
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "没有查询到判读依据！", Toast.LENGTH_SHORT).show();
                }
                if (!prefixCodes.toString().equals("")) {
                    showDialog(prefixCodes.toString());
                } else {
                    CheckActivity1.actionStart(CheckActivity1.this, task_id, mHandler, "3");
                    finish();
                }
            }
        });
        mWanzhengxing = (LinearLayout) findViewById(R.id.lin_check_wanzheng);
        mWanzhengxing.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkComplete() != 0) {
                    Toast.makeText(context, "存在未填写的值!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "全部填写完成!", Toast.LENGTH_SHORT).show();
                }
                OrientApplication.getApplication().setIsWanzheng(1);
                CheckActivity1.actionStart(CheckActivity1.this, task_id, mHandler, "3");
                finish();
            }
        });
    }

    // 初始化参数
    private void initParam() {
        htmlList = new ArrayList<HtmlData>();
        htmlList_text = new ArrayList<HtmlData>();
        currentTask = DataSupport.where("taskid = ?", task_id + "").find(Task.class).get(0);
        rowsnum = Integer.parseInt(currentTask.getTablesize());

        pagetype = OrientApplication.app.getPageflage();

        if (rowsnum > 1) {
            mBottom.setVisibility(View.VISIBLE);
            if (pagetype == 1) {
                mProview.setVisibility(View.GONE);
                mNext.setVisibility(View.VISIBLE);
                mSumit.setVisibility(View.GONE);
                listView_1.setVisibility(View.VISIBLE);

            } else if (pagetype > 1 && pagetype < rowsnum) {
                mProview.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.VISIBLE);
                mSumit.setVisibility(View.GONE);
                listView_1.setVisibility(View.GONE);

            } else if (pagetype == rowsnum) {
                mProview.setVisibility(View.VISIBLE);
                mNext.setVisibility(View.GONE);
                mSumit.setVisibility(View.VISIBLE);
                listView_1.setVisibility(View.GONE);

            }
        } else {
            mBottom.setVisibility(View.GONE);
            mSumit.setVisibility(View.VISIBLE);
            listView_1.setVisibility(View.VISIBLE);
        }

        htmlDoc = HtmlHelper.getHtmlDoc(currentTask);
        aerospacedb = new AerospaceDB();
        //获得该表格的所有CELL数据
        if (rowsnum > 1) {
            cellList = loadCellAdapter(task_id, OrientApplication.getApplication().loginUser.getUserid(), pagetype);
        } else {
            cellList = loadCellAdapter(task_id, OrientApplication.getApplication().loginUser.getUserid(), 1);
        }
        width = ScreenUtils.getScreenWidth(context);
        headMap = DataSupport.where("horizontalorder=? and taskid=? and rowsid=?", "1", task_id + "", String.valueOf(pagetype)).order("verticalorder asc").find(Cell.class);
        if (headMap.size() > 8) {
            avewdith = 250;
        } else if (headMap.size() != 0) {
            avewdith = width / headMap.size();
        } else {
            Toast.makeText(CheckActivity1.this, "数据有误，请检查数据！", Toast.LENGTH_LONG);
        }
        cellCount = headMap.size();
        if (cellList.size() == 0) {
            Toast.makeText(CheckActivity1.this, "数据有误，请检查数据！", Toast.LENGTH_LONG).show();
            return;
        }
        rowCount = cellList.size() / cellCount;
        headMap.clear();
        for (int i = 1; i <= cellCount; i++) {
            List<Cell> cellList = DataSupport.where("horizontalorder=? and taskid=? and verticalorder=? and rowsid=?", "1", task_id + "", i + "", String.valueOf(pagetype)).find(Cell.class);
            if (cellList.size() > 0) {
                headMap.add(cellList.get(0));
            }
        }
    }

    /**
     * 初始化Header
     */
    private void initHeaderUI() {
        TableRow tablerow = new TableRow(context);
        tablerow.setBackgroundColor(Color.rgb(236, 247, 82));
        tablerow.setGravity(Gravity.CENTER_VERTICAL);
        for (Cell cell : headMap) {
            android.widget.TableRow.LayoutParams para4 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
            android.widget.TableRow.LayoutParams para411 = new android.widget.TableRow.LayoutParams(avewdith - 1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
            android.widget.TableRow.LayoutParams para412 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
            LinearLayout linear4 = new LinearLayout(context);
            linear4.setOrientation(LinearLayout.HORIZONTAL);
            linear4.setLayoutParams(para4);
            //初始化EditText
            TextView textview = new TextView(context);
            String labelName1 = cell.getRowname();
            textview.setGravity(Gravity.CENTER);
//			textview.setText(HtmlHelper.transCellLabel(replaceStr(labelName1)));
            textview.setText(replaceStr(labelName1));
            textview.setTextSize(16);
            textview.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            linear4.addView(textview, para411);
            //初始化textInfo
            ImageView image = new ImageView(context);
            image.setBackgroundResource(R.drawable.blacktiao);
            linear4.addView(image, para412);
            tablerow.addView(linear4, para4);
        }
        table_header.addView(tablerow, new TableLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, 66));
    }

    /**
     * 初始化内容(type=1,checkbox    2,edittext)
     */
    private void initContentUI() {
        for (int i = 1; i <= rowCount; i++) {
            TableRow tablerow = new TableRow(context);
            if (i % 2 == 0)
                tablerow.setBackgroundColor(Color.rgb(255, 255, 255));
            else
                tablerow.setBackgroundColor(Color.rgb(153, 204, 255));
            List<Cell> cellList = DataSupport.where("horizontalorder=? and taskid=? and rowsid=?", i + "", task_id + "", String.valueOf(pagetype)).order("verticalorder asc").find(Cell.class);
            List<Cell> newCellList = new ArrayList<Cell>();
            for (int j = 1; j <= cellList.size(); j++) {
                for (int k = 1; k <= cellList.size(); k++) {
                    Cell cell = cellList.get(k - 1);
                    if (cell.getVerticalorder().equals(j + "")) {
                        newCellList.add(cell);
                        break;
                    }
                }
            }
            for (final Cell cell : newCellList) {
                switch (cell.getCelltype()) {
                    case "LABEL":
                        android.widget.TableRow.LayoutParams para1 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para1_1 = new android.widget.TableRow.LayoutParams(avewdith - 1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para1_2 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        para1_1.gravity = Gravity.CENTER_VERTICAL;
                        LinearLayout linear0 = new LinearLayout(context);
                        linear0.setOrientation(LinearLayout.HORIZONTAL);
                        linear0.setLayoutParams(para1);
                        //初始化EditText
                        TextView textview = new TextView(context);
                        String labelName = cell.getTextvalue();
                        textview.setGravity(Gravity.CENTER);
                        textview.setWidth(avewdith);
//					textview.setHeight(85);
                        textview.setTextSize(16);
//					textview.setText(HtmlHelper.transCellLabel(replaceStr(labelName)));
                        textview.setText(replaceStr(labelName));
                        linear0.addView(textview, para1_1);
                        //初始化textInfo
                        ImageView image = new ImageView(context);
                        image.setBackgroundResource(R.drawable.blacktiao);
                        linear0.addView(image, para1_2);
                        tablerow.addView(linear0, para1);
                        break;
                    case "STRING":
                        android.widget.TableRow.LayoutParams para2 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para2_1 = new android.widget.TableRow.LayoutParams(avewdith - 1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para2_2 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);

                        LinearLayout linear2 = new LinearLayout(context);
                        para2_1.gravity = Gravity.CENTER_VERTICAL;
                        linear2.setOrientation(LinearLayout.HORIZONTAL);
                        linear2.setLayoutParams(para2);
                        //初始化EditText
                        final Operation operation2 = DataSupport.where("cellid=? and taskid=?", cell.getCellid(), task_id + "").find(Operation.class).get(0);
                        final String str = CommonTools.null2String(operation2.getOpvalue());
                        final EditText edittext2 = new EditText(context);
                        edittext2.setTextSize(16);
                        edittext2.setText(replaceStr(str));
                        if (OrientApplication.getApplication().getPanduFlag() == 1) {
                        }
                        if (cell.getIsFuhe() != null) {
                            if (cell.getIsFuhe().equals(Config.buhege)) {
//							edittext2.setTextColor(this.getResources().getColor(R.color.red));
                                edittext2.setBackgroundColor(getResources().getColor(R.color.red));
                            }
                        }
                        if (currentTask.getTempType().equals("1") && cell.getRowname().equals("结论") && !cell.getOpvalue().equals("")) {
                            if (checkConclusionByCell(cell.getOpvalue())) {
                                edittext2.setBackgroundColor(getResources().getColor(R.color.gray));
                            } else {
                                edittext2.setBackgroundColor(getResources().getColor(R.color.red));
                            }
                        }
                        if (OrientApplication.getApplication().getIsWanzheng() == 1 && str == "") {
                            edittext2.setBackground(getResources().getDrawable(R.drawable.et_bg));
                        }
                        edittext2.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence text, int start, int before,
                                                      int count) {

                            }

                            @Override
                            public void beforeTextChanged(CharSequence text, int start,
                                                          int count, int after) {

                            }

                            @Override
                            public void afterTextChanged(Editable edit) {
                                String str = edit.toString();
                                cell.setOpvalue(str);
                                cell.update(cell.getId());
                                operation2.setOpvalue(str);
                                operation2.update(operation2.getId());
                                HtmlData data = new HtmlData(cell, operation2.getRealcellid());
                                htmlList_text.add(data);
//		    	            HtmlHelper.changeTextValue(htmlDoc, cell, operation2.getRealcellid());
                                if (currentTask.getTempType().equals("1") && cell.getRowname().equals("结论") && !str.equals("")) {
                                    if (checkConclusionByCell(str)) {
                                        edittext2.setBackgroundColor(getResources().getColor(R.color.gray));
                                    } else {
                                        edittext2.setBackgroundColor(getResources().getColor(R.color.red));
                                    }
                                }
                            }
                        });
                        edittext2.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                getQuickSellectParm(currentTask, cell, operation2, edittext2);
                                return false;
                            }
                        });
                        linear2.addView(edittext2, para2_1);

                        //下拉单选
                        final NiceSpinner spinner = new NiceSpinner(context);
                        List<String> dataset = new LinkedList<>();
                        //下拉框内容 常规性检验表下拉框值为:合格，不合格
                        if (currentTask.getTempType().equals("1")) {
                            if (str.equals("")) {
                                dataset.add(Config.hege);
                                dataset.add(Config.buhege);
                            } else {
                                dataset.add(replaceStr(str));
                                if (replaceStr(str).equals(Config.hege)) {
                                    dataset.add(Config.buhege);
                                } else {
                                    dataset.add(Config.hege);
                                    spinner.setBackgroundColor(getResources().getColor(R.color.red));
                                }
                            }
                        }
                        //下拉框内容 验收报告下拉框值为:验收通过，验收不通过
                        else if (currentTask.getTempType().equals("6")) {
                            if (str.equals("")) {
                                dataset.add(Config.ystg);
                                dataset.add(Config.ysbtg);
                            } else {
                                dataset.add(replaceStr(str));
                                if (replaceStr(str).equals(Config.ystg)) {
                                    dataset.add(Config.ysbtg);
                                } else {
                                    dataset.add(Config.ystg);
                                    spinner.setBackgroundColor(getResources().getColor(R.color.red));
                                }
                            }
                        }
                        spinner.attachDataSource(dataset);
                        spinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
                            @Override
                            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                                String item = parent.getItemAtPosition(position).toString();
                                cell.setOpvalue(item);
                                cell.update(cell.getId());
                                operation2.setOpvalue(item);
                                operation2.update(operation2.getId());
                                HtmlData data = new HtmlData(cell, operation2.getRealcellid());
                                htmlList_text.add(data);
                                if (checkConclusionByCell(item)) {
                                    spinner.setBackgroundColor(getResources().getColor(R.color.White));
                                    judgeResult(true, cell, operation2);
                                } else {
                                    spinner.setBackgroundColor(getResources().getColor(R.color.red));
                                    judgeResult(false, cell, operation2);
                                }
                            }
                        });
                        spinner.setLayoutParams(para2);

                        //初始化textInfo
                        ImageView image2 = new ImageView(context);
                        image2.setBackgroundResource(R.drawable.blacktiao);
                        linear2.addView(image2, para2_2);
                        if ((currentTask.getTempType().equals("1") && cell.getRowname().equals("结论")) ||
                                (currentTask.getTempType().equals("6")&& cell.getHorizontalorder().equals("2")) ) {
                            if (str.equals("")) {
                                if (currentTask.getTempType().equals("1")) {
                                    cell.setOpvalue(Config.hege);
                                    cell.update(cell.getId());
                                    operation2.setOpvalue(Config.hege);
                                    operation2.update(operation2.getId());
                                    HtmlData data = new HtmlData(cell, operation2.getRealcellid());
                                    htmlList_text.add(data);
                                    judgeResult(true, cell, operation2);
                                } else if (currentTask.getTempType().equals("6")) {
                                    cell.setOpvalue(Config.ystg);
                                    cell.update(cell.getId());
                                    operation2.setOpvalue(Config.ystg);
                                    operation2.update(operation2.getId());
                                    HtmlData data = new HtmlData(cell, operation2.getRealcellid());
                                    htmlList_text.add(data);
                                    judgeResult(true, cell, operation2);
                                }
                            }
                            tablerow.addView(spinner, para2);
                        } else {
                            tablerow.addView(linear2, para2);
                        }
                        break;
                    case "DIGIT":
                        break;
                    case "HOOK":
                        android.widget.TableRow.LayoutParams layoutParams3 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams layoutParams3_1 = new android.widget.TableRow.LayoutParams((avewdith - 1) / 2, 100);
                        android.widget.TableRow.LayoutParams layoutParams3_2 = new android.widget.TableRow.LayoutParams(1, 100);
                        LinearLayout linear3 = new LinearLayout(context);
                        linear3.setOrientation(LinearLayout.HORIZONTAL);
                        linear3.setLayoutParams(layoutParams3);
                        layoutParams3.gravity = Gravity.CENTER;
                        layoutParams3_1.setMargins((avewdith - 1) / 2, 0, 0, 0);                //左上右下
                        CheckBox cb3 = new CheckBox(context);
                        final Operation operation3 = DataSupport.where("cellid=? and taskid=?", cell.getCellid(), task_id + "").find(Operation.class).get(0);
                        String value = CommonTools.null2String(operation3.getOpvalue());
                        if (value != null) {
                            if (value.equals("is")) {                //打钩状态
                                cb3.setChecked(true);
                            } else if (value.equals("no")) {    //不打钩状态
                                cb3.setChecked(false);
                            } else {
                                cb3.setChecked(false);
                            }
                        }
                        cb3.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    cell.setIshook("is");
                                    cell.update(cell.getId());
                                    operation3.setOpvalue("is");
                                    operation3.update(operation3.getId());
                                    String firsttime = currentTask.getStartTime();
                                    if (firsttime == null || firsttime.equals("")) {
                                        currentTask.setStartTime(DateUtil.getCurrentDate());
                                        currentTask.update(currentTask.getId());
                                    }
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation3.getRealcellid());
                                    HtmlData data = new HtmlData(cell, operation3.getRealcellid());
                                    htmlList.add(data);
                                    //记录第一次操作的时间
                                    Toast.makeText(context, "选中", Toast.LENGTH_SHORT).show();
                                } else {
                                    cell.setIshook("no");
                                    cell.update(cell.getId());
                                    operation3.setOpvalue("no");
                                    operation3.update(operation3.getId());
                                    HtmlData data = new HtmlData(cell, operation3.getRealcellid());
                                    htmlList.add(data);
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation3.getRealcellid());
                                    Toast.makeText(context, "未选中", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        ImageView img = new ImageView(this);
                        img.setBackgroundResource(R.drawable.blacktiao);
                        linear3.addView(cb3, layoutParams3_1);
                        linear3.addView(img, layoutParams3_2);
                        tablerow.addView(linear3, layoutParams3);
                        break;
                    case "HOOKPHOTO":
                        android.widget.TableRow.LayoutParams para4 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para4_1 = new android.widget.TableRow.LayoutParams((avewdith - 81) / 2, 100);
                        android.widget.TableRow.LayoutParams para4_2 = new android.widget.TableRow.LayoutParams(80, 70);
                        android.widget.TableRow.LayoutParams para4_3 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        LinearLayout linear4 = new LinearLayout(context);
                        para4_1.gravity = Gravity.CENTER;
                        para4_2.gravity = Gravity.CENTER_VERTICAL;
                        para4_1.setMargins((avewdith - 81) / 2, 0, 0, 0);                //左上右下
                        linear4.setOrientation(LinearLayout.HORIZONTAL);
                        linear4.setLayoutParams(para4);
                        //初始化EditText
                        CheckBox checkbox4 = new CheckBox(context);
//					checkbox4.setGravity(Gravity.CENTER);
                        final Operation operation4 = DataSupport.where("cellid=? and taskid=?", cell.getCellid(), task_id + "").find(Operation.class).get(0);
                        String value4 = operation4.getOpvalue();
                        if (value4 != null) {
                            if (value4.equals("is")) {                //打钩状态
                                checkbox4.setChecked(true);
                            } else if (value4.equals("no")) {    //不打钩状态
                                checkbox4.setChecked(false);
                            } else {
                                checkbox4.setChecked(false);
                            }
                        }
                        checkbox4.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    cell.setIshook("is");
                                    cell.update(cell.getId());
                                    operation4.setOpvalue("is");
                                    operation4.update(operation4.getId());
                                    String firsttime = currentTask.getStartTime();
                                    if (firsttime == null || firsttime.equals("")) {
                                        currentTask.setStartTime(DateUtil.getCurrentDate());
                                        currentTask.update(currentTask.getId());
                                    }
                                    HtmlData data = new HtmlData(cell, operation4.getRealcellid());
                                    htmlList.add(data);
//								HtmlHelper.changeCheckValue(htmlDoc, cell, operation4.getRealcellid());
                                    Toast.makeText(context, "选中", Toast.LENGTH_SHORT).show();
                                } else {
                                    cell.setIshook("no");
                                    cell.update(cell.getId());
                                    operation4.setOpvalue("no");
                                    operation4.update(operation4.getId());
                                    HtmlData data = new HtmlData(cell, operation4.getRealcellid());
                                    htmlList.add(data);
//								HtmlHelper.changeCheckValue(htmlDoc, cell, operation4.getRealcellid());
                                    Toast.makeText(context, "未选中", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        linear4.addView(checkbox4, para4_1);

                        //初始化textInfo
                        NumImageButton image4_1 = new NumImageButton(context);
                        image4_1.setBackgroundResource(R.drawable.takephoto);
                        final String userId4 = OrientApplication.getApplication().loginUser.getUserid();
                        final String taskId4 = cell.getTaskid();
                        final String rowNum4 = cell.getHorizontalorder();
                        final String cellId4 = cell.getCellid();

                        if (picturenumbers == 0) {
                            if (operation4 != null) {
                                final String path1 = Environment.getExternalStorageDirectory() + Config.v2photoPath
                                        + File.separator
                                        + OrientApplication.getApplication().rw.getRwid()
                                        + File.separator
                                        + taskId4
                                        + File.separator
                                        + CommonTools.null2String(operation4.getOperationid())
                                        + File.separator;
                                getPictures(path1);
                            }
                        }
                        image4_1.setNum(picturenumbers);
                        totalPhNumber = totalPhNumber + picturenumbers;
                        picturenumbers = 0;
                        image4_1.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showPhotoDialog(cellId4, taskId4, operation4);
                            }
                        });
                        linear4.addView(image4_1, para4_2);
                        ImageView image4 = new ImageView(context);
                        image4.setBackgroundResource(R.drawable.blacktiao);
                        linear4.addView(image4, para4_3);
                        tablerow.addView(linear4, para4);
                        break;
                    case "STRINGPHOTO":
                        android.widget.TableRow.LayoutParams para5 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para5_1 = new android.widget.TableRow.LayoutParams(avewdith - 81, android.widget.TableRow.LayoutParams.MATCH_PARENT - 30);
                        android.widget.TableRow.LayoutParams para5_2 = new android.widget.TableRow.LayoutParams(80, 70);
                        android.widget.TableRow.LayoutParams para5_3 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        LinearLayout linear5 = new LinearLayout(context);
                        para5_1.gravity = Gravity.CENTER_VERTICAL;
                        para5_2.gravity = Gravity.CENTER_VERTICAL;
                        linear5.setOrientation(LinearLayout.HORIZONTAL);
                        linear5.setLayoutParams(para5);
                        //初始化EditText
                        final EditText edit5 = new EditText(context);
                        final Operation operation5 = DataSupport.where("cellid=? and taskid=?", cell.getCellid(), task_id + "").find(Operation.class).get(0);
                        String stringdata5 = CommonTools.null2String(operation5.getOpvalue());
                        edit5.setTextSize(16);
                        edit5.setText(replaceStr(stringdata5));
                        if (OrientApplication.getApplication().getIsWanzheng() == 1 && stringdata5 == "") {
                            edit5.setBackground(getResources().getDrawable(R.drawable.et_bg));
                        }
                        linear5.addView(edit5, para5_1);
                        //初始化textInfo
                        final String userId5 = OrientApplication.getApplication().loginUser.getUserid();
                        final String taskId5 = cell.getTaskid();
                        final String rowNum5 = cell.getHorizontalorder();
                        final String cellId5 = cell.getCellid();

                        if (picturenumbers == 0) {
                            if (operation5 != null) {
                                final String path1 = Environment.getExternalStorageDirectory() + Config.v2photoPath
                                        + File.separator
                                        + OrientApplication.getApplication().rw.getRwid()
                                        + File.separator
                                        + taskId5
                                        + File.separator
                                        + CommonTools.null2String(operation5.getOperationid())
                                        + File.separator;
                                getPictures(path1);
                            }
                        }

                        NumImageButton image5 = new NumImageButton(context);
                        image5.setBackgroundResource(R.drawable.takephoto);
                        image5.setNum(picturenumbers);
                        totalPhNumber = totalPhNumber + picturenumbers;
                        picturenumbers = 0;
                        image5.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                showPhotoDialog(cellId5, taskId5, operation5);
                            }
                        });
                        edit5.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence text, int start, int before,
                                                      int count) {
                            }

                            @Override
                            public void beforeTextChanged(CharSequence text, int start,
                                                          int count, int after) {
                            }

                            @Override
                            public void afterTextChanged(Editable edit) {
                                String str = edit5.getText().toString();
                                cell.setOpvalue(str);
                                cell.update(cell.getId());
//		    	        	Task task = DataSupport.where("rwid=? and taskid = ?", OrientApplication.getApplication().rw.getRwid(), cell.getTaskid()).find(Task.class).get(0);
//		    	        	Operation operation = cell.getStringOperation();
                                operation5.setOpvalue(str);
                                operation5.update(operation5.getId());
                                String firsttime = currentTask.getStartTime();
                                if (firsttime == null || firsttime.equals("")) {
                                    currentTask.setStartTime(DateUtil.getCurrentDate());
                                    currentTask.save();
                                }
//		    	        	HtmlHelper.changeTextValue(htmlDoc, cell, operation5.getRealcellid());
                                HtmlData data = new HtmlData(cell, operation5.getRealcellid());
                                htmlList_text.add(data);
                            }
                        });
                        edit5.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                getQuickSellectParm(currentTask, cell, operation5, edit5);
                                return false;
                            }
                        });
                        linear5.addView(image5, para5_2);
                        ImageView image5_1 = new ImageView(context);
                        image5_1.setBackgroundResource(R.drawable.blacktiao);
                        linear5.addView(image5_1, para5_3);
                        tablerow.addView(linear5, para5);
                        break;
                    case "HOOKSTRING":
                        android.widget.TableRow.LayoutParams para6 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para6_1 = new android.widget.TableRow.LayoutParams(avewdith - 81, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para6_2 = new android.widget.TableRow.LayoutParams(80, 80);
                        android.widget.TableRow.LayoutParams para6_3 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        LinearLayout linear6 = new LinearLayout(context);
                        linear6.setOrientation(LinearLayout.HORIZONTAL);
                        para6_1.gravity = Gravity.CENTER_VERTICAL;
                        para6_2.gravity = Gravity.CENTER_VERTICAL;
                        linear6.setLayoutParams(para6);
                        //初始化EditText
                        final EditText edit6 = new EditText(context);
                        final Operation operation6_1 = DataSupport.where("cellid=? and taskid=? and type=?", cell.getCellid(), task_id + "", "1").find(Operation.class).get(0);            //checkbox
                        final Operation operation6_2 = DataSupport.where("cellid=? and taskid=? and type=?", cell.getCellid(), task_id + "", "2").find(Operation.class).get(0);            //edittext
//					final Operation operation6_1 = cell.getHookOperation();
//					final Operation operation6_2 = cell.getStringOperation();
                        String stringdata6 = CommonTools.null2String(operation6_2.getOpvalue());
                        edit6.setText(replaceStr(stringdata6));
                        edit6.setTextSize(16);
                        if (OrientApplication.getApplication().getIsWanzheng() == 1 && stringdata6 == "") {
                            edit6.setBackground(getResources().getDrawable(R.drawable.et_bg));
                        }
                        linear6.addView(edit6, para6_1);
                        //初始化checkbox
                        CheckBox checkbox6 = new CheckBox(context);
                        String value6 = operation6_1.getOpvalue();
                        if (value6 != null) {
                            if (value6.equals("is")) {                //打钩状态
                                checkbox6.setChecked(true);
                            } else if (value6.equals("no")) {    //不打钩状态
                                checkbox6.setChecked(false);
                            } else {
                                checkbox6.setChecked(false);
                            }
                        }
                        checkbox6.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    cell.setIshook("is");
                                    cell.update(cell.getId());
                                    operation6_1.setOpvalue("is");
                                    operation6_1.update(operation6_1.getId());
                                    String firsttime = currentTask.getStartTime();
                                    if (firsttime == null || firsttime.equals("")) {
                                        currentTask.setStartTime(DateUtil.getCurrentDate());
                                        currentTask.update(currentTask.getId());
                                    }
                                    HtmlData data = new HtmlData(cell, operation6_1.getRealcellid());
                                    htmlList.add(data);
//								HtmlHelper.changeCheckValue(htmlDoc, cell, operation6_1.getRealcellid());
                                    Toast.makeText(context, "选中", Toast.LENGTH_SHORT).show();
                                } else {
                                    cell.setIshook("no");
                                    cell.update(cell.getId());
                                    operation6_1.setOpvalue("no");
                                    operation6_1.update(operation6_1.getId());
                                    HtmlData data = new HtmlData(cell, operation6_1.getRealcellid());
                                    htmlList.add(data);
//								HtmlHelper.changeCheckValue(htmlDoc, cell, operation6_1.getRealcellid());
                                    Toast.makeText(context, "未选中", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        linear6.addView(checkbox6, para6_2);
                        edit6.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence text, int start, int before,
                                                      int count) {
                            }

                            @Override
                            public void beforeTextChanged(CharSequence text, int start,
                                                          int count, int after) {
                            }

                            @Override
                            public void afterTextChanged(Editable edit) {
                                String str = edit6.getText().toString();
                                cell.setOpvalue(str);
                                cell.update(cell.getId());
//		    	        	Task task = DataSupport.where("rwid=? and taskid = ?", OrientApplication.getApplication().rw.getRwid(), cell.getTaskid()).find(Task.class).get(0);
                                operation6_2.setOpvalue(str);
                                operation6_2.update(operation6_2.getId());
                                String firsttime = currentTask.getStartTime();
                                if (firsttime == null || firsttime.equals("")) {
                                    currentTask.setStartTime(DateUtil.getCurrentDate());
                                    currentTask.update(currentTask.getId());
                                }
//		    	        	HtmlHelper.changeTextValue(htmlDoc, cell, operation6_2.getRealcellid());
                                HtmlData data = new HtmlData(cell, operation6_2.getRealcellid());
                                htmlList_text.add(data);
                            }
                        });
                        edit6.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                getQuickSellectParm(currentTask, cell, operation6_2, edit6);
                                return false;
                            }
                        });
                        ImageView image6_1 = new ImageView(context);
                        image6_1.setBackgroundResource(R.drawable.blacktiao);
                        linear6.addView(image6_1, para6_3);
                        tablerow.addView(linear6, para6);
                        break;
                    case "HOOKSTRINGPHOTO":
                        android.widget.TableRow.LayoutParams para7 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para7_1 = new android.widget.TableRow.LayoutParams(60, 60);            //checkbox
                        android.widget.TableRow.LayoutParams para7_2 = new android.widget.TableRow.LayoutParams(avewdith - 141, android.widget.TableRow.LayoutParams.MATCH_PARENT);            //string
                        android.widget.TableRow.LayoutParams para7_3 = new android.widget.TableRow.LayoutParams(80, 70);            //photo
                        android.widget.TableRow.LayoutParams para7_4 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        LinearLayout linear7 = new LinearLayout(context);
                        para7_3.gravity = Gravity.CENTER_VERTICAL;
                        para7_2.gravity = Gravity.CENTER_VERTICAL;
                        para7_1.gravity = Gravity.CENTER_VERTICAL;
                        linear7.setOrientation(LinearLayout.HORIZONTAL);
                        linear7.setLayoutParams(para7);
                        //初始化EditText
                        final EditText edit7 = new EditText(context);
                        final Operation operation71 = DataSupport.where("cellid=? and taskid=? and type=?", cell.getCellid(), task_id + "", "1").find(Operation.class).get(0);            //checkbox
                        final Operation operation72 = DataSupport.where("cellid=? and taskid=? and type=?", cell.getCellid(), task_id + "", "2").find(Operation.class).get(0);            //edittext

                        String stringdata7 = CommonTools.null2String(operation72.getOpvalue());
                        edit7.setTextSize(16);
                        edit7.setText(replaceStr(stringdata7));
                        if (OrientApplication.getApplication().getIsWanzheng() == 1 && stringdata7 == "") {
                            edit7.setBackground(getResources().getDrawable(R.drawable.et_bg));
                        }
                        linear7.addView(edit7, para7_2);
                        //初始化checkbox
                        CheckBox cb7 = new CheckBox(context);
                        String value7 = CommonTools.null2String(operation71.getOpvalue());
                        if (value7 != null) {
                            if (value7.equals("is")) {                        //打钩状态
                                cb7.setChecked(true);
                            } else if (value7.equals("no")) {            //不打钩状态
                                cb7.setChecked(false);
                            } else {
                                cb7.setChecked(false);
                            }
                        }
                        linear7.addView(cb7, para7_1);
                        cb7.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    cell.setIshook("is");
                                    cell.update(cell.getId());
                                    operation71.setOpvalue("is");
                                    operation71.update(operation71.getId());
                                    String firsttime = currentTask.getStartTime();
                                    if (firsttime == null || firsttime.equals("")) {
                                        currentTask.setStartTime(DateUtil.getCurrentDate());
                                        currentTask.save();
                                    }
                                    HtmlData data = new HtmlData(cell, operation71.getRealcellid());
                                    htmlList.add(data);
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation71.getRealcellid());
                                    //记录第一次操作的时间
                                    Toast.makeText(context, "选中", Toast.LENGTH_SHORT).show();
                                } else {
                                    cell.setIshook("no");
                                    cell.update(cell.getId());
                                    operation71.setOpvalue("no");
                                    operation71.update(operation71.getId());
                                    HtmlData data = new HtmlData(cell, operation71.getRealcellid());
                                    htmlList.add(data);
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation71.getRealcellid());
                                    Toast.makeText(context, "未选中", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        edit7.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence text, int start, int before,
                                                      int count) {
                            }

                            @Override
                            public void beforeTextChanged(CharSequence text, int start,
                                                          int count, int after) {
                            }

                            @Override
                            public void afterTextChanged(Editable edit) {
                                String str = edit7.getText().toString();
                                cell.setOpvalue(str);
                                cell.update(cell.getId());
                                operation72.setOpvalue(str);
                                operation72.update(operation72.getId());
                                String firsttime = currentTask.getStartTime();
                                if (firsttime == null || firsttime.equals("")) {
                                    currentTask.setStartTime(DateUtil.getCurrentDate());
                                    currentTask.update(currentTask.getId());
                                }
                                HtmlData data = new HtmlData(cell, operation72.getRealcellid());
                                htmlList_text.add(data);
                            }
                        });
                        final String userId7 = OrientApplication.getApplication().loginUser.getUserid();
                        final String taskId7 = cell.getTaskid();
                        final String rowNum7 = cell.getHorizontalorder();
                        final String cellId7 = cell.getCellid();

                        if (picturenumbers == 0) {
                            Operation operation = null;
                            if (operation71.getIsmedia().equals("TRUE")) {
                                operation = operation71;

                            }
                            if (operation72.getIsmedia().equals("TRUE")) {
                                operation = operation72;

                            }
                            if (operation != null) {
                                final String path1 = Environment.getExternalStorageDirectory() + Config.v2photoPath
                                        + File.separator
                                        + OrientApplication.getApplication().rw.getRwid()
                                        + File.separator
                                        + taskId7
                                        + File.separator
                                        + CommonTools.null2String(operation.getOperationid())
                                        + File.separator;
                                getPictures(path1);
                            }
                        }


                        NumImageButton image7_1 = new NumImageButton(context);
                        image7_1.setBackgroundResource(R.drawable.takephoto);
                        image7_1.setNum(picturenumbers);
                        totalPhNumber = totalPhNumber + picturenumbers;
                        picturenumbers = 0;
                        image7_1.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Operation temp = null;
                                if (operation71.getIsmedia().equals("TRUE")) {
                                    temp = operation71;
                                    HtmlHelper.changePhotoValue(htmlDoc, operation71);
                                }
                                if (operation72.getIsmedia().equals("TRUE")) {
                                    temp = operation72;
                                    HtmlHelper.changePhotoValue(htmlDoc, operation72);
                                }
                                if (temp != null)
                                    showPhotoDialog(cellId7, taskId7, temp);
                            }
                        });
                        linear7.addView(image7_1, para7_3);
                        ImageView image7_2 = new ImageView(context);
                        image7_2.setBackgroundResource(R.drawable.blacktiao);
                        linear7.addView(image7_2, para7_4);
                        tablerow.addView(linear7, para7);
                        break;
                    case "HOOKBITMAP":
                        android.widget.TableRow.LayoutParams para8 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para8_1 = new android.widget.TableRow.LayoutParams((avewdith - 81) / 2, 60);            //checkbox
                        android.widget.TableRow.LayoutParams para8_2 = new android.widget.TableRow.LayoutParams(80, 80);                //bitmap
                        android.widget.TableRow.LayoutParams para8_3 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        LinearLayout linear8 = new LinearLayout(context);
                        linear8.setOrientation(LinearLayout.HORIZONTAL);
                        linear8.setLayoutParams(para8);
                        //初始化EditText
                        final Operation operation8 = DataSupport.where("cellid=? and taskid=?", cell.getCellid(), task_id + "").find(Operation.class).get(0);
                        final String opId8 = operation8.getSketchmap();
                        final String userId8 = OrientApplication.getApplication().loginUser.getUserid();
                        final String taskId8 = cell.getTaskid();
                        //初始化checkbox
                        CheckBox cb8 = new CheckBox(context);
                        para8_1.setMargins((avewdith - 81) / 2, 0, 0, 0);
                        linear8.addView(cb8, para8_1);
                        String value8 = CommonTools.null2String(operation8.getOpvalue());
                        if (value8 != null) {
                            if (value8.equals("is")) {                        //打钩状态
                                cb8.setChecked(true);
                            } else if (value8.equals("no")) {                //不打钩状态
                                cb8.setChecked(false);
                            } else {
                                cb8.setChecked(false);
                            }
                        }
                        cb8.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
//								Task task = DataSupport.where("rwid=? and taskid = ?", OrientApplication.getApplication().rw.getRwid(), cell.getTaskid()).find(Task.class).get(0);
                                    cell.setIshook("is");
                                    cell.update(cell.getId());
                                    operation8.setOpvalue("is");
                                    operation8.update(operation8.getId());
                                    String firsttime = currentTask.getStartTime();
                                    if (firsttime == null || firsttime.equals("")) {
                                        currentTask.setStartTime(DateUtil.getCurrentDate());
                                        currentTask.update(currentTask.getId());
                                    }
                                    HtmlData data = new HtmlData(cell, operation8.getRealcellid());
                                    htmlList.add(data);
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation8.getRealcellid());
                                    //记录第一次操作的时间
                                    Toast.makeText(context, "选中", Toast.LENGTH_SHORT).show();
                                } else {
                                    cell.setIshook("no");
                                    cell.update(cell.getId());
                                    operation8.setOpvalue("no");
                                    operation8.update(operation8.getId());
                                    HtmlData data = new HtmlData(cell, operation8.getRealcellid());
                                    htmlList.add(data);
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation8.getRealcellid());
                                    Toast.makeText(context, "未选中", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        final String absPath = Environment.getExternalStorageDirectory()
                                + Config.opphotoPath + "/"
                                + taskId8 + "/" + opId8 + ".jpg";
                        Bitmap bitmap = BitmapFactory.decodeFile(absPath);
                        final ImageView image8 = new ImageButton(context);
                        if (bitmap != null)

                            Glide
                                    .with(context)
                                    .load(absPath)
                                    .override(80, 80)
                                    .fitCenter()
                                    .into(image8);

                        image8.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BitmapDialogActivity.actionStart(context, userId8, taskId8, opId8);
                            }
                        });
                        linear8.addView(image8, para8_2);
                        ImageView image8_2 = new ImageView(context);
                        image8_2.setBackgroundResource(R.drawable.blacktiao);
                        linear8.addView(image8_2, para8_3);
                        tablerow.addView(linear8, para8);
                        break;
                    case "HOOKSTRINGBITMAP":
                        android.widget.TableRow.LayoutParams para9 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para9_1 = new android.widget.TableRow.LayoutParams(60, 60);            //checkbox
                        android.widget.TableRow.LayoutParams para9_2 = new android.widget.TableRow.LayoutParams(avewdith - 141, android.widget.TableRow.LayoutParams.MATCH_PARENT);                //bitmap
                        android.widget.TableRow.LayoutParams para9_3 = new android.widget.TableRow.LayoutParams(80, 80);
                        android.widget.TableRow.LayoutParams para9_4 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        LinearLayout linear9 = new LinearLayout(context);
                        linear9.setOrientation(LinearLayout.HORIZONTAL);
                        linear9.setLayoutParams(para9);
                        //初始化EditText
                        final EditText edit9 = new EditText(context);
                        final Operation operation91 = DataSupport.where("cellid=? and taskid=? and type=?", cell.getCellid(), task_id + "", "1").find(Operation.class).get(0);            //checkbox
                        final Operation operation92 = DataSupport.where("cellid=? and taskid=? and type=?", cell.getCellid(), task_id + "", "2").find(Operation.class).get(0);            //edittext
//					final Operation operation91 = cell.getHookOperation();
//					final Operation operation92 = cell.getStringOperation();
                        String stringdata9 = CommonTools.null2String(operation92.getOpvalue());
                        edit9.setText(replaceStr(stringdata9));
                        edit9.setTextSize(16);
                        linear9.addView(edit9, para9_2);
                        //初始化checkbox
                        CheckBox cb9 = new CheckBox(context);
                        linear9.addView(cb9, para9_1);
                        String value9 = CommonTools.null2String(operation91.getOpvalue());
                        if (value9 != null) {
                            if (value9.equals("is")) {                        //打钩状态
                                cb9.setChecked(true);
                            } else if (value9.equals("no")) {            //不打钩状态
                                cb9.setChecked(false);
                            } else {
                                cb9.setChecked(false);
                            }
                        }
                        cb9.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    Task task = DataSupport.where("PCLBId=? and taskid = ?", OrientApplication.getApplication().rw.getRwid(), cell.getTaskid()).find(Task.class).get(0);
                                    cell.setIshook("is");
                                    cell.update(cell.getId());
                                    operation91.setOpvalue("is");
                                    operation91.update(operation91.getId());
                                    String firsttime = task.getStartTime();
                                    if (firsttime == null || firsttime.equals("")) {
                                        task.setStartTime(DateUtil.getCurrentDate());
                                        task.save();
                                    }
                                    HtmlData data = new HtmlData(cell, operation91.getRealcellid());
                                    htmlList.add(data);
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation91.getRealcellid());
                                    //记录第一次操作的时间
                                    Toast.makeText(context, "选中", Toast.LENGTH_SHORT).show();
                                } else {
                                    cell.setIshook("no");
                                    cell.update(cell.getId());
                                    operation91.setOpvalue("no");
                                    operation91.update(operation91.getId());
                                    HtmlData data = new HtmlData(cell, operation91.getRealcellid());
                                    htmlList.add(data);
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation91.getRealcellid());
                                    Toast.makeText(context, "未选中", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        edit9.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence text, int start, int before,
                                                      int count) {
                            }

                            @Override
                            public void beforeTextChanged(CharSequence text, int start,
                                                          int count, int after) {
                            }

                            @Override
                            public void afterTextChanged(Editable edit) {
                                String str = edit9.getText().toString();
                                cell.setOpvalue(str);
                                cell.update(cell.getId());
//		    	        	Task task = DataSupport.where("rwid=? and taskid = ?", OrientApplication.getApplication().rw.getRwid(), cell.getTaskid()).find(Task.class).get(0);
                                operation92.setOpvalue(str);
                                operation92.update(operation92.getId());
                                String firsttime = currentTask.getStartTime();
                                if (firsttime == null || firsttime.equals("")) {
                                    currentTask.setStartTime(DateUtil.getCurrentDate());
                                    currentTask.update(currentTask.getId());
                                }
//		    	        	HtmlHelper.changeTextValue(htmlDoc, cell, operation92.getRealcellid());
                                HtmlData data = new HtmlData(cell, operation92.getRealcellid());
                                htmlList_text.add(data);
                            }
                        });
                        String opId9 = "";
                        if (!operation91.getSketchmap().equals("") && operation91.getSketchmap() != null) {
                            opId9 = operation91.getSketchmap();
                        }
                        if (!operation92.getSketchmap().equals("") && operation92.getSketchmap() != null) {
                            opId9 = operation92.getSketchmap();
                        }
                        final String sketbitmap = opId9;
                        final String userId9 = OrientApplication.getApplication().loginUser.getUserid();
                        final String taskId9 = cell.getTaskid();
                        final String absPath9 = Environment.getExternalStorageDirectory()
                                + Config.opphotoPath + "/"
                                + taskId9 + "/" + sketbitmap + ".jpg";
                        Bitmap bitmap9 = BitmapFactory.decodeFile(absPath9);
                        final ImageView image9 = new ImageButton(context);
                        if (bitmap9 != null)

                            Glide
                                    .with(getApplicationContext())
                                    .load(absPath9)
                                    .override(80, 80)
                                    .fitCenter()
                                    .into(image9);

                        image9.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BitmapDialogActivity.actionStart(context, userId9, taskId9, sketbitmap);
                            }
                        });
                        linear9.addView(image9, para9_3);
                        ImageView image9_2 = new ImageView(context);
                        image9_2.setBackgroundResource(R.drawable.blacktiao);
                        linear9.addView(image9_2, para9_4);
                        tablerow.addView(linear9, para9);
                        break;
                    case "HOOKSTRINGPHOTOBITMAP":
                        android.widget.TableRow.LayoutParams para10 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para10_1 = new android.widget.TableRow.LayoutParams(60, 60);                //checkbox
                        android.widget.TableRow.LayoutParams para10_2 = new android.widget.TableRow.LayoutParams(avewdith - 221, 100);                //string
                        android.widget.TableRow.LayoutParams para10_3 = new android.widget.TableRow.LayoutParams(80, 80);                //photo
                        android.widget.TableRow.LayoutParams para10_4 = new android.widget.TableRow.LayoutParams(80, 80);                //bitmap
                        android.widget.TableRow.LayoutParams para10_5 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        LinearLayout linear10 = new LinearLayout(context);
                        linear10.setOrientation(LinearLayout.HORIZONTAL);
                        linear10.setLayoutParams(para10);
                        //初始化EditText
                        final Operation operation10_1 = DataSupport.where("cellid=? and taskid=? and type=?", cell.getCellid(), task_id + "", "1").find(Operation.class).get(0);
                        final Operation operation10_2 = DataSupport.where("cellid=? and taskid=? and type=?", cell.getCellid(), task_id + "", "2").find(Operation.class).get(0);

                        //初始化EditText
                        String stringdata10 = CommonTools.null2String(operation10_2.getOpvalue());
                        final EditText edit10 = new EditText(context);
                        edit10.setText(replaceStr(stringdata10));
                        edit10.setTextSize(16);
                        linear10.addView(edit10, para10_2);
                        //初始化checkbox
                        CheckBox cb10 = new CheckBox(context);
                        para10_1.gravity = Gravity.CENTER;
                        linear10.addView(cb10, para10_1);
                        String value10 = CommonTools.null2String(operation10_1.getOpvalue());
                        if (value10 != null) {
                            if (value10.equals("is")) {                        //打钩状态
                                cb10.setChecked(true);
                            } else if (value10.equals("no")) {            //不打钩状态
                                cb10.setChecked(false);
                            } else {
                                cb10.setChecked(false);
                            }
                        }
                        cb10.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
//								Task task = DataSupport.where("rwid=? and taskid = ?", OrientApplication.getApplication().rw.getRwid(), cell.getTaskid()).find(Task.class).get(0);
                                    cell.setIshook("is");
                                    cell.update(cell.getId());
                                    operation10_1.setOpvalue("is");
                                    operation10_1.update(operation10_1.getId());
                                    String firsttime = currentTask.getStartTime();
                                    if (firsttime == null || firsttime.equals("")) {
                                        currentTask.setStartTime(DateUtil.getCurrentDate());
                                        currentTask.update(currentTask.getId());
                                    }
                                    HtmlData data = new HtmlData(cell, operation10_1.getRealcellid());
                                    htmlList.add(data);
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation10_1.getRealcellid());
                                    //记录第一次操作的时间
                                    Toast.makeText(context, "选中", Toast.LENGTH_SHORT).show();
                                } else {
                                    cell.setIshook("no");
                                    cell.update(cell.getId());
                                    operation10_1.setOpvalue("no");
                                    operation10_1.update(operation10_1.getId());
                                    HtmlData data = new HtmlData(cell, operation10_1.getRealcellid());
                                    htmlList.add(data);
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation10_1.getRealcellid());
                                    Toast.makeText(context, "未选中", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        edit10.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence text, int start, int before,
                                                      int count) {
                            }

                            @Override
                            public void beforeTextChanged(CharSequence text, int start,
                                                          int count, int after) {
                            }

                            @Override
                            public void afterTextChanged(Editable edit) {
                                String str = edit10.getText().toString();
                                cell.setOpvalue(str);
                                cell.update(cell.getId());
//		    	        	Task task = DataSupport.where("rwid=? and taskid = ?", OrientApplication.getApplication().rw.getRwid(), cell.getTaskid()).find(Task.class).get(0);
                                operation10_2.setOpvalue(str);
                                operation10_2.update(operation10_2.getId());
                                String firsttime = currentTask.getStartTime();
                                if (firsttime == null || firsttime.equals("")) {
                                    currentTask.setStartTime(DateUtil.getCurrentDate());
                                    currentTask.update(currentTask.getId());
                                }
//		    	        	HtmlHelper.changeTextValue(htmlDoc, cell, operation10_2.getRealcellid());
                                HtmlData data = new HtmlData(cell, operation10_2.getRealcellid());
                                htmlList_text.add(data);
                            }
                        });
                        String opId10 = "";
                        if (!operation10_1.getSketchmap().equals("") && operation10_1.getSketchmap() != null) {
                            opId10 = operation10_1.getSketchmap();
                        }
                        if (!operation10_2.getSketchmap().equals("") && operation10_2.getSketchmap() != null) {
                            opId10 = operation10_2.getSketchmap();
                        }
                        final String sketbitmap10 = opId10;
                        final String userId10 = OrientApplication.getApplication().loginUser.getUserid();
                        final String taskId10 = cell.getTaskid();
                        final String rowNum10 = cell.getHorizontalorder();
                        final String cellId10 = cell.getCellid();
                        final String absPath10 = Environment.getExternalStorageDirectory()
                                + Config.opphotoPath + "/"
                                + taskId10 + "/" + sketbitmap10 + ".jpg";
                        Bitmap bitmap10 = BitmapFactory.decodeFile(absPath10);
                        final ImageView image10 = new ImageButton(context);
                        if (bitmap10 != null)
                            Glide
                                    .with(getApplicationContext())
                                    .load(absPath10)
                                    .override(80, 80)
                                    .fitCenter()
                                    .into(image10);

                        image10.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BitmapDialogActivity.actionStart(context, userId10, taskId10, sketbitmap10);
                            }
                        });
                        linear10.addView(image10, para10_3);
                        NumImageButton image10_1 = new NumImageButton(context);

                        if (picturenumbers == 0) {
                            Operation operation = null;
                            if (operation10_1.getIsmedia().equals("TRUE")) {
                                operation = operation10_1;

                            }
                            if (operation10_2.getIsmedia().equals("TRUE")) {
                                operation = operation10_2;

                            }
                            if (operation != null) {
                                final String path1 = Environment.getExternalStorageDirectory() + Config.v2photoPath
                                        + File.separator
                                        + OrientApplication.getApplication().rw.getRwid()
                                        + File.separator
                                        + cell.getTaskid()
                                        + File.separator
                                        + CommonTools.null2String(operation.getOperationid())
                                        + File.separator;
                                getPictures(path1);
                            }
                        }
                        image10_1.setNum(picturenumbers);
                        picturenumbers = 0;
                        image10_1.setBackgroundResource(R.drawable.takephoto);
                        image10_1.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Operation temp = null;
                                if (operation10_1.getIsmedia().equals("TRUE")) {
                                    temp = operation10_1;
                                    HtmlHelper.changePhotoValue(htmlDoc, operation10_1);
                                }
                                if (operation10_2.getIsmedia().equals("TRUE")) {
                                    temp = operation10_2;
                                    HtmlHelper.changePhotoValue(htmlDoc, operation10_2);
                                }
                                if (temp != null)
                                    showPhotoDialog(cellId10, taskId10, temp);
                            }
                        });
                        linear10.addView(image10_1, para10_4);
                        ImageView image10_2 = new ImageView(context);
                        image10_2.setBackgroundResource(R.drawable.blacktiao);
                        linear10.addView(image10_2, para10_5);
                        tablerow.addView(linear10, para10);
                        break;
                    case "STRINGBITMAP":
                        android.widget.TableRow.LayoutParams para11 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para11_1 = new android.widget.TableRow.LayoutParams(avewdith - 81, 60);            //checkbox
                        android.widget.TableRow.LayoutParams para11_2 = new android.widget.TableRow.LayoutParams(80, 80);                //bitmap
                        android.widget.TableRow.LayoutParams para11_3 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        LinearLayout linear11 = new LinearLayout(context);
                        linear11.setOrientation(LinearLayout.HORIZONTAL);
                        linear11.setLayoutParams(para11);
                        //初始化EditText
                        final Operation operation11 = DataSupport.where("cellid=? and taskid=?", cell.getCellid(), task_id + "").find(Operation.class).get(0);
                        //初始化EditText
                        String stringdata11 = CommonTools.null2String(operation11.getOpvalue());
                        final EditText edit11 = new EditText(context);
                        edit11.setText(replaceStr(stringdata11));
                        edit11.setTextSize(16);
                        edit11.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence text, int start, int before,
                                                      int count) {
                            }

                            @Override
                            public void beforeTextChanged(CharSequence text, int start,
                                                          int count, int after) {
                            }

                            @Override
                            public void afterTextChanged(Editable edit) {
                                String str = edit11.getText().toString();
                                cell.setOpvalue(str);
                                cell.update(cell.getId());
//		    	        	Task task = DataSupport.where("rwid=? and taskid = ?", OrientApplication.getApplication().rw.getRwid(), cell.getTaskid()).find(Task.class).get(0);
                                operation11.setOpvalue(str);
                                operation11.update(operation11.getId());
                                String firsttime = currentTask.getStartTime();
                                if (firsttime == null || firsttime.equals("")) {
                                    currentTask.setStartTime(DateUtil.getCurrentDate());
                                    currentTask.update(currentTask.getId());
                                }
//		    	        	HtmlHelper.changeTextValue(htmlDoc, cell, operation11.getRealcellid());
                                HtmlData data = new HtmlData(cell, operation11.getRealcellid());
                                htmlList_text.add(data);
                            }
                        });
                        linear11.addView(edit11, para11_1);
                        final String userId11 = OrientApplication.getApplication().loginUser.getUserid();
                        final String taskId11 = cell.getTaskid();
                        final String opId11 = operation11.getSketchmap();
                        final String absPath11 = Environment.getExternalStorageDirectory()
                                + Config.opphotoPath + "/"
                                + taskId11 + "/" + opId11 + ".jpg";
                        Bitmap bitmap11 = BitmapFactory.decodeFile(absPath11);

                        final ImageView image11 = new ImageButton(context);
                        if (bitmap11 != null)

                            Glide
                                    .with(context)
                                    .load(absPath11)
                                    .override(80, 80)
                                    .fitCenter()
                                    .into(image11);

                        image11.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BitmapDialogActivity.actionStart(context, userId11, taskId11, opId11);
                            }
                        });
                        linear11.addView(image11, para11_2);
                        ImageView image11_2 = new ImageView(context);
                        image11_2.setBackgroundResource(R.drawable.blacktiao);
                        linear11.addView(image11_2, para11_3);
                        tablerow.addView(linear11, para11);
                        break;
                    case "STRINGPHOTOBITMAP":
                        android.widget.TableRow.LayoutParams para12 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para12_1 = new android.widget.TableRow.LayoutParams(avewdith - 161, 100);                //string
                        android.widget.TableRow.LayoutParams para12_2 = new android.widget.TableRow.LayoutParams(80, 80);                //photo
                        android.widget.TableRow.LayoutParams para12_3 = new android.widget.TableRow.LayoutParams(80, 80);                //bitmap
                        android.widget.TableRow.LayoutParams para12_4 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        LinearLayout linear12 = new LinearLayout(context);
                        linear12.setOrientation(LinearLayout.HORIZONTAL);
                        linear12.setLayoutParams(para12);
                        //初始化EditText
                        final Operation operation12_2 = DataSupport.where("cellid=? and taskid=? and type=?", cell.getCellid(), task_id + "", "2").find(Operation.class).get(0);
                        //初始化EditText
                        String stringdata12 = CommonTools.null2String(operation12_2.getOpvalue());
                        final EditText edit12 = new EditText(context);
                        edit12.setText(replaceStr(stringdata12));
                        edit12.setTextSize(16);
                        linear12.addView(edit12, para12_1);
                        edit12.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void onTextChanged(CharSequence text, int start, int before,
                                                      int count) {
                            }

                            @Override
                            public void beforeTextChanged(CharSequence text, int start,
                                                          int count, int after) {
                            }

                            @Override
                            public void afterTextChanged(Editable edit) {
                                String str = edit12.getText().toString();
                                cell.setOpvalue(str);
                                cell.update(cell.getId());
//		    	        	Task task = DataSupport.where("rwid=? and taskid = ?", OrientApplication.getApplication().rw.getRwid(), cell.getTaskid()).find(Task.class).get(0);
                                operation12_2.setOpvalue(str);
                                operation12_2.update(operation12_2.getId());
                                String firsttime = currentTask.getStartTime();
                                if (firsttime == null || firsttime.equals("")) {
                                    currentTask.setStartTime(DateUtil.getCurrentDate());
                                    currentTask.update(currentTask.getId());
                                }
//		    	        	HtmlHelper.changeTextValue(htmlDoc, cell, operation12_2.getRealcellid());
                                HtmlData data = new HtmlData(cell, operation12_2.getRealcellid());
                                htmlList_text.add(data);
                            }
                        });
                        String opId12 = operation12_2.getSketchmap();
                        final String sketbitmap12 = opId12;
                        final String userId12 = OrientApplication.getApplication().loginUser.getUserid();
                        final String taskId12 = cell.getTaskid();
                        final String rowNum12 = cell.getHorizontalorder();
                        final String cellId12 = cell.getCellid();
                        final String absPath12 = Environment.getExternalStorageDirectory()
                                + Config.opphotoPath + "/"
                                + taskId12 + "/" + sketbitmap12 + ".jpg";
                        Bitmap bitmap12 = BitmapFactory.decodeFile(absPath12);
                        final ImageView image12 = new ImageButton(context);
                        if (bitmap12 != null)

                            Glide
                                    .with(context)
                                    .load(absPath12)
                                    .override(80, 80)
                                    .fitCenter()
                                    .into(image12);

                        image12.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BitmapDialogActivity.actionStart(context, userId12, taskId12, sketbitmap12);
                            }
                        });
                        linear12.addView(image12, para12_2);
                        NumImageButton image12_1 = new NumImageButton(context);
                        if (picturenumbers == 0) {
                            if (operation12_2 != null) {
                                final String path1 = Environment.getExternalStorageDirectory() + Config.v2photoPath
                                        + File.separator
                                        + OrientApplication.getApplication().rw.getRwid()
                                        + File.separator
                                        + cell.getTaskid()
                                        + File.separator
                                        + CommonTools.null2String(operation12_2.getOperationid())
                                        + File.separator;
                                getPictures(path1);
                            }
                        }
                        image12_1.setNum(picturenumbers);
                        picturenumbers = 0;
                        image12_1.setBackgroundResource(R.drawable.takephoto);
                        image12_1.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                showPhotoDialog(cellId12, taskId12, operation12_2);
                            }
                        });
                        linear12.addView(image12_1, para12_3);
                        ImageView image12_2 = new ImageView(context);
                        image12_2.setBackgroundResource(R.drawable.blacktiao);
                        linear12.addView(image12_2, para12_4);
                        tablerow.addView(linear12, para12);
                        break;
                    case "HOOKPHOTOBITMAP":
                        android.widget.TableRow.LayoutParams para13 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        android.widget.TableRow.LayoutParams para13_1 = new android.widget.TableRow.LayoutParams(avewdith - 161, 100);                //string
                        android.widget.TableRow.LayoutParams para13_2 = new android.widget.TableRow.LayoutParams(80, 80);                //photo
                        android.widget.TableRow.LayoutParams para13_3 = new android.widget.TableRow.LayoutParams(80, 80);                //bitmap
                        android.widget.TableRow.LayoutParams para13_4 = new android.widget.TableRow.LayoutParams(1, android.widget.TableRow.LayoutParams.MATCH_PARENT);
                        LinearLayout linear13 = new LinearLayout(context);
                        para13_1.gravity = Gravity.CENTER;
                        linear13.setOrientation(LinearLayout.HORIZONTAL);
                        linear13.setLayoutParams(para13);
                        //初始化EditText
                        final Operation operation13_1 = DataSupport.where("cellid=? and taskid=?", cell.getCellid(), task_id + "").find(Operation.class).get(0);
                        //初始化checkbox
                        CheckBox cb13 = new CheckBox(context);
                        String value13 = CommonTools.null2String(operation13_1.getOpvalue());
                        if (value13 != null) {
                            if (value13.equals("is")) {                        //打钩状态
                                cb13.setChecked(true);
                            } else if (value13.equals("no")) {            //不打钩状态
                                cb13.setChecked(false);
                            } else {
                                cb13.setChecked(false);
                            }
                        }
                        linear13.addView(cb13, para13_1);
                        cb13.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
//								Task task = DataSupport.where("rwid=? and taskid = ?", OrientApplication.getApplication().rw.getRwid(), cell.getTaskid()).find(Task.class).get(0);
                                    cell.setIshook("is");
                                    cell.update(cell.getId());
                                    operation13_1.setOpvalue("is");
                                    operation13_1.update(operation13_1.getId());
                                    String firsttime = currentTask.getStartTime();
                                    if (firsttime == null || firsttime.equals("")) {
                                        currentTask.setStartTime(DateUtil.getCurrentDate());
                                        currentTask.update(currentTask.getId());
                                    }
                                    HtmlData data = new HtmlData(cell, operation13_1.getRealcellid());
                                    htmlList.add(data);
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation13_1.getRealcellid());
                                    //记录第一次操作的时间
                                    Toast.makeText(context, "选中", Toast.LENGTH_SHORT).show();
                                } else {
                                    cell.setIshook("no");
                                    cell.update(cell.getId());
                                    operation13_1.setOpvalue("no");
                                    operation13_1.update(operation13_1.getId());
                                    HtmlData data = new HtmlData(cell, operation13_1.getRealcellid());
                                    htmlList.add(data);
//			    	            HtmlHelper.changeCheckValue(htmlDoc, cell, operation13_1.getRealcellid());
                                    Toast.makeText(context, "未选中", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        String opId13 = operation13_1.getSketchmap();
                        final String sketbitmap13 = opId13;
                        final String userId13 = OrientApplication.getApplication().loginUser.getUserid();
                        final String taskId13 = cell.getTaskid();
                        final String rowNum13 = cell.getHorizontalorder();
                        final String cellId13 = cell.getCellid();
                        final String absPath13 = Environment.getExternalStorageDirectory()
                                + Config.opphotoPath + "/"
                                + taskId13 + "/" + sketbitmap13 + ".jpg";
                        Bitmap bitmap13 = BitmapFactory.decodeFile(absPath13);
                        final ImageView image13 = new ImageButton(context);
                        if (bitmap13 != null)

                            Glide
                                    .with(context)
                                    .load(absPath13)
                                    .override(80, 80)
                                    .fitCenter()
                                    .into(image13);

                        image13.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BitmapDialogActivity.actionStart(context, userId13, taskId13, sketbitmap13);
                            }
                        });
                        linear13.addView(image13, para13_2);
                        NumImageButton image13_1 = new NumImageButton(context);
                        if (picturenumbers == 0) {
                            if (operation13_1 != null) {
                                final String path1 = Environment.getExternalStorageDirectory() + Config.v2photoPath
                                        + File.separator
                                        + OrientApplication.getApplication().rw.getRwid()
                                        + File.separator
                                        + cell.getTaskid()
                                        + File.separator
                                        + CommonTools.null2String(operation13_1.getOperationid())
                                        + File.separator;
                                getPictures(path1);
                            }
                        }
                        image13_1.setNum(picturenumbers);
                        picturenumbers = 0;
                        image13_1.setBackgroundResource(R.drawable.takephoto);
                        image13_1.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                showPhotoDialog(cellId13, taskId13, operation13_1);
                            }
                        });
                        linear13.addView(image13_1, para13_3);
                        ImageView image13_2 = new ImageView(context);
                        image13_2.setBackgroundResource(R.drawable.blacktiao);
                        linear13.addView(image13_2, para13_4);
                        tablerow.addView(linear13, para13);
                        break;
//                    case "SELCET":
//                        android.widget.TableRow.LayoutParams para14 = new android.widget.TableRow.LayoutParams(avewdith, android.widget.TableRow.LayoutParams.MATCH_PARENT);
//                        NiceSpinner spinner = new NiceSpinner(context);
//                        List<String> dataset = new LinkedList<>();
//                        for (int n = 0; n < 5; n++) {
//                            dataset.add(String.valueOf(n));
//                        }
//                        spinner.attachDataSource(dataset);
//                        spinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
//                            @Override
//                            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
//                                String item = parent.getItemAtPosition(position).toString();
//                                Toast.makeText(CheckActivity1.this, "Selected: " + item, Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                        spinner.setLayoutParams(para14);
//                        tablerow.addView(spinner, para14);
//                        break;
                    default:

                }
            }
            table_content.addView(tablerow, new TableLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, 66));
        }
    }

    //加载Condition数据
    private void loadConditionAdapter(long taskid, String userid) {
        List<Scene> conditions = aerospacedb.loadConditionAdapter(taskid, userid);
        scenelists = conditions;
    }

    //加载Table数据
    private List<Cell> loadCellAdapter(long taskid, String userid, int pagetype) {
        List<Cell> cellList = aerospacedb.loadTableAdapter(taskid, userid, pagetype);
        return cellList;
    }

    private void showPhotoDialog(final String cellId, final String taskId, final Operation operation) {
        String opId = "";

        final String path = Environment.getExternalStorageDirectory() + Config.v2photoPath
                + File.separator
                + OrientApplication.getApplication().rw.getRwid()
                + File.separator
                + taskId
                + File.separator
                + CommonTools.null2String(operation.getOperationid())
                + File.separator;
        //取消拍照选择弹框，直接进入相册
        Intent intent = new Intent(CheckActivity1.this, AlbumActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("checkType", "check");
        startActivity(intent);
//
//		View view = LayoutInflater.from(CheckActivity1.this).inflate(R.layout.dialog_take_photo, null);
//        AlertDialog.Builder builder1 = new AlertDialog.Builder(CheckActivity1.this)
//                .setIcon(R.drawable.logo_title)
//                .setTitle("请选择")
//                .setView(view);
//        final AlertDialog dialog = builder1.create();
//        dialog.show();
//        if (dialog.isShowing()) {
//            dialog.setCanceledOnTouchOutside(false);
//        }
//        Button btn_take_photo = (Button) view.findViewById(R.id.btn_take_photo);
//        Button btn_look_over = (Button) view.findViewById(R.id.btn_look_over_album);
//        Button btn_cancel = (Button) view.findViewById(R.id.btn_cancel);
//        btn_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.cancel();
//            }
//        });
//        btn_take_photo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            	HtmlHelper.changePhotoValue(htmlDoc, operation);
////				PhotoUtils.transferCamera(CheckActivity1.this, path);
//				Intent intent2 = new Intent();
//				intent2.setClass(CheckActivity1.this, CameraActivity.class);
//				intent2.putExtra("path", path);
//				startActivity(intent2);
//				dialog.dismiss();
//            }
//        });
//        btn_look_over.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.cancel();
//                Intent intent = new Intent(CheckActivity1.this, AlbumActivity.class);
//                intent.putExtra("path", path);
//                intent.putExtra("checkType", "check");
//                startActivity(intent);
//				dialog.dismiss();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.readactivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                this.prodlg = ProgressDialog.show(this, "返回", "正在保存数据");
                prodlg.setIcon(this.getResources().getDrawable(R.drawable.logo_title));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (HtmlData data : htmlList) {
                            HtmlHelper.changeCheckValue(htmlDoc, data.getCell(), data.getId());
                        }
                        for (HtmlData data : htmlList_text) {
                            HtmlHelper.changeTextValue(htmlDoc, data.getCell(), data.getId());
                        }
                        Message message = new Message();
                        message.what = 1;
                        mHandler.sendMessage(message);
                    }
                }).start();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            if (HtmlHelper.saveHtmlDoc(currentTask, htmlDoc))
                Log.i("HTML编辑保存", currentTask.getTaskname() + "保存成功！");
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * Activity加载完成之后的时间
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            EventBus.getDefault().post(new LocationEvent("ok"));
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onScrollChanged(int scrollY) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent() {

    }

    public void getPictures(String string) {
        // TODO Auto-generated method stub
        File file = new File(string);
        File[] files = file.listFiles();
        if (files != null) {
            for (int j = 0; j < files.length; j++) {
                String name = files[j].getName();
                if (files[j].isDirectory()) {
                    String dirPath = files[j].toString().toLowerCase();
                    System.out.println(dirPath);
                    getPictures(dirPath + "/");
                } else if (files[j].isFile() & name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".gif") || name.endsWith(".jpeg")) {
                    System.out.println("FileName===" + files[j].getName());
                    picturenumbers++;
                }
            }
        }


    }

    @Subscribe
    public void onEventMainThread(LocationEvent locationEvent) {
        if (locationEvent.getContent().equals("finish")) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        OrientApplication.getApplication().setPanduFlag(0);
//		totalPhotoNum = 0;
    }

    private void quickCollectPopu(final Cell cell, final Operation operation, final EditText editText) {
        final Dialog dialog1 = new Dialog(this);

        View contentView1 = LayoutInflater.from(this).inflate(R.layout.quick_collect_popu, null);
        dialog1.setContentView(contentView1);
        dialog1.setTitle("快速采集数据");
        dialog1.setCanceledOnTouchOutside(false);

        mTitlels = (ListView) contentView1.findViewById(R.id.title_ls);
        mConfirmBtn = (Button) contentView1.findViewById(R.id.confirm_btn);
        mCancelBtn = (Button) contentView1.findViewById(R.id.cancel_btn);

        quickCollectAdapter = new QuickCollectAdapter();
        mTitlels.setAdapter(quickCollectAdapter);
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < quickCollectMapList.size(); i++) {
                    //保存快速采集数据
                    saveQuickCollectData(cell, operation, quickCollectMapList.get(i));
                }
                dialog1.dismiss();
                if (quickCollectMapList.size() > 0 && quickCollectTaskList.size() > 0) {
                    setInputText(editText, quickCollectMapList.get(0).get(quickCollectTaskList.get(0).getPath()));
                } else {
                    Toast.makeText(CheckActivity1.this, "数据不能为空", Toast.LENGTH_SHORT).show();
                }
                quickCollectTaskList.clear();
                quickCollectMapList.clear();
            }
        });
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog1.dismiss();
                quickCollectTaskList.clear();
                quickCollectMapList.clear();
            }
        });
        dialog1.show();
    }

    private class QuickCollectAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return quickCollectTaskList.size();
        }

        @Override
        public Object getItem(int position) {
            return quickCollectTaskList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final QuickCollectAdapter.ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_quick_collect, null);
                viewHolder = new QuickCollectAdapter.ViewHolder();
                final Map<String, String> m = new HashMap<String, String>();
                final String[] string = {""};
                viewHolder.title = (TextView) convertView.findViewById(R.id.tv_title);
                viewHolder.content = (EditText) convertView.findViewById(R.id.ed_content);
                viewHolder.title.setText(quickCollectTaskList.get(position).getPath() + "：");
                viewHolder.content.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        string[0] = viewHolder.content.getText().toString();
                        m.put(quickCollectTaskList.get(position).getPath(), string[0]);
                        for (int i = 0; i < quickCollectMapList.size(); i++) {
                            if (quickCollectMapList.get(i).containsValue(s.toString())) {
                                quickCollectMapList.remove(i);
                            }
                        }
                        quickCollectMapList.add(m);
                        if (position == 0) {
//							setText();
                        }
                    }
                });

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (QuickCollectAdapter.ViewHolder) convertView.getTag();
            }
            return convertView;
        }


        class ViewHolder {
            public TextView title;
            public EditText content;
        }

    }

    /**
     * @param
     * @return
     * @Description: 获取支撑快速采集操作所需数据
     * @author qiaozhili
     * @date 2019/3/7 15:40
     */
    private void getQuickSellectParm(Task task, Cell cell, Operation operation, EditText editText) {
        if (task.getIsBrother() != 1) {

            quickCollectTaskList.add(task);
            List<Task> taskList = DataSupport.where("broTaskId=? and location=?", task.getTaskid() + "", "1").find(Task.class);
            for (Task task1 : taskList) {
                quickCollectTaskList.add(task1);
            }
            //调起弹窗
            quickCollectPopu(cell, operation, editText);
        } else {
            Toast.makeText(context, "此表为复制表，不能进行快速采集！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @param
     * @return
     * @Description: 保存快速检查采集数据
     * @author qiaozhili
     * @date 2019/3/7 17:30
     */
    private void saveQuickCollectData(Cell cell1, Operation operation, Map<String, String> map) {
        String value = "";
        String taskId = "";
        for (Task task : quickCollectTaskList) {
            String key1 = task.getPath();
            if (map.containsKey(key1)) {
                value = map.get(key1);
                taskId = task.getTaskid();
            }
        }
        List<Cell> cellList = DataSupport.where("taskid=? and cellidold=?", taskId, cell1.getCellid()).find(Cell.class);
        List<Operation> operationList = DataSupport.where("taskid=? and cellidold=?", taskId, cell1.getCellid()).find(Operation.class);
        if (cellList.size() > 0) {
            cellList.get(0).setOpvalue(value);
            cellList.get(0).update(cellList.get(0).getId());
            if (operationList.size() > 0) {
                operationList.get(0).setOpvalue(value);
                operationList.get(0).update(operationList.get(0).getId());
                HtmlData data = new HtmlData(cellList.get(0), operationList.get(0).getRealcellid());
                htmlList_text.add(data);
            } else {
                Toast.makeText(context, "operation数据有问题", Toast.LENGTH_SHORT).show();
            }

        }


    }

    private void setInputText(EditText editText, String str) {
        editText.setText(str);
    }

    /**
     * @param
     * @return newStr
     * @Description: 替换尖括号
     * @author qiaozhili
     * @date 2019/8/26 16:37
     */
    public static String replaceStr(String oldStr) {
        String newStr = "";
        if (oldStr.contains("#lt;") || oldStr.contains("#gt;")) {
            newStr = oldStr.replace("#lt;", "<").replace("#gt;", ">");
        } else {
            newStr = oldStr;
        }
        return newStr;
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void pandu(Cell cell, StringBuffer prefixCodes) {
        //实测值
        List<Cell> actualvalCellList = DataSupport.where("horizontalorder=? and taskid=? and markup=? and rowsid=?", cell.getHorizontalorder(), task_id + "", Config.actualval, String.valueOf(pagetype)).find(Cell.class);
        //要求值
        List<Cell> requirevalCellList = DataSupport.where("horizontalorder=? and taskid=? and markup=? and rowsid=?", cell.getHorizontalorder(), task_id + "", Config.requireval, String.valueOf(pagetype)).find(Cell.class);
//        //上偏差
//        List<Cell> upperCellList = DataSupport.where("horizontalorder=? and taskid=? and markup=? and rowsid=?", cell.getHorizontalorder(), task_id+"", Config.upper, String.valueOf(pagetype)).find(Cell.class);
//        //下偏差
//        List<Cell> lowerCellList = DataSupport.where("horizontalorder=? and taskid=? and markup=? and rowsid=?", cell.getHorizontalorder(), task_id+"", Config.lower, String.valueOf(pagetype)).find(Cell.class);
        //符合度
//        List<Cell> complianceCellList = DataSupport.where("horizontalorder=? and taskid=? and markup=? and rowsid=?", cell.getHorizontalorder(), task_id+"", Config.compliance, String.valueOf(pagetype)).find(Cell.class);
//        Operation operation2 = DataSupport.where("cellid=? and taskid=?", cell.getCellid(), task_id+"").find(Operation.class).get(0);
        Operation operation2 = DataSupport.findAll(Operation.class).get(0);
        //实测值
        String actualvalNum = cell.getOpvalue();
        if (actualvalNum.equals("")) {
            return;
        }
//        if (actualvalCellList.size() > 0) {
//            actualvalNum = actualvalCellList.get(0).getOpvalue();
//        }
        //要求值
        String prefixCode = "";
        if (requirevalCellList.size() > 0) {
            prefixCode = requirevalCellList.get(0).getTextvalue();
        }
        //要求值中有无≥/≤/＞/＜等的情况
        String pattern = "^(\\≥|\\＞|\\≤|\\＜)(\\-|\\+)?([\\d]+)(\\.[\\d]+)?$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(prefixCode);
        System.out.println(m.matches());

        //要求值中是否包含汉字
        String pattern1 = "[\\u4e00-\\u9fa5]";
        Pattern r1 = Pattern.compile(pattern1);
        Matcher m1 = r1.matcher(prefixCode);
        System.out.println(m1.matches());

        //要求值中是否包5(+1, -1)、 5[+1,-0]
        String pattern2 = "^([\\d]+)(\\.[\\d]+)?(\\(|\\[){1}(\\+){1}([\\d]+)(\\.[\\d]+)?(\\,)?(\\-){1}([\\d]+)(\\.[\\d]+)?(\\)|\\]){1}$";
        Pattern r2 = Pattern.compile(pattern2);
        Matcher m2 = r2.matcher(prefixCode);
        System.out.println(m2.matches());

        //要求值为纯数字
        String pattern3 = "^(\\-|\\+)?([\\d]+)(\\.[\\d]+)?$";
        Pattern r3 = Pattern.compile(pattern3);
        Matcher m3 = r3.matcher(prefixCode);
        System.out.println(m3.matches());

        //实测值中是否为纯数字
        String pattern4 = "^^(0|[1-9]\\d*)$|^(0|[1-9]\\d*)\\.(\\d+)$";
        Pattern r4 = Pattern.compile(pattern4);
        Matcher m4 = r4.matcher(actualvalNum);
        System.out.println(m4.matches());
//        if (!m4.matches()) {
////			Toast.makeText(context, "实测值" + actualvalNum + "非纯数字，无法判读！", Toast.LENGTH_SHORT).show();
//            return;
//        }
        //包含≥/≤/＞/＜等的情况
        if (m.matches()) {
            if (!m4.matches()) {
//				Toast.makeText(context, "实测值"+"非纯数字，无法判读！", Toast.LENGTH_SHORT).show();
                return;
            }
            //要求值
            String requirevalNum = prefixCode.substring(1, prefixCode.length());
            String symbol = prefixCode.substring(0, 1);
            if (!actualvalNum.equals("") && !requirevalNum.equals("")) {
                switch (symbol) {
                    case "≥":
                        judgeResult((Float.valueOf(actualvalNum) >= Float.valueOf(requirevalNum)), cell, operation2);
                        break;
                    case "＞":
                        judgeResult((Float.valueOf(actualvalNum) > Float.valueOf(requirevalNum)), cell, operation2);
                        break;
                    case "≤":
                        judgeResult((Float.valueOf(actualvalNum) <= Float.valueOf(requirevalNum)), cell, operation2);
                        break;
                    case "＜":
                        judgeResult((Float.valueOf(actualvalNum) < Float.valueOf(requirevalNum)), cell, operation2);
                        break;
                }
            } else {
                judgeResult(false, cell, operation2);
            }
        }
        //文字类型判读，判断是否包含“已”“未”
        else if (m1.find()) {
            if (actualvalNum.equals("/")) {
                return;
            }
            if (!actualvalNum.equals("")) {
                if (actualvalNum.contains(Config.bmz) || actualvalNum.contains(Config.bfh) || actualvalNum.contains(Config.bn)
                        || actualvalNum.contains(Config.bzc) || actualvalNum.contains(Config.bwz) || actualvalNum.contains(Config.yqx)
                        || actualvalNum.contains(Config.cw) || actualvalNum.contains(Config.ygs) || actualvalNum.contains(Config.wqs)
                        || actualvalNum.contains(Config.wmq) || actualvalNum.contains(Config.bhg)) {
                    judgeResult(false, cell, operation2);
                } else {
                    judgeResult(true, cell, operation2);
                }
            } else {
                judgeResult(false, cell, operation2);
            }
        }
        //5(+1, -1)、 5[+1, 0]；
        else if (m2.matches()) {
            if (!m4.matches()) {
//				Toast.makeText(context, "实测值非纯数字，无法判读！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!actualvalNum.equals("") && !prefixCode.equals("")) {
                String leftS = prefixCode.indexOf("(") >= 0 ? "1" : "0";// 判断有没有左小括号
                String rightS = prefixCode.indexOf(")") >= 0 ? "1" : "0";// 判断有没有右小括号
                String leftM = prefixCode.indexOf("[") >= 0 ? "1" : "0";// 判断有没有左中括号
                String rightM = prefixCode.indexOf("]") >= 0 ? "1" : "0";// 判断有没有右中括号
                String condition = leftS + rightS + leftM + rightM;
                switch (condition) {
                    case "1100":
                        Float mid = Float.valueOf(prefixCode.substring(0, prefixCode.indexOf("(")));
                        Float sup = Float.valueOf(prefixCode.substring(prefixCode.indexOf("+"), prefixCode.indexOf(",")));
                        Float sub = Float.valueOf(prefixCode.substring(prefixCode.indexOf("-") + 1, prefixCode.lastIndexOf(")")));
                        Float min = mid - sub;
                        Float max = mid + sup;
                        Float req = Float.valueOf(actualvalNum);
                        if (req >= max || req <= min) {
                            judgeResult(false, cell, operation2);
                        } else {
                            judgeResult(true, cell, operation2);
                        }
                        break;
                    case "1001":
                        Float mid1 = Float.valueOf(prefixCode.substring(0, prefixCode.indexOf("(")));
                        Float sup1 = Float.valueOf(prefixCode.substring(prefixCode.indexOf("+"), prefixCode.indexOf(",")));
                        Float sub1 = Float.valueOf(prefixCode.substring(prefixCode.indexOf("-") + 1, prefixCode.lastIndexOf("]")));
                        Float min1 = mid1 - sub1;
                        Float max1 = mid1 + sup1;
                        Float req1 = Float.valueOf(actualvalNum);
                        if (req1 >= max1 || req1 < min1) {
                            judgeResult(false, cell, operation2);
                        } else {
                            judgeResult(true, cell, operation2);
                        }
                        break;
                    case "0110":
                        Float mid2 = Float.valueOf(prefixCode.substring(0, prefixCode.indexOf("[")));
                        Float sup2 = Float.valueOf(prefixCode.substring(prefixCode.indexOf("+"), prefixCode.indexOf(",")));
                        Float sub2 = Float.valueOf(prefixCode.substring(prefixCode.indexOf("-") + 1, prefixCode.lastIndexOf(")")));
                        Float min2 = mid2 - sub2;
                        Float max2 = mid2 + sup2;
                        Float req2 = Float.valueOf(actualvalNum);
                        if (req2 > max2 || req2 <= min2) {
                            judgeResult(false, cell, operation2);
                        } else {
                            judgeResult(true, cell, operation2);
                        }
                        break;
                    case "0011":
                        Float mid3 = Float.valueOf(prefixCode.substring(0, prefixCode.indexOf("[")));
                        Float sup3 = Float.valueOf(prefixCode.substring(prefixCode.indexOf("+"), prefixCode.indexOf(",")));
                        Float sub3 = Float.valueOf(prefixCode.substring(prefixCode.indexOf("-") + 1, prefixCode.lastIndexOf("]")));
                        Float min3 = mid3 - sub3;
                        Float max3 = mid3 + sup3;
                        Float req3 = Float.valueOf(actualvalNum);
                        if (req3 > max3 || req3 < min3) {
                            judgeResult(false, cell, operation2);
                        } else {
                            judgeResult(true, cell, operation2);
                        }
                        break;
                }
            } else {
                judgeResult(false, cell, operation2);
            }

        }
        //0~2
        else if (prefixCode.contains("~")) {
            if (!m4.matches()) {
//				Toast.makeText(context, "实测值非纯数字，无法判读！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!actualvalNum.equals("") && !prefixCode.equals("")) {
                String min = prefixCode.substring(0, prefixCode.indexOf("~"));
                String max = prefixCode.substring(prefixCode.indexOf("~") + 1, prefixCode.length());
                if ((Float.valueOf(actualvalNum) < Float.valueOf(min)) || (Float.valueOf(actualvalNum) > Float.valueOf(max))) {
                    judgeResult(false, cell, operation2);
                } else {
                    judgeResult(true, cell, operation2);
                }
            } else {
                judgeResult(false, cell, operation2);
            }
        }
        //5±0.2
        else if (prefixCode.contains("±")) {
            if (!m4.matches()) {
//				Toast.makeText(context, "实测值非纯数字，无法判读！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!actualvalNum.equals("") && !prefixCode.equals("")) {
                String mid = prefixCode.substring(0, prefixCode.indexOf("±"));
                String mm = prefixCode.substring(prefixCode.indexOf("±") + 1, prefixCode.length());
                Float min = Float.valueOf(mid) - Float.valueOf(mm);
                Float max = Float.valueOf(mid) + Float.valueOf(mm);
                if (Float.valueOf(actualvalNum) < min || Float.valueOf(actualvalNum) > max) {
                    judgeResult(false, cell, operation2);
                } else {
                    judgeResult(true, cell, operation2);
                }
            } else {
                judgeResult(false, cell, operation2);
            }
        } else {
            prefixCodes.append(prefixCode).append("；");
//            Toast.makeText(context, "“"+prefixCode+ "”" + "格式有误，不可判读！", Toast.LENGTH_SHORT).show();
        }
//        //上下偏差情况
//        else {
//            if (m3.matches()) {
//                //要求值
//                String require = requirevalCellList.get(0).getTextvalue();
//                if (!actualvalNum.equals("") && !require.equals("")) {
//                    //上限 实测值+上偏差
//                    Float max = Float.valueOf(require) + Float.valueOf(upperCellList.get(0).getTextvalue());
//                    //下限 实测值+下偏差
//                    Float min = Float.valueOf(require) + Float.valueOf(lowerCellList.get(0).getTextvalue());
//                    boolean judge = Float.valueOf(actualvalNum) > min && Float.valueOf(actualvalNum) < max;
//                    judgeResult(judge, cell, operation2);
//                } else {
//                    judgeResult(false, cell, operation2);
//                }
//            } else {
//                Toast.makeText(context, prefixCode + "不符合判读标准", Toast.LENGTH_SHORT).show();
//                judgeResult(false, cell, operation2);
//            }
//        }

    }

    private void judgeResult(boolean judge, Cell cell, Operation operation) {
        if (judge) {
            cell.setIsFuhe(Config.hege);
//            cell.setOpvalue(Config.hege);
//            operation.setOpvalue("符合");
        } else {
//            cell.setOpvalue(Config.buhege);
//            operation.setOpvalue("不符合");
            cell.setIsFuhe(Config.buhege);
        }
        cell.update(cell.getId());
//        operation.update(operation.getId());
//        HtmlData data = new HtmlData(cell, operation.getRealcellid());
        HtmlData data = new HtmlData(cell, cell.getCellid());
        htmlList_text.add(data);
    }

    private void judgeResult1(boolean judge, Cell cell, Operation operation) {
        if (judge) {
            cell.setIsFuhe(Config.hege);
            cell.setOpvalue(Config.hege);
            operation.setOpvalue(Config.hege);
        } else {
            cell.setOpvalue(Config.buhege);
            operation.setOpvalue(Config.buhege);
            cell.setIsFuhe(Config.buhege);
        }
        cell.update(cell.getId());
        operation.update(operation.getId());
        HtmlData data = new HtmlData(cell, operation.getRealcellid());
//        HtmlData data = new HtmlData(cell, cell.getCellid());
        htmlList_text.add(data);
    }

    /**
     * @param
     * @return
     * @Description: 采集值完整性检查 isWanzheng=0采集值全部填写完成
     * @author qiaozhili
     * @date 2020/6/29 17:25
     */
    public int checkComplete() {
        List<Cell> actualvalCellList = DataSupport.where("taskid=? and type=?", task_id + "", "TRUE").find(Cell.class);
        String actualvalNum = "";
        int isWanzheng = 0;
        if (actualvalCellList.size() > 0) {
            for (Cell cell : actualvalCellList) {
                actualvalNum = cell.getOpvalue();
                if (actualvalNum.equals("")) {
                    isWanzheng = isWanzheng + 1;
                }
            }
        }
        return isWanzheng;
    }

    public void commitWarnInfo() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.logo_title).setTitle("警告！");
        dialog.setMessage("存在未填写的数据，请全部填写完整后重试！");
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
//		dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
//			}
//		});
        dialog.show();
    }

    private void back() {
        CheckActivity1.this.prodlg = ProgressDialog.show(CheckActivity1.this, "返回", "正在保存数据");
        if (OrientApplication.app.getPageflage() > 1) {
            OrientApplication.app.setPageflage(pagetype - 1);
        }
        prodlg.setIcon(CheckActivity1.this.getResources().getDrawable(R.drawable.logo_title));
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (HtmlData data : htmlList) {
                    HtmlHelper.changeCheckValue(htmlDoc, data.getCell(), data.getId());
                }
                for (HtmlData data : htmlList_text) {
                    HtmlHelper.changeTextValue(htmlDoc, data.getCell(), data.getId());
                }
                Message message = new Message();
                message.what = 1;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    /**
     * @param
     * @return
     * @Description: 常规性检验表-结论-校验
     * @author qiaozhili
     * @date 2020/7/12 11:35
     */
    public boolean checkConclusionByCell(String str) {
        if (str.equals(Config.hege) || str.equals(Config.ystg)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param
     * @return
     * @Description: 检查表单完整性（除去备注列）
     * @author qiaozhili
     * @date 2020/7/12 15:11
     */
    public void checkComplete1() {
        List<Cell> CellList = DataSupport.where("taskid=? and type=?", task_id + "", "TRUE").find(Cell.class);
        String actualvalNum = "";
        int isWanzheng = 0;
        List<Cell> inputCellList = new ArrayList<>();
        if (CellList.size() > 0) {
            for (Cell cell : CellList) {
                if (!cell.getRowname().equals("备注")) {
                    actualvalNum = cell.getOpvalue();
                    if (actualvalNum.equals("")) {
                        inputCellList.add(cell);
                        isWanzheng = isWanzheng + 1;
                    }
                }
            }
        }
        if (isWanzheng != 0) {
            inputWarnInfo(inputCellList);
        } else {
            currentTask.setEndTime(DateUtil.getCurrentDate());
            currentTask.setLocation(2);
            currentTask.save();
        }
    }

    public void inputWarnInfo(final List<Cell> cellList) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.drawable.logo_title).setTitle("警告！");
        dialog.setMessage("有未填写的项，确认是否检查完成？");
        dialog.setCancelable(false);
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (Cell cell : cellList) {
                    final Operation operation = DataSupport.where("cellid=? and taskid=?", cell.getCellid(), task_id + "").find(Operation.class).get(0);
                    cell.setOpvalue("/");
                    cell.update(cell.getId());
                    operation.setOpvalue("/");
                    operation.update(operation.getId());
                    HtmlData data = new HtmlData(cell, operation.getRealcellid());
                    htmlList_text.add(data);
                }
                currentTask.setEndTime(DateUtil.getCurrentDate());
                currentTask.setLocation(2);
                currentTask.save();
                dialog.dismiss();
                CheckActivity1.actionStart(CheckActivity1.this, task_id, mHandler, "3");
                finish();
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                checkSwitch.setChecked(false);
            }
        });
        dialog.show();
    }

    public void showDialog(String str) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.logo_title).setTitle("提示:以下要求值格式不符合标准，无法判读");
        builder.setMessage(str)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CheckActivity1.actionStart(CheckActivity1.this, task_id, mHandler, "3");
                        finish();
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
