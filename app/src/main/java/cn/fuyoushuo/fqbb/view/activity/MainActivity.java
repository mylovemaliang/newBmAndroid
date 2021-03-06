package cn.fuyoushuo.fqbb.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.commonlib.utils.LocalStatisticConstants;
import cn.fuyoushuo.fqbb.commonlib.utils.RxBus;
import cn.fuyoushuo.fqbb.ext.LocalStatisticInfo;
import cn.fuyoushuo.fqbb.presenter.impl.AutoFanliPresenter;
import cn.fuyoushuo.fqbb.presenter.impl.LocalLoginPresent;
import cn.fuyoushuo.fqbb.presenter.impl.TaobaoInterPresenter;
import cn.fuyoushuo.fqbb.view.Layout.AppUpdateView;
import cn.fuyoushuo.fqbb.view.flagment.AlimamaLoginDialogFragment;
import cn.fuyoushuo.fqbb.view.flagment.BindEmailDialogFragment;
import cn.fuyoushuo.fqbb.view.flagment.JdWebviewDialogFragment;
import cn.fuyoushuo.fqbb.view.flagment.JxspDetailDialogFragment;
import cn.fuyoushuo.fqbb.view.flagment.MainFlagment;
import cn.fuyoushuo.fqbb.view.flagment.MainTipDialogFragment;
import cn.fuyoushuo.fqbb.view.flagment.MyOrderFragment;
import cn.fuyoushuo.fqbb.view.flagment.SelectedGoodFragment;
import cn.fuyoushuo.fqbb.view.flagment.SilentLoginTbFragment;
import cn.fuyoushuo.fqbb.view.flagment.TbWvDialogFragment;
import cn.fuyoushuo.fqbb.view.flagment.TixianFlagment;
import cn.fuyoushuo.fqbb.view.flagment.UnbindEmailDialogFragment;
import cn.fuyoushuo.fqbb.view.flagment.UpdatePasswordDialogFragment;
import cn.fuyoushuo.fqbb.view.flagment.UserCenterFragment;
import cn.fuyoushuo.fqbb.view.flagment.zhifubao.BindZfbDialogFragment;
import cn.fuyoushuo.fqbb.view.flagment.zhifubao.UpdateZfbDialogFragment;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends BaseActivity {

    public static final String VOLLEY_TAG_NAME = "main_activity";

    @Bind(R.id.bottomRadioGroupLayout)
    public LinearLayout bottomRgLayout;

    @Bind(R.id.bottomRg)
    RadioGroup menuGr;

    @Bind(R.id.rbHome)
    RadioButton mainButton;

    @Bind(R.id.rb_myorder)
    RadioButton myOrderButton;

    @Bind(R.id.rbjxsc)
    RadioButton jxscButton;

    @Bind(R.id.rb_user_center)
    RadioButton ucButton;

//    @Bind(R.id.drawerLayout)
//    SafeDrawerLayout drawerLayout;

//    @Bind(R.id.rightMenu)
//    RelativeLayout drawerMenuContent;

//    @Bind(R.id.rmMyTaobaoLo)
//    RelativeLayout rightMenuMyTaobao;
//
//    @Bind(R.id.rmFirstpageLo)
//    RelativeLayout rightMenuMyFirstpage;
//
//    @Bind(R.id.rmTixianLo)
//    RelativeLayout rightMenuTixian;
//
//    /*@Bind(R.id.rmMyjfbLo)
//    RelativeLayout rightMenuMyJfb;*/
//
//    @Bind(R.id.rmHelpLo)
//    RelativeLayout rightMenuHelp;
//
//    @Bind(R.id.rmMyOrderLo)
//    RelativeLayout rightMenuMyOrder;
//
//    @Bind(R.id.rmConfigLo)
//    RelativeLayout rightMenuConfig;

    List<Fragment> fragmentList;

    //管理的flagments
    MainFlagment mainFlagment;

    SelectedGoodFragment selectedGoodFragment;

    MyOrderFragment myOrderFlagment;

    UserCenterFragment userCenterFragment;

    SilentLoginTbFragment silentLoginTbFragment;

    int currentShowBizPage = 0;  //0  首页    1 精选商品    2 我的订单    3 个人中心

    int preShowBizPage;

    private CompositeSubscription mSubscriptions;

    private FragmentManager fragmentManager;

    private final int MAIN_FRAGMENT_INDEX = 0;

    private final int JXSC_FRAGMENT_INDEX = 1;

    private final int MYORDER_FRAGMENT_INDEX = 2;

    private final int USER_CENTER_INDEX = 3;

    private Fragment mContent;

    private LocalLoginPresent localLoginPresent;

    private SharedPreferences tipSharedPreferences;

    public static final String TIP_IMAGE = "tip_image_info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        mSubscriptions = new CompositeSubscription();
        fragmentManager = getSupportFragmentManager();
        localLoginPresent = new LocalLoginPresent();
        tipSharedPreferences = MyApplication.getContext().getSharedPreferences(TIP_IMAGE,Activity.MODE_PRIVATE);
        initButtonClick();
        initView();
        initBusEventListen();
        initAutoFanli();
        getUpdateInfo(true);
    }

    public void getUpdateInfo(boolean b){
        AppUpdateView updateView = new AppUpdateView(this);
        updateView.getUpdateInfo(b);
    }

    //呈现提示
    public void showTipImage(){
        if(!tipSharedPreferences.contains("isImageTip") || !tipSharedPreferences.getBoolean("isImageTip",false)){
           MainTipDialogFragment.newInstance().show(getSupportFragmentManager(),"MainTipDialogFragment");
        }
    }

    //确认已经提示过
    public void quireTipImage(){
        SharedPreferences.Editor edit = tipSharedPreferences.edit();
        edit.putBoolean("isImageTip",true);
        edit.commit();
    }

    public void initAutoFanli(){
        AutoFanliPresenter.initAutoFanli();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //什么都不做,防止FRAGMENT重叠
        Log.d("saveInstanceState:","yes");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processIntent();

        boolean toFirstPage = intent.getBooleanExtra("toFirstPage", false);
        if(toFirstPage){
            mainButton.setChecked(true);
        }else if(intent.getBooleanExtra("toOrderPage", false)){
            myOrderButton.setChecked(true);
        }
        //处理回调的任务
        String bizCallback = intent.getStringExtra("bizCallBack");
        bizCallback = bizCallback==null ? "" : bizCallback;

        if("MainToUc".equals(bizCallback)){
            changeView(USER_CENTER_INDEX);
            ucButton.setChecked(true);
        }
        else if("MainToLocalOrder".equals(bizCallback)){
            myOrderFlagment.reflashLocalOrder();
        }
        else if("MainToJdWv".equals(bizCallback)){
            Fragment jdWebviewDialogFragment = getSupportFragmentManager().findFragmentByTag("JdWebviewDialogFragment");
            if(jdWebviewDialogFragment != null) {
                ((JdWebviewDialogFragment)jdWebviewDialogFragment).reloadGoodPage();
            }
        }
    }

    private void processIntent(){
        //判断是否直接转发到详情页
        if(currentShowBizPage==0){
            changeView(MAIN_FRAGMENT_INDEX);
            mainButton.setChecked(true);
        }
        else if(currentShowBizPage == 1){
            changeView(JXSC_FRAGMENT_INDEX);
            jxscButton.setChecked(true);
        }
        else if(currentShowBizPage==2){
            changeView(MYORDER_FRAGMENT_INDEX);
            myOrderButton.setChecked(true);
        }
        else if(currentShowBizPage==3){
           changeView(USER_CENTER_INDEX);
           ucButton.setChecked(true);
        }
    }

    //每一小时触发一次
    private void initAutoTbLoginListen(){
        mSubscriptions.add(rx.Observable.interval(0,60,TimeUnit.MINUTES)
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe(new Subscriber<Long>() {
                 @Override
                 public void onCompleted() {

                 }

                 @Override
                 public void onError(Throwable e) {

                 }

                 @Override
                 public void onNext(Long aLong) {
                     TaobaoInterPresenter.judgeAlimamaLogin(new TaobaoInterPresenter.LoginCallback() {
                         @Override
                         public void hasLoginCallback() {
                            return;
                         }

                         @Override
                         public void nologinCallback() {
                             if(silentLoginTbFragment != null){
                                 silentLoginTbFragment.autoLogin();
                             }
                         }

                         @Override
                         public void judgeErrorCallback() {
                              return;
                         }
                     },VOLLEY_TAG_NAME);
                 }
             }));
    }

    private void initBusEventListen(){
        mSubscriptions.add(RxBus.getInstance().toObserverable().compose(this.<RxBus.BusEvent>bindToLifecycle()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<RxBus.BusEvent>() {

            @Override
            public void onCompleted() {
                 return;
            }

            @Override
            public void onError(Throwable e) {
                  //默认的异常拦截
                  return;
            }

            @Override
            public void onNext(RxBus.BusEvent busEvent) {
                if(busEvent instanceof MainFlagment.SearchTextClickEvent){
                    // TODO: 2016/7/5
                    Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.putExtra("intentFromMain",true);
                    startActivity(intent);
                }
                else if(busEvent instanceof AlimamaLoginDialogFragment.AlimamaLoginToUserCenterEvent){
                    // TODO: 2016/11/2
                    userCenterFragment.refreshUserInfo();
                }
                else if(busEvent instanceof UserCenterFragment.LogoutToMainEvent){
                    userCenterFragment.refreshUserInfo();
//                    changeView(MAIN_FRAGMENT_INDEX);
//                    currentShowBizPage = MAIN_FRAGMENT_INDEX;
//                    mainButton.setChecked(true);
                }
                else if(busEvent instanceof JxspDetailDialogFragment.JxscToGoodInfoEvent){
                    JxspDetailDialogFragment.JxscToGoodInfoEvent event = (JxspDetailDialogFragment.JxscToGoodInfoEvent) busEvent;
                    /*Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                      intent.putExtra("goodUrl",event.getGoodUrl());*/
                    //Intent intent = new Intent(MainActivity.this, WebviewActivity.class);
                    //intent.putExtra("bizString","tbGoodDetail");
                    //intent.putExtra("loadUrl",event.getGoodUrl());
                    //intent.putExtra("forSearchGoodInfo",false);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    TbWvDialogFragment.newInstance("tbGoodDetail",event.getGoodUrl(),false).show(getSupportFragmentManager(),"TbWvDialogFragment");
                    //startActivity(intent);
                }
                else if(busEvent instanceof BindEmailDialogFragment.AfterBindEmailSuccessEvent){
                    userCenterFragment.refreshUserInfo();
                }
                else if(busEvent instanceof UnbindEmailDialogFragment.AfterUnbindEmailSuccEvent){
                    userCenterFragment.refreshUserInfo();
                }
                else if(busEvent instanceof BindZfbDialogFragment.AfterBindAlipaySuccEvent){
                    userCenterFragment.refreshUserInfo();
                }
                else if(busEvent instanceof UpdateZfbDialogFragment.AfterUpdateAlipaySuccEvent){
                    userCenterFragment.refreshUserInfo();
                }
                else if(busEvent instanceof UpdatePasswordDialogFragment.AfterUpdatePasswordSuccEvent){
                    userCenterFragment.refreshUserInfo();
                }
                else if(busEvent instanceof AlimamaLoginDialogFragment.AlimamaLoginToTbOrderEvent){
                    myOrderFlagment.reflashTbOrder();
                }
                else if(busEvent instanceof AlimamaLoginDialogFragment.AlimamaLoginToTixianEvent){
                    Fragment tixianFlagment = getSupportFragmentManager().findFragmentByTag("TixianFlagment");
                    if(tixianFlagment != null && !tixianFlagment.isDetached()){
                        ((TixianFlagment)tixianFlagment).loadWebviewPage();
                    }
                }
                else if(busEvent instanceof MainTipDialogFragment.MainTipCompleteEvent){
                    quireTipImage();
                }
            }

        }));
    }

    private void initView() {
        fragmentList = new ArrayList<Fragment>();

        mainFlagment = MainFlagment.newInstance();
        userCenterFragment = UserCenterFragment.newInstance();
        myOrderFlagment = MyOrderFragment.newInstance();
        //myJifenFlagment = new MyJifenFlagment();
        selectedGoodFragment = SelectedGoodFragment.newInstance();
        silentLoginTbFragment = SilentLoginTbFragment.newInstance();

        fragmentList.add(mainFlagment);
        fragmentList.add(selectedGoodFragment);
        fragmentList.add(myOrderFlagment);
        fragmentList.add(userCenterFragment);

        //初始化flagment
        fragmentManager = getSupportFragmentManager();
        mContent = mainFlagment;
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_area,mainFlagment).show(mainFlagment);
        fragmentTransaction.add(R.id.main_area,silentLoginTbFragment).hide(silentLoginTbFragment);
