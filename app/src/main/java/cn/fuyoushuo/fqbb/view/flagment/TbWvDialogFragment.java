package cn.fuyoushuo.fqbb.view.flagment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeUtil;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.components.support.RxDialogFragment;
import com.umeng.analytics.MobclickAgent;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.commonlib.utils.CommonUtils;
import cn.fuyoushuo.fqbb.commonlib.utils.EventIdConstants;
import cn.fuyoushuo.fqbb.commonlib.utils.LocalStatisticConstants;
import cn.fuyoushuo.fqbb.commonlib.utils.LoginInfoStore;
import cn.fuyoushuo.fqbb.commonlib.utils.PageSession;
import cn.fuyoushuo.fqbb.commonlib.utils.RxBus;
import cn.fuyoushuo.fqbb.commonlib.utils.okhttp.PersistentCookieStore;
import cn.fuyoushuo.fqbb.domain.entity.WvGoodEvent;
import cn.fuyoushuo.fqbb.ext.LocalStatisticInfo;
import cn.fuyoushuo.fqbb.presenter.impl.SearchPresenter;
import cn.fuyoushuo.fqbb.presenter.impl.TaobaoInterPresenter;
import cn.fuyoushuo.fqbb.view.activity.HelpActivity;
import cn.fuyoushuo.fqbb.view.listener.MyTagHandler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by QA on 2016/12/16.
 */

public class TbWvDialogFragment extends RxDialogFragment{

    public static final String VOLLEY_TAG_NAME = "TbwvDialogFragment";

    private CompositeSubscription mSubscriptions;

    private BridgeWebView myWebView;

    private Button loginAlimama;//登录按钮

    private Button ktfxQx;//开通权限按钮

    private Button wsxx;//修复权限按钮

    private Button qdfx;//启动返现按钮

    private Button qdfjfb;//启动返集分宝按钮

    private Button reflashFl;  //刷新获取返利按钮（比如获取到返利了，但是获取返利链接流程节点出问题，就显示）

    private String etaoJfbUrl;

    private String loginPreUrl;

    private String ktqxPreUrl;  //开通返现权限按钮点击前的URL

    public Handler handler = new Handler();

    private Long currentItemId = 0l;

    private String currentItemUrl;

    private Integer currentItemIsGaofan = 0;  //1表示高返    0表示非高返

    private RelativeLayout webviewBottom;

    private TextView leftDetailInfo;//有红色背景的文案提示信息

    private TextView itemfxinfo1;//反金额和返集分宝的“返”字；或者提示信息（如请开通权限才能获得返钱）

    private TextView itemfxinfo2;//返现百分比的数字，或者集分宝数字

    private TextView itemfxinfo3;//约返XXX元；集分宝

    private final String tbcacheFile = "tbcache";

    private String myTaobaoPageUrl = "https://h5.m.taobao.com/mlapp/mytaobao.html#mlapp-mytaobao";

    private Long initTaobaoItemId;  //进入当前activity时的商品ID（如果是我的淘宝，那么这个值为-1），用于控制是网页后退还是关闭activity

    private LinearLayout back;

    private ImageView webviewBackImg;

    private LinearLayout webviewToHomeLl;

    private LinearLayout tipArea;

    private TextView funText;

    private RelativeLayout rightArea;

    private FrameLayout centerFrameLayout;

    private int funCode = 0; // 0:代表默认功能  1:我的订单未登录

    //是否从商品搜索页转发过来
    private boolean isFromGoodSearch = false;

    TextView webviewTitleText;

    TextView webviewToHome;

    private boolean doGoBack = false;

    private String preWebViewUrl;

    private String relatedGoodUrl = "";

    //保存最初加载的URL
    private String originLoadUrl = "";

    //加载的URL
    private String loadUrl = "";

    //保存当前页面的业务类型
    private String bizString;

    public String getOriginLoadUrl() {
        return originLoadUrl;
    }

    public boolean isFromGoodSearch() {
        return isFromGoodSearch;
    }

    private PageSession pageSession;

    private SearchPresenter searchPresenter;

    private LayoutInflater layoutInflater;

    //coupon
    FrameLayout couponFrameLayout;

    TextView couponText;

    //定义是否领取优惠券
    private boolean hasGetCoupon;

    //记录优惠券状态
    private int couponFunCode = 1; // 1.优惠券不可领取 2.优惠券可以领取 3.优惠券已经领取

    private String couponLink = "";

