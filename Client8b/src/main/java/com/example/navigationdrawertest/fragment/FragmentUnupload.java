package com.example.navigationdrawertest.fragment;

import java.util.ArrayList;
import java.util.List;

import org.litepal.crud.DataSupport;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navigationdrawertest.R;
import com.example.navigationdrawertest.SweetAlert.SweetAlertDialog;
import com.example.navigationdrawertest.activity.CheckActivity;
import com.example.navigationdrawertest.activity.CheckActivity1;
import com.example.navigationdrawertest.activity.MainActivity1;
import com.example.navigationdrawertest.activity.ReadActivity;
import com.example.navigationdrawertest.activity.ReadActivity1;
import com.example.navigationdrawertest.activity.SignActivity;
import com.example.navigationdrawertest.activity.SignActivity1;
import com.example.navigationdrawertest.adapter.Event.LocationEvent;
import com.example.navigationdrawertest.application.OrientApplication;
import com.example.navigationdrawertest.model.BCRelation;
import com.example.navigationdrawertest.model.Cell;
import com.example.navigationdrawertest.model.Operation;
import com.example.navigationdrawertest.model.Post;
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
import com.example.navigationdrawertest.utils.NodeButtonEnum;

import de.greenrobot.event.EventBus;

/**
 * 通讯录Fragment的界面
 * 
 * http://blog.csdn.net/guolin_blog/article/details/26365683
 * 
 * @author guolin
 */

@SuppressLint("ValidFragment")
public class FragmentUnupload extends Fragment {
	private ListView mListView = null;
	private MyBaseAdapter adapter = null;
	private ArrayList<TreeNode> nodeList = new ArrayList<TreeNode>();
	private String currentUserid = OrientApplication.getApplication().loginUser.getUserid();
	Context context;
	TreeNode rootNode;
	private long clicktaskid;
	private NodeButtonEnum buttontype;
	private RwRelation proEntity;				//传递过来的项目树节点
	private BCRelation BCproEntity;
	private static User user;
	private String fieldType = "";  //1产品验收  2武器所检  3靶场试验
	