//        fragmentTransaction.add(R.id.main_area,selectedGoodFragment).hide(selectedGoodFragment);
//        fragmentTransaction.add(R.id.main_area,myOrderFlagment).hide(myOrderFlagment);
//        fragmentTransaction.add(R.id.main_area,userCenterFragment).hide(userCenterFragment);

        fragmentTransaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
        currentShowBizPage = 0;
        processIntent();
    }

    //转换flagment
    public void switchContent(Fragment from,Fragment to){
        if (mContent != to) {
            mContent = to;
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (!to.isAdded()) {    // 先判断是否被add过
                transaction.hide(from).add(R.id.main_area, to).commitAllowingStateLoss(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
                transaction.hide(from).show(to).commitAllowingStateLoss(); // 隐藏当前的fragment，显示下一个
            }
            fragmentManager.executePendingTransactions();
        }
    }

    //手动设置VIEWPAGE要显示的视图
    private void changeView(int desTab){
         switchContent(mContent,fragmentList.get(desTab));
    }

    private void initButtonClick() {
        menuGr.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbHome:
                          LocalStatisticInfo.getIntance().onClickPage(LocalStatisticConstants.MAIN_PAGE);
                          currentShowBizPage = MAIN_FRAGMENT_INDEX;
                          changeView(MAIN_FRAGMENT_INDEX);
                        break;

                    case R.id.rb_myorder:
//                        drawerLayout.closeDrawer(drawerMenuContent);
//                        preShowBizPage = currentShowBizPage;
                        currentShowBizPage = MYORDER_FRAGMENT_INDEX;
                        changeView(MYORDER_FRAGMENT_INDEX);
                        break;

                 case R.id.rb_user_center:
                       LocalStatisticInfo.getIntance().onClickPage(LocalStatisticConstants.USER_CENTER);
                       changeView(USER_CENTER_INDEX);
                       currentShowBizPage = USER_CENTER_INDEX;
//                      localLoginPresent.isFqbbLocalLogin(new LocalLoginPresent.LoginCallBack() {
//                          @Override
//                          public void localLoginSuccess() {
//                              changeView(USER_CENTER_INDEX);
//                              //userCenterFragment.refreshUserInfo();
//                              currentShowBizPage = USER_CENTER_INDEX;
//                          }
//
//                          @Override
//                          public void localLoginFail() {
//                               Intent intent = new Intent(MainActivity.this,UserLoginActivity.class);
//                               intent.putExtra("biz","MainToUc");
//                               startActivity(intent);
//                          }
//                      });
                      break;

                    case R.id.rbjxsc:
                        LocalStatisticInfo.getIntance().onClickPage(LocalStatisticConstants.JXSC);
                        currentShowBizPage = JXSC_FRAGMENT_INDEX;
                        changeView(JXSC_FRAGMENT_INDEX);
                        break;

                    default:
                        break;
                }
            }
        });

