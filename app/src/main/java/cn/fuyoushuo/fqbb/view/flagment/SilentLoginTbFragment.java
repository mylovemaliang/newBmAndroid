package cn.fuyoushuo.fqbb.view.flagment;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.github.lzyzsd.jsbridge.BridgeUtil;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.trello.rxlifecycle.components.support.RxFragment;

import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.commonlib.utils.LoginInfoStore;
import cn.fuyoushuo.fqbb.presenter.impl.TaobaoInterPresenter;

/**
 * Created by QA on 2016/12/14.
 */

public class SilentLoginTbFragment extends RxFragment{

     BridgeWebView myWebView;

     private boolean isDetched = true;

     public static final String TAOBAOKE_LOGIN_URL = "http://login.taobao.com/member/login.jhtml?style=common&from=alimama&redirectURL=http%3A%2F%2Flogin.taobao.com%2Fmember%2Ftaobaoke%2Flogin.htm%3Fis_login%3d1&full_redirect=true&disableQuickLogin=true&qq-pf-to=pcqq.discussion";

      public static SilentLoginTbFragment newInstance() {

        Bundle args = new Bundle();
        SilentLoginTbFragment fragment = new SilentLoginTbFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                        || url.startsWith("http://media.alimama.com/account/account.htm")){
                    Log.i("autoLogin","aready autoLogin");
                    // 已登录
                    //保存淘宝登录的COOKIE
                    TaobaoInterPresenter.saveLoginCookie(url);
                    view.stopLoading();
                }
                //加载需要回调的JS
                if(!url.equals("https://login.m.taobao.com/login.htm?_input_charset=utf-8")){
                BridgeUtil.webViewLoadLocalJs(view,"autoLogin.js");
                myWebView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myWebView.callHandler("autoLogin", LoginInfoStore.getIntance().getAliInfoJson(), new CallBackFunction() {
                            @Override
                            public void onCallBack(String data) {
                            }
                        });
                    }
                },1000);
              }else{
                    Log.i("autoLogin","login failed");
              }
            }});
    }

    //自动登录
    public void autoLogin(){
        if(!isDetched && myWebView != null){
            myWebView.loadUrl(TAOBAOKE_LOGIN_URL);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        isDetched = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isDetched = true;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(myWebView != null){
            myWebView.removeAllViews();
            myWebView.destroy();
        }
    }

    //----------------------------------------总线事件--------------------------------------------------------

}