    public static TbWvDialogFragment newInstance(String bizString,String loadUrl,boolean isFromGoodSearch){
        Bundle args = new Bundle();
        TbWvDialogFragment fragment = new TbWvDialogFragment();
        args.putString("bizString",bizString);
        args.putString("loadUrl",loadUrl);
        args.putBoolean("isFromGoodSearch",isFromGoodSearch);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSubscriptions = new CompositeSubscription();
        if(getArguments() != null){
            bizString = getArguments().getString("bizString","");
            loadUrl = getArguments().getString("loadUrl","");
            isFromGoodSearch = getArguments().getBoolean("isFromGoodSearch",false);
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.fullScreenDialog);
        searchPresenter = new SearchPresenter();
        initBusEventListen();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        layoutInflater = LayoutInflater.from(activity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.activity_webview, container, false);
        this.getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    goBack();
                    return true;
                } else{
                    return false;
                }
            }
        });

        //coupon
        couponFrameLayout = (FrameLayout) inflate.findViewById(R.id.wv_coupon_tip_area);
        couponText = (TextView) inflate.findViewById(R.id.wv_coupon_info);

        couponFrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(couponFunCode == 2){
                    if(myWebView != null && !TextUtils.isEmpty(couponLink)){
                        myWebView.loadUrl(couponLink);
                    }
                }
            }
        });

        webviewTitleText = (TextView) inflate.findViewById(R.id.webviewTitleText);
        webviewToHome = (TextView) inflate.findViewById(R.id.webviewToHome);

        webviewBackImg = (ImageView) inflate.findViewById(R.id.webviewBackImg);
        back = (LinearLayout) inflate.findViewById(R.id.webviewGoBackLl);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        webviewToHomeLl = (LinearLayout) inflate.findViewById(R.id.webviewToHomeLl);
        webviewToHomeLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWebviewActivity();
            }
        });

        webviewBottom = (RelativeLayout) inflate.findViewById(R.id.webviewFragBottom);

        tipArea = (LinearLayout) inflate.findViewById(R.id.detailBottomRmBtn);

        tipArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemfxinfo2 != null){
                    CharSequence text = itemfxinfo2.getText();
                    if(text != null) {
                        if(getActivity() != null && !getActivity().isFinishing()) {
                            GoodDetailTipDialogFragment.newInstance(text.toString()).show(getFragmentManager(), "GoodDetailTipDialogFragment");
                        }
                    }
                }
            }
        });

        rightArea = (RelativeLayout) inflate.findViewById(R.id.rightArea);

        centerFrameLayout = (FrameLayout) inflate.findViewById(R.id.wv_frameLayout);

        funText = (TextView) inflate.findViewById(R.id.webview_fun_text);
        funText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(funCode == 1){
                    if(getActivity() != null && !getActivity().isFinishing()) {
                        AlimamaLoginDialogFragment.newInstance(AlimamaLoginDialogFragment.FROM_MY_TAOBAO_PAGE).show(getFragmentManager(), "AlimamaLoginDialogFragment");
                    }
                }
            }
        });

        myWebView = (BridgeWebView) inflate.findViewById(R.id.tb_h5page_webview);

        /*if(myWebView==null){
            LinearLayout webviewLl = (LinearLayout) this.findViewById(R.id.tb_h5page_webview);
            myWebView = new WebView(this.getApplicationContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            myWebView.setLayoutParams(lp);
            webviewLl.addView(myWebView);
        }*/
        myWebView.getSettings().setJavaScriptEnabled(true);
        //myWebView.getSettings().setBuiltInZoomControls(true);//是否显示缩放按钮，默认false
        myWebView.getSettings().setSupportZoom(true);//是否可以缩放，默认true
        myWebView.getSettings().setDomStorageEnabled(true);
        myWebView.getSettings().setAllowFileAccess(true);

        myWebView.getSettings().setUseWideViewPort(true);// 设置此属性，可任意比例缩放。大视图模式
        myWebView.getSettings().setLoadWithOverviewMode(true);// 和setUseWideViewPort(true)一起解决网页自适应问题

        myWebView.getSettings().setSupportZoom(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setDisplayZoomControls(false);

        myWebView.requestFocusFromTouch();
        myWebView.setWebChromeClient(new WebChromeClient());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            myWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            CookieManager.getInstance().setAcceptThirdPartyCookies(myWebView, true);

        if(Build.VERSION.SDK_INT <= 18){
            myWebView.getSettings().setSavePassword(false);
        }

        myWebView.registerHandler("getFanliForTbSearch",new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.d("testCallbackForTbSearch",data);
                if(TextUtils.isEmpty(data)) return;
                JSONObject goods = JSONObject.parseObject(data);
                if(!goods.containsKey("goodIds")) return;
                JSONArray goodIds = goods.getJSONArray("goodIds");
                String[] ids = new String[goodIds.size()];
                goodIds.toArray(ids);
                searchPresenter.getDiscountInfoForWv(ids, 5, new SearchPresenter.WvFanliInfoCallback() {
                    @Override
                    public void onUpdateFanliSucc(WvGoodEvent event) {
                       if(event == null) return;
                        final JSONObject result = new JSONObject();
                        result.put(event.getEventId(),event);
                        if(result.isEmpty()) return;
                        if(myWebView != null){
                          myWebView.post(new Runnable() {
                             @Override
                             public void run() {
                                myWebView.callHandler("afterTbSearchFanliLoaded", result.toJSONString(), new CallBackFunction() {
                                    @Override
                                    public void onCallBack(String data) {}
                                });
                             }
                         });
                     }
                    }

                    @Override
                    public void onUpdateFanliError() {
                        return;
                    }
                });
            }
        });

        myWebView.registerHandler("getFanliForCart",new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.d("testCallbackForCart",data);
                if(TextUtils.isEmpty(data)) return;
                JSONObject goods = JSONObject.parseObject(data);
                if(!goods.containsKey("goodIds")) return;
                JSONArray goodIds = goods.getJSONArray("goodIds");
                String[] ids = new String[goodIds.size()];
                goodIds.toArray(ids);
                searchPresenter.getDiscountInfoForWv(ids, 5, new SearchPresenter.WvFanliInfoCallback() {
                    @Override
                    public void onUpdateFanliSucc(WvGoodEvent event) {
                        if(event == null) return;
                        final JSONObject result = new JSONObject();
                        result.put(event.getEventId(),event);
                        if(result.isEmpty()) return;
                        if(myWebView != null){
                            myWebView.post(new Runnable() {
                                @Override
                                public void run() {
                                    myWebView.callHandler("afterCartFanliLoaded", result.toJSONString(), new CallBackFunction() {
                                        @Override
                                        public void onCallBack(String data) {}
                                    });
                                }
                            });
                        }
                    }

                    @Override
                    public void onUpdateFanliError() {
                        return;
                    }
                });
            }
        });

        myWebView.registerHandler("getFanliForFav",new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.d("testCallbackForFav",data);
                if(TextUtils.isEmpty(data)) return;
                JSONObject goods = JSONObject.parseObject(data);
                if(!goods.containsKey("goodIds")) return;
                JSONArray goodIds = goods.getJSONArray("goodIds");
                String[] ids = new String[goodIds.size()];
                goodIds.toArray(ids);
                searchPresenter.getDiscountInfoForWv(ids, 5, new SearchPresenter.WvFanliInfoCallback() {
                    @Override
                    public void onUpdateFanliSucc(WvGoodEvent event) {
                        if(event == null) return;
                        final JSONObject result = new JSONObject();
                        result.put(event.getEventId(),event);
                        if(result.isEmpty()) return;
                        if(myWebView != null){
                            myWebView.post(new Runnable() {
                                @Override
                                public void run() {
                                    myWebView.callHandler("afterFavFanliLoaded", result.toJSONString(), new CallBackFunction() {
                                        @Override
                                        public void onCallBack(String data) {}
                                    });
                                }
                            });
                        }
                    }

                    @Override
                    public void onUpdateFanliError() {
                        return;
                    }
                });
            }
        });

        //显示优惠券信息
        myWebView.registerHandler("afterCouponGet", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                 if(TextUtils.isEmpty(data)) return;
                 JSONObject couponData = JSONObject.parseObject(data);
                 if(!couponData.containsKey("couponNum")) return;
                 String couponNum  = couponData.getString("couponNum");
                 if(!TextUtils.isEmpty(couponNum)){
                     couponText.setText("已领取"+couponNum+"元优惠券");
                     couponFunCode = 3;
                 }

            }
        });

        myWebView.setWebViewClient(new BridgeWebViewClient(myWebView){


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                if(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("www://")){
                    Log.d("shouldOverrideUrl", url);
                    if(url.contains("ali_trackid=2:mm_") || url.contains("ali_trackid=2%3Amm_")){
                        showLeftTishi("已进入返钱模式");
                        webviewBottom.setVisibility(View.VISIBLE);
                        if(couponFrameLayout != null && !couponFrameLayout.isShown()){
                            couponFrameLayout.setVisibility(View.VISIBLE);
                        }
                        return false;
                    }else if(url.contains("frm=etao")){
                        showLeftTishi("已进入返集分宝模式");
                        webviewBottom.setVisibility(View.VISIBLE);
                        return false;
                    }else{
                        hideLeftTishi();
                    }
                    //http://h5.m.taobao.com/awp/base/order.htm?itemId=535027728789&item_num_id=535027728789&_input_charset=utf-8&buyNow=true&v=0&quantity=1&skuId=3193002206132&exParams=%7B%22id%22%3A%22535027728789%22%2C%22fqbb%22%3A%221%22%7D
                    //https://buy.m.tmall.com/order/confirmOrderWap.htm?enc=%E2%84%A2&buyNow=true&_input_charset=utf-8&itemId=533800099546&skuId=3184727994633&quantity=1&divisionCode=310100&x-itemid=533800099546&x-uid=589338408
                    //https://h5.m.taobao.com/cart/order.html?skuId=3263426004240&quantity=1&itemId=530144742802&buyNow=true&exParams=%7B%22id%22%3A%22530144742802%22%2C%22fqbb%22%3A%221%22%7D&spm=a1z3i.7c.0.i7c
                    //进入淘宝下单页面需要判断阿里妈妈是否在线
                    if(url.replace("http://","").replace("https://","").startsWith("h5.m.taobao.com/cart/order.html") ||
                            isTmallOrderPage(url)){
                        TaobaoInterPresenter.judgeAlimamaLogin(new TaobaoInterPresenter.LoginCallback() {
                            @Override
                            public void hasLoginCallback() {
                                // TODO: 2016/10/14
                                view.loadUrl(url);
                            }

                            @Override
                            public void nologinCallback() {
                                myWebView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        myWebView.stopLoading();
                                    }
                                });
                                showFanliLoginDialog();
                            }

                            @Override
                            public void judgeErrorCallback() {
                                // TODO: 2016/10/14
                            }
                        },VOLLEY_TAG_NAME);
                        return true;
                    }

                    if(isTaobaoItemDetail(url)){//是商品详情页
                        relatedGoodUrl = url.replace("&fqbb=1","");
                        webviewBottom.setVisibility(View.VISIBLE);

                        if(!url.contains("&fqbb=1")){//是新的商品详情页
                            MobclickAgent.onEvent(MyApplication.getContext(), EventIdConstants.BROWSE_TAOBAO_GOODS_NUM);
                            String itemIdStr = getParamsMapByUrlStr(url).get("id");
                            Long newItemId = 0l;
                            if(itemIdStr!=null){
                                newItemId = Long.parseLong(itemIdStr.trim());
                            }

                            if(currentItemId!=newItemId.longValue() && newItemId!=0l){
                                Log.d("taobao webview detail", "not same item id:"+currentItemId+","+newItemId);
                                currentItemId = newItemId;
                                currentItemUrl = url;
                                getItemFanliInfo(currentItemId);
                                return true;
                            }else{
                                Log.i("taobao webview detail", "same item id:"+newItemId);
                                return false;
                            }
                        }
                    }
//                     else if((url.startsWith("https://login.m.taobao.com/login.htm") || url.startsWith("https://login.tmall.com"))
//                            && !url.contains("http://login.taobao.com/member/login.jhtml?style=common&from=alimama&redirectURL=http%3A%2F%2Flogin.taobao.com%2Fmember%2Ftaobaoke%2Flogin.htm")
//                            && !url.contains("https://login.m.taobao.com/login.htm?redirectURL=http://login.taobao.com/member/taobaoke/login.htm")){
//                            showLoginPage();
//                            return true;
//                    }
                    else{
                        cleanCurrentItemId();
                        webviewBottom.setVisibility(View.GONE);
                    }
                    return super.shouldOverrideUrlLoading(view,url);
                }else{
                    return super.shouldOverrideUrlLoading(view,url);
                }
            }
            //兼容 android 5.0 以上
            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView webView, WebResourceRequest webResourceRequest) {
                String url = webResourceRequest.getUrl().toString();
                if((url.startsWith("https://login.m.taobao.com/login.htm") || url.startsWith("http://login.m.taobao.com/login.htm"))
                        && url.contains("iframe&")){
                    //needShowLoginDialogForTaobao = true;
                    myWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.stopLoading();
                        }
                    });
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                showFanliLoginDialog();
                            } catch (Exception e) {}
                        }
                    });
                }
                if(url.replace("http://","").replace("https://","").startsWith("mclient.alipay.com/h5/cashierPay.htm") ||
                        url.replace("http://","").replace("https://","").startsWith("buyertrade.taobao.com/trade/pay_success.htm") ||
                        url.replace("http://","").replace("https://","").startsWith("buy.tmall.com/order/paySuccess.htm")) {

                    MobclickAgent.onEvent(MyApplication.getContext(),EventIdConstants.SUCCESS_BUY_FOR_TAOBAO);
                }
                return null;
            }

            //兼容 android 4.4 及以上
            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {//也能拦截iframe的链接,图片,css,js等
                //淘宝立即购买的登录
                //http://login.m.taobao.com/login.htm?ttid=h5%40iframe&tpl_redirect_url=http%3A%2F%2Fh5.m.taobao.com%2Fother%2Floginend.html%3Forigin%3Dhttp%253A%252F%252Fh5.m.taobao.com
                if((url.startsWith("https://login.m.taobao.com/login.htm") || url.startsWith("http://login.m.taobao.com/login.htm"))
                        && url.contains("iframe&")){
                    //needShowLoginDialogForTaobao = true;
                    myWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            view.stopLoading();
                        }
                    });
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                showFanliLoginDialog();
                            } catch (Exception e) {}
                        }
                    });
                }
                if(url.replace("http://","").replace("https://","").startsWith("mclient.alipay.com/h5/cashierPay.htm") ||
                        url.replace("http://","").replace("https://","").startsWith("buyertrade.taobao.com/trade/pay_success.htm") ||
                        url.replace("http://","").replace("https://","").startsWith("buy.tmall.com/order/paySuccess.htm")) {

                    MobclickAgent.onEvent(MyApplication.getContext(),EventIdConstants.SUCCESS_BUY_FOR_TAOBAO);
                }
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                /*if(isTaobaoItemDetail(url)){
                    doGoBack = false;
                    url = url.replace("&fqbb=1", "");
                    getItemFanliInfo(currentItemId);
                    return;
                }*/

                super.onPageFinished(view, url);



                preWebViewUrl = url;//为了解决用户没登录就直接点击立即购买，弹出登录框我们拦截到然后到自己的登录页，登录后需要跳转回来的

                /*if((url.startsWith("http://h5.m.taobao.com/cart/order.html") || url.startsWith("https://h5.m.taobao.com/cart/order.html"))
                        && url.contains("#bridgeDetail-address_1")){
                    String s = "http://buy.m.tmall.com/order/addressList.htm?enableStation=true&requestStationUrl=%2F%2Fstationpicker-i56.m.taobao.com%2Finland%2FshowStationInPhone.htm&_input_charset=utf8&hidetoolbar=true&bridgeMessage=true&buyUrl=";
                    try {
                        String reloadUrl = s + URLEncoder.encode(url, "UTF-8");
                        myWebView.loadUrl(reloadUrl);
                        return;
                    } catch (Exception e) {

                    }
                }*/

                if(url.equals("https://login.m.taobao.com/login.htm?redirectURL=http://login.taobao.com/member/taobaoke/login.htm?is_login=1&loginFrom=wap_alimama")){
                    webviewTitleText.setText("淘宝账号登录");
                }else{
                    webviewTitleText.setText("");
                }

                Log.i("taobao webview url", url);

                if(doGoBack){
                    if(isTaobaoItemDetail(url)){//是商品详情页
                        webviewBottom.setVisibility(View.VISIBLE);

                        //获取商品返利信息
                        String itemIdStr = getParamsMapByUrlStr(url).get("id");
                        Long newItemId = 0l;
                        if(itemIdStr!=null){
                            newItemId = Long.parseLong(itemIdStr.trim());
                        }

                        if(currentItemId!=newItemId.longValue() && newItemId!=0l){
                            Log.d("taobao webview detail", "not same item id:"+currentItemId+","+newItemId);
                            currentItemId = newItemId;
                            currentItemUrl = url.replace("&fqbb=1", "");
                            getItemFanliInfo(currentItemId);
                        }else{
                            Log.i("taobao webview detail", "same item id:"+newItemId);
                        }
                    }else{
                        webviewBottom.setVisibility(View.GONE);
                    }

                    doGoBack = false;
                }

