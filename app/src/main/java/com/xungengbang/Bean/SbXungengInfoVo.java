package com.xungengbang.Bean;

import java.util.Date;
import java.util.List;

/**
 * SbXungengInfo entity. @author MyEclipse Persistence Tools
 */

public class 	SbXungengInfoVo  {

	// Fields

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String xgId;			//电子巡更ID
	private String compId;			//数据所属单位ID
	private String  ztLx;				//巡更点安装主体类型：1监控中心、2值班室、3储存场所、4其他
	private String ztId;			//巡更点安装主体ID
	private String ztName;			//巡更点安装主体名称
	private String xgdName;			//巡更点名称
	private String xgdBianhao;		//巡更点编号
	private String xgRyId;				//巡更设备持有人ID或责任人ID
	private String xgRyName;		//巡更设备持有人姓名或责任人姓名
	private String xgsbName;		//巡更设备名称
	private String xgsbBianhao;		//巡更设备编号
	private String xgTime;			//巡更时间
	private String state;				//数据状态：0未上报、1已上报

	private List<SbXungengInfoFj> fjs;;		//上传巡更检查照片，字符串数组
	// Property accessors

	public String getXgId() {
		return this.xgId;
	}

	public void setXgId(String xgId) {
		this.xgId = xgId;
	}

	public String getCompId() {
		return this.compId;
	}

	public void setCompId(String compId) {
		this.compId = compId;
	}

	public String getZtLx() {
		return this.ztLx;
	}

	public void setZtLx(String ztLx) {
		this.ztLx = ztLx;
	}

	public String getZtId() {
		return this.ztId;
	}

	public void setZtId(String ztId) {
		this.ztId = ztId;
	}

	public String getZtName() {
		return this.ztName;
	}

	public void setZtName(String ztName) {
		this.ztName = ztName;
	}

	public String getXgdName() {
		return this.xgdName;
	}

	public void setXgdName(String xgdName) {
		this.xgdName = xgdName;
	}

	public String getXgdBianhao() {
		return this.xgdBianhao;
	}

	public void setXgdBianhao(String xgdBianhao) {
		this.xgdBianhao = xgdBianhao;
	}

	public String getXgRyId() {
		return this.xgRyId;
	}

	public void setXgRyId(String xgRyId) {
		this.xgRyId = xgRyId;
	}

	public String getXgRyName() {
		return this.xgRyName;
	}

	public void setXgRyName(String xgRyName) {
		this.xgRyName = xgRyName;
	}

	public String getXgsbName() {
		return this.xgsbName;
	}

	public void setXgsbName(String xgsbName) {
		this.xgsbName = xgsbName;
	}

	public String getXgsbBianhao() {
		return this.xgsbBianhao;
	}

	public void setXgsbBianhao(String xgsbBianhao) {
		this.xgsbBianhao = xgsbBianhao;
	}

	public String getXgTime() {
		return this.xgTime;
	}

	public void setXgTime(String xgTime) {
		this.xgTime = xgTime;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<SbXungengInfoFj> getFjs() {
		return fjs;
	}

	public void setFjs(List<SbXungengInfoFj> fjs) {
		this.fjs = fjs;
	}
}