//        RxView.clicks(rightAreaButton).throttleFirst(500, TimeUnit.MILLISECONDS)
//                .compose(this.<Void>bindToLifecycle())
//                .subscribe(new Action1<Void>() {
//            @Override
//            public void call(Void aVoid) {
//                drawerLayout.openDrawer(drawerMenuContent);
//            }
//        });
//        RxView.clicks(rightMenuMyFirstpage).throttleFirst(1000, TimeUnit.MILLISECONDS)
//                .compose(this.<Void>bindToLifecycle())
//                .subscribe(new Action1<Void>() {
//            @Override
//            public void call(Void aVoid) {
//                drawerLayout.closeDrawer(drawerMenuContent);
//
//                preShowBizPage = currentShowBizPage;
//                currentShowBizPage = 0;
//
//                if(!mainButton.isChecked()){
//                    mainButton.setChecked(true);
//                }else{
//                    changeView(MAIN_FRAGMENT_INDEX);
//                }
//                bottomRgLayout.setVisibility(View.VISIBLE);
//            }
//        });
//        RxView.clicks(rightMenuMyTaobao).throttleFirst(1000,TimeUnit.MILLISECONDS)
//                .compose(this.<Void>bindToLifecycle())
//                .subscribe(new Action1<Void>() {
//            @Override
//            public void call(Void aVoid) {
//                //统计侧边栏我的淘宝按钮
//                MobclickAgent.onEvent(MainActivity.this, EventIdConstants.SLIDER_MY_TAOBAO_BTN);
//                drawerLayout.closeDrawer(drawerMenuContent);
//                showMyTaobaoPage();
//            }
//        });
//        RxView.clicks(rightMenuMyOrder).throttleFirst(1000,TimeUnit.MILLISECONDS)
//                .compose(this.<Void>bindToLifecycle())
//                .subscribe(new Action1<Void>() {
//            @Override
//            public void call(Void aVoid) {
//                drawerLayout.closeDrawer(drawerMenuContent);
//                if(currentShowBizPage!=1){
//                    clickMyOrder();
//                    //bottomRgLayout.setVisibility(View.VISIBLE);
//                }
//            }
//        });
        /*RxView.clicks(rightMenuMyJfb).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                drawerLayout.closeDrawer(drawerMenuContent);
                if(currentShowBizPage!=2){
                    clickMyJfb();
                    //bottomRgLayout.setVisibility(View.VISIBLE);
                }
            }
        });*/
