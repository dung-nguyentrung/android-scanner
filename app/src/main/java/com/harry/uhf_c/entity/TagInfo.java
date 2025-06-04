package com.harry.uhf_c.entity;

import java.io.Serializable;

public class TagInfo implements Serializable {

    private String epc;
    private Integer count;
    private Integer ant;
    private String firstTime;

    public TagInfo() {
    }

    public TagInfo(String epc, Integer count, String firstTime, Integer ant) {
        this.epc = epc;
        this.count = count;
        this.firstTime = firstTime;
        this.ant = ant;
    }


    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public Integer getAnt() {
        return ant + 1;
    }

    public void setAnt(Integer ant) {
        this.ant = ant;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(String firstTime) {
        this.firstTime = firstTime;
    }

    @Override
    public String toString() {
        return "TagInfo{" +
                ", epc='" + epc + '\'' +
                ", count=" + count +
                ", ant=" + ant +
                '}';
    }
}
