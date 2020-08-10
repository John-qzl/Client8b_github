package com.example.navigationdrawertest.fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.greenrobot.eventbus.Subscribe;
import org.jsoup.nodes.Document;
import org.litepal.crud.DataSupport;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navigationdrawertest.MainActivity;
import com.example.navigationdrawertest.R;
import com.example.navigationdrawertest.SweetAlert.SweetAlertDialog;
import com.example.navigationdrawertest.activity.CheckActivity;
import com.example.navigationdrawertest.activity.CheckActivity1;
import com.example.navigationdrawertest.activity.LoginActivity;
import com.example.navigationdrawertest.activity.MainActivity1;
import com.example.navigationdrawertest.activity.ReadActivity;
import com.example.navigationdrawertest.activity.ReadActivity1;
import com.example.navigationdrawertest.activity.SignActivity;
import com.example.navigationdrawertest.activity.SignActivity1;
import com.example.navigationdrawertest.adapter.Event.LocationEvent;
import com.example.navigationdrawertest.application.OrientApplication;
import com.example.navigationdrawertest.model.Cell;
import com.example.navigationdrawertest.model.Operation;
import com.example.navigationdrawertest.model.Post;
import com.example.navigationdrawertest.model.Rows;
import com.example.navigationdrawertest.model.Rw;
import com.example.navigationdrawertest.model.RwRelation;
import com.example.navigationdrawertest.model.Scene;
import com.example.navigationdrawertest.model.Signature;
import com.example.navigationdrawertest.model.Task;
import com.example.navigationdrawertest.model.User;
import com.example.navigationdrawertest.tree.DepartmentNode;
import com.example.navigationdrawertest.tree.TreeHelper;
import com.example.navigationdrawertest.tree.TreeNode;
import com.example.navigationdrawertest.tree.UserNode;
import com.example.navigationdrawertest.utils.Config;
import com.example.navigationdrawertest.utils.FileOperation;
import com.example.navigationdrawertest.utils.HtmlHelper;
import com.example.navigationdrawertest.utils.NodeButtonEnum;

import de.greenrobot.event.EventBus;

/**
 * 待检查Fragment
 * @author liu
 *	2015-10-20 下午3:57:37
 */

@SuppressLint("ValidFragment")
public class FragmentCheck extends Fragment {
	private ListView mListView = null;
	private MyBaseAdapter adapter = null;
	private ArrayList<TreeNode> nodeList = new ArrayList<TreeNode>();
	private String currentUserid = OrientApplication.getApplication().loginUser.getUserid();
	Context context;
	TreeNode rootNode;
	private long clicktaskid;
	private NodeButtonEnum buttontype;
	private RwRelation proEntity;				//传递过来的项目树节点
	private static User user;
	private ProgressDialog prodlg;
	private AlertDialog.Builder dialog;
	private AlertDialog alertDialog3; //多选框

	public FragmentCheck(RwRelation proEntity){
		this.proEntity = proEntity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		OrientApplication.app.setPageflage(1);
		OrientApplication.getApplication().setIsWanzheng(0);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = inflater.inflate(R.layout.tree_main, container, false);
		mListView = (ListView) v.findViewById(R.id.listView1);
		context = getActivity();

		adapter = new MyBaseAdapter();
		loadData();
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(listViewItemClickListener);
		return v;
	}
	
