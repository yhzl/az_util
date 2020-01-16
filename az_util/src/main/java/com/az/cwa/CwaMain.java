package com.az.cwa;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 考勤统计
 *
 * @author zhongxianzhe
 * @date 2020/1/15 16:59
 **/
public class CwaMain {

    private static String db_path = "G://cwa//AGHO185060489_attlog.dat";
    private static Map<String, String> personMap = new HashMap<>();
    private static Map<String, Integer> personIndex = new HashMap<>();
    private static int start_date = 93000;
    private static int ts_date = 100000;
    private static int end_date = 183000;

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(db_path)));
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        String line = null;
        String[] arr = null;
        List<CwaDTO> list = new ArrayList<>();
        List<CwaDTO> all = new ArrayList<>();
        String currentDay = "";
        while ((line = reader.readLine()) != null) {
            arr = line.split("\t");
            list.add(new CwaDTO(arr[0].trim(), arr[1]));
        }
        reader.close();
        List<CwaDTO> soloDay = null;
        for (CwaDTO temp : list) {
            String day = temp.getDate().split(" ")[0];
            if (!day.equals(currentDay)) {
                if (null != soloDay) {
                    for (String id : personMap.keySet()) {
                        List<CwaDTO> soloPerson = new ArrayList<>();
                        for (CwaDTO solop : soloDay) {
                            if (solop.getId().equals(id)) {
                                soloPerson.add(solop);
                            }
                        }
                        CwaDTO allDTO = new CwaDTO();
                        allDTO.setId(id);
                        allDTO.setName(personMap.get(id));
                        boolean flag = false;
                        for (CwaDTO vl : all) {
                            if (vl.getId().equals(allDTO.getId())) {
                                allDTO = vl;
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            all.add(allDTO);
                        }
                        calendar.setTime(dateFormat.parse(soloDay.get(0).getDate()));
                        int week = calendar.get(Calendar.DAY_OF_WEEK);
                        if (week == 1) {
                            continue;
                        }
                        if (soloPerson.size() > 0) {
                            if (1 == soloPerson.size()) {
                                allDTO.setLdk(allDTO.getLdk() + 1);
                                String fg = convertTime(soloPerson.get(0).getDate()) > 140000 ? "上午" : "下午";
                                allDTO.setNote(allDTO.getNote() + currentDay + fg + "漏打卡\r\n");
                            } else {
                                if (convertTime(soloPerson.get(0).getDate()) > (week == 7 ? ts_date : start_date)) {
                                    allDTO.setCd(allDTO.getCd() + 1);
                                    allDTO.setNote(allDTO.getNote() + currentDay + "迟到\r\n");
                                }
                                if (convertTime(soloPerson.get(soloPerson.size() - 1).getDate()) < end_date) {
                                    allDTO.setZt(allDTO.getZt() + 1);
                                    allDTO.setNote(allDTO.getNote() + currentDay + "早退\r\n");
                                }
                            }
                            allDTO.setCq(allDTO.getCq() + 1);
                        } else {
                            allDTO.setQj(allDTO.getQj() + 1);
                            allDTO.setNote(allDTO.getNote() + currentDay + "请假\r\n");
                        }

                    }

                }
                currentDay = day;
                soloDay = new ArrayList<>();
            }
            soloDay.add(temp);
        }
        list.sort(new Comparator<CwaDTO>() {
            @Override
            public int compare(CwaDTO o1, CwaDTO o2) {
                Integer s1 = Integer.valueOf(o1.getId());
                Integer s2 = Integer.valueOf(o2.getId());
                return s1.compareTo(s2);
            }
        });
        // 写入excel
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream("G:\\cwa\\tmpl.xlsx"));
        Sheet sheet = workbook.getSheetAt(1);
        Row row = null;
        Cell cell = null;
        int index = 2;
        all.sort(new Comparator<CwaDTO>() {
            @Override
            public int compare(CwaDTO o1, CwaDTO o2) {
                Integer s1 = Integer.valueOf(o1.getId());
                Integer s2 = Integer.valueOf(o2.getId());
                return s1.compareTo(s2);
            }
        });
        for (CwaDTO temp : all) {
            row = sheet.createRow(index++);
            row.setHeight((short)(0xFF | 0x100));
            cell = row.createCell(0);
            cell.setCellValue(temp.getName());
            cell = row.createCell(1);
            cell.setCellValue(temp.getId());
            cell = row.createCell(2);
            cell.setCellValue("");
            cell = row.createCell(3);
            cell.setCellValue(temp.getCq());
            cell = row.createCell(4);
            cell.setCellValue(temp.getQj());
            cell = row.createCell(5);
            cell.setCellValue(temp.getCd());
            cell = row.createCell(6);
            cell.setCellValue(temp.getLdk());
            cell = row.createCell(7);
            cell.setCellValue(temp.getNote());
        }
        index = 1;
        sheet = workbook.getSheetAt(0);
        for(CwaDTO temp : list){
            row = sheet.createRow(index++);
            row.setHeight((short)(0xFF | 0x100));
            cell = row.createCell(0);
            cell.setCellValue("蜂大侠");
            cell = row.createCell(1);
            cell.setCellValue(personMap.get(temp.getId()));
            cell = row.createCell(2);
            cell.setCellValue(temp.getId());
            cell = row.createCell(3);
            cell.setCellValue(temp.getDate());
        }
        FileOutputStream outputStream = new FileOutputStream("G:/cwa/考勤.xlsx");
        workbook.write(outputStream);
        outputStream.close();
    }

    private static Integer convertTime(String date) {
        return Integer.valueOf(date.split(" ")[1].replace(":", ""));
    }

    static {
        personMap.put("1", "何小曼");
        personMap.put("2", "文爱瑶");
        personMap.put("3", "周可望");
        personMap.put("4", "钟贤哲");
        personMap.put("5", "陆荣");
        personMap.put("6", "洪双毅");
        personMap.put("8", "胡小米");
        personMap.put("10", "余志坤");
        personMap.put("11", "邓秀维");
        personMap.put("12", "万明");
        personIndex.put("1", 0);
        personIndex.put("2", 1);
        personIndex.put("3", 2);
        personIndex.put("4", 3);
        personIndex.put("5", 4);
        personIndex.put("6", 5);
        personIndex.put("8", 6);
        personIndex.put("10", 7);
        personIndex.put("11", 8);
        personIndex.put("12", 9);

    }
}
