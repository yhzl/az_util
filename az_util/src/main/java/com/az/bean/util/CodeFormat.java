package com.az.bean.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.az.bean.dto.ValidDTO;

/**
 * 根据jsr303标准获取基本校验错误码详情列表
 * 
 * @author 御魂之龙
 *
 */
public class CodeFormat {
	//优点:无需引入相关类包
	//缺点:groups需放在最后面，不能出现换行
	private static Logger logger = Logger.getLogger(CodeFormat.class);
	private static final String dirUrl = "C:/Users/DELL/git/ts-taxi/ts-taxi-forum/ts-taxi-forum-comm/src/main/java/com/ts/taxi/forum/back/dto/";
	private static final boolean isField = false,// 是否显示字段名
			isParent = false;// 是否显示父字段名
	private static String[] screenArr = { "regexp\\=\".*?\"", "groups\\=\\{.*?\\}" };
	private static List<String> exitsList = new ArrayList<String>();
	//分组过滤
	private static List<String> groupsList = Arrays.asList(new String[]{"PasterVLD.PasterList.class"});
	public static void main(String[] args) throws Exception {
		List<String> list = getStatusCode();
		Collections.sort(list);
		for(String temp : list){
			if(null != temp && !temp.trim().equals("")){
				System.out.println(temp);
			}
		}
	}

	public static List<String> getStatusCode() throws Exception {
		File dir = new File(dirUrl);
		List<String> statusList = new ArrayList<String>();
		if (!dir.isDirectory()) {
			throw new Exception("主路径不是目录");
		} else {
			File[] files = dir.getAbsoluteFile().listFiles();
			for (File temp : files) {
				statusList.addAll(detail(temp));
			}
		}
		return statusList;
	}