	private void loadData() {
		List<Rw> rwList = new ArrayList<Rw>();				//产品类型
		List<Post> postList = new ArrayList<Post>();		//岗位实例ID---path
		user = DataSupport.where("userid = ?", currentUserid).find(User.class).get(0);
		nodeList.clear();

		String testteamStr = user.getTtidandname();
		List<Task> taskList_rw = DataSupport.where("location = ? and xhId =?", "1",proEntity.getRwid()).find(Task.class);
		if(taskList_rw.size() != 0){
			rwList.clear();
			for (int i = 0; i < taskList_rw.size(); i++) {
				String cplx = taskList_rw.get(i).getRwname();
				String cplxId = taskList_rw.get(i).getRwid();
				Rw rw = new Rw();
				rw.setRwid(cplxId);
				rw.setRwname(cplx);
				if (rwList.size() > 0) {
					String str = "";
					for (int j = 0; j < rwList.size(); j++) {
						str = str + rwList.get(j).getRwid();
					}
					if (!str.contains(cplxId)) {
						rwList.add(rw);
					}
				} else {
					rwList.add(rw);
				}
			}
			rootNode = new DepartmentNode(Long.valueOf(proEntity.getRwid()), proEntity.getRwname(), "0", null, 0);
			//3,获取所有的岗位实例-path
			//产品类型节点
			for(int i=0; i<rwList.size(); i++){
//				rootNode = new DepartmentNode(Long.valueOf(rwList.get(i).getRwid()), rwList.get(i).getRwname(), "0", null, 0);
				TreeNode node0 = new DepartmentNode(Long.valueOf(rwList.get(i).getRwid()), rwList.get(i).getRwname(), "0", rootNode, 1);
//				rootNode.add(node0);

				List<Rw> rwList_pc = new ArrayList<Rw>();				//批次
				List<Task> taskList_cplx = DataSupport.where("location = ? and rwid =?", "1",rwList.get(i).getRwid()).find(Task.class);
				rwList_pc.clear();
				for (int l = 0; l < taskList_cplx.size(); l++) {
					String pc = taskList_cplx.get(l).getPath();
					String pcId = taskList_cplx.get(l).getPathId();
					Rw rw = new Rw();
					rw.setRwid(pcId);
					rw.setRwname(pc);
					if (rwList_pc.size() > 0) {
						String str = "";
						for (int j = 0; j < rwList_pc.size(); j++) {
							str = str + rwList_pc.get(j).getRwid();
						}
						if (!str.contains(pcId)) {
							rwList_pc.add(rw);
						}
					} else {
						rwList_pc.add(rw);
					}
				}
				//批次节点
				for (int j = 0; j < rwList_pc.size(); j++) {
					TreeNode node1 = new DepartmentNode(Long.valueOf(rwList_pc.get(j).getRwid()), rwList_pc.get(j).getRwname(),  "0", rootNode, 2);
//					rootNode.add(node1);

					List<Rw> rwList_ch = new ArrayList<Rw>();				//策划
					List<Task> taskList_cppc = DataSupport.where("location = ? and pathId =?", "1",rwList_pc.get(j).getRwid()).find(Task.class);
					rwList_ch.clear();
					for (int l = 0; l < taskList_cppc.size(); l++) {
						String ch = taskList_cppc.get(l).getChbh();
						String chId = taskList_cppc.get(l).getChId();
						Rw rw = new Rw();
						rw.setRwid(chId);
						rw.setRwname(ch);
						if (rwList_ch.size() > 0) {
							String str = "";
							for (int i1 = 0; i1 < rwList_ch.size(); i1++) {
								str = str + rwList_ch.get(i1).getRwid();
							}
							if (!str.contains(chId)) {
								rwList_ch.add(rw);
							}
						} else {
							rwList_ch.add(rw);
						}
					}
					//4,获取所有表格ID---表格名称
					for(int k=0; k<rwList_ch.size(); k++){
						TreeNode node = new DepartmentNode(Long.valueOf(rwList_ch.get(k).getRwid()), rwList_ch.get(k).getRwname(),  "0", node1, 3);
//						rootNode.add(node);

						List<Task> tasknodeList = new ArrayList<Task>();
						for(Task task : taskList_cppc){
							if(task.getChId().equals(rwList_ch.get(k).getRwid())){
								tasknodeList.add(task);
							}
						}
						for(int loop=0; loop<tasknodeList.size(); loop++){
							node.add(new UserNode(Long.valueOf(tasknodeList.get(loop).getTaskid()), tasknodeList.get(loop).getTaskname(), node, 4));
						}
						node1.add(node);
					}
					node0.add(node1);
				}
				rootNode.add(node0);
			}
			rootNode.expandAllNode();
			rootNode.filterVisibleNode(nodeList);
		}
		adapter.notifyDataSetChanged();
	}
	
