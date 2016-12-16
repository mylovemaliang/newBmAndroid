package cn.fuyoushuo.fqbb.ext;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.ExifInterface;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.ServiceManager;
import cn.fuyoushuo.fqbb.commonlib.utils.MD5;
import cn.fuyoushuo.fqbb.commonlib.utils.PageSession;
import cn.fuyoushuo.fqbb.domain.ext.HttpResp;
import cn.fuyoushuo.fqbb.domain.httpservice.FqbbLocalHttpService;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * 用于单例管理统计信息
 * Created by QA on 2016/10/31.
 */
public class LocalStatisticInfo {

    private static class LoginInfoStoreHolder{
         private static LocalStatisticInfo INTANCE = new LocalStatisticInfo();
    }

    public static LocalStatisticInfo getIntance(){
         return LoginInfoStoreHolder.INTANCE;
    }

    public void init(Context context){
        this.context = context;
        packageManager = context.getPackageManager();
        try{
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            this.versionNum = packageInfo.versionCode;
        }catch (Exception e){
            this.versionNum = 0;
        }
        //获取设备的唯一标识码
        this.idString = getUniquePsuedoID();
        this.channelString = MyApplication.getChannelValue();
        //暂时置为空
        this.osName = "";
        this.osVersionNum = "";
    }

    private PackageManager packageManager;

    private Context context;

    //手机标识码
    private String idString;

    //渠道
    private String channelString;

    //应用版本号
    private int versionNum;

    //操作系统名
    private String osName;

    //操作系统版本名
    private String osVersionNum;


    //当页面开始的时候
    public void onPageStart(PageSession pageSession){
        if(pageSession == null) return;
        pageSession.setBeginTime(new Date().getTime());
    }

    //当页面结束的时候
    public void onPageEnd(PageSession pageSession){
        if(pageSession == null) return;
        pageSession.setEndTime(new Date().getTime());
        // TODO: 2016/12/15 处理日志的发送逻辑
        if(pageSession.isUseable()){
            Long beginTime = pageSession.getBeginTime();
            Long endTime = pageSession.getEndTime();
            Long time  = (endTime - beginTime)/1000;
            String id = this.idString;
            String md5Id = MD5.MD5Encode(this.idString);

            Log.d("timeCount",pageSession.toString());

            ServiceManager.createService(FqbbLocalHttpService.class)
                .timeCount(pageSession.getBizCode(),md5Id,id,this.channelString,this.versionNum,this.osName,this.osVersionNum,time)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        return;
                    }

                    @Override
                    public void onNext(Object o) {
                        return;
                    }
                });
        }
    }


    //获得独一无二的Psuedo ID
    public static String getUniquePsuedoID() {
        String serial = null;

        String m_szDevIDShort = "35" +
                Build.BOARD.length()%10+ Build.BRAND.length()%10 +

                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +

                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +

                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +

                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +

                Build.TAGS.length()%10 + Build.TYPE.length()%10 +

                Build.USER.length()%10 ; //13 位

        try {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
            //API>=9 使用serial号
            return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
        } catch (Exception exception) {
            //serial需要一个初始化
            serial = "serial"; // 随便一个初始化
        }
        //使用硬件信息拼凑出来的15位号码
        return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
    }

}
