package com.az.grab.china.pca;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.az.send.sao.HttpSendSao;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
/**
 * 抓取国家统计局省市区数据
 * @author 御魂之龙
 *
 */
public class PcaUtil {
	private static Logger logger = Logger.getLogger(PcaUtil.class);
	public static final String chinaPcaCn = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2017/";
	public static final String chinaPcaHome = "index.html";
	public static final String pcaArr[] = {"provincetr", "citytr", "countytr"};
	public static final String tableArr[] = {"tb_province", "tb_city", "tb_county"};
	static HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();
	static{
		 t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);  
	     t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);  
	     t3.setVCharType(HanyuPinyinVCharType.WITH_V);
	}
	public static void main(String[] args) {
		for(PcaDTO temp : getPcaList()){
			System.out.println(temp);
			if(null != temp.getChildrens()){
				for(PcaDTO test : temp.getChildrens()){
					System.out.println(test);
					if(null != test.getChildrens()){
						for(PcaDTO tt : test.getChildrens()){
							System.out.println(tt);
						}
					}
				}
			}
		}
	}
	public static List<PcaDTO> getPcaList(){
        List<PcaDTO> list = new ArrayList<PcaDTO>();
        int count = 1;
		while(true){
			try {
				String allUrl = chinaPcaCn+chinaPcaHome;
				Document body = Jsoup.parse(HttpSendSao.getHtml(allUrl));
				Elements trs = body.getElementsByClass("provincetr");
				for(int i=0;i<trs.size();i++){
					Element tr = trs.get(i);
					Elements as = tr.getElementsByTag("a");
					for(int j=0;j<as.size();j++){
						Element a = as.get(j);
						PcaDTO firstPca = new PcaDTO("", (count++)+"", a.text(), PinyinHelper.toHanYuPinyinString(a.text(), t3, "", true));
						list.add(firstPca);
						firstPca.getChildrens().addAll(parseHtml(chinaPcaCn+a.attr("href"), 1, firstPca));
					} 
				}
				break;
			} catch (Exception e) {
				logger.error("访问国家统计局区域统计模块失败", e);
			}
		}
		return list;
	}
	public static List<PcaDTO> parseHtml(String url, int pcaIndex, PcaDTO parentDTO){
		List<PcaDTO> list = new ArrayList<PcaDTO>();
		try {
			Document body = Jsoup.parse(HttpSendSao.getHtml(url));
			Elements trs = body.getElementsByClass(pcaArr[pcaIndex]);
			for(int i=0;i<trs.size();i++){
				Element tr = trs.get(i);
				Elements as = tr.getElementsByTag("a");
				if(as.size() < 1){
					continue;
				}
				Element a0 = as.get(0);
				Element a = as.get(1);
				PcaDTO secondPca = null;
				if("省直辖县级行政区划".equals(a.text()) || "自治区直辖县级行政区划".equals(a.text())){
					parentDTO.getChildrens().addAll((parseHtml(chinaPcaCn+a0.attr("href"), 2, parentDTO)));
				}else{
					boolean flag = false;
					if("市辖区".equals(a.text()) || "县".equals(a.text())){
						if(list.size() == 1 && list.get(0).getName().equals(parentDTO.getName())){
							secondPca = list.get(0);
							flag = true;
						}else{
							secondPca = new PcaDTO(parentDTO.getPcaCode(), a0.text(), parentDTO.getName(), parentDTO.getPy());
						}
					}else{
						secondPca = new PcaDTO(parentDTO.getPcaCode(), a0.text(), a.text(), PinyinHelper.toHanYuPinyinString(a.text(), t3, "", true));
					}
					if(!flag){
						list.add(secondPca);
					}
					if(pcaIndex == 1){
						secondPca.getChildrens().addAll(parseHtml(chinaPcaCn+a0.attr("href"), 2, secondPca));
					}
				}
				
			}
		} catch (Exception e) {
			logger.error("链接失败", e);
		}
		return list;
	}
}
class PcaDTO{
	private String parentCode;
	private String pcaCode;
	private String name;
	private String py;
	private List<PcaDTO> childrens;
	public PcaDTO(){}
	public PcaDTO(String parentCode, String pcaCode, String name, String py){
		this.parentCode = parentCode;
		this.pcaCode = pcaCode;
		this.name = name;
		this.py = py;
		childrens = new ArrayList<PcaDTO>();
	}
	public String getParentCode() {
		return parentCode;
	}
	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}
	public String getPcaCode() {
		return pcaCode;
	}
	public void setPcaCode(String pcaCode) {
		this.pcaCode = pcaCode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPy() {
		return py;
	}
	public void setPy(String py) {
		this.py = py;
	}
	public List<PcaDTO> getChildrens() {
		return childrens;
	}
	public void setChildrens(List<PcaDTO> childrens) {
		this.childrens = childrens;
	}
	@Override
	public String toString() {
		return "PcaDTO [parentCode=" + parentCode + ", pcaCode=" + pcaCode + ", name=" + name + ", py=" + py + "]";
	}
}
