package com.az.db.util;

import java.util.Arrays;
import java.util.List;

/**
 * 生成常用sql
 * 
 * @author 御魂之龙
 *
 */
public class JdbcToSql {
	static String[] tables = { "tb_paster_detail" };// 需要转换字段的表名
	static String[] filterFields = { "id", "create_date", "create_by", "updated_date", "updated_by" };// 需要过滤的字段

	public static void main(String[] args)throws Exception {
		List<?> filterList = Arrays.asList(filterFields);
		for(String tableName : tables){
			insert(tableName, filterList);
			select(tableName, filterList);
		}
	}

	/**
	 * 生成insert语句
	 * 
	 * @throws Exception
	 */
	public static void insert(String tableName, List<?> filterList) throws Exception {
		List<ColumnDTO> columnList = JdbcToExcel.queryColumn(tableName);
		System.out.println("insert into " + tableName + "(");
		StringBuilder beforeBuilder = new StringBuilder(), afterBuilder = new StringBuilder();
		for (ColumnDTO columnDTO : columnList) {
			if (!filterList.contains(columnDTO.getColumnName())) {
				beforeBuilder.append("\t" + columnDTO.getColumnName() + ",\r\n");
				afterBuilder.append("\t#{" + JdbcToDTO.removeUnderline(columnDTO.getColumnName()) + "},\r\n");
			}
		}
		System.out.println(beforeBuilder.toString().substring(0, beforeBuilder.length() - 3) + "\r\n)values(\r\n"
				+ afterBuilder.toString().substring(0, afterBuilder.length() - 3) + "\r\n)");
	}

	/**
	 * 生成select语句
	 * 
	 * @throws Exception
	 */
	public static void select(String tableName, List<?> filterList) throws Exception {
		List<ColumnDTO> columnList = JdbcToExcel.queryColumn(tableName);
		System.out.println("select");
		StringBuilder beforeBuilder = new StringBuilder();
		for (ColumnDTO columnDTO : columnList) {
			if (!filterList.contains(columnDTO.getColumnName())) {
				beforeBuilder.append("\t" + columnDTO.getColumnName() + " "
						+ JdbcToDTO.removeUnderline(columnDTO.getColumnName()) + ",\r\n");
			}
		}
		System.out.println(beforeBuilder.toString().substring(0, beforeBuilder.length() - 3) + "\r\nfrom " + tableName);
	}
}
