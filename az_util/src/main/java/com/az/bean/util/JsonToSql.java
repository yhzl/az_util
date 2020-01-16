package com.az.bean.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 将postman json转为 权限sql
 *
 * @author zhongxianzhe
 * @date 2019/4/2 9:29
 **/
public class JsonToSql {
    public static String jsonPath = "G:\\sql\\接口备份\\渠道后台.postman_collection.json";
    public static String sql = "insert into t_permission(id,per_name,per_url,parent_id,orders)values(%s,'%s','%s',%s,%s);";

    public static void main(String[] args) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream((jsonPath))));
        StringBuffer stringBuffer = new StringBuffer();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuffer.append(line);
        }
        JSONObject jsonObj = JSONObject.parseObject(stringBuffer.toString());
        JSONArray items = jsonObj.getJSONArray("item");
        Long oneId, twoId = 0L, threeId;
        String oneName, twoName, url;
        for (int i = 0; i < items.size(); i++) {
            oneId = SnowFlake.getId();
            oneName = items.getJSONObject(i).getString("name");
            System.out.println(getSql(oneId, oneName, "/", 0L, (i + 1)));
            JSONArray twoItem = items.getJSONObject(i).getJSONArray("item");
            for (int j = 0; j < twoItem.size(); j++) {
                twoName = twoItem.getJSONObject(j).getString("name");
                if (j == 0) {
                    twoId = SnowFlake.getId();
                    System.out.println("\t"+getSql(twoId, twoName, "/", oneId, 1));
                }
                threeId = SnowFlake.getId();
                url = twoItem.getJSONObject(j).getJSONObject("request").getJSONObject("url").getString("raw");
                url = url.replace("{{base_url}}", "/");
                System.out.println("\t\t"+getSql(threeId, twoName, url, twoId, (j + 1)));
            }

        }
    }

    public static String getSql(Long id, String name, String url, Long parentId, Integer orders) {
        return String.format(sql, id, name, url, parentId, orders);
    }
}
