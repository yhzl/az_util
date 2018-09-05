package com.az.db.util;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
/**
 * 将mysql库中所有用户表以Excel展示
 * @author 御魂之龙
 * @version 1.0.0
 */
public class JdbcToExcel {
	private static Logger logger = LogManager.getLogger(JdbcToExcel.class);
	public static final String excelUrl = "F:/study/";//excel地址
	public static final String databaseName = "ts_forum";//库名
	public static final int heightValue = 25;//行高
	public static final String url="jdbc:mysql://192.168.31.30:3306/";
	public static final String user="zhongxz"; //用户名
	public static final String password="tushun2018";//密码
	static Connection conn = null;
	public static Connection getConn(){
		return conn;
	}
	static{
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn=DriverManager.getConnection(url+databaseName,user,password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args)throws Exception {
		long startTime = System.currentTimeMillis();
		logger.info("任务开始:");
		int count = 1;
		List<TableDTO> tableList = queryTable();
		XSSFWorkbook workbook = new XSSFWorkbook();
		setCellStyle(workbook);
		OutputStream outputStream = new FileOutputStream(excelUrl+"/"+databaseName+".xlsx");
		Sheet sheet = workbook.createSheet(databaseName);
		for(TableDTO temp : tableList){
			logger.info("当前处理第"+(count++)+"个["+temp.getTableName()+"],共"+tableList.size()+"个.");
			List<ColumnDTO> columnList = queryColumn(temp.getTableName());
			List<IndexDTO> indexList = queryIndex(temp.getTableName());
			createExcelTable(sheet, temp, columnList, indexList);
		}
		workbook.write(outputStream);
		outputStream.close();
		conn.close();
		long endTime = System.currentTimeMillis();
		logger.info("任务结束,总耗时:"+(endTime-startTime));
	}
	//创建Excel表格
	public static void createExcelTable(Sheet sheet, TableDTO tableDTO, List<ColumnDTO> columnList, List<IndexDTO> indexList){
		//设置默认行高
		sheet.setDefaultRowHeight((short)heightValue);
		sheet.setDefaultRowHeightInPoints((float)heightValue);
		
		int lastRowNum = sheet.getLastRowNum();
		int rowNum = lastRowNum==0?1:lastRowNum+3;
		Row row = null;
		Cell cell = null;
		//获取表头合并单元格对象
		getCellRange(rowNum, rowNum, 1, 5);
		
		//创建表头
		row = createRowHeight(sheet ,rowNum++);
		for(int i=1;i<6;i++){
			cell = row.createCell(i);
			cell.setCellStyle(tableHeadStyle);
		}
		cell = row.getCell(1);
		cell.setCellValue(tableDTO.getTableName()+"("+tableDTO.getTableComment()+")"+"["+tableDTO.getEngine()+"]");
		sheet.addMergedRegion(cra);
		
		//创建菜单栏
		row = createRowHeight(sheet ,rowNum++);
		for(int i = 0;i<menuArray.length;i++){
			cell = row.createCell(i+1);
			cell.setCellValue(menuArray[i]);
			cell.setCellStyle(descTyle);
		}
		//创建列
		for(ColumnDTO column : columnList){
			row = createRowHeight(sheet ,rowNum++);
			
			cell = row.createCell(1);
			cell.setCellValue(column.getColumnName());
			cell.setCellStyle(defualStyle);
			
			cell = row.createCell(2);
			cell.setCellValue(column.getColumnType().toUpperCase());
			cell.setCellStyle(defualStyle);
			
			cell = row.createCell(3);
			cell.setCellValue(column.getIsNullt().toUpperCase());
			cell.setCellStyle(defualStyle);
			
			cell = row.createCell(4);
			cell.setCellValue(column.getColumnDesc());
			cell.setCellStyle(chStyle);
			
			cell = row.createCell(5);
			cell.setCellValue("");
			cell.setCellStyle(defualStyle);
		}
		
		//获取索引合并单元格对象
		getCellRange(rowNum, rowNum, 2, 4);
		//创建索引菜单栏
		row = createRowHeight(sheet ,rowNum++);
		for(int i = 0;i<indexArray.length;i++){
			cell = row.createCell(i+1);
			cell.setCellValue(indexArray[i]);
			cell.setCellStyle(descTyle);
		}
		sheet.addMergedRegion(cra);
		//创建索引列
		for(IndexDTO indexDTO : indexList){
			//获取索引合并单元格对象
			getCellRange(rowNum, rowNum, 2, 4);
			
			row = createRowHeight(sheet ,rowNum++);
			
			cell = row.createCell(1);
			cell.setCellValue(indexDTO.getIndexName().toUpperCase());
			cell.setCellStyle(defualStyle);
			
			cell = row.createCell(2);
			cell.setCellValue(indexDTO.getIndexType().toUpperCase());
			cell.setCellStyle(defualStyle);
			
			cell = row.createCell(3);
			cell.setCellValue("");
			cell.setCellStyle(defualStyle);
			
			cell = row.createCell(4);
			cell.setCellValue("");
			cell.setCellStyle(defualStyle);
			
			cell = row.createCell(5);
			cell.setCellValue(indexDTO.getIndexColumn());
			cell.setCellStyle(defualStyle);
			
			sheet.addMergedRegion(cra);
		}
		
	}
	//创建带行高的row对象
	public static Row createRowHeight(Sheet sheet, int rowNum){
		Row row = sheet.createRow(rowNum);
		row.setHeight((short)heightValue);
		row.setHeightInPoints((short)heightValue);
		return row;
	}
	//默认菜单栏
	static String menuArray [] ={"字段名","字段类型","是否可空","字段描述","备注"};
	//默认索引栏
	static String indexArray [] ={"索引名称","索引类型","","","索引列"};
	//设置单元格样式
	static XSSFCellStyle tableHeadStyle = null;
	static XSSFCellStyle descTyle = null;
	static XSSFCellStyle defualStyle = null;
	static XSSFCellStyle chStyle = null;
	public static void setCellStyle(XSSFWorkbook workbook){
		tableHeadStyle = workbook.createCellStyle();
		
		//设置自定义颜色(背景色)
		XSSFColor color = new XSSFColor();
		color.setRgb(new byte[]{(byte) 250,(byte)191,(byte)143});
		tableHeadStyle.setFillForegroundColor(color);
		tableHeadStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		
		//设置字体位置
		tableHeadStyle.setAlignment(XSSFCellStyle.ALIGN_LEFT); // 居左
		tableHeadStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);//垂直
		
		//设置边框
		tableHeadStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN); //下边框    
		tableHeadStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);//左边框    
		tableHeadStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);//上边框    
		tableHeadStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);//右边框  
		
		//设置字体
		XSSFFont font = workbook.createFont();
		font.setFontName("微软雅黑");
		font.setFontHeightInPoints((short) 11);//字号大小
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);//加粗      
		tableHeadStyle.setFont(font);
		
		//自动换行
		tableHeadStyle.setWrapText(true);
		
		//设置菜单样式
		descTyle = workbook.createCellStyle();
		
		//设置自定义颜色(背景色)
		color = new XSSFColor();
		color.setRgb(new byte[]{(byte) 50,(byte)192,(byte)204});
		descTyle.setFillForegroundColor(color);
		descTyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		
		//设置字体位置
		descTyle.setAlignment(XSSFCellStyle.ALIGN_CENTER); // 水平居中   
		descTyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);//垂直
		
		//设置边框
		descTyle.setBorderBottom(XSSFCellStyle.BORDER_THIN); //下边框    
		descTyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);//左边框    
		descTyle.setBorderTop(XSSFCellStyle.BORDER_THIN);//上边框    
		descTyle.setBorderRight(XSSFCellStyle.BORDER_THIN);//右边框  
		
		//设置字体
		font = workbook.createFont();
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 11);//字号大小
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);//加粗      
		descTyle.setFont(font);
		
		//自动换行
		descTyle.setWrapText(true);
		
		//设置默认样式
		defualStyle = workbook.createCellStyle();
		
		//设置自定义颜色(背景色)
		/*color = new XSSFColor();
		color.setRgb(new byte[]{(byte) 50,(byte)192,(byte)204});
		defualStyle.setFillForegroundColor(color);
		defualStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);*/
		
		//设置字体位置
		defualStyle.setAlignment(XSSFCellStyle.ALIGN_RIGHT); // 水平居右
		//defualStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);//垂直
		
		//设置边框
		defualStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN); //下边框    
		defualStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);//左边框    
		defualStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);//上边框    
		defualStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);//右边框  
		
		//设置字体
		font = workbook.createFont();
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 11);//字号大小
		//font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);//加粗      
		defualStyle.setFont(font);
		
		//自动换行 
		defualStyle.setWrapText(true);
		
		//设置中文描述样式
		chStyle = workbook.createCellStyle();
		
		//设置自定义颜色(背景色)
		/*color = new XSSFColor();
		color.setRgb(new byte[]{(byte) 50,(byte)192,(byte)204});
		defualStyle.setFillForegroundColor(color);
		defualStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);*/
		
		//设置字体位置
		chStyle.setAlignment(XSSFCellStyle.ALIGN_RIGHT); // 水平居左
		//defualStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);//垂直
		
		//设置边框
		chStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN); //下边框    
		chStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);//左边框    
		chStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);//上边框    
		chStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);//右边框  
		
		//设置字体
		font = workbook.createFont();
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 11);//字号大小
		color = new XSSFColor();
		color.setRgb(new byte[]{(byte) 15,(byte)36,(byte)62});
		font.setColor(color);
		//font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);//加粗      
		chStyle.setFont(font);
		
		//自动换行
		chStyle.setWrapText(true);
		
	}
	static  CellRangeAddress cra = null;
    // 获取合并单元格对象
	public static void getCellRange(int firsttRow, int lastRow, int firstCol, int lastCol){
        cra=new CellRangeAddress(firsttRow, lastRow, firstCol, lastCol);     
	}
	//查询库中所有表
	public static List<TableDTO> queryTable()throws Exception{
        String sql = "SELECT t.`TABLE_NAME` tableName,t.`TABLE_COMMENT` tableComment,t.`ENGINE` engine FROM information_schema.tables t WHERE t.`TABLE_SCHEMA`=?";
		
		PreparedStatement statement = conn.prepareStatement(sql);
		
		statement.setString(1, databaseName);
		
		ResultSet resultSet = statement.executeQuery();
		
		List<TableDTO> tableList = new ArrayList<TableDTO>();
		while(resultSet.next()){
			String tableName = resultSet.getString("tableName");
			String tableComment = resultSet.getString("tableComment");
			String engine = resultSet.getString("engine");
			tableList.add(new TableDTO(tableName, tableComment, engine));
		}
		return tableList;
	}
	//查询表中所有字段
	public static List<ColumnDTO> queryColumn(String tableName)throws Exception{
        String sql = "SELECT t.column_name columnName,t.column_type columnType,t.column_comment columnDesc,IF(t.is_nullable='NO','NOT NULL','')  isNullt FROM information_schema.columns t WHERE t.table_schema = ?  AND t.table_name = ? ORDER BY t.`ORDINAL_POSITION`;";
		
		PreparedStatement statement = conn.prepareStatement(sql);
		
		statement.setString(1, databaseName);
		
		statement.setString(2, tableName);
		
		ResultSet resultSet = statement.executeQuery();
		
		List<ColumnDTO> columnList = new ArrayList<ColumnDTO>();
		while(resultSet.next()){
			String columnName = resultSet.getString("columnName");
			String columnDesc = resultSet.getString("columnDesc");
			String columnType = resultSet.getString("columnType");
			String isNullt = resultSet.getString("isNullt");
			columnList.add(new ColumnDTO(columnName, columnDesc, columnType ,isNullt));
		}
		return columnList;
	}
	//查询表中所有索引
	public static List<IndexDTO> queryIndex(String tableName)throws Exception{
	        String sql = "SHOW INDEX FROM "+tableName+";";
			
			PreparedStatement statement = conn.prepareStatement(sql);
			
			
			ResultSet resultSet = statement.executeQuery();
			
			List<IndexDTO> indexList = new ArrayList<IndexDTO>();
			while(resultSet.next()){
				String indexName = resultSet.getString("Key_name");
				String indexType = resultSet.getString("Index_type");
				String indexColumn = resultSet.getString("Column_name");
				indexList.add(new IndexDTO(indexName, indexType, indexColumn));
			}
			return indexList;
		}
}
class TableDTO{
	public TableDTO(String tableName, String tableComment, String engine){
		this.tableName = tableName;
		this.tableComment = tableComment;
		this.engine = engine;
	}
	private String tableName;//表名
	private String tableComment;//表描述
	private String engine;//表类型
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getTableComment() {
		return tableComment;
	}
	public void setTableComment(String tableComment) {
		this.tableComment = tableComment;
	}
	public String getEngine() {
		return engine;
	}
	public void setEngine(String engine) {
		this.engine = engine;
	}
	@Override
	public String toString() {
		return "TableDTO [tableName=" + tableName + ", tableComment=" + tableComment + ", engine=" + engine + "]";
	}
	
}
class ColumnDTO{
	public ColumnDTO(String columnName ,String columnDesc ,String columnType ,String isNullt){
		this.columnName = columnName;
		this.columnDesc = columnDesc;
		this.columnType = columnType;
		this.isNullt = isNullt;
	}
	private String columnName;//字段名
	private String columnDesc;//字段描述
	private String columnType;//字段类型
	private String isNullt;//是否可空
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnDesc() {
		return columnDesc;
	}
	public void setColumnDesc(String columnDesc) {
		this.columnDesc = columnDesc;
	}
	public String getColumnType() {
		return columnType;
	}
	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}
	public String getIsNullt() {
		return isNullt;
	}
	public void setIsNullt(String isNullt) {
		this.isNullt = isNullt;
	}
	@Override
	public String toString() {
		return "ColumnDTO [columnName=" + columnName + ", columnDesc=" + columnDesc + ", columnType=" + columnType
				+ ", isNullt=" + isNullt + "]";
	}
	
}
class IndexDTO{
	public IndexDTO(String indexName ,String indexType ,String indexColumn){
		this.indexName = indexName;
		this.indexType = indexType;
		this.indexColumn = indexColumn;
	}
	private String indexName;//索引名
	private String indexType;//索引类型
	private String indexColumn;//索引列
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public String getIndexType() {
		return indexType;
	}
	public void setIndexType(String indexType) {
		this.indexType = indexType;
	}
	public String getIndexColumn() {
		return indexColumn;
	}
	public void setIndexColumn(String indexColumn) {
		this.indexColumn = indexColumn;
	}
	@Override
	public String toString() {
		return "IndexDTO [indexName=" + indexName + ", indexType=" + indexType + ", indexColumn=" + indexColumn + "]";
	}
	
}
