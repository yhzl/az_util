package com.az.db.util;

import java.util.Arrays;
import java.util.List;
/**
 * 将数据库字段转为java属性
 * @author 御魂之龙
 *
 */
public class JdbcToDTO {
	static String[] tables = {"tb"};//需要转换字段的表名
	static String[] filterFields = {"create_date", "create_by", "updated_date", "updated_by"};//需要过滤的字段
	public static void main(String[] args)throws Exception {
		List<?> filterList = Arrays.asList(filterFields);
		for(String tableName : tables){
			List<ColumnDTO> columnList = JdbcToExcel.queryColumn(tableName);
			for(ColumnDTO temp: columnList){
				if(!filterList.contains(temp.getColumnName())){
					System.out.println("private "+(temp.getColumnType().contains("int")?"Integer":"String")+" "+removeUnderline(temp.getColumnName())+";//"+temp.getColumnDesc());
				}
			}
		}
		
	}
	/**
	 * 去下划线使用驼峰规则
	 * @param fieldName 字段名
	 * @return
	 */
	public static String removeUnderline(String fieldName){
		while(true){
			if(!fieldName.contains("_")){
				break;
			}
			int index = fieldName.indexOf("_");
			if(index == fieldName.length() - 1){
				break;
			}
				fieldName = fieldName.substring(0, index)+firstToUpper(fieldName.substring(index+1));
		}
		return fieldName;
	}
	/**
	 * 使字段首字母大写
	 * @param name 字段名
	 * @return
	 */
	public static String firstToUpper(String name){
		return name.substring(0, 1).toUpperCase()+name.substring(1);
	}
	
}