	OnItemClickListener listViewItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			// TODO Auto-generated method stub,arg2Ϊposition arg3Ϊid.
			TreeNode node = nodeList.get(position);
			Log.d("表的ID", clicktaskid+"");
			clicktaskid = nodeList.get(position).getId();
			if (node.getExpandStatus() == 2) {				//点击末尾节点事件
				node.setCheckStatus(node.getCheckStatus() == 0 ? 1 : 0);
				node.getParent().UpChecked();
				adapter.notifyDataSetChanged();
			}
			if (node.getExpandStatus() == 1) {
				node.setExpandStatus(0);
				nodeList = new ArrayList<TreeNode>();
				rootNode.filterVisibleNode(nodeList);
				adapter.notifyDataSetChanged();
			}
			else if (node.getExpandStatus() == 0) {
				node.setExpandStatus(1);
				nodeList = new ArrayList<TreeNode>();
				rootNode.filterVisibleNode(nodeList);
				adapter.notifyDataSetChanged();
			}
		}
	};
	
	/**
	 * 该方法在遇到LISTVIEW负责页面展现时有问题，需要重新优化
	 */
	OnClickListener OnCheckListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			switch(arg0.getId()){
			case R.id.check_button:
				Log.d("表的ID", clicktaskid+"");
				break;
			case R.id.read_button:
				Log.d("表的ID", clicktaskid+"");
				break;

			default:
				int index = (Integer) arg0.getTag();
				TreeNode node = nodeList.get(index);
				TreeHelper.onDepartmentChecked(node);
				if (node.getParent() != null)
					node.getParent().UpChecked();
				nodeList = new ArrayList<TreeNode>();
				rootNode.filterVisibleNode(nodeList);
				adapter.notifyDataSetChanged();
			}
		}
	};
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			prodlg.dismiss();
			if(msg.what == 1) {
				dialog = new AlertDialog.Builder(getActivity());
				dialog.setIcon(R.drawable.logo_title).setTitle(R.string.app_name);
				dialog.setMessage("策划完成，点击确定重新加载！");
				dialog.setCancelable(false);
				dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent1 = new Intent(getActivity(), MainActivity1.class);
						startActivity(intent1);

					}
				});
				dialog.show();
			}
		}
	};

	private void showSweetAlertDialog(long clicktaskid, NodeButtonEnum nodebutton) {

        buttontype = nodebutton;
        if(buttontype == NodeButtonEnum.READBUTTON){
        	Toast.makeText(getActivity(), "数据初始化中，1秒后加载.......", Toast.LENGTH_LONG).show();
        	ReadActivity1.actionStart(getActivity(), clicktaskid, mHandler, "1");
        }
        if(buttontype == NodeButtonEnum.CHECKBUTTON){
        	Toast.makeText(getActivity(), "数据初始化中，1秒后加载.......", Toast.LENGTH_LONG).show();
        	CheckActivity1.actionStart(getActivity(), clicktaskid, mHandler, "1");
        }
        if(buttontype == NodeButtonEnum.SIGNBUTTON){
        	Toast.makeText(getActivity(), "数据初始化中，1秒后加载.......", Toast.LENGTH_LONG).show();
        	SignActivity1.actionStart(getActivity(), clicktaskid, mHandler, "1");
        }
    }

	/**
	 * ListView适配器。
	 */
	private class MyBaseAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return nodeList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return nodeList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			int layer = nodeList.get(position).getLayer();
			List<Task> taskList = new ArrayList<Task>();
			if (layer == 3) {
				Long chId = nodeList.get(position).getId();
				taskList = DataSupport.where("chid = ? and location=? and IsBrother = ?", String.valueOf(chId), "1", "0").find(Task.class);
			}
			String broTaskId = "";
			if (layer == 4) {
				List<Task> taskList1 = DataSupport.where("taskid = ?", String.valueOf(nodeList.get(position).getId())).find(Task.class);
				if (taskList1.size() > 0) {
					broTaskId = taskList1.get(0).getBroTaskId();
				}
			}
			int[] leftIds = { R.drawable.icon_plusminus_add_black, R.drawable.icon_plusminus_reduce_black, R.drawable.icon_head_default, R.drawable.check};
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				holder = new ViewHolder();
				if(layer == 4){
					convertView = inflater.inflate(R.layout.tree_item, null);
					holder.tv_name = (TextView) convertView.findViewById(R.id.txt_tree_name);
					holder.tv_width = (TextView) convertView.findViewById(R.id.txt_tree_width);
					holder.iv_left = (ImageView) convertView.findViewById(R.id.img_tree_left);
					holder.check_button = (Button) convertView.findViewById(R.id.check_button);
					holder.read_button = (Button) convertView.findViewById(R.id.read_button);
					if (broTaskId != null) {
						if (broTaskId.equals("")) {
							holder.check_button.setVisibility(View.INVISIBLE);
						}
					} else if (broTaskId == null){
						holder.check_button.setVisibility(View.INVISIBLE);
					}
					holder.check_button.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							clicktaskid = nodeList.get(position).getId();
							showSweetAlertDialog(clicktaskid, NodeButtonEnum.CHECKBUTTON);
						}
					});
					holder.read_button.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							clicktaskid = nodeList.get(position).getId();
							showSweetAlertDialog(clicktaskid, NodeButtonEnum.READBUTTON);
						}
					});
				} else if (layer == 3) {
					convertView = inflater.inflate(R.layout.tree_item_init_copy, null);
					holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
					holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
					holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
					holder.ch_button = (Button) convertView.findViewById(R.id.cehua_button);
					final List<Task> finalTaskList = taskList;
					if (finalTaskList != null) {

						holder.ch_button.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								warnInfo(finalTaskList);
							}
						});
					} else {
						Toast.makeText(getActivity(), "无策划模板，请检查数据。", Toast.LENGTH_LONG).show();
					}
				} else if (layer == 2) {
					convertView = inflater.inflate(R.layout.tree_item_init, null);
					holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
					holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
					holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
				} else if (layer == 1) {
					convertView = inflater.inflate(R.layout.tree_item_init1, null);
					holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
					holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
					holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);

				} else  if (layer == 0) {
					convertView = inflater.inflate(R.layout.tree_item_init_null, null);
					holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
					holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
					holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
				}
				convertView.setTag(holder);
			}
			else {
				LayoutInflater inflater = LayoutInflater.from(context);
				holder = new ViewHolder();
				if(layer == 4){
					convertView = inflater.inflate(R.layout.tree_item, null);
					holder.tv_name = (TextView) convertView.findViewById(R.id.txt_tree_name);
					holder.tv_width = (TextView) convertView.findViewById(R.id.txt_tree_width);
					holder.iv_left = (ImageView) convertView.findViewById(R.id.img_tree_left);
					holder.check_button = (Button) convertView.findViewById(R.id.check_button);
					holder.read_button = (Button) convertView.findViewById(R.id.read_button);
					if (broTaskId != null) {
						if (broTaskId.equals("")) {
							holder.check_button.setVisibility(View.INVISIBLE);
						}
					} else if (broTaskId == null){
						holder.check_button.setVisibility(View.INVISIBLE);
					}
					holder.check_button.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							clicktaskid = nodeList.get(position).getId();
							showSweetAlertDialog(clicktaskid, NodeButtonEnum.CHECKBUTTON);
						}
					});
					holder.read_button.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							clicktaskid = nodeList.get(position).getId();
							showSweetAlertDialog(clicktaskid, NodeButtonEnum.READBUTTON);
						}
					});
				} else if (layer == 3) {
					convertView = inflater.inflate(R.layout.tree_item_init_copy, null);
					holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
					holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
					holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
					holder.ch_button = (Button) convertView.findViewById(R.id.cehua_button);
					final List<Task> finalTaskList = taskList;
					if (finalTaskList != null) {

						holder.ch_button.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								warnInfo(finalTaskList);
							}
						});
					} else {
						Toast.makeText(getActivity(), "无策划模板，请检查数据。", Toast.LENGTH_LONG).show();
					}
				} else if (layer == 2) {
					convertView = inflater.inflate(R.layout.tree_item_init, null);
					holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
					holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
					holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
				} else if (layer == 1) {
					convertView = inflater.inflate(R.layout.tree_item_init1, null);
					holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
					holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
					holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
				} else  if (layer == 0) {
					convertView = inflater.inflate(R.layout.tree_item_init_null, null);
					holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
					holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
					holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
				}
				convertView.setTag(holder);
			}
			holder.tv_name.setText("" + nodeList.get(position).getName());
			holder.tv_width.setText("");

			holder.iv_left.setImageResource(leftIds[nodeList.get(position).getExpandStatus()]);
			if (nodeList.get(position).getExpandStatus() == 2 && broTaskId != null) {
				holder.iv_left.setImageResource(leftIds[3]);
			}
			holder.tv_width.setMinWidth(layer * (holder.iv_left.getLayoutParams().width));

			return convertView;
		}

		public final class ViewHolder {
			public ImageView iv_left;
			public TextView tv_name;
			public TextView tv_width;
			//查看，检查，签署按钮
			public Button check_button;
			public Button read_button;
			public Button delete_button;
			public Button copy_button;
			public Button ch_button;
		}
	}

	@Subscribe
	public void onEventMainThread(LocationEvent locationEvent){
		if(locationEvent != null){
			loadData();
			adapter.notifyDataSetChanged();

		}
	}

	public void warnInfo(final List<Task> taskList) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setIcon(R.drawable.logo_title).setTitle("是否进行策划！");
		dialog.setMessage("此操作只允许管理员执行，请谨慎操作！");
		dialog.setCancelable(false);
		dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showMutilAlertDialog(taskList);

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

	/**
	 * @Description: 自定义节点名称
	 * @author qiaozhili
	 * @date 2019/3/4 11:39
	 * @param
	 * @return
	 */
	public String getNodeName(final List<Task> taskList) {
		LayoutInflater factory = LayoutInflater.from(getActivity());//提示框
		final View view = factory.inflate(R.layout.editbox_layout, null);//这里必须是final的
		final EditText edit = (EditText) view.findViewById(R.id.editText);//获得输入框对象
		final String[] nodeName = {""};
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		String timeString = formatter.format(curDate);
		edit.setText(taskList.get(0).getTaskname() + timeString);
		new AlertDialog.Builder(getActivity())
				.setTitle("请输入新表单名称！")//提示框标题
				.setView(view)
				.setPositiveButton("确定",//提示框的两个按钮
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
												int which) {
								Message message = new Message();
								prodlg = ProgressDialog.show(context, "警告", "正在策划，请稍侯...");
								nodeName[0] = String.valueOf(edit.getText());
								for (Task task : taskList) {
									copyTask(task, nodeName[0]);
								}
								message.what = 1;
								mHandler.sendMessage(message);
							}
						})
				.setNegativeButton("取消", null).create().show();
		return nodeName[0];
	}

	/**
	 * @Description: 复制节点及task
	 * @author qiaozhili
	 * @date 2019/3/4 10:56
	 * @param
	 * @return
	 */
	public void copyTask(Task task, String nodeName) {
		Task taskNew = new Task();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		String timeString = formatter.format(curDate);
		long timeL = 0;
		String taskIdNew = "";
		String currentSecond = getTime(timeString);
		long taskIdL = Long.parseLong(task.getTaskid());
		long PathIdL = Long.parseLong(task.getPathId());
		if (task.getTaskid().length() > currentSecond.length()) {
			int size = task.getTaskid().length() - currentSecond.length();
			timeL = (long) (Long.parseLong(currentSecond) * Math.pow(10,size));
		}
		if (!task.getTaskid().equals("")) {
			String taskIdN = String.valueOf(taskIdL + timeL);
			taskNew.setTaskid(taskIdN);
			taskIdNew = taskIdN;
		} else {
			taskNew.setTaskid("");
		}
//		if (!task.getPathId().equals("")) {
//			String pathIdN = String.valueOf(PathIdL + timeL);
//			taskNew.setPathId(pathIdN);
//			taskNew.setChbh(pathIdN);
//		} else {
//			taskNew.setPathId("");
//			taskNew.setChbh("");
//		}
		taskNew.setTempid(task.getTempid());
		taskNew.setTempType(task.getTempType());
		taskNew.setPath(task.getPath());
		taskNew.setPathId(task.getPathId());
		taskNew.setChbh(task.getChbh());
		taskNew.setChId(task.getChId());
		taskNew.setRwid(task.getRwid());
		taskNew.setRwname(task.getRwname());
		taskNew.setXhId(task.getXhId());
		taskNew.setXhName(task.getXhName());
		taskNew.setTaskname(nodeName);
		taskNew.setRemark(task.getRemark());
		taskNew.setVersion(task.getVersion());
		taskNew.setLocation(task.getLocation());
		taskNew.setTaskpic(task.getTaskpic());
		taskNew.setTablesize(task.getTablesize());
		taskNew.setPost(task.getPost());
		taskNew.setPostinstanceid(task.getPostinstanceid());
		taskNew.setPostname(task.getPostname());
		taskNew.setNodeLeaderId(task.getNodeLeaderId());
		taskNew.setLinenum(task.getLinenum());
		taskNew.setRownum(task.getRownum());
		taskNew.setStartTime(task.getStartTime());
		taskNew.setEndTime(task.getEndTime());
		taskNew.setIsfinish(task.getIsfinish());
		taskNew.setIsfirstfinish(task.getIsfirstfinish());
		taskNew.setInitStatue(task.getInitStatue());
		taskNew.setConditions(task.getConditions());
		taskNew.setSigns(task.getSigns());
		taskNew.setCells(task.getCells());
		taskNew.setRownummap(task.getRownummap());
		taskNew.setIsBrother(1);
		taskNew.setBroTaskId(task.getTaskid());
		taskNew.setPCLBId(task.getPCLBId());
		taskNew.save();
		if (!taskIdNew.equals("")) {
			copyCondition(task, taskIdNew, timeL);
			copySignature(task, taskIdNew, timeL);
			copyRows(task, taskIdNew, timeL);
			copyCells(task, taskIdNew, timeL);
//			copyHtml(task, task.getPostname(), taskIdNew, timeL);
		} else {
			Toast.makeText(getActivity(), "数据有误，请检查数据", Toast.LENGTH_LONG);
		}

	}

	/**
	 * @param
	 * @return
	 * @Description: 复制conditions
	 * @author qiaozhili
	 * @date 2019/3/4 14:44
	 */
	public void copyCondition(Task task, String taskIdN, long timeL) {
		List<Scene> conditionList = DataSupport.where("taskid=?", task.getTaskid()).find(Scene.class);
		for (Scene scne : conditionList) {
			Scene scneNew = new Scene();
			if (!scne.getConditionid().equals("")) {
				long conditionIdL = Long.parseLong(scne.getConditionid());
				String conditionIdN = String.valueOf(conditionIdL + timeL);
				scneNew.setConditionid(conditionIdN);
			} else {
				scneNew.setConditionid("");
			}
			scneNew.setTaskid(taskIdN);
			scneNew.setTimeL(timeL);
			scneNew.setConditionname(scne.getConditionname());
			scneNew.setConditionDefId(scne.getConditionDefId());
			scneNew.setSceneorder(scne.getSceneorder());
			scneNew.setScenevalue(scne.getScenevalue());
			scneNew.setmTTID(scne.getmTTID());
			scneNew.save();
		}
	}

	/**
	 * @param
	 * @return
	 * @Description: 复制signs Signature
	 * @author qiaozhili
	 * @date 2019/3/4 14:45
	 */
	public void copySignature(Task task, String taskIdN, long timeL) {
		List<Signature> signatureList = DataSupport.where("taskid=?", task.getTaskid()).find(Signature.class);
		for (Signature signature : signatureList) {
			Signature signstureNew = new Signature();
			if (!signature.getSignid().equals("")) {
				long signIdL = Long.parseLong(signature.getSignid());
				String signIdN = String.valueOf(signIdL + timeL);
				signstureNew.setSignid(signIdN);
			} else {
				signstureNew.setSignid("");
			}
			signstureNew.setTaskid(taskIdN);
			signstureNew.setTimeL(timeL);
			signstureNew.setSignname(signature.getSignname());
			signstureNew.setSignorder(signature.getSignorder());
			signstureNew.setTime(signature.getTime());
			signstureNew.setRemark(signature.getRemark());
			signstureNew.setSignvalue(signature.getSignvalue());
			signstureNew.setSignDefID(signature.getSignDefID());
			signstureNew.setmTTId(signature.getmTTId());
			signstureNew.setSignTime(signature.getSignTime());
			signstureNew.setIsFinish(signature.getIsFinish());
			signstureNew.setBitmappath(signature.getBitmappath());
			signstureNew.save();
		}
	}

	/**
	 * @param
	 * @return
	 * @Description: 复制Rows
	 * @author qiaozhili
	 * @date 2019/3/4 14:47
	 */
	public void copyRows(Task task, String taskIdN, long timeL) {
		List<Rows> rowsList = DataSupport.where("taskid=?", task.getTaskid()).find(Rows.class);
		for (Rows rows : rowsList) {
			Rows rowsNew = new Rows();
			rowsNew.setTaskid(taskIdN);
			rowsNew.setTimeL(timeL);
			rowsNew.setRowsid(rows.getRowsid());
			rowsNew.setRowsnumber(rows.getRowsnumber());
			rowsNew.save();
		}
	}

	/**
	 * @param
	 * @return
	 * @Description: 复制Cells
	 * @author qiaozhili
	 * @date 2019/3/4 15:24
	 */
	public void copyCells(Task task, String taskIdN, long timeL) {
		Document htmlDoc = HtmlHelper.getHtmlDoc(task);
		String fileAbsPath = Environment.getDataDirectory().getPath() + Config.packagePath
				+ Config.htmlPath+ "/"+ task.getPostname()+"/" + taskIdN;
		String htmlStr = htmlDoc.toString();
		List<Cell> cellList = DataSupport.where("taskid=?", task.getTaskid()).find(Cell.class);
		for (Cell cell : cellList) {
			Cell cellNew = new Cell();
			String cellIdNew = "";
			String cellIdOlder = cell.getCellid();
			if (!cellIdOlder.equals("")) {
				long cellIdL = Long.parseLong(cellIdOlder);
				String cellIdN = String.valueOf(cellIdL + timeL);
				cellNew.setCellid(cellIdN);
				cellIdNew = cellIdN;
			} else {
				cellNew.setCellid("");
			}
			if (htmlStr.contains(cellIdOlder)) {
				htmlStr = htmlStr.replace(cellIdOlder, cellIdNew);
			}
//			cellNew.setCellid(cell.getCellid());
			cellNew.setCellidold(cellIdOlder);
			cellNew.setTaskid(taskIdN);
			cellNew.setTimeL(timeL);
			cellNew.setRowname(cell.getRowname());
			cellNew.setHorizontalorder(cell.getHorizontalorder());
			cellNew.setVerticalorder(cell.getVerticalorder());
			cellNew.setType(cell.getType());
			cellNew.setTextvalue(cell.getTextvalue());
			cellNew.setColumnid(cell.getColumnid());
			cellNew.setTablesize(cell.getTablesize());
			cellNew.setRowsid(cell.getRowsid());
			cellNew.setmTTID(cell.getmTTID());
			cellNew.setIshook(cell.getIshook());
			cellNew.setOpvalue(cell.getOpvalue());
			cellNew.setCelltype(cell.getCelltype());
			cellNew.setActualval(cell.getActualval());
			cellNew.setRequireval(cell.getRequireval());
			cellNew.setUpper(cell.getUpper());
			cellNew.setLower(cell.getLower());
			cellNew.setCompliance(cell.getCompliance());
			cellNew.setMarkup(cell.getMarkup());
			cellNew.setIsFuhe(cell.getIsFuhe());
			cellNew.save();

			if (!cell.getCellid().equals("")) {
				copyOperetion(task, taskIdN, cellIdNew, cell.getCellid(), timeL);
			} else {
				Toast.makeText(getActivity(), "数据有误，请检查数据！", Toast.LENGTH_LONG);
			}
		}

		boolean bWriteOK = HtmlHelper.writeTaskHtml(fileAbsPath, htmlStr);
		if (bWriteOK) {
			Toast.makeText(getActivity(), taskIdN + ":HTML策划成功", Toast.LENGTH_LONG);
		} else {
			Toast.makeText(getActivity(), taskIdN + ":HTML策划失败", Toast.LENGTH_LONG);
		}

	}

	/**
	 * @param
	 * @return
	 * @Description: 复制Operetion
	 * @author qiaozhili
	 * @date 2019/3/4 15:25
	 */
	public void copyOperetion(Task task, String taskIdN, String cellIdN, String cellIdOld, Long timeL) {
		List<Operation> operationList = DataSupport.where("cellid=? and taskid=?", cellIdOld, task.getTaskid()).find(Operation.class);
		for (Operation operation : operationList) {
			Operation operationNew = new Operation();
			operationNew.setTaskid(taskIdN);
			operationNew.setCellidold(cellIdOld);
			operationNew.setTimeL(timeL);
			operationNew.setCellid(cellIdN);
			operationNew.setOperationid(cellIdN);
			operationNew.setRealcellid(cellIdN);
			operationNew.setType(operation.getType());
			operationNew.setOpvalue(operation.getOpvalue());
			operationNew.setRemark(operation.getRemark());
			operationNew.setIsfinished(operation.getIsfinished());
			operationNew.setTextvalue(operation.getTextvalue());
			operationNew.setmTTID(operation.getmTTID());
			operationNew.setOperationtype(operation.getOperationtype());
			operationNew.setIldd(operation.getIldd());
			operationNew.setIildd(operation.getIildd());
			operationNew.setTighten(operation.getTighten());
			operationNew.setErr(operation.getErr());
			operationNew.setLastaction(operation.getLastaction());
			operationNew.setIsmedia(operation.getIsmedia());
			operationNew.setSketchmap(operation.getSketchmap());
			operationNew.setTime(operation.getTime());
			operationNew.save();
		}
	}

	/**
	 * @param
	 * @return
	 * @Description: 复制HTMl
	 * @author qiaozhili
	 * @date 2019/3/4 17:49
	 */
	public void copyHtml(Task task, String postName, String taskIdN, long timeL) {
		Document htmlDoc = HtmlHelper.getHtmlDoc(task);
		String fileAbsPath = Environment.getDataDirectory().getPath() + Config.packagePath
				+ Config.htmlPath+ "/"+ postName+"/" + taskIdN;
		boolean bWriteOK = HtmlHelper.writeTaskHtml(fileAbsPath, htmlDoc.toString());
		if (bWriteOK) {
			Toast.makeText(getActivity(), taskIdN + ":HTML策划成功", Toast.LENGTH_LONG);
		} else {
			Toast.makeText(getActivity(), taskIdN + ":HTML策划失败", Toast.LENGTH_LONG);
		}
	}

	/**
	 * @Description: 获取当前时间毫秒
	 * @author qiaozhili
	 * @date 2019/3/4 11:10
	 * @param
	 * @return
	 */
	public static String getTime(String timeString){

		String timeStamp = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
		Date d;
		try{
			d = sdf.parse(timeString);
			long l = d.getTime();
			timeStamp = String.valueOf(l);
		} catch(ParseException e){
			e.printStackTrace();
		}
		return timeStamp;
	}

	public void showMutilAlertDialog(final List<Task> taskList){
		final String[] items = new String[taskList.size()];
		final boolean[] itemvalue = new boolean[taskList.size()];
		final List<Task> taskList1 = new ArrayList<>();
//		for (Task task : taskList) {
//			taskList1.add(task);
//		}
		for (int i = 0; i < taskList.size(); i++) {
			items[i] = taskList.get(i).getTaskname();
			itemvalue[i] = true;
		}
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		alertBuilder.setTitle("请选择模板");
		alertBuilder.setCancelable(false);
		/**
		 *第一个参数:弹出框的消息集合，一般为字符串集合
		 * 第二个参数：默认被选中的，布尔类数组
		 * 第三个参数：勾选事件监听
		 */
		alertBuilder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
//				Toast.makeText(context, items[i], Toast.LENGTH_SHORT).show();
				taskList1.clear();
				taskList1.add(taskList.get(i));
			}
		});
//		alertBuilder.setMultiChoiceItems(items, itemvalue, new DialogInterface.OnMultiChoiceClickListener() {
//			@Override
//			public void onClick(DialogInterface dialogInterface, int i, boolean isChecked) {
//				if (isChecked){
//					for (int j = 0; j < taskList.size(); j++) {
//						if (taskList.get(j).getTaskname().equals(items[i])) {
//							taskList1.add(taskList.get(j));
//						}
//					}
//					itemvalue[i] = true;
//				}else {
//					for (int j = 0; j < taskList1.size(); j++) {
//						if (taskList1.get(j).getTaskname().equals(items[i])) {
//							taskList1.remove(j);
//						}
//					}
//					itemvalue[i] = false;
//				}
//			}
//		});
		alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				if (taskList1.size() == 0) {
					taskList1.add(taskList.get(0));
				}
				String nodeName = getNodeName(taskList1);
				alertDialog3.dismiss();
			}
		});

		alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				alertDialog3.dismiss();
			}
		});


		alertDialog3 = alertBuilder.create();
		alertDialog3.show();
	}

	@Override
	public void onResume() {
		OrientApplication.getApplication().setIsWanzheng(0);
		super.onResume();
	}
}
