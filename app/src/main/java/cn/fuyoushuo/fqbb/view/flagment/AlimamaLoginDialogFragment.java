package cn.fuyoushuo.fqbb.view.flagment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.alibaba.fastjson.JSONObject;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeUtil;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.components.support.RxDialogFragment;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.commonlib.utils.LoginInfoStore;
import cn.fuyoushuo.fqbb.commonlib.utils.RxBus;
import cn.fuyoushuo.fqbb.commonlib.utils.UserInfoStore;
import cn.fuyoushuo.fqbb.presenter.impl.TaobaoInterPresenter;
import rx.functions.Action1;

/**
 * 阿里妈妈登录页面
 * Created by QA on 2016/10/27.
 */
public class AlimamaLoginDialogFragment extends RxDialogFragment{


    @Bind(R.id.alimama_login_backArea)
    View backArea;

    @Bind(R.id.alimama_login_webview_area)
    ViewGroup alimamaLoginView;

    BridgeWebView myWebView;

    int fromCode;

    public static final String TAOBAOKE_LOGIN_URL = "http://login.taobao.com/member/login.jhtml?style=common&from=alimama&redirectURL=http%3A%2F%2Flogin.taobao.com%2Fmember%2Ftaobaoke%2Flogin.htm%3Fis_login%3d1&full_redirect=true&disableQuickLogin=true&qq-pf-to=pcqq.discussion";

    //从 用户中心 过来
    public static final int FROM_USER_CENTER = 1;

    //从 淘宝天猫详情页 过来
    public static final int FROM_TB_GOOD_DETAIL = 2;

    //从 淘宝订单页面 过来
    public static final int FROM_TB_ORDER_PAGE = 3;

    //从 提现页面 过来
    public static final int FROM_TIXIAN_PAGE = 4;

    //从 我的淘宝 过来
    public static final int FROM_MY_TAOBAO_PAGE = 5;


    public static AlimamaLoginDialogFragment newInstance(int fromCode){
        Bundle args = new Bundle();
        args.putInt("fromCode",fromCode);
        AlimamaLoginDialogFragment adf = new AlimamaLoginDialogFragment();
        adf.setArguments(args);
        return adf;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.fullScreenDialog);
        int fromCode = getArguments().getInt("fromCode", 0);
        if(fromCode != 0){
            this.fromCode = fromCode;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.view_alimama_login,container);
        ButterKnife.bind(this,inflate);
        return inflate;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //初始化本页面的webview
        myWebView = new BridgeWebView(MyApplication.getContext());
        myWebView.getSettings().setJavaScriptEnabled(true);
        //myWebView.getSettings().setBuiltInZoomControls(true);//是否显示缩放按钮，默认false
        myWebView.getSettings().setSupportZoom(true);//是否可以缩放，默认true
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.getSettings().setAllowFileAccess(true);

        myWebView.getSettings().setUseWideViewPort(true);// 设置此属性，可任意比例缩放。大视图模式
        myWebView.getSettings().setLoadWithOverviewMode(true);// 和setUseWideViewPort(true)一起解决网页自适应问题

        myWebView.requestFocusFromTouch();
        myWebView.setWebChromeClient(new WebChromeClient());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            myWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            CookieManager.getInstance().setAcceptThirdPartyCookies(myWebView, true);

