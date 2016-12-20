package cn.fuyoushuo.fqbb.domain.entity;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by QA on 2016/12/20.
 * 用于返利信息获取
 */

public class WvGoodEvent implements Serializable {

    //商品ID
    private String eventId;

    //返钱比例
    private String eventRate;

    //返钱金额
    private String eventPrice;

    private boolean fanliAble;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventPrice() {
        return eventPrice;
    }

    public void setEventPrice(String eventPrice) {
        this.eventPrice = eventPrice;
    }

    public String getEventRate() {
        return eventRate;
    }

    public void setEventRate(String eventRate) {
        this.eventRate = eventRate;
    }

    public boolean getFanliAble() {
        if(TextUtils.isEmpty(this.eventPrice) || TextUtils.isEmpty(this.eventRate)){
            return false;
        }else {
            return true;
        }
    }
}
