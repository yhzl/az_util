package com.az.cwa;

import java.io.Serializable;


/**
 * 考勤记录dto
 * @author zhongxianzhe
 * @date 2020/1/15 17:10
 **/
public class CwaDTO implements Serializable {

    private String id;
    private String date;
    private String name;
    private int qj = 0;// 请假
    private int cd = 0;// 迟到
    private int ldk = 0;// 漏打卡
    private int cq = 0;// 迟到
    private int zt = 0;// 早退
    private String note = "";// 备注

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CwaDTO(){}
    public CwaDTO(String id, String date){
        this.id = id;
        this.date = date;
    }

    public int getQj() {
        return qj;
    }

    public void setQj(int qj) {
        this.qj = qj;
    }

    public int getCd() {
        return cd;
    }

    public void setCd(int cd) {
        this.cd = cd;
    }

    public int getLdk() {
        return ldk;
    }

    public void setLdk(int ldk) {
        this.ldk = ldk;
    }

    public int getCq() {
        return cq;
    }

    public void setCq(int cq) {
        this.cq = cq;
    }

    public int getZt() {
        return zt;
    }

    public void setZt(int zt) {
        this.zt = zt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "CwaDTO{" +
                "name='" + name + '\'' +
                ", qj=" + qj +
                ", cd=" + cd +
                ", ldk=" + ldk +
                ", cq=" + cq +
                ", zt=" + zt +
                ", note=" + note +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        try{
            if(this.id.equals(obj.getClass().getMethod("getId").invoke(obj).toString())){
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