//                if(url.startsWith("http://www.alimama.com/index.htm") || url.startsWith("http://www.alimama.com/index.htm")
//                        || url.startsWith("http://media.alimama.com/account/overview.htm")
//                        || url.startsWith("https://www.alimama.com/index.htm") || url.startsWith("https://www.alimama.com/index.htm")
//                        || url.startsWith("https://media.alimama.com/account/overview.htm")){//已登录
//                    if(loginPreUrl!=null){
//                        TaobaoInterPresenter.saveLoginCookie(url);
//
//                        if(isTaobaoItemDetail(loginPreUrl)){//是商品详情页
//                            String itemIdStr = getParamsMapByUrlStr(loginPreUrl).get("id");
//                            Long newItemId = 0l;
//                            if(itemIdStr!=null){
//                                newItemId = Long.parseLong(itemIdStr.trim());
//                                currentItemId = newItemId;
//                                currentItemUrl = loginPreUrl.replace("&fqbb=1", "");
//                            }
//
//                            getItemFanliInfo(currentItemId);
//                        }else{
//                            if(myWebView != null){
//                                myWebView.loadUrl(loginPreUrl);
//                            }
//                        }
//
//                        loginPreUrl = null;
//                        loginAlimama.setVisibility(View.GONE);
//                    }
//                }
                //进入淘宝搜索页
                if(myWebView != null && myWebView.getContext() != null && url.startsWith("https://s.m.taobao.com/h5?")){
                    BridgeUtil.webViewLoadLocalJs(myWebView,"tbSearch.js");
                }
                if(myWebView != null && myWebView.getContext() != null && url.startsWith("https://h5.m.taobao.com/mlapp/cart.html")){
                    BridgeUtil.webViewLoadLocalJs(myWebView,"tbcart.js");
                    myWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            showCartTipDialog();
                        }
                    });
                }
                if(myWebView != null && myWebView.getContext() != null && url.startsWith("https://h5.m.taobao.com/fav/index.htm?") && url.contains("goods")){
                    BridgeUtil.webViewLoadLocalJs(myWebView,"tbfav.js");
                }
                if(myWebView != null && myWebView.getContext() != null && url.startsWith("https://uland.taobao.com/coupon/edetail")){
                    BridgeUtil.webViewLoadLocalJs(myWebView,"tbCoupon.js");
                    if(couponFrameLayout != null && couponFrameLayout.isShown()){
                        couponFrameLayout.setVisibility(View.GONE);
                    }
                }

                String js = "var rmadjs = document.createElement(\"script\");";
                js += "rmadjs.src=\"//www.fanqianbb.com/static/mobile/rmad.js\";";
                js += "document.body.appendChild(rmadjs);";
                view.loadUrl("javascript:" + js);
            }
        });

        //登录阿里妈妈
        loginAlimama = (Button)inflate.findViewById(R.id.loginAlimama);
        loginAlimama.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(myWebView!=null){
                    loginPreUrl = myWebView.getUrl();
                    //showLoginPage();
                    if(getActivity() != null && !getActivity().isFinishing()) {
                        AlimamaLoginDialogFragment.newInstance(AlimamaLoginDialogFragment.FROM_TB_GOOD_DETAIL).show(getFragmentManager(), "AlimamaLoginDialogFragment");
                    }
                }
            }
        });

        //开通返现权限
        ktfxQx = (Button)inflate.findViewById(R.id.ktfxQx);
        ktfxQx.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //http://media.alimama.com/user/limit_status.htm?from=union
                if(myWebView!=null){
                    ktqxPreUrl = myWebView.getUrl();
                    String ktfxQxUrl = "http://pub.alimama.com/myunion.htm";
                    myWebView.loadUrl(ktfxQxUrl);
                    webviewBottom.setVisibility(View.GONE);
                }
            }
        });

        //完善信息（修复权限）
        wsxx = (Button)inflate.findViewById(R.id.wsxx);
        wsxx.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getChannelAdzoneInfo(null);
            }
        });

        //启动返现
        qdfx = (Button)inflate.findViewById(R.id.qdfx);
        qdfx.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getCpsLink(null);
            }
        });

        //返集分宝
        qdfjfb = (Button)inflate.findViewById(R.id.qdfjfb);
        qdfjfb.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(etaoJfbUrl!=null && !"".equals(etaoJfbUrl.trim())){
                    qdfjfb.setVisibility(View.GONE);
                    if(myWebView!=null){
                        myWebView.loadUrl(etaoJfbUrl);
                    }
                }
            }
        });

        //刷新进入返利
        reflashFl = (Button)inflate.findViewById(R.id.reflashFl);
        reflashFl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(myWebView != null){
                    String url = myWebView.getUrl();
                    myWebView.loadUrl(url);
                }
            }
        });

        itemfxinfo1 = (TextView) inflate.findViewById(R.id.itemfxinfo1);
        itemfxinfo2 = (TextView) inflate.findViewById(R.id.itemfxinfo2);
        itemfxinfo3 = (TextView) inflate.findViewById(R.id.itemfxinfo3);

        leftDetailInfo = (TextView) inflate.findViewById(R.id.leftDetailInfo);
        // 后面的动作
        return inflate;
    }


    public void showCartTipDialog(){
        //上下文不存在或者正在销毁，取消弹出框弹出
        if(getActivity() == null || getActivity().isFinishing()) return;
        if(!LoginInfoStore.getIntance().IsCartTip()) return;
        final android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getActivity()).create();
        View dialog = layoutInflater.inflate(R.layout.main_tip_content_dialog, null);
        TextView content = (TextView) dialog.findViewById(R.id.main_tip_content);
        Button leftButton = (Button) dialog.findViewById(R.id.leftCommit);
        Button rightButton = (Button) dialog.findViewById(R.id.rightCommit);
        RxView.clicks(leftButton).throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        LoginInfoStore.getIntance().writeCartTipInfo(false);
                        alertDialog.dismiss();
                    }
                });

        RxView.clicks(rightButton).throttleFirst(1000,TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        alertDialog.dismiss();
                    }
                });

        String htmlForCart = "1.在购物车中直接结算无返利!<br>2.请进入返钱模式后直接购买!";
        content.setText(Html.fromHtml(htmlForCart));
        int mScreenWidth = MyApplication.getDisplayMetrics().widthPixels;
        alertDialog.show();
        WindowManager.LayoutParams params = alertDialog.getWindow().getAttributes();
        params.width = CommonUtils.getIntHundred(mScreenWidth*0.6f);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        alertDialog.getWindow().setAttributes(params);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));
        alertDialog.setContentView(dialog);
        alertDialog.setCanceledOnTouchOutside(false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        originLoadUrl = loadUrl;
        if(loadUrl.startsWith("//")){
            loadUrl = "http:"+loadUrl;
        }
        if(isFromGoodSearch){
            webviewToHome.setText("返回搜索列表");
        }else{
            webviewToHome.setText("返回宝宝主页");
        }
        //初始化页面会话
        initPageSession();
        if("myTaoBao".equals(bizString)){
            initTaobaoItemId = -1l;
            isLoginForMyTaobao();
        }else{
            if(isTaobaoItemDetail(loadUrl)){//是商品详情页
                String itemIdStr = getParamsMapByUrlStr(loadUrl).get("id");
                try{
                    if(itemIdStr!=null && !"".equals(itemIdStr.trim()))
                        initTaobaoItemId = Long.parseLong(itemIdStr);
                }catch(Exception e){}
            }else{
                initTaobaoItemId = -2l;//其它进入WEBVIEW H5页面的入口（如首页轮播图的链接点击）
            }
            if(myWebView != null){
                myWebView.loadUrl(loadUrl);
            }
        }
    }

    //初始化总线事件监听
    private void initBusEventListen(){
        mSubscriptions.add(RxBus.getInstance().toObserverable().compose(this.<RxBus.BusEvent>bindToLifecycle()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<RxBus.BusEvent>() {
            @Override
            public void onCompleted() {
                return;
            }

            @Override
            public void onError(Throwable e) {
                return;
            }

            @Override
            public void onNext(RxBus.BusEvent busEvent) {
                if(busEvent instanceof AlimamaLoginDialogFragment.AlimamaLoginToTbGoodDetailEvent){
                    if(myWebView == null) return;
                    if(loginPreUrl!=null){
                        if(isTaobaoItemDetail(loginPreUrl)){//是商品详情页
                            String itemIdStr = getParamsMapByUrlStr(loginPreUrl).get("id");
                            Long newItemId = 0l;
                            if(itemIdStr!=null){
                                newItemId = Long.parseLong(itemIdStr.trim());
                                currentItemId = newItemId;
                                currentItemUrl = loginPreUrl.replace("&fqbb=1", "");
                            }
                            getItemFanliInfo(currentItemId);
                        }else{
                            if(myWebView != null){
                                myWebView.loadUrl(loginPreUrl);
                            }
                        }
                        loginPreUrl = null;
                        loginAlimama.setVisibility(View.GONE);
                    }
                }
                else if(busEvent instanceof AlimamaLoginDialogFragment.AlimamaLoginToMyTaobaoEvent){
                    if(myWebView == null) return;
                    centerFrameLayout.setVisibility(View.GONE);
                    funCode = 0;
                    myWebView.loadUrl(myTaobaoPageUrl);
                }
            }
        }));
    }

    private void initPageSession(){
        if("myTaoBao".equals(bizString)){
            pageSession = new PageSession(LocalStatisticConstants.MY_TAOBAO);
        }
        else if("taobao".equals(bizString)){
            pageSession = new PageSession(LocalStatisticConstants.TAO_BAO);
        }
        else if("tmall".equals(bizString)){
            pageSession = new PageSession(LocalStatisticConstants.TMALL);
        }
    }

    @Override
    public void onResume() {
        if(pageSession != null){
            LocalStatisticInfo.getIntance().onPageStart(this.pageSession);
        }
        if("tbGoodDetail".equals(bizString)){
            MobclickAgent.onPageStart("tbGoodDetail_wv");
        }
        else if("myTaoBao".equals(bizString)){
            MobclickAgent.onPageStart("myTaoBao_wv");
        }
        else if("taobao".equals(bizString)){
            MobclickAgent.onPageStart("taobao_wv");
        }
        else if("tmall".equals(bizString)){
            MobclickAgent.onPageStart("tmall_wv");
        }
        super.onResume();
    }


    @Override
    public void onPause() {
        if(pageSession != null){
            LocalStatisticInfo.getIntance().onPageEnd(this.pageSession);
        }
        if("tbGoodDetail".equals(bizString)){
            MobclickAgent.onPageEnd("tbGoodDetail_wv");
        }
        else if("myTaoBao".equals(bizString)){
            MobclickAgent.onPageEnd("myTaoBao_wv");
        }
        else if("taobao".equals(bizString)){
            MobclickAgent.onPageEnd("taobao_wv");
        }
        else if("tmall".equals(bizString)){
            MobclickAgent.onPageEnd("tmall_wv");
        }
        super.onPause();
    }

    private void showFanliLoginDialog() {
        if(getActivity() == null || getActivity().isFinishing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("登录到返利模式才会有返利.");

        builder.setCancelable(false);
        builder.setPositiveButton("返利登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loginPreUrl = preWebViewUrl;
                //showLoginPage();
                AlimamaLoginDialogFragment.newInstance(AlimamaLoginDialogFragment.FROM_TB_GOOD_DETAIL).show(getFragmentManager(),"AlimamaLoginDialogFragment");
                //关闭自身
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showMyTaobaoLoginDialog() {
        if(getActivity() == null || getActivity().isFinishing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("登录淘宝才能查看我的淘宝.");

        builder.setCancelable(true);
        builder.setPositiveButton("去淘宝登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loginPreUrl = myTaobaoPageUrl;
                //showLoginPage();
                AlimamaLoginDialogFragment.newInstance(AlimamaLoginDialogFragment.FROM_MY_TAOBAO_PAGE).show(getFragmentManager(),"AlimamaLoginDialogFragment");
                //关闭自身
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

//    private void showHasPayDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setIcon(android.R.drawable.ic_dialog_info);
//        builder.setTitle("购买成功");
//
//        builder.setMessage("付款后就可以查返利订单啦，快去检查是否返利成功");
//
//        builder.setCancelable(false);
//        builder.setPositiveButton("查订单", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                payCloseWebviewActivity(2);
//            }
//        });
//        builder.setNegativeButton("回首页", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                payCloseWebviewActivity(1);
//            }
//        });
//        builder.create().show();
//    }

    private boolean isLoginUrl(String url){
        String replaceUrl = url.replace("https://", "").replace("http://", "");

        if(replaceUrl.startsWith("login.taobao.com")
                || replaceUrl.startsWith("login.m.taobao.com")){
            return true;
        }

        return false;
    }

    private boolean isTaobaoItemDetail(String url){
        String replaceUrl = url.replace("https://", "").replace("http://", "");

        if(replaceUrl.startsWith("h5.m.taobao.com/awp/core/detail.htm")
                || replaceUrl.startsWith("detail.m.tmall.com")
                || replaceUrl.startsWith("www.taobao.com/market/ju/detail_wap.php")
                || replaceUrl.startsWith("item.taobao.com/item.htm")
                || replaceUrl.startsWith("detail.tmall.com/item.htm")
                ){
            return true;
        }

        return false;
    }

    public void goBack(){
        if(myWebView == null) {
            return;
        }
        cleanCurrentItemId();
        String currentWebviewUrl = myWebView.getUrl();
        String contentWebviewUrl = "";
        if(currentWebviewUrl!=null && !"".equals(currentWebviewUrl.trim())){
            contentWebviewUrl = currentWebviewUrl.replace("http://","").replace("https://","");
            if(initTaobaoItemId==-1){//我的淘宝进入当前activity
                if(!currentWebviewUrl.contains("login.taobao.com/member/login.jhtml")
                        && !currentWebviewUrl.contains("h5.m.taobao.com/mlapp/mytaobao.html")
                        && !currentWebviewUrl.contains("login.m.taobao.com/login.htm")){
                    myWebView.goBack();
                    doGoBack = true;
                    return;
                }
            }else if(contentWebviewUrl.startsWith("login.m.taobao.com/login.htm")){
                if(myWebView == null){
                    return;
                }
                doGoBack = true;
                myWebView.loadUrl(relatedGoodUrl);
                return;
            } else if(initTaobaoItemId>0){//商品详情进入当前activity
                String itemIdStr = getParamsMapByUrlStr(currentWebviewUrl).get("id");
                try{
                    if(itemIdStr!=null && !"".equals(itemIdStr.trim())){
                        Long itemid = Long.parseLong(itemIdStr);
                        if(initTaobaoItemId.longValue() != itemid.longValue()){
                            myWebView.goBack();
                            doGoBack = true;
                            return;
                        }
                    }else{
                        myWebView.goBack();
                        doGoBack = true;
                        return;
                    }
                }catch(Exception e){
                    myWebView.goBack();
                    doGoBack = true;
                    return;
                }
            }else{
                if(myWebView.canGoBack()){
                    myWebView.goBack();
                    doGoBack = true;
                    return;
                }
            }
        }else{
            Log.i("in goback url--->","null or empty");
        }
        closeWebviewActivity();
    }

    public void onDestroy() {
        TaobaoInterPresenter.cancelTagedRuquests(VOLLEY_TAG_NAME);
        if(mSubscriptions != null && mSubscriptions.hasSubscriptions()){
            mSubscriptions.unsubscribe();
        }
        if(searchPresenter != null){
            searchPresenter.onDestroy();
        }
        if(myWebView!=null){
            ViewGroup viewGroup = (ViewGroup) myWebView.getParent();
            if(viewGroup!=null){
                viewGroup.removeView(myWebView);
            }
            myWebView.removeAllViews();
            myWebView.destroy();
            myWebView=null;
        }

        back.setOnClickListener(null);
        webviewToHomeLl.setOnClickListener(null);
        loginAlimama.setOnClickListener(null);
        ktfxQx.setOnClickListener(null);
        qdfx.setOnClickListener(null);
        qdfjfb.setOnClickListener(null);
        reflashFl.setOnClickListener(null);
        super.onDestroy();
    }

//    private void payCloseWebviewActivity(int type){
//        if(myWebView == null){
//            return;
//        }
//        currentItemId = 0l;
//        currentItemIsGaofan = 0;
//
//        clearWebview();
//        myWebView.setVisibility(View.GONE);
//
//        Intent intent = new Intent(WebviewActivity.this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//
//        if(type==1){//回首页
//            intent.putExtra("toFirstPage", true);
//        }else if(type==2){//查订单
//            intent.putExtra("toOrderPage", true);
//        }
//
//        startActivity(intent);
//        finish();
//    }

    private void closeWebviewActivity(){
        dismissAllowingStateLoss();
    }

    public void clearWebview(){
        Log.i("clear webview","in clear webview ==================");
        if(myWebView!=null){
            myWebView.loadUrl("file:///android_asset/index.html");
        }
        showFlowBtn(null);
        webviewBottom.setVisibility(View.GONE);
    }

    public void showFanliInfo(Float fxRate, Float fxFee){
        itemfxinfo1.setText("返");
        itemfxinfo2.setText(fxRate+"%");
        itemfxinfo3.setText("     约返"+new DecimalFormat("#.##").format(fxFee)+"元");
        if(!rightArea.isShown()){
            rightArea.setVisibility(View.VISIBLE);
        }
        itemfxinfo1.setVisibility(View.VISIBLE);
        itemfxinfo2.setVisibility(View.VISIBLE);
        itemfxinfo3.setVisibility(View.VISIBLE);
        tipArea.setVisibility(View.VISIBLE);
    }

    public void showFanJfbInfo(Long jfbAmount){
        itemfxinfo1.setText("返");
        itemfxinfo2.setText(jfbAmount+"");
        itemfxinfo3.setText("集分宝");
        itemfxinfo1.setVisibility(View.VISIBLE);
        itemfxinfo2.setVisibility(View.VISIBLE);
        itemfxinfo3.setVisibility(View.VISIBLE);
    }

    public void showLeftTishi(String tsxx){
        leftDetailInfo.setText(tsxx);
        leftDetailInfo.setVisibility(View.VISIBLE);
    }

    public void hideLeftTishi(){
        leftDetailInfo.setVisibility(View.GONE);
    }

    public void showRightTishi(String tsxx){
        itemfxinfo1.setText(tsxx);
        if(!rightArea.isShown()){
           rightArea.setVisibility(View.VISIBLE);
        }
        itemfxinfo1.setVisibility(View.VISIBLE);
        itemfxinfo2.setVisibility(View.GONE);
        itemfxinfo3.setVisibility(View.GONE);
        tipArea.setVisibility(View.GONE);
    }

    public void showFlowBtn(String jsonInfoFromAlimama){//显示登录按钮/完善信息按钮/开通权限按钮/启动返钱按钮
        Log.d("showFlowBtn", "in showFlowBtn:"+jsonInfoFromAlimama);

        loginAlimama.setVisibility(View.GONE);
        ktfxQx.setVisibility(View.GONE);
        wsxx.setVisibility(View.GONE);
        qdfx.setVisibility(View.GONE);
        qdfjfb.setVisibility(View.GONE);
        leftDetailInfo.setVisibility(View.GONE);
        reflashFl.setVisibility(View.GONE);

        JSONObject jsonObject;
        if(jsonInfoFromAlimama!=null && !"".equals(jsonInfoFromAlimama)){
            jsonObject = JSONObject.parseObject(jsonInfoFromAlimama);
            if(jsonObject!=null){
                int tag = jsonObject.getIntValue("tag");

                if(tag==0){//显示登录按钮
                    loginAlimama.setVisibility(View.VISIBLE);
                }
                if(tag==1){//完善信息按钮（修复权限）；新增媒体、渠道、推广位失败，取CPS链接出现异常
                    wsxx.setVisibility(View.VISIBLE);
                }
                if(tag==2){//开通权限按钮，没有实名认证
                    ktfxQx.setVisibility(View.VISIBLE);
                    //jfbDisplay();
                }
                if(tag==3){//启动返钱模式按钮
                    qdfx.setVisibility(View.VISIBLE);
                }
                if(tag==4){//启动返集分宝模式按钮
                    qdfjfb.setVisibility(View.VISIBLE);
                }
                if(tag==5){//刷新进入返利按钮
                    rightArea.setVisibility(View.GONE);
                    reflashFl.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void getItemFanliInfo(Long itemId){
        currentItemId = itemId;
        String u = "http%3A%2F%2Fitem.taobao.com%2Fitem.htm%3Fid%3D"+itemId;
        final String itemFanliUrl = "http://pub.alimama.com/items/search.json?toPage=1&perPagesize=40&_input_charset=utf-8&t="+new Date().getTime()+"&q="+u+"&_tb_token_=";

        Log.d("getFanliInfoCallbackUrl", itemFanliUrl);

        //RequestQueue volleyRq = Volley.newRequestQueue(this, new OkHttpStack(this));
        RequestQueue volleyRq = TaobaoInterPresenter.getVolleyRequestQueue();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, itemFanliUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response!=null && !"".equals(response.trim())){
                            JSONObject jsonObj =null;
                            try{
                                jsonObj = JSONObject.parseObject(response.trim());
                            }catch(Exception e){
                                reflashFanliDisplay();
                                return;
                            }
                            JSONObject dataJsonObj = jsonObj.getJSONObject("data");
                            if(dataJsonObj!=null){
                                JSONArray pageListJson = dataJsonObj.getJSONArray("pageList");
                                if(pageListJson!=null && pageListJson.size()>0){
                                    JSONObject itemFanliInfoJson = pageListJson.getJSONObject(0);
                                    final Float gfRate = itemFanliInfoJson.getFloat("eventRate");//高返比例
                                    Float gfFeeCount = null;

                                    final Float fxRate = itemFanliInfoJson.getFloat("tkRate");//返现比率
                                    final Float fxFee = itemFanliInfoJson.getFloat("tkCommFee");//返现金额

                                    if(gfRate!=null && gfRate>0){
                                        Float itemPrice = itemFanliInfoJson.getFloat("zkPrice");//折扣价格
                                        if(itemPrice==null){
                                            itemPrice = itemFanliInfoJson.getFloat("reservePrice");//原价
                                        }
                                        gfFeeCount = itemPrice*gfRate/100;
                                    }
                                    final Float gfFee = gfFeeCount;


                                    URI uri = URI.create(itemFanliUrl);
                                    PersistentCookieStore s = new PersistentCookieStore(MyApplication.getContext());
                                    Log.d("cookie itemFanliUrl", s.get(uri).toString());

                                    if((gfFee!=null && gfFee>0) || (fxFee!=null && fxFee>0)){
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    if(gfFee!=null && gfFee>0){
                                                        showFanliInfo(gfRate, gfFee);
                                                        currentItemIsGaofan = 1;
                                                        isLoginForItemdDetail(false,new CachedFanliInfo(fxFee,fxRate,gfFee,gfRate));
                                                    }else if(fxFee!=null && fxFee>0){
                                                        showFanliInfo(fxRate, fxFee);
                                                        currentItemIsGaofan = 0;
                                                        isLoginForItemdDetail(false,new CachedFanliInfo(fxFee,fxRate,gfFee,gfRate));
                                                    }else{
                                                        jfbDisplay();
                                                        isLoginForItemdDetail(true,new CachedFanliInfo(fxFee,fxRate,gfFee,gfRate));
                                                    }
                                                } catch (Exception e) {
                                                    Log.d("getFlInfoCallbackError",e.getMessage());
                                                    showCurrentItemUrl();
                                                }
                                            }
                                        });
                                    }else{
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    jfbDisplay();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }else{//未获取到返利信息
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                jfbDisplay();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 出错了怎么办，显示通信失败信息
                        showCurrentItemUrl();
                    }
                });

        // 把这个请求加入请求队列
        stringRequest.setTag(VOLLEY_TAG_NAME);
        volleyRq.add(stringRequest);
    }

    public void isLoginForItemdDetail(boolean isJfb, final CachedFanliInfo fanliInfo){
        final boolean isJfbTag = isJfb;
        TaobaoInterPresenter.judgeAlimamaLogin(new TaobaoInterPresenter.LoginCallback() {
            @Override
            public void hasLoginCallback() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(myWebView!=null){
                            try {
                                if(!isJfbTag){
                                    isValidCpsUser(fanliInfo);//判断用户是否实名认证
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                showCurrentItemUrl();
                            }
                        }
                    }
                });
            }

            @Override
            public void nologinCallback() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(myWebView!=null){
                            try {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("tag", 0);
                                showFlowBtn(jsonObj.toJSONString());//显示登录(我要返钱)按钮
                                showCurrentItemUrl();
                                if(couponFrameLayout != null && couponFrameLayout.isShown()){
                                    couponText.setText("");
                                    couponFunCode = 1;
                                    couponFrameLayout.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                showCurrentItemUrl();
                            }
                        }
                    }
                });
            }

            @Override
            public void judgeErrorCallback() {

            }
        },VOLLEY_TAG_NAME);
    }


    //判断是否是天猫下单页面
    private boolean isTmallOrderPage(String url){
        if(TextUtils.isEmpty(url)) return false;
        else if(url.replace("http://","").replace("https://","").startsWith("buy.m.tmall.com/order/confirmOrderWap.htm")) {
            return true;
        }
        else if(url.replace("http://","").replace("https://","").startsWith("login.tmall.com/")){
            Map<String, String> paramsMapByUrlStr = getParamsMapByUrlStr(url);
            if(!paramsMapByUrlStr.isEmpty()){
                if((paramsMapByUrlStr.containsKey("redirectURL") && paramsMapByUrlStr.get("redirectURL").contains("detail.m.tmall.com%2Fitem.htm"))){
                    return true;
                }
            }
        }
        return false;
    }

    public void isLoginForMyTaobao(){
        loginPreUrl = myTaobaoPageUrl;
        cleanCurrentItemId();

        TaobaoInterPresenter.judgeAlimamaLogin(new TaobaoInterPresenter.LoginCallback() {
            @Override
            public void hasLoginCallback() {
                if(myWebView!=null){
                    centerFrameLayout.setVisibility(View.GONE);
                    funCode = 0;
                    myWebView.loadUrl(myTaobaoPageUrl);
                }
            }

            @Override
            public void nologinCallback() {
//                if(myWebView!=null){
//                    myWebView.loadUrl(TaobaoInterPresenter.TAOBAOKE_LOGINURL);
//                }
                centerFrameLayout.setVisibility(View.VISIBLE);
                funCode = 1;
                //showMyTaobaoLoginDialog();
            }

            @Override
            public void judgeErrorCallback() {

            }
        },VOLLEY_TAG_NAME);
    }

    public void showLoginPage(){
        String loginPageUrl = "http://login.taobao.com/member/login.jhtml?style=common&from=alimama&redirectURL=http%3A%2F%2Flogin.taobao.com%2Fmember%2Ftaobaoke%2Flogin.htm%3Fis_login%3d1&full_redirect=true&disableQuickLogin=true&qq-pf-to=pcqq.discussion";
        if(myWebView!=null){
            myWebView.loadUrl(loginPageUrl);
        }
        webviewBottom.setVisibility(View.GONE);
        //cleanCurrentItemId();
    }

    public void isValidCpsUser(final CachedFanliInfo cachedFanliInfo){
        Log.d("isValidCpsUser", "isValidCpsUser");
        long siteId = TaobaoInterPresenter.loginUserInfoCache.getLong("siteId", 0l);
        long adzoneId = TaobaoInterPresenter.loginUserInfoCache.getLong("adzoneId", 0l);
        long channelId = TaobaoInterPresenter.loginUserInfoCache.getLong("channelId", 0l);

        if(siteId!=0 && adzoneId!=0 && channelId!=0){
            getChannelAdzoneInfo(cachedFanliInfo);
        }else{
            long t = new Date().getTime();
            String isValidCpsUserUrl = "http://pub.alimama.com/common/site/generalize/guideList.json?t="+t+"&_input_charset=utf-8";

            URI uri = URI.create(isValidCpsUserUrl);
            PersistentCookieStore s = new PersistentCookieStore(MyApplication.getContext());
            Log.d("cookie isValidCpsUser:",isValidCpsUserUrl+s.get(uri).toString());

            //RequestQueue volleyRq = Volley.newRequestQueue(this, new OkHttpStack(this));
            RequestQueue volleyRq = TaobaoInterPresenter.getVolleyRequestQueue();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, isValidCpsUserUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try{
                                JSONObject jsonObj =null;
                                jsonObj = JSONObject.parseObject(response.trim());
                                if(jsonObj!=null){
                                    boolean hasGuide = true;
                                    if(hasGuide){//获取用户的媒体、渠道等信息
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    getChannelAdzoneInfo(cachedFanliInfo);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    showCurrentItemUrl();
                                                }
                                            }
                                        });
                                    }
					                /*if(needAddGuid){

					                }*/
                                }else{
                                    Log.d("isValidCpsUserCallback", "需要实名认证");
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                JSONObject jsonObj = new JSONObject();
                                                jsonObj.put("tag", 2);
                                                //jsonObj.put("url", "http://login.taobao.com/member/login.jhtml?style=common&from=alimama&redirectURL=http%3A%2F%2Flogin.taobao.com%2Fmember%2Ftaobaoke%2Flogin.htm%3Fis_login%3d1&full_redirect=true&disableQuickLogin=true&qq-pf-to=pcqq.discussion");
                                                showFlowBtn(jsonObj.toJSONString());//显示没有权限按钮
                                                showCurrentItemUrl();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                showCurrentItemUrl();
                                            }
                                        }
                                    });
                                    return;
                                }
                            }catch(Exception e){
                                Log.d("isValidCpsUserCallback", "需要实名认证");
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject jsonObj = new JSONObject();
                                            jsonObj.put("tag", 2);
                                            //jsonObj.put("url", "http://login.taobao.com/member/login.jhtml?style=common&from=alimama&redirectURL=http%3A%2F%2Flogin.taobao.com%2Fmember%2Ftaobaoke%2Flogin.htm%3Fis_login%3d1&full_redirect=true&disableQuickLogin=true&qq-pf-to=pcqq.discussion");
                                            showFlowBtn(jsonObj.toJSONString());//显示没有权限按钮
                                            showCurrentItemUrl();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            showCurrentItemUrl();
                                        }
                                    }
                                });
                                return;
                            }
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // 出错了怎么办，显示通信失败信息
                            showCurrentItemUrl();
                        }
                    });

            //stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1.0f));

            // 把这个请求加入请求队列
            stringRequest.setTag(VOLLEY_TAG_NAME);
            volleyRq.add(stringRequest);
        }
    }

    public void getChannelAdzoneInfo(final CachedFanliInfo cachedFanliInfo){
        Log.d("getChannelAdzoneInfo", "getChannelAdzoneInfo");
		/*long siteId = TaobaoInterPresenter.loginUserInfoCache.getLong("siteId", 0l);
        long adzoneId = TaobaoInterPresenter.loginUserInfoCache.getLong("adzoneId", 0l);
        long channelId = TaobaoInterPresenter.loginUserInfoCache.getLong("channelId", 0l);

        if(siteId!=0 && adzoneId!=0 && channelId!=0){
            getCpsLink();
        }else{*/
        long t = new Date().getTime();
        String getChannelAdzoneInfoUrl = "http://pub.alimama.com/common/adzone/newSelfAdzone2.json?tag=29&t="+t;

        URI uri = URI.create(getChannelAdzoneInfoUrl);
        PersistentCookieStore s = new PersistentCookieStore(MyApplication.getContext());
        Log.d("getChannelAdzoneInfo:",getChannelAdzoneInfoUrl+s.get(uri).toString());

        //RequestQueue volleyRq = Volley.newRequestQueue(this, new OkHttpStack(this));
        RequestQueue volleyRq = TaobaoInterPresenter.getVolleyRequestQueue();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getChannelAdzoneInfoUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Long siteId = null;
                        Long adzoneId = null;
                        Long channelId = null;

                        JSONObject jsonObj =null;
                        try{
                            jsonObj = JSONObject.parseObject(response.trim());
                        }catch(Exception e){
                            reflashFanliDisplay();
                            return;
                        }
                        JSONObject dataJsonObj = jsonObj.getJSONObject("data");
                        if(dataJsonObj!=null){
                            JSONArray otherListsArr = dataJsonObj.getJSONArray("otherList");
                            if(otherListsArr!=null && otherListsArr.size()>0){
                                for(int i=0; i<otherListsArr.size(); i++){
                                    JSONObject oneSiteJson = otherListsArr.getJSONObject(i);
                                    if("代购".equals(oneSiteJson.get("name"))){
                                        siteId = oneSiteJson.getLong("siteid");
                                        break;
                                    }
                                }
                            }

                            JSONArray channelslistArr = dataJsonObj.getJSONArray("channelslist");
                            if(channelslistArr!=null && channelslistArr.size()>0){
                                for(int i=0; i<channelslistArr.size(); i++){
                                    JSONObject oneChannelJson = channelslistArr.getJSONObject(i);
                                    if("代购".equals(oneChannelJson.getString("channelName"))){
                                        channelId = oneChannelJson.getLong("channelId");
                                        break;
                                    }
                                }
                            }

                            JSONArray otherAdzonesArr = dataJsonObj.getJSONArray("otherAdzones");
                            if(otherAdzonesArr!=null && otherAdzonesArr.size()>0){
                                for(int i=0; i<otherAdzonesArr.size(); i++){
                                    JSONObject oneAdzonelJson = otherAdzonesArr.getJSONObject(i);
                                    Long sid = Long.parseLong(oneAdzonelJson.getString("id"));
                                    if(sid != null && siteId != null && sid.longValue() == siteId.longValue()){
                                        JSONArray adzoneSubArr = oneAdzonelJson.getJSONArray("sub");
                                        if(adzoneSubArr!=null && adzoneSubArr.size()>0){
                                            JSONObject adzoneSubJson = adzoneSubArr.getJSONObject(0);
                                            adzoneId = adzoneSubJson.getLong("id");
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        boolean lackUserAlimamaInfo = false;

                        SharedPreferences.Editor prefsWriter = TaobaoInterPresenter.loginUserInfoCache.edit();
                        if(siteId!=null && siteId>0){
                            prefsWriter.putLong("siteId", siteId);

                            if(channelId!=null && channelId>0){
                                prefsWriter.putLong("channelId", channelId);

                                if(adzoneId!=null && adzoneId>0){
                                    prefsWriter.putLong("adzoneId", adzoneId);
                                }else{
                                    lackUserAlimamaInfo = true;
                                }
                            }else{
                                lackUserAlimamaInfo = true;
                            }
                        }else{
                            lackUserAlimamaInfo = true;
                        }

                        prefsWriter.commit();

                        if(lackUserAlimamaInfo){//信息不完整
                            try{
                                //新增媒体
                                if(siteId==null || siteId<=0l){
                                    addSite(cachedFanliInfo);
                                    return;
                                }

                                //新增渠道
                                if(channelId==null || channelId<=0l){
                                    addChannel(cachedFanliInfo);
                                    return;
                                }

                                //新增推广位
                                if(adzoneId==null || adzoneId<=0l){
                                    addAdZone(siteId, channelId);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }else{
                            Log.d("getCAInfoCallback", "信息完整");
                            Log.d("getCAInfoCallback", "auto to CPSLink");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        getCpsLink(cachedFanliInfo);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            /*if(AutoFanliPresenter.isAutoFanli()){
                                Log.d("getChannelAdzoneInfoCallback", "auto to CPSLink");
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            getCpsLink();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }else{
                                Log.d("getChannelAdzoneInfoCallback", "display 启动返现模式 button");
                                JSONObject jsonFxBtnObj = new JSONObject();
                                jsonFxBtnObj.put("tag", 3);
                                showFlowBtn(jsonFxBtnObj.toJSONString());//显示启动返现按钮
                                showCurrentItemUrl();
                            }*/
                        }
                    }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showCurrentItemUrl();
                        // 出错了怎么办，显示通信失败信息
                    }
                });

        // 把这个请求加入请求队列
        stringRequest.setTag(VOLLEY_TAG_NAME);
        volleyRq.add(stringRequest);
        //}
    }

    public void getCpsLink(final CachedFanliInfo cachedFanliInfo){
        Log.d("getCpsLink", "getCpsLink");
        long siteId = TaobaoInterPresenter.loginUserInfoCache.getLong("siteId", 0l);
        long adzoneId = TaobaoInterPresenter.loginUserInfoCache.getLong("adzoneId", 0l);
        long channelId = TaobaoInterPresenter.loginUserInfoCache.getLong("channelId", 0l);

        if(siteId!=0 && adzoneId!=0 && channelId!=0){
            long t = new Date().getTime();

            String highCpsLinkUrl = "http://pub.alimama.com/common/code/getAuctionCode.json?auctionid="+currentItemId+"&siteid="+siteId+"&adzoneid="+adzoneId+"&t="+t+"&_input_charset=utf-8&scenes=3&channel=tk_qqhd";

            final String lowCpsLinkUrl = "http://pub.alimama.com/common/code/getAuctionCode.json?auctionid="+currentItemId+"&siteid="+siteId+"&adzoneid="+adzoneId+"&t="+t+"&_input_charset=utf-8";

            String getCpsLinkUrl = lowCpsLinkUrl;
            if(currentItemIsGaofan==1){
                getCpsLinkUrl = highCpsLinkUrl;
            }

            URI uri = URI.create(getCpsLinkUrl);
            PersistentCookieStore s = new PersistentCookieStore(MyApplication.getContext());
            List<HttpCookie> listCookie = s.get(uri);
            Log.d("cookie getCpsLink",getCpsLinkUrl+listCookie.toString());

            //RequestQueue volleyRq = Volley.newRequestQueue(this, new OkHttpStack(this));
            RequestQueue volleyRq = TaobaoInterPresenter.getVolleyRequestQueue();
            StringRequest stringRequest = new StringRequest(Request.Method.GET, getCpsLinkUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObj =null;
                            try{
                                jsonObj = JSONObject.parseObject(response.trim());
                            }catch(Exception e){
                                reflashFanliDisplay();
                                return;
                            }
                            JSONObject dataJsonObj = jsonObj.getJSONObject("data");
                            if(dataJsonObj!=null){
                                //final String cpsUrl = dataJsonObj.getString("shortLinkUrl");
                                final String cpsUrl = dataJsonObj.getString("clickUrl");
                                //初始化商权信息
                                final String couponLinkUrl;
                                if(dataJsonObj.containsKey("couponShortLinkUrl") && !TextUtils.isEmpty(dataJsonObj.getString("couponShortLinkUrl"))){
                                    couponLinkUrl = dataJsonObj.getString("couponShortLinkUrl");
                                }else if(dataJsonObj.containsKey("couponLink") && !TextUtils.isEmpty(dataJsonObj.getString("couponLink"))){
                                    couponLinkUrl = dataJsonObj.getString("couponLink");
                                }else{
                                    couponLinkUrl = "";
                                }
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            if(myWebView!=null){
                                                myWebView.loadUrl(cpsUrl);
                                                qdfx.setVisibility(View.GONE);
                                                MobclickAgent.onEvent(MyApplication.getContext(),EventIdConstants.NUMBER_OF_FANLI_FOR_TAOBAO);
                                                initCouponInfo(couponLinkUrl);
                                                //addItemTextInfo("已进入返利模式，", null);
                                                //testDd();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }else{
                                //当高返没有结果时候尝试普通返利cps跳转
                                if(currentItemIsGaofan == 1){
                                    _retryGetCpsLink(lowCpsLinkUrl,cachedFanliInfo);
                                }
                            }
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            showCurrentItemUrl();
                            // 出错了怎么办，显示通信失败信息
                        }
                    });

            // 把这个请求加入请求队列
            stringRequest.setTag(VOLLEY_TAG_NAME);
            volleyRq.add(stringRequest);
        }else{
            return;
        }
    }

    //重新获取cps链接
    private void _retryGetCpsLink(String getCpsLinkUrl, final CachedFanliInfo cachedFanliInfo){
        RequestQueue volleyRq = TaobaoInterPresenter.getVolleyRequestQueue();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, getCpsLinkUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObj =null;
                        try{
                            jsonObj = JSONObject.parseObject(response.trim());
                        }catch(Exception e){
                            reflashFanliDisplay();
                            return;
                        }
                        JSONObject dataJsonObj = jsonObj.getJSONObject("data");
                        if(dataJsonObj!=null){
                            //final String cpsUrl = dataJsonObj.getString("shortLinkUrl");
                            final String cpsUrl = dataJsonObj.getString("clickUrl");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if(myWebView!=null){
                                            myWebView.loadUrl(cpsUrl);
                                            qdfx.setVisibility(View.GONE);
                                            MobclickAgent.onEvent(MyApplication.getContext(),EventIdConstants.NUMBER_OF_FANLI_FOR_TAOBAO);
                                            if(cachedFanliInfo != null && cachedFanliInfo.getFxFee() != null){
                                                showFanliInfo(cachedFanliInfo.getFxRate(),cachedFanliInfo.getFxFee());
                                            }
                                            //addItemTextInfo("已进入返利模式，", null);
                                            //testDd();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showCurrentItemUrl();
                        // 出错了怎么办，显示通信失败信息
                    }
                });

        // 把这个请求加入请求队列
        stringRequest.setTag(VOLLEY_TAG_NAME);
        volleyRq.add(stringRequest);
    }

    private void initCouponInfo(String couponLinkUrl) {
        if(TextUtils.isEmpty(couponLinkUrl)){
            couponText.setText("暂无可用优惠券");
            couponFunCode = 1;
        }else{
            couponText.setText("请领取优惠券");
            couponFunCode = 2;
            this.couponLink = couponLinkUrl;
        }
        if(couponFrameLayout != null && !couponFrameLayout.isShown()){
            couponFrameLayout.setVisibility(View.VISIBLE);
        }
    }

    public void jfbDisplay(){
        showRightTishi("该商品没返利");
        hideLeftTishi();
        showCurrentItemUrl();

        /*String jfbUrl = "http://ok.etao.com/api/purchase_detail.do?src=auction_detail&partner=2006&nid=" + currentItemId;

        //RequestQueue volleyRq = Volley.newRequestQueue(this, new OkHttpStack(this));
        RequestQueue volleyRq = TaobaoInterPresenter.getVolleyRequestQueue();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, jfbUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response!=null && !"".equals(response.trim())){
                            JSONObject jsonObject =null;
                            try{
                                jsonObject = JSONObject.parseObject(response.trim());
                            }catch(Exception e){
                                showRightTishi("没获取到该商品返利信息");
                                hideLeftTishi();
                            }
                            if(jsonObject!=null){
                                JSONObject r = jsonObject.getJSONObject("result");
                                if(r!=null){
                                    JSONObject bestPlan = r.getJSONObject("bestPlan");
                                    if(bestPlan!=null){
                                        String jfbAmount = bestPlan.getString("rebateSaving");//集分宝数
                                        String cpsHref = bestPlan.getString("cpsHref");//集分宝CPS跳转URL

                                        if(jfbAmount!=null && !"".equals(jfbAmount.trim())
                                                && cpsHref!=null && !"".equals(cpsHref.trim()) && !isTaobaoItemDetail(cpsHref)){
                                            int jfbInt = Integer.parseInt(jfbAmount.trim());
                                            if(jfbInt>0){
                                                Long jfbAmountLong = 0l;
                                                if(jfbAmount!=null && !"".equals(jfbAmount.trim())){
                                                    jfbAmountLong = Long.parseLong(jfbAmount.trim());
                                                    if(jfbAmountLong!=null && jfbAmountLong>0){
                                                        showFanJfbInfo(jfbAmountLong);

                                                        if(AutoFanliPresenter.isAutoFanli()){
                                                            myWebView.loadUrl(cpsHref);
                                                        }else{
                                                            Log.d("jfbDisplay", "display 启动返集分宝模式 button");
                                                            etaoJfbUrl = cpsHref;
                                                            JSONObject jsonFjfbBtnObj = new JSONObject();
                                                            jsonFjfbBtnObj.put("tag", 4);
                                                            showFlowBtn(jsonFjfbBtnObj.toJSONString());//显示启动返集分宝按钮
                                                        }

                                                        return;
                                                    }
                                                }
                                            }else{
                                                //显示没有返利信息
                                                showRightTishi("该商品没返利");
                                                hideLeftTishi();
                                                showCurrentItemUrl();
                                            }
                                        }else{
                                            //显示没有返利信息
                                            showRightTishi("该商品没返利");
                                            hideLeftTishi();
                                            showCurrentItemUrl();
                                        }
                                    }else{
                                        //显示没有返利信息
                                        showRightTishi("该商品没返利");
                                        hideLeftTishi();
                                        showCurrentItemUrl();
                                    }
                                }
                            }
                        }
                    }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 出错了怎么办，显示通信失败信息
                    }
                });

        // 把这个请求加入请求队列
        volleyRq.add(stringRequest);*/
    }

    public void addSite(final CachedFanliInfo cachedFanliInfo){
        try{
            PersistentCookieStore s = new PersistentCookieStore(MyApplication.getContext());
            URI uri = URI.create("http://www.alimama.com");
            List<HttpCookie> listAlimamaCookies = s.get(uri);
            String tbTokenCookie = null;
            if(listAlimamaCookies!=null && listAlimamaCookies.size()>0){
                for(HttpCookie c : listAlimamaCookies){
                    if(c.getName().equals("_tb_token_")){
                        tbTokenCookie = c.getValue();
                    }
                }
            }

            //新增媒体
            //"http://pub.alimama.com/common/site/generalize/guideAdd.json?name=" + encodeURIComponent("代购")(媒体名称) + "&categoryId=24&_tb_token_=" + itemInfo.tbToken + "&account1=" + itemInfo.account(用户的旺旺名)
            String mmNick = TaobaoInterPresenter.loginUserInfoCache.getString("mmNick", null);
            String addSiteUrl = "http://pub.alimama.com/common/site/generalize/guideAdd.json?name=" + URLEncoder.encode("代购", "UTF-8") + "&categoryId=24&_tb_token_=" + tbTokenCookie + "&account1=" + URLEncoder.encode(mmNick, "UTF-8");
            //RequestQueue volleyRq = Volley.newRequestQueue(this, new OkHttpStack(this));
            RequestQueue volleyRq = TaobaoInterPresenter.getVolleyRequestQueue();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, addSiteUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObj =null;
                            try{
                                jsonObj = JSONObject.parseObject(response.trim());
                            }catch(Exception e){
                                wsxxDisplay();
                                return;
                            }
                            if(jsonObj.getBooleanValue("ok") && jsonObj.getJSONObject("info")!=null && jsonObj.getJSONObject("info").getBooleanValue("ok")){
                                //新增成功，调用新增渠道的方法
                                addChannel(cachedFanliInfo);
                            }else{
                                wsxxDisplay();
                            }
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            wsxxDisplay();
                        }
                    });

            // 把这个请求加入请求队列
            stringRequest.setTag(VOLLEY_TAG_NAME);
            volleyRq.add(stringRequest);
        }catch(Exception e){
            wsxxDisplay();
        }
    }

    public void addChannel(final CachedFanliInfo cachedFanliInfo){
        try{
            PersistentCookieStore s = new PersistentCookieStore(MyApplication.getContext());
            URI uri = URI.create("http://www.alimama.com");
            List<HttpCookie> listAlimamaCookies = s.get(uri);
            String tbTokenCookie = null;
            if(listAlimamaCookies!=null && listAlimamaCookies.size()>0){
                for(HttpCookie c : listAlimamaCookies){
                    if(c.getName().equals("_tb_token_")){
                        tbTokenCookie = c.getValue();
                    }
                }
            }

            //新增渠道
            String addChannelUrl = "http://pub.alimama.com/common/channel/channelSave.json?act=new&channelName=" + URLEncoder.encode("代购", "UTF-8") + "&selectAdzoneIds=&_tb_token_=" + tbTokenCookie;
            //RequestQueue volleyRq = Volley.newRequestQueue(this, new OkHttpStack(this));
            RequestQueue volleyRq = TaobaoInterPresenter.getVolleyRequestQueue();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, addChannelUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObj =null;
                            try{
                                jsonObj = JSONObject.parseObject(response.trim());
                            }catch(Exception e){
                                wsxxDisplay();
                                return;
                            }
                            if(jsonObj.getBooleanValue("ok") && jsonObj.getJSONObject("info")!=null && jsonObj.getJSONObject("info").getBooleanValue("ok")){
                                getChannelAdzoneInfo(cachedFanliInfo);
                            }else{
                                wsxxDisplay();
                            }
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            wsxxDisplay();
                        }
                    });

            // 把这个请求加入请求队列
            stringRequest.setTag(VOLLEY_TAG_NAME);
            volleyRq.add(stringRequest);
        }catch(Exception e){
            wsxxDisplay();
        }
    }

    public void addAdZone(Long siteId, Long channelId){
        try{
            PersistentCookieStore s = new PersistentCookieStore(MyApplication.getContext());
            URI uri = URI.create("http://www.alimama.com");
            List<HttpCookie> listAlimamaCookies = s.get(uri);
            String tbTokenCookie = null;
            if(listAlimamaCookies!=null && listAlimamaCookies.size()>0){
                for(HttpCookie c : listAlimamaCookies){
                    if(c.getName().equals("_tb_token_")){
                        tbTokenCookie = c.getValue();
                    }
                }
            }

            final Long siteId2 = siteId;
            final Long channelId2 = channelId;

            //新增广告位
            //  String addAdZoneUrl = "http://pub.alimama.com/common/adzone/selfAdzoneCreate.json?promotion_type=29" + URLEncoder.encode("#", "UTF-8") + "29&gcid=8&siteid=" + siteId + "&selectact=add&newadzonename=" + URLEncoder.encode("代购", "UTF-8") + "&channelIds=" + channelId + "&_tb_token_=" + tbTokenCookie;
            String addAdZoneUrl = "http://pub.alimama.com/common/adzone/selfAdzoneCreate.json?tag=29&gcid=8&siteid=" + siteId + "&selectact=add&newadzonename=" + URLEncoder.encode("代购", "UTF-8") + "&channelIds=" + channelId + "&_tb_token_=" + tbTokenCookie;
            //RequestQueue volleyRq = Volley.newRequestQueue(this, new OkHttpStack(this));
            RequestQueue volleyRq = TaobaoInterPresenter.getVolleyRequestQueue();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, addAdZoneUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObj =null;
                            try{
                                jsonObj = JSONObject.parseObject(response.trim());
                            }catch(Exception e){
                                wsxxDisplay();
                                return;
                            }
                            if(jsonObj.getBooleanValue("ok") && jsonObj.getJSONObject("info")!=null && jsonObj.getJSONObject("info").getBooleanValue("ok")){
                                //新增成功，则新增鹊桥的广告位
                                addQueqiaoAdZone(siteId2, channelId2);
                            }else{
                                wsxxDisplay();
                            }
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            wsxxDisplay();
                        }
                    });

            // 把这个请求加入请求队列
            stringRequest.setTag(VOLLEY_TAG_NAME);
            volleyRq.add(stringRequest);
        }catch(Exception e){
            wsxxDisplay();
        }
    }

    public void addQueqiaoAdZone(Long siteId, Long channelId){
        try{
            PersistentCookieStore s = new PersistentCookieStore(MyApplication.getContext());
            URI uri = URI.create("http://www.alimama.com");
            List<HttpCookie> listAlimamaCookies = s.get(uri);
            String tbTokenCookie = null;
            if(listAlimamaCookies!=null && listAlimamaCookies.size()>0){
                for(HttpCookie c : listAlimamaCookies){
                    if(c.getName().equals("_tb_token_")){
                        tbTokenCookie = c.getValue();
                    }
                }
            }

            //新增鹊桥广告位
            String addAdZoneUrl = "http://pub.alimama.com/common/adzone/selfAdzoneCreate.json?promotion_type=59" + URLEncoder.encode("#", "UTF-8") + "59&gcid=8&siteid=" + siteId + "&selectact=add&newadzonename=" + URLEncoder.encode("代购_鹊桥", "UTF-8") + "&channelIds=" + channelId + "&_tb_token_=" + tbTokenCookie;
            //RequestQueue volleyRq = Volley.newRequestQueue(this, new OkHttpStack(this));
            RequestQueue volleyRq = TaobaoInterPresenter.getVolleyRequestQueue();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, addAdZoneUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObj = JSONObject.parseObject(response);
                            if(jsonObj.getBooleanValue("ok") && jsonObj.getJSONObject("info")!=null && jsonObj.getJSONObject("info").getBooleanValue("ok")){
                                Long reflashId = currentItemId;
                                currentItemId = 0l;
                                currentItemIsGaofan = 0;
                                if(myWebView != null) {
                                    myWebView.loadUrl("http://h5.m.taobao.com/awp/core/detail.htm?id=" + reflashId);
                                }
                            }else{
                                wsxxDisplay();
                            }
                        }},
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // 出错了怎么办，显示通信失败信息
                            wsxxDisplay();
                        }
                    });

            // 把这个请求加入请求队列
            stringRequest.setTag(VOLLEY_TAG_NAME);
            volleyRq.add(stringRequest);
        }catch(Exception e){

        }
    }

    private void wsxxDisplay(){
        JSONObject jsonObjShowReflash = new JSONObject();
        jsonObjShowReflash.put("tag", 1);
        showFlowBtn(jsonObjShowReflash.toJSONString());
        showCurrentItemUrl();
    }

    private void reflashFanliDisplay(){
        JSONObject jsonObjShowReflash = new JSONObject();
        jsonObjShowReflash.put("tag", 5);
        showFlowBtn(jsonObjShowReflash.toJSONString());
        showCurrentItemUrl();
    }

    private void showCurrentItemUrl(){
        if(currentItemUrl!=null && !"".equals(currentItemUrl)){
            String url = currentItemUrl;
            int jinIndex = url.indexOf("#");
            if(jinIndex>0){
                url = url.substring(0, jinIndex) + "&fqbb=1" + url.substring(jinIndex);
            }else{
                url = url+"&fqbb=1";
            }
            if(myWebView!=null){
                myWebView.loadUrl(url);
            }
        }
    }

    public void cleanCurrentItemId(){
        this.currentItemId = 0l;
        this.currentItemIsGaofan = 0;
        this.currentItemUrl = null;
    }

    public static Map<String, String> getParamsMapByUrlStr(String url) {
        String queryStr = null;

        if(url==null || "".equals(url.trim())){
            return new HashMap<String, String>();
        }

        int whIndex = url.indexOf("?");
        if(whIndex>=0){
            queryStr = url.substring(whIndex+1);
        }

        return getParamsMapByQueryStr(queryStr);
    }

    public static Map<String, String> getParamsMapByQueryStr(String queryStr) {
        Map<String, String> paramsMap = new HashMap<String, String>();

        if(queryStr==null || "".equals(queryStr.trim())){
            return paramsMap;
        }

        int i = queryStr.indexOf("#");
        if(i>0){
            queryStr = queryStr.substring(0, i);
        }

        if(queryStr!=null && !"".equals(queryStr.trim())){
            String[] paramsArr = queryStr.split("&");
            if(paramsArr!=null && paramsArr.length>0){
                for(String oneParamPair : paramsArr){
                    if(oneParamPair!=null && !"".equals(oneParamPair.trim())){
                        String[] oneParamKv = oneParamPair.split("=");
                        if(oneParamKv!=null && oneParamKv.length==2){
                            paramsMap.put(oneParamKv[0].trim(), oneParamKv[1].trim());
                        }else if(oneParamKv!=null && oneParamKv.length==1){
                            paramsMap.put(oneParamKv[0].trim(), "");
                        }
                    }
                }
            }
        }

        return paramsMap;
    }

    class CachedFanliInfo{

        Float gfFee;

        Float gfRate;

        Float fxFee;

        Float fxRate;

        public CachedFanliInfo(Float fxFee, Float fxRate, Float gfFee, Float gfRate) {
            this.fxFee = fxFee;
            this.fxRate = fxRate;
            this.gfFee = gfFee;
            this.gfRate = gfRate;
        }

        public Float getFxFee() {
            return fxFee;
        }

        public void setFxFee(Float fxFee) {
            this.fxFee = fxFee;
        }

        public Float getFxRate() {
            return fxRate;
        }

        public void setFxRate(Float fxRate) {
            this.fxRate = fxRate;
        }

        public Float getGfFee() {
            return gfFee;
        }

        public void setGfFee(Float gfFee) {
            this.gfFee = gfFee;
        }

        public Float getGfRate() {
            return gfRate;
        }

        public void setGfRate(Float gfRate) {
            this.gfRate = gfRate;
        }
    }


}
