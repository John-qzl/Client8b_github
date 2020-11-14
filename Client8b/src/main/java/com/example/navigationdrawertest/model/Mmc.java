package com.example.navigationdrawertest.model;

import org.litepal.crud.DataSupport;

public class Mmc extends DataSupport{
	
	private int id;
	private String mmc_Id;
	private String mmc_Name;
	private String gw_Id;					   		//岗位ID，也就是试验队ID
	private String displaypath_Name;			//展示路径
	private String rw_Id;								//任务ID
	private String type;
	private String taskId;
	private String taskName;
	private String productId;//型号ID
	private String productName;//型号Name
	private String categoryId;//类别ID
	private String categoryName;//类别Name
	private String batchId;//批次ID
	private String batchName;//批次Name
	private String PlanId;//策划ID
	private String PlanName;//策划Name
	private String fieldType;//策划Name

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getBatchName() {
		return batchName;
	}

	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	public String getPlanName() {
		return PlanName;
	}

	public void setPlanName(String planName) {
		PlanName = planName;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getPlanId() {
		return PlanId;
	}

	public void setPlanId(String planId) {
		PlanId = planId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMmc_Id() {
		return mmc_Id;
	}
	public void setMmc_Id(String mmc_Id) {
		this.mmc_Id = mmc_Id;
	}
	public String getMmc_Name() {
		return mmc_Name;
	}
	public void setMmc_Name(String mmc_Name) {
		this.mmc_Name = mmc_Name;
	}
	public String getGw_Id() {
		return gw_Id;
	}
	public void setGw_Id(String gw_Id) {
		this.gw_Id = gw_Id;
	}
	public String getDisplaypath_Name() {
		return displaypath_Name;
	}
	public void setDisplaypath_Name(String displaypath_Name) {
		this.displaypath_Name = displaypath_Name;
	}
	public String getRw_Id() {
		return rw_Id;
	}
	public void setRw_Id(String rw_Id) {
		this.rw_Id = rw_Id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}