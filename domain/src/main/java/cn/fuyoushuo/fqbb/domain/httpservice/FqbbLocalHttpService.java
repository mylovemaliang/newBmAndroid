package cn.fuyoushuo.fqbb.domain.httpservice;

import cn.fuyoushuo.fqbb.domain.ext.HttpResp;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by maliang on 2016/6/30.
 */
public interface FqbbLocalHttpService {

    @GET("/vcc/sc.htm")
    Observable<HttpResp> getVerifiCode(@Query("vct") String vct,@Query("eop") String eop);

    @GET("/user/doregist.htm?regFrom=2")
    Observable<HttpResp> registerUser(@Query("phone") String phoneNum,@Query("password") String password,@Query("mobileCode") String mobileCode);

    @GET("/user/mlogin.htm")
    Observable<HttpResp> userLogin(@Query("loginid") String loginid,@Query("password") String passwordMD5);

    @GET("/user/mlu.htm")
    Observable<HttpResp> getUserInfo();

    @GET("/mall/mjdrate.htm")
    Observable<HttpResp> getJdFanliInfo(@Query("itemid") String itemId);

    @GET("/point/getSkus.htm?itemId=1")
    Observable<HttpResp> getPhoneRechargeSkus();

    @GET("/point/mpbuy.htm?itemId=1")
    Observable<HttpResp> createPhoneRechargeOrder(@Query("skuId") Long skuId,@Query("diliverPhone") String phoneNum);

    @GET("/user/doFindPwd.htm")
    Observable<HttpResp> findUserPass(@Query("eop") String accountValue,@Query("code") String verifiCode,@Query("password") String newpass);

    @GET("/user/mlogout.htm")
    Observable<HttpResp> userLogout();

    @GET("/user/mdoBindEmail.htm")
    Observable<HttpResp> bindEmail(@Query("email") String email,@Query("code") String verifiCode);

    @GET("/user/mdoUnBindEmail.htm")
    Observable<HttpResp> unbindEmail(@Query("eop") String account,@Query("code") String code);

    @GET("/user/validePhone.htm")
    Observable<HttpResp> validePhone(@Query("phone") String phoneNum);

    @GET("/user/valideEmail.htm")
    Observable<HttpResp> valideEmail(@Query("email") String email);

    @GET("/user/mUpdatePwd.htm")
    Observable<HttpResp> updatePassword(@Query("currentPwd") String originPassword,@Query("newPassword") String newPassword);

    //获取兑换订单
    @GET("/point/morder-{pagenum}.htm")
    Observable<HttpResp> getDhOrders(@Path("pagenum") int pageNum,@Query("queryStatus") Integer queryStatus);

    //获取兑换详情
    @GET("/point/mpdetail-{pagenum}.htm?pagesize=50")
    Observable<HttpResp> getDhDetails(@Path("pagenum") int pageNum);

    //--------------------------------------------提现与支付宝相关--------------------------------------------------

    //绑定支付宝
    @GET("/user/mdoBindAlipay.htm")
    Observable<HttpResp> bindZfb(@Query("alipayNo") String alipayNo,@Query("realName") String realName,@Query("idCard") String idCard,@Query("code") String verifiCode);

    //换绑支付宝
    @GET("/user/mchangeAlipay.htm")
    Observable<HttpResp> updateZfb(@Query("alipayNo") String alipayNo,@Query("code") String verifiCode);

    //提现订单分页查询接口
    @GET("/point/mcashOrder-{pagenum}.htm")
    Observable<HttpResp> getCashOrders(@Path("pagenum") int pageNum,@Query("queryStatus") Integer queryStatus);

    //提现订单创建
    @GET("/point/mpcash.htm")
    Observable<HttpResp> createCashOrders(@Query("cashNum") float cashNum,@Query("code") String code);

   //---------------------------------------------统计----------------------------------------------------------------------
   //点击时间记录
   @GET("/mc.gif?d=android")
   Observable<Object> clickCount(@Query("b") int biz,@Query("u") String idMd5,@Query("ur") String id,@Query("c") String channel,@Query("v") int versionCode,@Query("os") String osName,@Query("ov") String osVersion);

   //记录时长
   @GET("/mt.gif?d=android")
   Observable<Object> timeCount(@Query("b") int biz, @Query("u") String idMd5, @Query("ur") String id, @Query("c") String channel, @Query("v") int versionCode, @Query("os") String osName, @Query("ov") String osVersion, @Query("st") long time);

   //记录安装
   @GET("/ma.gif?d=android")
   Observable<Object> apkInstall(@Query("u") String idMd5,@Query("ur") String id,@Query("c") String channel,@Query("v") int versionCode,@Query("os") String osName,@Query("ov") String osVersion);

   //apk活跃
   @GET("/ml.gif?d=android")
   Observable<Object> apkActive(@Query("u") String idMd5,@Query("ur") String id,@Query("c") String channel,@Query("v") int versionCode,@Query("os") String osName,@Query("ov") String osVersion);

    //webview 跳转
    @GET("/mwv.gif?d=android")
    Observable<Object> appWvLoad(@Query("u") String idMd5,@Query("ur") String id,@Query("c") String channel,@Query("v") int versionCode,@Query("os") String osName,@Query("ov") String osVersion,@Query("pa") String weburl,@Query("refer") String refer);

}












