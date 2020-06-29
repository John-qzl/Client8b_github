package com.example.navigationdrawertest.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.litepal.crud.DataSupport;

import com.example.navigationdrawertest.R;
import com.example.navigationdrawertest.application.OrientApplication;
import com.example.navigationdrawertest.model.Mmc;
import com.example.navigationdrawertest.model.Product;
import com.example.navigationdrawertest.model.Rw;
import com.example.navigationdrawertest.model.RwRelation;
import com.example.navigationdrawertest.secret.FileEncryption;
import com.example.navigationdrawertest.tree.DepartmentNode;
import com.example.navigationdrawertest.tree.TreeNode;
import com.example.navigationdrawertest.tree.UserNode;
import com.example.navigationdrawertest.utils.ArrUtil;
import com.example.navigationdrawertest.utils.FileOperation;
import com.example.navigationdrawertest.utils.Setting;
import com.example.navigationdrawertest.utils.ThridToolUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DocActivity extends BaseActivity{
	
	private ArrayList<TreeNode> nodeList = new ArrayList<TreeNode>();
	private MyBaseAdapter adapter;
	private TreeNode rootNode;
	private ListView searchList;
	private ProgressDialog progressDialog;
	private Context context;
	private ImageView mBack;
	private String nowProductId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.doclist);
		getActionBar().hide();
		nowProductId = getIntent().getStringExtra("nowProductId");
		context = this;
		initUI();
		adapter = new MyBaseAdapter();
		searchList.setAdapter(adapter);
		searchList.setOnItemClickListener(listViewItemClickListener);
		initData();
	}
	
	public void initUI(){
		searchList = (ListView) findViewById(R.id.doclist_list);
		mBack = (ImageView) findViewById(R.id.back);
		mBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(DocActivity.this);
                progressDialog.setTitle("加密操作");
                progressDialog.setMessage("数据正在加密处理中，请稍候！");
                progressDialog.setCancelable(false);               					//设置进度条是否可以按退回键取消
                progressDialog.setCanceledOnTouchOutside(false);  		//设置点击进度对话框外的区域对话框不消失
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //Android文件系统短期内删除后重新创建需要提前更名删除
                        File file = new File(Setting.FILE_SECRET_END);
                        FileOperation.RecursionDeleteFile(file);
                        Message message = new Message();
                        message.what = 1;
                        mHandler.sendMessage(message);
                    }
                }).start();
            }
        });
	}

	/**
	 * 0层节点：product
	 * 1层节点：project
	 * 2层节点：path
	 * 3层节点：task  qzl添加
	 * 4层节点多媒体资料
	 */
	public void initData(){
		List<Product> productList = DataSupport.findAll(Product.class);
//		List<Mmc> mmcList = DataSupport.findAll(Mmc.class);
		List<Mmc> mmcList = DataSupport.where("productId = ?", nowProductId).find(Mmc.class);
		List<Rw> mmcRWList = new ArrayList<>();
		if (mmcList.size() > 0) {
			for (Mmc mmc : mmcList) {
				Rw rw0 = new Rw();
				rw0.setRwid(mmc.getCategoryId());
				rw0.setRwname(mmc.getCategoryName());
				if (mmcRWList.size() == 0) {
					mmcRWList.add(rw0);
				} else {
					List<String> list = new ArrayList<String>();
					for (Rw rw : mmcRWList) {
						list.add(rw.getRwid());
					}
					if (!list.contains(mmc.getCategoryId())) {
						mmcRWList.add(rw0);
					}
				}
			}
		}
		//0,根节点
//		rootNode = new DepartmentNode(Long.valueOf(-1), "多媒体资料", "0", null, 0);

		if(mmcRWList.size() > 0){
			//2,类别节点
			for(Rw rw : mmcRWList){
//				TreeNode rwNode = new DepartmentNode(Long.valueOf(rw.getRwid()), rw.getRwname(), "0", productNode, 2);
				rootNode = new DepartmentNode(Long.valueOf(rw.getRwid()), rw.getRwname(), "0", null, 0);
				List<Mmc> mmcbatchList = DataSupport.where("categoryId = ?", rw.getRwid()).find(Mmc.class);
				List<Rw> mmcbatchList1 = new ArrayList<>();
				if (mmcbatchList.size() > 0) {
					for (Mmc mmc : mmcbatchList) {
						Rw rw1 = new Rw();
						rw1.setRwid(mmc.getBatchId());
						rw1.setRwname(mmc.getBatchName());
						if (mmcbatchList1.size() == 0) {
							mmcbatchList1.add(rw1);
						} else {
							List<String> list = new ArrayList<String>();
							for (Rw rw11 : mmcbatchList1) {
								list.add(rw11.getRwid());
							}
							if (!list.contains(rw1.getRwid())) {
								mmcbatchList1.add(rw1);
							}
						}
					}
				}
				//3,批次节点
				for (Rw rw1 : mmcbatchList1) {
					TreeNode rwNode3 = new DepartmentNode(Long.valueOf(rw1.getRwid()), rw1.getRwname(), "0", rootNode, 1);

					List<Mmc> mmcPlanIdList = DataSupport.where("batchId = ?", rw1.getRwid()).find(Mmc.class);
					List<Rw> mmcPlanIdList1 = new ArrayList<>();
					if (mmcPlanIdList.size() > 0) {
						for (Mmc mmc : mmcPlanIdList) {
							Rw rw2 = new Rw();
							rw2.setRwid(mmc.getPlanId());
							rw2.setRwname(mmc.getPlanName());
							if (mmcPlanIdList1.size() == 0) {
								mmcPlanIdList1.add(rw2);
							} else {
								List<String> list = new ArrayList<String>();
								for (Rw rw11 : mmcPlanIdList1) {
									list.add(rw11.getRwid());
								}
								if (!list.contains(rw2.getRwid())) {
									mmcPlanIdList1.add(rw2);
								}
							}
						}
					}
					//4,策划节点
					for (Rw rw2 : mmcPlanIdList1) {
						TreeNode rwNode4 = new DepartmentNode(Long.valueOf(rw2.getRwid()), rw2.getRwname(), "0", rwNode3, 2);

						List<Mmc> mmcFileIdList = DataSupport.where("PlanId = ?", rw2.getRwid()).find(Mmc.class);
						if (mmcFileIdList.size() > 0) {
							//5，文件节点
							for(Mmc mmc : mmcFileIdList){
								rwNode4.add(new UserNode(Long.valueOf(mmc.getMmc_Id()), mmc.getMmc_Name(), rwNode4, 3));
							}
						}
						rwNode3.add(rwNode4);
					}
					rootNode.add(rwNode3);
				}
//				productNode.add(rwNode);
				rootNode.expandAllNode();
				rootNode.filterVisibleNode(nodeList);
			}
//			rootNode.add(productNode);
		}

//		if(mmcProductList.size() > 0){
//			//1,型号节点
//			for(Product product : mmcProductList){
////				rootNode = new DepartmentNode(-1, product.getProduct_Name(), null, 0);
//				TreeNode productNode = new DepartmentNode(Long.valueOf(product.getProduct_Id()), product.getProduct_Name(), "0", rootNode, 1);
//				List<Mmc> mmcCategoryList = DataSupport.where("productId = ?", product.getProduct_Id()).find(Mmc.class);
//				List<Rw> mmcCategoryList1 = new ArrayList<>();
//				if (mmcCategoryList.size() > 0) {
//					for (Mmc mmc : mmcCategoryList) {
//						Rw rw = new Rw();
//						rw.setRwid(mmc.getCategoryId());
//						rw.setRwname(mmc.getCategoryName());
//						if (mmcCategoryList1.size() == 0) {
//							mmcCategoryList1.add(rw);
//						} else {
//							List<String> list = new ArrayList<String>();
//							for (Rw rw12 : mmcCategoryList1) {
//								list.add(rw12.getRwid());
//							}
//							if (!list.contains(rw.getRwid())) {
//								mmcCategoryList1.add(rw);
//							}
//						}
//					}
//				}
//
//			}
//		}else{
//			rootNode = new DepartmentNode(Long.valueOf(-1), "无", "0", null, 0);
//		}
//		nodeList.add(rootNode);


		adapter.notifyDataSetChanged();
	}
	
	
	OnItemClickListener listViewItemClickListener = new OnItemClickListener(){
		@Override
		public void onItemClick(android.widget.AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			TreeNode node = nodeList.get(arg2);
			if (node.getExpandStatus() == 2) {		//点击末尾节点事件
				final Mmc mmc = DataSupport.where("mmc_Id = ?", node.getId()+"").find(Mmc.class).get(0);
				if(mmc != null){
					/**
					 * 读取文件的时候临时生成一个文件
					 */
					progressDialog = new ProgressDialog(context);
	                progressDialog.setTitle("加密操作");
	                progressDialog.setMessage("文件正在解密处理中，请稍候！");
	                progressDialog.setCancelable(false);               					//设置进度条是否可以按退回键取消
	                progressDialog.setCanceledOnTouchOutside(false);  		//设置点击进度对话框外的区域对话框不消失
	                progressDialog.show();
		        	new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								String secret_start = Setting.FILE_SECRET_START + File.separator + mmc.getMmc_Name()
										+ "." + mmc.getType();
								FileOperation.createDir(Setting.FILE_SECRET_END);
								String secret_end = Setting.FILE_SECRET_END + File.separator + mmc.getMmc_Name()
										+ "." + mmc.getType();
								//乔志理  注销加密操作
//								FileEncryption.decryptold(secret_start, secret_end);
//								DESUtils.decrypt(secret_start, secret_end);
								Message message = new Message();
								message.what = 2;
								Bundle bundle = new Bundle();
								bundle.putString("path", secret_start);
								message.setData(bundle);
								mHandler.sendMessage(message);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
//					String path = Environment.getExternalStorageDirectory() + File.separator + "mmc" + 
//							 File.separator + mmc.getMmc_Id() + "." + mmc.getType();
//					File file = new File(path);
//					File file = new File(secret_end);
//					boolean s = file.exists();
//					ThridToolUtils.openFile(file, DocActivity.this);
				}else{
					Toast.makeText(DocActivity.this, "该文件不存在，请联系管理员", Toast.LENGTH_SHORT).show();
				}
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
		};
	};
	
	private class MyBaseAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			return nodeList.size();
		}

		@Override
		public Object getItem(int position) {
			return nodeList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View view, ViewGroup parent) {
			ViewHolder viewholder = null;
			if(viewholder == null){
				viewholder = new ViewHolder();
				LayoutInflater mInflater = LayoutInflater.from(DocActivity.this);
				view = mInflater.inflate(R.layout.tree_item_init, null);
				viewholder.iv_left = (ImageView) view.findViewById(R.id.init_img_tree_left);
				viewholder.tv_name = (TextView) view.findViewById(R.id.init_txt_tree_name);
				viewholder.tv_width = (TextView) view.findViewById(R.id.init_txt_tree_width);
				viewholder.delete = (ImageView) view.findViewById(R.id.init_img_tree_delete);
				view.setTag(viewholder);
			}else{
				viewholder = (ViewHolder) view.getTag();
			}
			
			int layer = nodeList.get(position).getLayer();
			viewholder.tv_name.setText("" + nodeList.get(position).getName());
			viewholder.tv_width.setText("");
			int[] leftIds = {R.drawable.icon_plusminus_add_black, R.drawable.icon_plusminus_reduce_black, R.drawable.icon_head_default};
			int[] fourType = {R.drawable.iconfont_doc, R.drawable.iconfont_dwg, R.drawable.iconfont_mp3, R.drawable.iconfont_txt, R.drawable.iconfont_jpg};
			if(layer == 5){
				viewholder.delete.setVisibility(View.VISIBLE);
				String name = nodeList.get(position).getName();
				if(name.contains(".doc") || name.contains(".DOC") || name.contains(".docx") || name.contains(".DOCX") || name.contains(".xls") || name.contains(".XLS")
						|| name.contains(".xlsx") || name.contains(".XLSX")){
					viewholder.iv_left.setImageResource(fourType[0]);
				}else if(name.contains(".dwg") || name.contains(".dwf") || name.contains(".dxf") || name.contains(".DWG") || name.contains(".DWF") || name.contains(".DXF")){
					viewholder.iv_left.setImageResource(fourType[1]);
				}else if(name.contains(".txt") || name.contains(".TXT")){
					viewholder.iv_left.setImageResource(fourType[3]);
				}else if(name.contains(".mp3") || name.contains(".avi") || name.contains(".3gp")
						||name.equals(".mp4") || name.contains(".MP3") || name.contains(".AVI") || name.contains(".3GP")
						||name.equals(".MP4")){
					viewholder.iv_left.setImageResource(fourType[2]);
				}else if(name.contains(".jpg") || name.contains(".png") || name.contains(".JPG") || name.contains(".PNG")){
					viewholder.iv_left.setImageResource(fourType[4]);
				}else{
					viewholder.iv_left.setImageResource(leftIds[nodeList.get(position).getExpandStatus()]);
				}
			}else{
				viewholder.iv_left.setImageResource(leftIds[nodeList.get(position).getExpandStatus()]);
			}
			viewholder.tv_width.setMinWidth(layer * (viewholder.iv_left.getLayoutParams().width));
			
			//2016-10-27 20:36:07
			viewholder.delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(DocActivity.this);
	                builder.setIcon(R.drawable.logo_title).setTitle("删除");
	                builder.setMessage("确定删除本文档？");
	                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialogInterface, int i) {
	                    	TreeNode node = nodeList.get(position);
//	            			if (node.getExpandStatus() == 2) {		//点击末尾节点事件
	            				Mmc mmc = DataSupport.where("mmc_Id = ?", node.getId()+"").find(Mmc.class).get(0);
	            				if(mmc != null){
	            					mmc.delete();
	            				}else{
	            					Toast.makeText(DocActivity.this, "该文件不存在，请联系管理员", Toast.LENGTH_SHORT).show();
	            				}
//	            			}
	            			nodeList.remove(position);
	            			dialogInterface.dismiss();
	            			adapter.notifyDataSetChanged();
	                    }
	                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
	                builder.show();
				}
			});
			return view;
		}
		
		public final class ViewHolder{
			public ImageView iv_left;
			public TextView tv_name;
			public TextView tv_width;
			public ImageView delete;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.docmenu, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
	        case R.id.doc_fanhui:
	        	progressDialog = new ProgressDialog(this);
//	        	progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                progressDialog.setTitle("加密操作");
//                progressDialog.setIcon(R.drawable.orient_min);
                progressDialog.setMessage("数据正在加密处理中，请稍候！");
                progressDialog.setCancelable(false);               					//设置进度条是否可以按退回键取消
                progressDialog.setCanceledOnTouchOutside(false);  		//设置点击进度对话框外的区域对话框不消失
                progressDialog.show();
	        	new Thread(new Runnable() {
					@Override
					public void run() {
						//Android文件系统短期内删除后重新创建需要提前更名删除
						File file = new File(Setting.FILE_SECRET_END);
						FileOperation.RecursionDeleteFile(file);
						Message message = new Message();
						message.what = 1;
						mHandler.sendMessage(message);
					}
				}).start();
	        	//finish();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
	}
	
	/**
     * 1,启动；2更新；3关闭
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch ((msg.what)) {
                case 1:
                	progressDialog.dismiss();
                	onDestroy();
                    break;
                case 2:
                	progressDialog.dismiss();
                	String path = msg.getData().getString("path"); 
					File file = new File(path);
					boolean s = file.exists();
					ThridToolUtils.openFile(file, DocActivity.this);
                    break;
            }
        }
    };
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		finish();
	}
	
}
