package com.az.send.sao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
/**
 * 发送http请求公共类
 * @author 御魂之龙
 *
 */
public class HttpSendSao {
	/**
	 * 使用get形式获取html
	 * @param url 访问路径
	 * @return html值
	 */
	public static String getHtml(String url){
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet httpPost = new HttpGet(url);
		Header[] headers = {new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.62 Safari/537.36"),
							new BasicHeader("Referer", "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2017/index.html")};
		httpPost.setHeaders(headers);
		StringBuilder builder = new StringBuilder();
		try {
			HttpResponse httpResponse = client.execute(httpPost);
			if(httpResponse.getStatusLine().getStatusCode() == 200){
				InputStream inputStream = httpResponse.getEntity().getContent();
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "gbk"));
				String line = null;
				while((line = bufferedReader.readLine()) != null){
					builder.append(line+"\r\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return getHtml(url);
		}
		
		return builder.toString();
	}
	public static void main(String[] args) {
		System.out.println(getHtml("http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2017/11.html"));
	}
}
