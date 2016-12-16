package cn.fuyoushuo.fqbb.commonlib.utils;

import java.io.Serializable;

/**
 * 用于计算页面会话的时间
 * Created by QA on 2016/12/15.
 */

public class PageSession implements Serializable {

    //业务类型
    private int bizCode;

    private Long beginTime;

    private Long endTime;

    public boolean isUseable(){
        if(bizCode != 0 && beginTime != null && endTime != null && endTime > beginTime){
            return true;
        }else{
            return false;
        }
    }

    public PageSession(int bizCode) {
        this.bizCode = bizCode;
    }

    public Long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Long beginTime) {
        this.beginTime = beginTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public int getBizCode() {
        return bizCode;
    }

    public void setBizCode(int bizCode) {
        this.bizCode = bizCode;
    }

    @Override
    public String toString() {
        return "PageSession{" +
                "beginTime=" + beginTime +
                ", bizCode=" + bizCode +
                ", endTime=" + endTime +
                '}';
    }
}
