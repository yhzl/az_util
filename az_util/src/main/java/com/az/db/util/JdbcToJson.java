package com.az.db.util;

import java.util.Arrays;
import java.util.List;
/**
 * 将数据库字段转为java属性
 * @author 御魂之龙
 *
 */
public class JdbcToJson {
	static String[] tables = {"tb_paster"};//需要转换字段的表名
	static String[] filterFields = {"create_date", "create_by", "updated_date", "updated_by"};//需要过滤的字段
	public static void main(String[] args)throws Exception {
		List<?> filterList = Arrays.asList(filterFields);
		int count = 0;
		for(String tableName : tables){
			List<ColumnDTO> columnList = JdbcToExcel.queryColumn(tableName);
			for(ColumnDTO temp: columnList){
				if(!filterList.contains(temp.getColumnName())){
					System.out.println("\""+JdbcToDTO.removeUnderline(temp.getColumnName())+"\":\""+(++count)+"\",");
				}
			}
		}
		
	}
}