//        RxView.clicks(rightMenuTixian).throttleFirst(1000,TimeUnit.MILLISECONDS)
//                .compose(this.<Void>bindToLifecycle()).subscribe(new Action1<Void>() {
//                    @Override
//                    public void call(Void aVoid) {
//                        drawerLayout.closeDrawer(drawerMenuContent);
//                        if(currentShowBizPage!=3){
//                            clickTixian();
//                            //bottomRgLayout.setVisibility(View.VISIBLE);
//                        }
//                    }
//        });
//        RxView.clicks(rightMenuHelp).throttleFirst(1000,TimeUnit.MILLISECONDS)
//                .compose(this.<Void>bindToLifecycle())
//                .subscribe(new Action1<Void>() {
//                    @Override
//                    public void call(Void aVoid) {
//                        //统计侧边栏使用帮助
//                        MobclickAgent.onEvent(MainActivity.this, EventIdConstants.SLIDER_USERHELPER_FAQ_BTN);
//                        drawerLayout.closeDrawer(drawerMenuContent);
//                        Intent intent = new Intent(MainActivity.this,HelpActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                        startActivity(intent);
//                    }
//        });
//        RxView.clicks(rightMenuConfig).throttleFirst(500, TimeUnit.MILLISECONDS)
//                .compose(this.<Void>bindToLifecycle())
//                .subscribe(new Action1<Void>() {
//                    @Override
//                    public void call(Void aVoid) {
//                        drawerLayout.closeDrawer(drawerMenuContent);
//                        Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                        startActivity(intent);
//                    }
//        });
    }

    public void clickMyOrder(){
        myOrderButton.setChecked(true);
    }

    /*public void clickMyJfb(){
        jifenButton.setChecked(true);
    }*/

    public void clickTixian(){
        //tixianButton.setChecked(true);
    }

//    public void showMyTaobaoPage(){
//        Intent intent = new Intent(MainActivity.this, WebviewActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        intent.putExtra("loadUrl", "https://h5.m.taobao.com/mlapp/mytaobao.html#mlapp-mytaobao");
//        intent.putExtra("forSearchGoodInfo", false);
//        intent.putExtra("bizString","myTaoBao");
//        startActivity(intent);
//    }


    @Override
    protected void onStart() {
        super.onStart();
        //自动登录
        showTipImage();
        initAutoTbLoginListen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TaobaoInterPresenter.cancelTagedRuquests(VOLLEY_TAG_NAME);
        if(localLoginPresent != null){
            localLoginPresent.onDestroy();
        }
        if(mSubscriptions.hasSubscriptions()){
            mSubscriptions.unsubscribe();
        }
    }

    private long exitTime = 0;

    @Override
    public void onBackPressed() {
        exit();
    }


    //所有Activity退出
    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finishAll();
        }
    }

    public void finishAll(){
        MyApplication.getMyapplication().finishAllActivity();
        MyApplication.getMyapplication().finishProgram();
    }

   //--------------------------------------------统计---------------------------------------------


}