	public FragmentUnupload(RwRelation proEntity, String fieldType, BCRelation BCproEntity){
		this.proEntity = proEntity;
		this.fieldType = fieldType;
		this.BCproEntity = BCproEntity;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
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
//		if(adapter == null){		//这里有可能有问题，再次滑动过来就不会刷新了
//			adapter = new MyBaseAdapter();
//			loadData();
//		}
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
		String xhId = "";
		if (fieldType.equals("1")) {
			xhId = proEntity.getRwid();
		} else {
			xhId = BCproEntity.getSsxh();
		}
		List<Task> taskList_rw = DataSupport.where("location = ? and xhId =?  and fieldType=?", "3", xhId, fieldType).find(Task.class);
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
			String rwName = "";
			String rwId = "";
			if (fieldType.equals("1")) {
				rwId = proEntity.getRwid();
				rwName = proEntity.getRwname();
			} else {
				rwId = BCproEntity.getSsxh();
				rwName = BCproEntity.getXhdh();
			}
			rootNode = new DepartmentNode(Long.valueOf(rwId), rwName, "0", null, 0);
			//产品验收领域树结构
			if (fieldType.equals("1")) {

				//3,获取所有的岗位实例-path
				//产品类型节点
				for(int i=0; i<rwList.size(); i++){
//				rootNode = new DepartmentNode(Long.valueOf(rwList.get(i).getRwid()), rwList.get(i).getRwname(), "0", null, 0);
					TreeNode node0 = new DepartmentNode(Long.valueOf(rwList.get(i).getRwid()), rwList.get(i).getRwname(), "0", rootNode, 1);
//				rootNode.add(node0);

					List<Rw> rwList_pc = new ArrayList<Rw>();				//批次
					List<Task> taskList_cplx = DataSupport.where("location = ? and rwid =?", "3",rwList.get(i).getRwid()).find(Task.class);
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
						List<Task> taskList_cppc = DataSupport.where("location = ? and pathId =?", "3",rwList_pc.get(j).getRwid()).find(Task.class);
						rwList_ch.clear();
						for (int l = 0; l < taskList_cppc.size(); l++) {
							String ch = taskList_cppc.get(l).getChbh();
							String chId = taskList_cppc.get(l).getChId();
							//判读当前人是否有查看此策划的权限
							List<RwRelation> proList = DataSupport.where("userid = ?", OrientApplication.getApplication().loginUser.getUserid()).find(RwRelation.class);
							List<String> CHIDs = new ArrayList<>();
							for (RwRelation rw : proList) {
								if (!CHIDs.contains(rw.getProductid())) {
									CHIDs.add(rw.getProductid());
								}
							}
							if (CHIDs.contains(chId)){
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
			}
			//其他领域树结构
			else {
				List<BCRelation> bcRelationList = new ArrayList<>();
				if (fieldType.equals("3")) {
					bcRelationList = DataSupport.where("ssxh = ? and fieldType = ?", BCproEntity.getSsxh(), "3").find(BCRelation.class);
				} else if (fieldType.equals("2")) {
					bcRelationList = DataSupport.where("ssxh = ? and fieldType = ?", BCproEntity.getSsxh(), "2").find(BCRelation.class);
				}
				if (bcRelationList.size() > 0) {
					StringBuffer stringBuffer = new StringBuffer();
					for (int k = 0; k < bcRelationList.size(); k++) {
						if (!stringBuffer.toString().contains(bcRelationList.get(k).getChid())) {
							List<Task> BCtaskList = DataSupport.where("location = ? and chId =?", "3", bcRelationList.get(k).getChid()).find(Task.class);;
							if (BCtaskList.size() > 0) {
								TreeNode node = new DepartmentNode(Long.valueOf(bcRelationList.get(k).getChid()), bcRelationList.get(k).getChname(), "0", rootNode, 1);
								for (int loop = 0; loop < BCtaskList.size(); loop++) {
									node.add(new UserNode(Long.valueOf(BCtaskList.get(loop).getTaskid()), BCtaskList.get(loop).getTaskname(), node, 2));
								}
								rootNode.add(node);
							}
						}
						stringBuffer.append(bcRelationList.get(k).getChid());
					}
				}
			}
			rootNode.expandAllNode();
			rootNode.filterVisibleNode(nodeList);
		}
		adapter.notifyDataSetChanged();
	}
	
	OnItemClickListener listViewItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			TreeNode node = nodeList.get(position);
			Log.d("表的ID", clicktaskid+"");
			clicktaskid = nodeList.get(position).getId();
			if (node.getExpandStatus() == 2) {
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
//				showSweetAlertDialog();
				break;
			case R.id.read_button:
				Log.d("表的ID", clicktaskid+"");
//				showSweetAlertDialog();
				break;
//			case R.id.sign_button:
//				Log.d("表的ID", clicktaskid+"");
////				showSweetAlertDialog();
////				CheckActivity.actionStart(getActivity(), clicktaskid); 
//				break;
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
	
	protected  Handler mHandler =new Handler(){
		@Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {  
            case 0:  
//            	pDialog.dismiss();
                break;   
            }
        }
    };
    
    
	private void showSweetAlertDialog(long clicktaskid, NodeButtonEnum nodebutton) {
        buttontype = nodebutton;
//        dismiss(pDialog, clicktaskid);
        if(buttontype == NodeButtonEnum.READBUTTON){
        	Toast.makeText(getActivity(), "数据初始化中，5秒后加载.......", Toast.LENGTH_LONG).show();
        	ReadActivity1.actionStart(getActivity(), clicktaskid, mHandler, "1");
        }
        if(buttontype == NodeButtonEnum.CHECKBUTTON){
        	Toast.makeText(getActivity(), "数据初始化中，5秒后加载.......", Toast.LENGTH_LONG).show();
        	CheckActivity1.actionStart(getActivity(), clicktaskid, mHandler, "1");
        }
        if(buttontype == NodeButtonEnum.CHECKBUTTON){
        	Toast.makeText(getActivity(), "数据初始化中，5秒后加载.......", Toast.LENGTH_LONG).show();
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
			return arg0;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			int layer = nodeList.get(position).getLayer();
			Long pathId = nodeList.get(position).getId();
			final List<Task> taskList = DataSupport.where("chid = ? and location=?", String.valueOf(pathId), "3").find(Task.class);
			String broTaskId = "";
			if (layer == 4) {
				List<Task> taskList1 = DataSupport.where("taskid = ?", String.valueOf(nodeList.get(position).getId())).find(Task.class);
				if (taskList1.size() > 0) {
					broTaskId = taskList1.get(0).getBroTaskId();
				}
			}
			int[] leftIds = { R.drawable.icon_plusminus_add_black, R.drawable.icon_plusminus_reduce_black, R.drawable.check, R.drawable.check};
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				holder = new ViewHolder();
				if (fieldType.equals("1")) {
					//产品验收树结构
					if(layer == 4){
						convertView = inflater.inflate(R.layout.tree_item_upload, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.fragmentupload_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.fragmentupload_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.fragmentupload_img_tree_left);
						holder.read_button = (Button) convertView.findViewById(R.id.fragmentupload_look_button);
						holder.read_button.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								clicktaskid = nodeList.get(position).getId();
								showSweetAlertDialog(clicktaskid, NodeButtonEnum.READBUTTON);
							}
						});
						holder.unupload_back = (Button) convertView.findViewById(R.id.fragmentUnupload_back_button);
						holder.unupload_delete = (Button) convertView.findViewById(R.id.fragmentupload_look_deletebutton);
						holder.unupload_delete.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Dialog alertDialog = new AlertDialog.Builder(context).
										setTitle("确定删除？").
										setMessage("您确定删除该条表单吗？").
										setIcon(R.drawable.logo_title).
										setPositiveButton("确定", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												String deleteint = nodeList.get(position).getId()+"";
												DataSupport.deleteAll(Task.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Signature.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Cell.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Operation.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Scene.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Rw.class, "tableinstanceid = ?", deleteint);
												EventBus.getDefault().post(new LocationEvent("ok"));
											}
										}).
										setNegativeButton("取消", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										}).
										create();
								alertDialog.show();
							}
						});
					} else if (layer == 0) {
						convertView = inflater.inflate(R.layout.tree_item_init_null, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
					}else{
						convertView = inflater.inflate(R.layout.tree_item_init, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
					}
				} else {
					//武器所检树结构
					if(layer == 2){
						convertView = inflater.inflate(R.layout.tree_item_upload, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.fragmentupload_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.fragmentupload_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.fragmentupload_img_tree_left);
						holder.read_button = (Button) convertView.findViewById(R.id.fragmentupload_look_button);
						holder.read_button.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								clicktaskid = nodeList.get(position).getId();
								showSweetAlertDialog(clicktaskid, NodeButtonEnum.READBUTTON);
							}
						});
						holder.unupload_back = (Button) convertView.findViewById(R.id.fragmentUnupload_back_button);
						holder.unupload_delete = (Button) convertView.findViewById(R.id.fragmentupload_look_deletebutton);
						holder.unupload_delete.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Dialog alertDialog = new AlertDialog.Builder(context).
										setTitle("确定删除？").
										setMessage("您确定删除该条表单吗？").
										setIcon(R.drawable.logo_title).
										setPositiveButton("确定", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												String deleteint = nodeList.get(position).getId()+"";
												DataSupport.deleteAll(Task.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Signature.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Cell.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Operation.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Scene.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Rw.class, "tableinstanceid = ?", deleteint);
												EventBus.getDefault().post(new LocationEvent("ok"));
											}
										}).
										setNegativeButton("取消", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										}).
										create();
								alertDialog.show();
							}
						});
					} else if (layer == 0) {
						convertView = inflater.inflate(R.layout.tree_item_init_null, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
					}else{
						convertView = inflater.inflate(R.layout.tree_item_init, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
					}
				}

				convertView.setTag(holder);
			}
			else {
				LayoutInflater inflater = LayoutInflater.from(context);
				holder = new ViewHolder();
				if (fieldType.equals("1")) {
					//产品验收树结构
					if(layer == 4){
						convertView = inflater.inflate(R.layout.tree_item_upload, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.fragmentupload_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.fragmentupload_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.fragmentupload_img_tree_left);
						holder.read_button = (Button) convertView.findViewById(R.id.fragmentupload_look_button);
						holder.read_button.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								clicktaskid = nodeList.get(position).getId();
								showSweetAlertDialog(clicktaskid, NodeButtonEnum.READBUTTON);
							}
						});
						holder.unupload_back = (Button) convertView.findViewById(R.id.fragmentUnupload_back_button);
						holder.unupload_delete = (Button) convertView.findViewById(R.id.fragmentupload_look_deletebutton);
						holder.unupload_delete.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Dialog alertDialog = new AlertDialog.Builder(context).
										setTitle("确定删除？").
										setMessage("您确定删除该条表单吗？").
										setIcon(R.drawable.logo_title).
										setPositiveButton("确定", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												String deleteint = nodeList.get(position).getId()+"";
												DataSupport.deleteAll(Task.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Signature.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Cell.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Operation.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Scene.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Rw.class, "tableinstanceid = ?", deleteint);
												EventBus.getDefault().post(new LocationEvent("ok"));
											}
										}).
										setNegativeButton("取消", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										}).
										create();
								alertDialog.show();
							}
						});
					} else if (layer == 0) {
						convertView = inflater.inflate(R.layout.tree_item_init_null, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
					}else{
						convertView = inflater.inflate(R.layout.tree_item_init, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
					}
				} else {
					//武器所检树结构
					if(layer == 4){
						convertView = inflater.inflate(R.layout.tree_item_upload, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.fragmentupload_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.fragmentupload_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.fragmentupload_img_tree_left);
						holder.read_button = (Button) convertView.findViewById(R.id.fragmentupload_look_button);
						holder.read_button.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View v) {
								clicktaskid = nodeList.get(position).getId();
								showSweetAlertDialog(clicktaskid, NodeButtonEnum.READBUTTON);
							}
						});
						holder.unupload_back = (Button) convertView.findViewById(R.id.fragmentUnupload_back_button);
						holder.unupload_delete = (Button) convertView.findViewById(R.id.fragmentupload_look_deletebutton);
						holder.unupload_delete.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Dialog alertDialog = new AlertDialog.Builder(context).
										setTitle("确定删除？").
										setMessage("您确定删除该条表单吗？").
										setIcon(R.drawable.logo_title).
										setPositiveButton("确定", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												String deleteint = nodeList.get(position).getId()+"";
												DataSupport.deleteAll(Task.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Signature.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Cell.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Operation.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Scene.class, "taskid = ?", deleteint);
												DataSupport.deleteAll(Rw.class, "tableinstanceid = ?", deleteint);
												EventBus.getDefault().post(new LocationEvent("ok"));
											}
										}).
										setNegativeButton("取消", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												dialog.dismiss();
											}
										}).
										create();
								alertDialog.show();
							}
						});
					} else if (layer == 0) {
						convertView = inflater.inflate(R.layout.tree_item_init_null, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
					}else{
						convertView = inflater.inflate(R.layout.tree_item_init, null);
						holder.tv_name = (TextView) convertView.findViewById(R.id.init_txt_tree_name);
						holder.tv_width = (TextView) convertView.findViewById(R.id.init_txt_tree_width);
						holder.iv_left = (ImageView) convertView.findViewById(R.id.init_img_tree_left);
					}
				}

				convertView.setTag(holder);
			
			}
			
			holder.tv_name.setText("" + nodeList.get(position).getName());
			holder.tv_width.setText("");

			holder.iv_left.setImageResource(leftIds[nodeList.get(position).getExpandStatus()]);
			if (nodeList.get(position).getExpandStatus() == 2 && broTaskId != null) {
				holder.iv_left.setImageResource(leftIds[3]);
			}