        myWebView.registerHandler("rememberUserInfo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.d("jsCallBack",data);
                //每次注册提交的信息保存下来
                if(!TextUtils.isEmpty(data)){
                    JSONObject result = JSONObject.parseObject(data);
                    String userName = result.getString("username");
                    String password = result.getString("password");
                    boolean rempwd = result.getBooleanValue("rempwd");
                    UserInfoStore userInfoStore = new UserInfoStore();
                    userInfoStore.setAliUserName(userName);
                    userInfoStore.setAliPassword(password);
                    userInfoStore.setRemAliInfo(rempwd);
                    LoginInfoStore.getIntance().writeUserInfo(userInfoStore);
                }
            }
        });

        myWebView.setWebViewClient(new BridgeWebViewClient(myWebView){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                 if(!TextUtils.isEmpty(url) && url.equals(TAOBAOKE_LOGIN_URL)){
                     view.loadUrl(url);
                     return true;
                 }
                 return super.shouldOverrideUrlLoading(view,url);
            }

            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view,url);
                if(url.startsWith("http://www.alimama.com/index.htm")
                    || url.startsWith("http://media.alimama.com/account/overview.htm")
                    || url.startsWith("http://media.alimama.com/account/account.htm")
                    || url.startsWith("http://media.alimama.com/user/limit_status.htm")){// 已登录
                    //保存淘宝登录的COOKIE
                    TaobaoInterPresenter.saveLoginCookie(url);
                    view.stopLoading();
                    afterSaveCookies();
                }
                BridgeUtil.webViewLoadLocalJs(view,"autoRemPass.js");
                if(url.equals("https://login.m.taobao.com/login.htm?_input_charset=utf-8")){
                    if(myWebView != null){
                        myWebView.callHandler("fillUserInfo", LoginInfoStore.getIntance().getAliInfoJson(), new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {

                            }
                        });
                    }
                }
//                if(!url.equals("https://login.m.taobao.com/login.htm?_input_charset=utf-8")){
//                   //加载需要回调的JS
//                   BridgeUtil.webViewLoadLocalJs(view,"autoRemPass.js");
//                }
//              String js = "var rmadjs = document.createElement(\"script\");";
//              js += "rmadjs.innerHTML="+"\'"+jsContent+"\'";
//              js += "document.body.appendChild(rmadjs);";
//              view.loadUrl("javascript:"+js);
            }});

        //添加webview
        alimamaLoginView.addView(myWebView);

        RxView.clicks(backArea).throttleFirst(1000, TimeUnit.MILLISECONDS)
                .compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        InputMethodManager inputMethodManager = (InputMethodManager) MyApplication.getMyapplication().getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager.isActive()) {
                            inputMethodManager.hideSoftInputFromWindow(backArea.getApplicationWindowToken(),0);
                        }
                        dismissAllowingStateLoss();
           }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        myWebView.loadUrl(TAOBAOKE_LOGIN_URL);
        if(myWebView != null){
            myWebView.callHandler("fillUserInfo", LoginInfoStore.getIntance().getAliInfoJson(), new CallBackFunction() {
                @Override
                public void onCallBack(String data) {

                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        if(alimamaLoginView != null){
            alimamaLoginView.removeView(myWebView);
        }
        if(myWebView != null){
            myWebView.removeAllViews();
            myWebView.destroy();
        }
    }

    private void afterSaveCookies(){
        if(fromCode == 0) return;
        switch (fromCode){
           case FROM_USER_CENTER:
               RxBus.getInstance().send(new AlimamaLoginToUserCenterEvent());
               dismissAllowingStateLoss();
               break;
            case FROM_TB_GOOD_DETAIL:
               RxBus.getInstance().send(new AlimamaLoginToTbGoodDetailEvent());
               dismissAllowingStateLoss();
               break;
            case FROM_MY_TAOBAO_PAGE:
               RxBus.getInstance().send(new AlimamaLoginToMyTaobaoEvent());
               dismissAllowingStateLoss();
               break;
            case FROM_TB_ORDER_PAGE:
               RxBus.getInstance().send(new AlimamaLoginToTbOrderEvent());
               dismissAllowingStateLoss();
               break;
            case FROM_TIXIAN_PAGE:
               RxBus.getInstance().send(new AlimamaLoginToTixianEvent());
               dismissAllowingStateLoss();
               break;
           default:
               break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("alimamaLoginPage");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("alimamaLoginPage");
    }

    //---------------------------------实现总线事件----------------------------------------------------
    public class AlimamaLoginToUserCenterEvent extends RxBus.BusEvent{}

    public class AlimamaLoginToTbGoodDetailEvent extends RxBus.BusEvent{}

    public class AlimamaLoginToMyTaobaoEvent extends RxBus.BusEvent{}

    public class AlimamaLoginToTbOrderEvent extends RxBus.BusEvent{}

    public class AlimamaLoginToTixianEvent extends RxBus.BusEvent{}

}
