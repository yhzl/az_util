package com.az.bean.util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.az.db.util.JdbcToExcel;
import com.google.gson.Gson;

public class DataToJson {
	static String querySql = "select id,name,level,upid from pre_common_district";
	static Connection conn = null;
	static int count = 1;
	static ExecutorService threadPool;
	static List<Thread> threadList = new ArrayList<Thread>();
	static List<AreaDTO> list = null;
	public static void main(String[] args)throws Exception {
		threadPool = Executors.newSingleThreadExecutor();
		conn = JdbcToExcel.getConn();
		ResultSet resultSet = conn.createStatement().executeQuery(querySql);
		list = new ArrayList<AreaDTO>();
		AreaDTO ad = null;
		while(resultSet.next()){
			ad = new AreaDTO(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("level"), resultSet.getInt("upid"));
			list.add(ad);
		}
		List<AreaDTO> resList = getJson(0);
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("F:/work/分析/1.json")));
		bufferedWriter.write(new Gson().toJson(resList));
		bufferedWriter.close();
	}
	public static List<AreaDTO> getJson(int upid) throws Exception{
		List<AreaDTO> lists = new ArrayList<AreaDTO>();
		for(AreaDTO temp : list){
			if(upid == temp.getUpid()){
				AreaDTO newDTO = temp.clone();
				lists.add(newDTO);
				if(newDTO.getLevel()!=4){
					newDTO.getDetails().addAll(getJson(newDTO.getId()));
				}
				if(upid == 0){
					break;
				}
			}
		}
		return lists;
	}
}
class AreaRunnable implements Runnable{
	private AreaDTO areaDTO;
	public AreaRunnable(AreaDTO areaDTO){
		this.areaDTO = areaDTO;
	}
	@Override
	public void run() {
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
class AreaDTO implements Cloneable{
	private int id;
	private String name;
	private int level;
	private int upid;
	private List<AreaDTO> details = new ArrayList<AreaDTO>();
	public AreaDTO(){}
	public AreaDTO(int id, String name,int level, int upid){
		this.id = id;
		this.name = name;
		this.level = level;
		this.upid = upid;
	}
	public List<AreaDTO> getDetails() {
		return details;
	}
	public void setDetails(List<AreaDTO> details) {
		this.details = details;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level; 
	}
	public int getUpid() {
		return upid;
	}
	public void setUpid(int upid) {
		this.upid = upid;
	}
	@Override
	public String toString() {
		return "AreaDTO [id=" + id + ", name=" + name + ", level=" + level + ", upid=" + upid + "]";
	}
	@Override
	protected AreaDTO clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (AreaDTO)super.clone();
	}
}