	public static List<String> detail(File dtoFile, String... parent) {
		List<String> topList = new ArrayList<String>(), statusList = new ArrayList<String>();
		if (!exitsList.contains(dtoFile.getName()) && dtoFile.isFile()) {
			String parentName = "";
			if(parent.length > 0){
				parentName = parent[0];
			}
			InputStream inputStream = null;
			InputStreamReader inputStreamReader = null;
			BufferedReader bufferedReader = null;
			try {
				inputStream = new FileInputStream(dtoFile);
				inputStreamReader = new InputStreamReader(inputStream);
				bufferedReader = new BufferedReader(inputStreamReader);
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					line = line.trim();
					if (line.startsWith("@")) {
						topList.add(line);
					}
					if (line.startsWith("private") && topList.size() > 0) {
						String fieldName = line.substring(0, line.indexOf(";")).split(" ")[2];
						String notes = line.substring(line.indexOf(";")+1).trim().substring(2);
						int kIndex = notes.indexOf("(");
						notes = notes.substring(0, kIndex == -1 ? notes.length() : kIndex);
						for (String anno : topList) {
							if("@Valid".equals(anno)){
								continue;
							}
							statusList.add(cueContent(new ValidDTO((isParent?parentName+fieldName:fieldName), notes, anno)));
						}
						if(topList.contains("@Valid")){
							statusList.addAll(detail(new File(dirUrl+File.separator+line.substring(0, line.indexOf(";")).split(" ")[1]+".java"), fieldName));
						}
						topList.clear();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					bufferedReader.close();
					inputStreamReader.close();
					inputStream.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		exitsList.add(dtoFile.getName());
		return statusList;
	}

	/**
	 * 提取主键名
	 * 
	 * @param list
	 *            带注解内容的集合
	 * @return 注解名集合
	 */
	public static List<String> cvList(List<String> list) {
		List<String> rList = new ArrayList<String>();
		for (String temp : list) {
			String rStr = temp;
			if (rStr.contains("(")) {
				rStr = temp.substring(0, temp.indexOf("("));
			}
			rList.add(rStr);
		}
		return rList;
	}

	/**
	 * 使用正则拆分属性项
	 * 
	 * @param attrList
	 *            属性集合
	 * @param end
	 *            属性字符串
	 * @return 被抽取后的属性字符串
	 */
	public static String screenAttr(List<String> attrList, String end) {
		Matcher matcher = null;
		String converStr = end;
		for (String temp : screenArr) {
			matcher = Pattern.compile(temp).matcher(end);
			if (matcher.find()) {
				attrList.add(matcher.group());
				converStr = converStr.replaceAll(temp, "");
			}
		}
		return converStr;
	}

	/**
	 * 获取符号#
	 * 
	 * @param length
	 *            字符串类型长度
	 * @return 生成的符号#
	 */
	public static String getShap(String length) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < Integer.valueOf(length); i++) {
			builder.append("#");
		}
		return builder.toString();
	}

	/**
	 * 转换区间值描述
	 * 
	 * @param map
	 *            区间值map
	 * @return 错误码描述
	 * @throws Exception
	 *             大小不存一异常
	 */
	public static String converSectionValue(Map<String, Object> map) throws Exception {
		String resStr = "";
		Object max = map.get("max");
		Object min = map.get("min");
		if (min == null) {
			if (max == null) {
				throw new Exception("最大值与最小值都为空");
			} else {
				resStr = "小于" + max;
			}
		} else {
			if (max == null) {
				resStr = "大于" + min;
			} else {
				resStr = "在[" + min + "-" + max + "]之间";
			}
		}
		return resStr;
	}

	/**
	 * 生成错误码描述
	 * 
	 * @param validDTO
	 *            字段信息
	 * @param groupNames
	 *            组名过滤
	 * @return 错误码及描述
	 */
	@SuppressWarnings("unchecked")
	public static String cueContent(ValidDTO validDTO, String... groupNames) {
		String annoStr = validDTO.getAnno();
		String cueStr = "";
		String head = annoStr.substring(0, annoStr.indexOf("("));
		String end = annoStr.substring(annoStr.indexOf("(") + 1, annoStr.length() - 1);
		List<String> attrList = new ArrayList<String>();
		end = screenAttr(attrList, end);
		attrList.addAll(Arrays.asList(end.trim().split(",")));
		Map<String, Object> map = new HashMap<String, Object>();
		for (String temp : attrList) {
			temp = temp.trim();
			String attrName = temp.substring(0, temp.indexOf("=")).trim();
			Object obj = null;
			switch (attrName) {
			case "groups":
				String group = temp.substring(temp.indexOf("=") + 1).trim();
				group = group.substring(1, group.length() - 1);
				String[] groups = group.split(",");
				List<String> gList = new ArrayList<String>();
				for (String inter : groups) {
					gList.add(inter.trim());
				}
				obj = gList;
				break;
			default:
				obj = new String(temp.substring(temp.indexOf("=") + 1).trim()).replace("\"", "");
				break;
			}
			map.put(attrName, obj);
		}
		boolean flag = false;
		if(null != map.get("groups")){
			for(String temp : (List<String>)map.get("groups")){
				if(groupsList.contains(temp)){
					flag = true;
				}
			}
		}
		if(!flag && groupsList.size() > 0){
			return cueStr;
		}
		switch (head) {
		case "@NotEmpty":
			cueStr = "不能为空或空串";
			break;
		case "@Range":
			try {
				cueStr = "的值需" + converSectionValue(map);
			} catch (Exception e) {
				throw new Error(validDTO.getFieldName() + "出现min与max都没有的错误");
			}
			break;
		case "@Null":
			cueStr = "必须为空";
			break;
		case "@NotNull":
			cueStr = "不能为空";
			break;
		case "@AssertTrue":
			cueStr = "必须为true";
			break;
		case "@AssertFalse":
			cueStr = "必须为false";
			break;
		case "@Min":
			cueStr = "必须为为数字且要大于" + map.get("value");
			break;
		case "@Max":
			cueStr = "必须为为数字且要小于" + map.get("value");
			break;
		case "@DecimalMin":
			cueStr = "必须为为数字且要大于" + map.get("value");
			break;
		case "@DecimalMax":
			cueStr = "必须为为数字且要小于" + map.get("value");
			break;
		case "@Size":
			try {
				cueStr = "的长度" + converSectionValue(map);
			} catch (Exception e) {
				throw new Error(validDTO.getFieldName() + "出现min与max都没有的错误");
			}
			break;
		case "@Digits":
			cueStr = "的值需为" + getShap(map.get("integer").toString()) + "." + getShap(map.get("fraction").toString())
					+ "格式";
			break;
		case "@Past":
			cueStr = "必须为一个过去的日期";
			break;
		case "@Future":
			cueStr = "必须为一个将来的日期";
			break;
		case "@Pattern":
			cueStr = "的格式不正确";
			break;
		case "@Email":
			cueStr = "必须为邮箱格式";
			break;
		case "@Length":
			try {
				cueStr = "的长度需" + converSectionValue(map);
			} catch (Exception e) {
				throw new Error(validDTO.getFieldName() + "出现min与max都没有的错误");
			}
			break;
		default:
			logger.info("未知注释");
			break;
		}
		return map.get("message") + "=" + validDTO.getFieldNote() + (isField ? "(" + validDTO.getFieldName() + ")" : "")
				+ cueStr;
	}
}
