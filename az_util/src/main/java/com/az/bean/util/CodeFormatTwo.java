package com.az.bean.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

import com.az.bean.dto.ValidDTO;

public class CodeFormatTwo {
	private static Logger logger = Logger.getLogger(CodeFormat.class);
	private static final String dirUrl = "com.rl.op.user.base.dto";
	private static final boolean isField = false, // 是否显示字段名
			isParent = false;// 是否显示父字段名
	// 分组过滤
	private static List<Class<?>> groupsList = Arrays.asList(new Class<?>[] {});
	// 注解过滤
	private static List<String> annoFilterList = Arrays.asList(new String[] { "@Valid" });
	// 字段过滤
	private static List<String> fieldList = Arrays.asList(new String[] { "serialVersionUID", "pageMsg" });

	private static List<Class<? extends String>> classList = new ArrayList<Class<? extends String>>();

	private static List<String> fileList = new ArrayList<String>();

	private static ClassLoader classLoader = CodeFormatTwo.class.getClassLoader();// 默认使用的类加载器

	public static void main(String[] args) throws Exception {
		findClassLocal(dirUrl);
		Map<String, Map<String, String>> noteMap = new HashMap<String, Map<String, String>>();
		for (String temp : fileList) {
			String path = temp.replaceAll("target\\\\classes", "src\\\\main\\\\java").replaceAll(".class", ".java");
			File file = new File(path);
			String className = file.getName().split("\\.")[0];
			InputStream inputStream = null;
			InputStreamReader inputStreamReader = null;
			BufferedReader bufferedReader = null;
			try {
				inputStream = new FileInputStream(file);
				inputStreamReader = new InputStreamReader(inputStream);
				bufferedReader = new BufferedReader(inputStreamReader);
				String line = null;
				Map<String, String> fieldMap = new HashMap<String, String>();
				while ((line = bufferedReader.readLine()) != null) {
					line = line.trim();
					if (line.startsWith("private")) {
						boolean flag = false;
						for (String fieldName : fieldList) {
							if (line.contains(fieldName)) {
								flag = true;
								break;
							}
						}
						if (flag) {
							continue;
						}
						int index = line.indexOf(";");
						String[] arr = line.substring(0, index).split(" ");
						String key = arr[2];
						String value = line.substring(index + 1).trim().replace("//", "").trim();
						if (value.contains("(")) {
							value = value.substring(0, value.indexOf("("));
						}
						fieldMap.put(key, value);
					}
				}
				noteMap.put(className, fieldMap);
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
		List<CodeMsg> codeMsgList = new ArrayList<CodeMsg>();
		for (Class<?> temp : classList) {
			Field[] fields = temp.getDeclaredFields();
			for (Field field : fields) {
				Annotation[] annotations = field.getAnnotations();
				for (Annotation annotation : annotations) {
					CodeMsg codeMsg = new CodeMsg();
					codeMsg.setClsName(temp.getSimpleName());
					codeMsg.setName(field.getName());
					codeMsg.setAnno(getAnnoOpen(annotation.annotationType().getName()));
					if (annoFilterList.contains(codeMsg.getAnno())) {
						continue;
					}
					codeMsg.setCode(invokeAnno(annotation, "message").toString());
					Object obj = invokeAnno(annotation, "groups");
					if (null != obj) {
						codeMsg.setGroups((Class<?>[]) obj);
					}
					obj = invokeAnno(annotation, "max");
					if (null != obj) {
						codeMsg.setMax(Integer.valueOf(obj.toString()));
					}
					obj = invokeAnno(annotation, "min");
					if (null != obj) {
						codeMsg.setMin(Integer.valueOf(obj.toString()));
					}
					obj = invokeAnno(annotation, "regexp");
					if (null != obj) {
						codeMsg.setRegexp(obj.toString());
					}
					obj = invokeAnno(annotation, "value");
					if (null != obj) {
						codeMsg.setValue(obj.toString());
					}
					obj = invokeAnno(annotation, "integer");
					if (null != obj) {
						codeMsg.setInteger(obj.toString());
					}
					obj = invokeAnno(annotation, "fraction");
					if (null != obj) {
						codeMsg.setFraction(obj.toString());
					}
					codeMsgList.add(codeMsg);
				}
			}
		}
		for (CodeMsg codeMsg : codeMsgList) {
			codeMsg.setNote(noteMap.get(codeMsg.getClsName()).get(codeMsg.getName()));
			codeMsg.setDesc(settingDesc(codeMsg));
			//过滤条件等都在这写
			System.out.println(codeMsg.getCode()+"="+codeMsg.getNote()+codeMsg.getDesc());
		}
	}
	public static String settingDesc(CodeMsg codeMsg) {
		String cueStr = "";
		switch (codeMsg.getAnno()) {
		case "@NotEmpty":
			cueStr = "不能为空或空串";
			break;
		case "@Range":
			try {
				cueStr = "的值需" + converSectionValue(codeMsg);
			} catch (Exception e) {
				throw new Error(codeMsg.getName() + "出现min与max都没有的错误");
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
			cueStr = "必须为为数字且要大于" + codeMsg.getValue();
			break;
		case "@Max":
			cueStr = "必须为为数字且要小于" + codeMsg.getValue();
			break;
		case "@DecimalMin":
			cueStr = "必须为为数字且要大于" + codeMsg.getValue();
			break;
		case "@DecimalMax":
			cueStr = "必须为为数字且要小于" + codeMsg.getValue();
			break;
		case "@Size":
			try {
				cueStr = "的长度" + converSectionValue(codeMsg);
			} catch (Exception e) {
				throw new Error(codeMsg.getName() + "出现min与max都没有的错误");
			}
			break;
		case "@Digits":
			cueStr = "的值需为" + CodeFormat.getShap(codeMsg.getInteger()) + "." + CodeFormat.getShap(codeMsg.getFraction().toString())
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
				cueStr = "的长度需" + converSectionValue(codeMsg);
			} catch (Exception e) {
				throw new Error(codeMsg.getName() + "出现min与max都没有的错误");
			}
			break;
		default:
			logger.info("未知注释");
			break;
		}
		return cueStr;
	}
	public static String converSectionValue(CodeMsg codeMsg) throws Exception {
		String resStr = "";
		Object max = codeMsg.getMax();
		Object min = codeMsg.getMin();
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

	public static Object invokeAnno(Annotation annotation, String methodName) {
		Class<?> annoClass = annotation.getClass();
		Object obj = null;
		try {
			obj = annoClass.getMethod(methodName).invoke(annotation);
		} catch (Exception e) {

		}
		return obj;
	}

	public static String getAnnoOpen(String className) {
		String[] arr = className.split("\\.");
		return "@" + arr[arr.length - 1];
	}

	/**
	 * 本地查找
	 * 
	 * @param packName
	 */
	private static void findClassLocal(final String packName) {
		URI url = null;
		try {
			url = classLoader.getResource(packName.replace(".", "/")).toURI();
		} catch (URISyntaxException e1) {
			throw new RuntimeException("未找到策略资源");
		}
		File file = new File(url);
		file.listFiles(new FileFilter() {

			@SuppressWarnings("unchecked")
			public boolean accept(File chiFile) {
				if (chiFile.isDirectory()) {
					findClassLocal(packName + "." + chiFile.getName());
				}
				if (chiFile.getName().endsWith(".class")) {
					Class<?> clazz = null;
					try {
						clazz = classLoader.loadClass(packName + "." + chiFile.getName().replace(".class", ""));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					classList.add((Class<? extends String>) clazz);
					fileList.add(chiFile.getAbsolutePath());
					return true;
				}
				return false;
			}
		});

	}

	/**
	 * jar包查找
	 * 
	 * @param packName
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private void findClassJar(final String packName) {
		String pathName = packName.replace(".", "/");
		JarFile jarFile = null;
		try {
			URL url = classLoader.getResource(pathName);
			JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
			jarFile = jarURLConnection.getJarFile();
		} catch (IOException e) {
			throw new RuntimeException("未找到策略资源");
		}

		Enumeration<JarEntry> jarEntries = jarFile.entries();
		while (jarEntries.hasMoreElements()) {
			JarEntry jarEntry = jarEntries.nextElement();
			String jarEntryName = jarEntry.getName();

			if (jarEntryName.contains(pathName) && !jarEntryName.equals(pathName + "/")) {
				// 递归遍历子目录
				if (jarEntry.isDirectory()) {
					String clazzName = jarEntry.getName().replace("/", ".");
					int endIndex = clazzName.lastIndexOf(".");
					String prefix = null;
					if (endIndex > 0) {
						prefix = clazzName.substring(0, endIndex);
					}
					findClassJar(prefix);
				}
				if (jarEntry.getName().endsWith(".class")) {
					Class<?> clazz = null;
					try {
						clazz = classLoader.loadClass(jarEntry.getName().replace("/", ".").replace(".class", ""));
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					classList.add((Class<? extends String>) clazz);
				}
			}
		}
	}
}

class CodeMsg implements Serializable {

	private static final long serialVersionUID = -6928329857947067899L;
	private String anno;
	private String note;
	private String code;
	private String name;
	private String desc;
	private Integer max;
	private Integer min;
	private String regexp;
	private String clsName;
	private String value;
	private String integer;
	private String fraction;
	private Class<?>[] groups;
	public CodeMsg() {
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public Integer getMin() {
		return min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public String getRegexp() {
		return regexp;
	}

	public void setRegexp(String regexp) {
		this.regexp = regexp;
	}

	public Class<?>[] getGroups() {
		return groups;
	}

	public void setGroups(Class<?>[] groups) {
		this.groups = groups;
	}

	public String getAnno() {
		return anno;
	}

	public void setAnno(String anno) {
		this.anno = anno;
	}

	public String getClsName() {
		return clsName;
	}

	public void setClsName(String clsName) {
		this.clsName = clsName;
	}
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getInteger() {
		return integer;
	}

	public void setInteger(String integer) {
		this.integer = integer;
	}

	public String getFraction() {
		return fraction;
	}

	public void setFraction(String fraction) {
		this.fraction = fraction;
	}
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Override
	public String toString() {
		return "CodeMsg [anno=" + anno + ", code=" + code + ", name=" + name + ", desc=" + desc + ", max=" + max
				+ ", min=" + min + ", regexp=" + regexp + ", groups=" + Arrays.toString(groups) + "]";
	}

	public String toCodeString() {
		return code + "=" + desc;
	}
}