//			int[] leftIds = { R.drawable.icon_plusminus_add_black, R.drawable.icon_plusminus_reduce_black, R.drawable.icon_head_default };
			holder.iv_left.setImageResource(leftIds[nodeList.get(position).getExpandStatus()]);
			int[] rightIds = { R.drawable.icon_checkbox_none, R.drawable.icon_checkbox_all, R.drawable.icon_checkbox_part };
			holder.tv_width.setMinWidth(layer * (holder.iv_left.getLayoutParams().width));

			return convertView;
		}

		public final class ViewHolder {
			public ImageView iv_left;
//			public ImageView iv_right;
			public TextView tv_name;
			public TextView tv_width;
			//查看，检查，签署按钮
			public Button read_button;
			public Button unupload_delete;
			public Button unupload_back;
		}
	}
	
	public void onEventMainThread(LocationEvent locationEvent){
		if(locationEvent != null){
			loadData();
			adapter.notifyDataSetChanged();
		}
	}

	public void setLocation(Task task) {
		task.setLocation(2);
		task.update(task.getId());
		adapter.notifyDataSetChanged();
//		getActivity().finish();
		Intent intent1 = new Intent(getActivity(), MainActivity1.class);
		startActivity(intent1);
	}

	public void warnInfo(final Task task) {
		String file = "warn.txt";
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setIcon(R.drawable.logo_title).setTitle("是否进行状态回退！");
		dialog.setMessage("此操作只允许管理员执行，请谨慎操作！");
		dialog.setCancelable(false);
		dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setLocation(task);
				dialog.dismiss();
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
}
