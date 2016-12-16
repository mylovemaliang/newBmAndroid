package cn.fuyoushuo.fqbb.view.flagment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.text.TextDirectionHeuristicCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.trello.rxlifecycle.FragmentEvent;
import com.umeng.analytics.MobclickAgent;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.commonlib.utils.DateUtils;
import cn.fuyoushuo.fqbb.commonlib.utils.EventIdConstants;
import cn.fuyoushuo.fqbb.commonlib.utils.LocalStatisticConstants;
import cn.fuyoushuo.fqbb.commonlib.utils.PageSession;
import cn.fuyoushuo.fqbb.commonlib.utils.RxBus;
import cn.fuyoushuo.fqbb.ext.LocalStatisticInfo;
import cn.fuyoushuo.fqbb.presenter.impl.LocalLoginPresent;
import cn.fuyoushuo.fqbb.presenter.impl.UserCenterPresenter;
import cn.fuyoushuo.fqbb.view.activity.ConfigActivity;
import cn.fuyoushuo.fqbb.view.activity.HelpActivity;
import cn.fuyoushuo.fqbb.view.activity.PointMallActivity;
import cn.fuyoushuo.fqbb.view.activity.UserLoginActivity;
import cn.fuyoushuo.fqbb.view.flagment.pointsmall.PhoneRechargeDialogFragment;
import cn.fuyoushuo.fqbb.view.flagment.zhifubao.BindZfbDialogFragment;
import cn.fuyoushuo.fqbb.view.flagment.zhifubao.UpdateZfbDialogFragment;
import cn.fuyoushuo.fqbb.view.view.UserCenterView;
import rx.functions.Action1;

/**
 * Created by QA on 2016/10/27.
 */
public class UserCenterFragment extends BaseFragment implements UserCenterView{


    private UserCenterPresenter userCenterPresenter;

    @Bind(R.id.user_center_account)
    TextView accountView;

    @Bind(R.id.userinfo_currentpoints_value)
    TextView currentPoints;

    @Bind(R.id.userinfo_freezepoints_value)
    TextView freezePoints;

    @Bind(R.id.userinfo_useablepoints_value)
    TextView useablePoints;

    @Bind(R.id.userinfo_month_20day_value)
    TextView thisMonth20Count;

    @Bind(R.id.userinfo_nextmonth_20day_value)
    TextView nextMonth20Count;

    @Bind(R.id.userinfo_useable_money_value)
    TextView useableCount;

    @Bind(R.id.user_center_alimama_login)
    View alimamaLogin;

    @Bind(R.id.alimama_login_icon)
    TextView loginText;

    @Bind(R.id.user_center_refreshview)
    SwipeRefreshLayout userCenterRefreshView;

    @Bind(R.id.user_center_points_icon)
    RelativeLayout pointsIcon;

    @Bind(R.id.user_center_help_icon)
    RelativeLayout helpIcon;

    @Bind(R.id.user_center_balance_icon)
    RelativeLayout tixianIcon;

    @Bind(R.id.logout_area)
    CardView logoutArea;

    @Bind(R.id.bind_email)
    TextView bindEmailView;

    @Bind(R.id.bind_zfb)
    TextView bindAlipay;

    @Bind(R.id.update_password)
    TextView updatePasswordView;

    @Bind(R.id.user_set)
    TextView userSetView;

    boolean isEmailBind = false;

    boolean isAlipayBind = false;

    private String phoneNum = "";

    private String email = "";

    private String alipayNo = "";

    private boolean isDataInit = false;

    private boolean isAccountClickable = false;

    private boolean isAliPayClickable = false;

    private boolean isLocalLogin = false;

    private PageSession pageSession;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return super.onCreateView(inflater,container,savedInstanceState);
    }

    @Override
    protected String getPageName() {
        return "userCenterPage";
    }

    @Override
    protected int getRootLayoutId() {
        return R.layout.fragment_user_center;
    }

    @Override
    protected void initView() {


        RxView.clicks(alimamaLogin).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        AlimamaLoginDialogFragment.newInstance(AlimamaLoginDialogFragment.FROM_USER_CENTER)
                                .show(getFragmentManager(),"AlimamaLoginDialogFragment");
                    }
                });

        userCenterRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                 userCenterRefreshView.setRefreshing(true);
                 refreshUserInfo();

            }
        });
        //积分商城
        RxView.clicks(pointsIcon).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                         if(isLocalLogin){
                           Intent intent = new Intent(mactivity, PointMallActivity.class);
                           startActivity(intent);
                         }else{
                           showLocalLoginDialog();
                         }
                    }
                });
        //帮助中心
        RxView.clicks(helpIcon).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        Intent intent = new Intent(mactivity,HelpActivity.class);
                        startActivity(intent);
                    }
                });
        //余额提现
        RxView.clicks(tixianIcon).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        //提现页面
                        MobclickAgent.onEvent(MyApplication.getContext(), EventIdConstants.USER_TIXIAN_BTN);
                        TixianFlagment.newInstance().show(getFragmentManager(),"TixianFlagment");
                    }
                });


        RxView.clicks(logoutArea).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                          userCenterPresenter.logout();
                    }
                });

        bindEmailView.setText(Html.fromHtml("绑定邮箱<font color=\"#ff0000\">(强烈建议,方便找回账号)</font>"));

        RxView.clicks(bindEmailView).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(!isLocalLogin){
                            showLocalLoginDialog();
                            return;
                        }
                        // TODO: 2016/11/9 绑定邮箱逻辑
                        if(!isEmailBind){
                          BindEmailDialogFragment.newInstance().show(getFragmentManager(),"BindEmailDialogFragment");
                        }else{
                          UnbindEmailDialogFragment.newInstance(phoneNum,email).show(getFragmentManager(),"UnbindEmailDialogFragment");
                        }
                    }
                });

        RxView.clicks(bindAlipay).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(!isLocalLogin){
                            showLocalLoginDialog();
                            return;
                        }
                        // TODO: 2016/11/9 绑定邮箱逻辑
                        if(!isAlipayBind){
                            BindZfbDialogFragment.newInstance().show(getFragmentManager(),"BindZfbDialogFragment");
                        }else{
                            UpdateZfbDialogFragment.newInstance(alipayNo).show(getFragmentManager(),"UpdateZfbDialogFragment");
                        }
                    }
                });

        RxView.clicks(updatePasswordView).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(!isLocalLogin){
                            showLocalLoginDialog();
                            return;
                        }
                        // TODO: 2016/11/9 修改密码逻辑
                        UpdatePasswordDialogFragment.newInstance().show(getFragmentManager(),"UpdatePasswordDialogFragment");
                    }
                });

        RxView.clicks(userSetView).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        // TODO: 2016/11/9 用户设置逻辑
                        Intent intent = new Intent(getActivity(), ConfigActivity.class);
                        startActivity(intent);
                    }
                });

        RxView.clicks(accountView).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(!isAccountClickable) return;
                        // TODO: 2016/11/9 账号点击功能
                        Intent intent = new Intent(getActivity(), UserLoginActivity.class);
                        intent.putExtra("biz","MainToUc");
                        startActivity(intent);
                    }
                });

        RxView.clicks(useableCount).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(!isAliPayClickable) return;
                        // TODO: 2016/11/9 阿里妈妈点击登录
                        AlimamaLoginDialogFragment.newInstance(AlimamaLoginDialogFragment.FROM_USER_CENTER)
                                .show(getFragmentManager(),"AlimamaLoginDialogFragment");
                    }
                });

    }

    @Override
    protected void initData() {
      pageSession = new PageSession(LocalStatisticConstants.USER_CENTER);
      userCenterPresenter = new UserCenterPresenter(this);
    }

    public static UserCenterFragment newInstance() {
        UserCenterFragment fragment = new UserCenterFragment();
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshUserInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userCenterPresenter.onDestroy();
    }

    //----------------------------------用于外部调用--------------------------------------------------

    public void refreshUserInfo(){
        if(!isDetched && userCenterPresenter != null){
          userCenterPresenter.getUserInfo();
          userCenterPresenter.getAlimamaInfo();
        }
    }

    //初始化字体图标
    private void initIconFront() {
        Typeface iconfont = Typeface.createFromAsset(getActivity().getAssets(), "iconfront/iconfont_alimamalogin.ttf");
        loginText.setTypeface(iconfont);
    }


 //--------------------------------------------统计相关-----------------------------------------------------

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            LocalStatisticInfo.getIntance().onPageStart(pageSession);
        }else{
            LocalStatisticInfo.getIntance().onPageEnd(pageSession);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!this.isHidden()){
            LocalStatisticInfo.getIntance().onPageStart(pageSession);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(!this.isHidden()){
            LocalStatisticInfo.getIntance().onPageEnd(pageSession);
        }
    }

    private void showLocalLoginDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("需要登录才能进行操作!");

        builder.setCancelable(true);
        builder.setPositiveButton("去登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity(), UserLoginActivity.class);
                intent.putExtra("biz", "MainToUc");
                startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.create().show();
    }


   //------------------------------------view 层回调--------------------------------------------------

    @Override
    public void onUserInfoGetError() {
        currentPoints.setText("--");
        freezePoints.setText("--");
        useablePoints.setText("--");
        accountView.setText("登录/注册");
        accountView.setTextColor(getResources().getColor(R.color.module_11));
        isAccountClickable = true;
        isLocalLogin = false;
        logoutArea.setVisibility(View.GONE);
        userCenterRefreshView.setRefreshing(false);
    }

    @Override
    public void onUserInfoGetSucc(JSONObject result) {
         if(result == null || result.isEmpty()) return;
         Integer validPoint = 0;
         Integer orderFreezePoint = 0;
         Integer convertFreezePoint = 0;
         String account = "";
         String email = "";
         String alipayNo = "";
         if(result.containsKey("validPoint")){
             validPoint = result.getIntValue("validPoint");
         }
         if(result.containsKey("orderFreezePoint")){
             orderFreezePoint =result.getIntValue("orderFreezePoint");
         }
         if(result.containsKey("convertFreezePoint")){
             convertFreezePoint = result.getIntValue("convertFreezePoint");
         }
         if(result.containsKey("account")){
             account = result.getString("account");
         }
         if(result.containsKey("email")){
              email = result.getString("email");
         }
         if(result.containsKey("alipay")){
              alipayNo = result.getString("alipay");
         }

         if(!TextUtils.isEmpty(email)){
             bindEmailView.setText("解绑邮箱");
             this.email = email;
             isEmailBind = true;
         }else{
             bindEmailView.setText(Html.fromHtml("绑定邮箱<font color=\"#ff0000\">(强烈建议,方便找回账号)</font>"));
             isEmailBind = false;
             this.email = "";
         }

         if(!TextUtils.isEmpty(alipayNo)){
             bindAlipay.setText("换绑支付宝");
             this.alipayNo = alipayNo;
             this.isAlipayBind = true;
         }else{
             bindAlipay.setText(Html.fromHtml("绑定支付宝<font color=\"#ff0000\">(强烈建议,方便积分提现)</font>"));
             isAlipayBind = false;
             this.alipayNo = "";
         }

         this.phoneNum = account;
         currentPoints.setText(String.valueOf(validPoint+orderFreezePoint+convertFreezePoint));
         freezePoints.setText(String.valueOf(orderFreezePoint+convertFreezePoint));
         useablePoints.setText(String.valueOf(validPoint));
         //账号信息的处理
         logoutArea.setVisibility(View.VISIBLE);
         accountView.setText(account);
         accountView.setTextColor(currentPoints.getCurrentTextColor());
         isAccountClickable = false;
         isLocalLogin = true;
         userCenterRefreshView.setRefreshing(false);
         isDataInit = true;
    }

//    @Override
//    public void onLoginFail() {
//        Intent intent = new Intent(mactivity, UserLoginActivity.class);
//        intent.putExtra("fromWhere","UserCenter");
//        startActivity(intent);
//    }

    @Override
    public void onAlimamaLoginFail() {
        //Toast.makeText(MyApplication.getContext(),"请稍后重新登录阿里妈妈",Toast.LENGTH_SHORT).show();
        //alimamaLogin.setVisibility(View.VISIBLE);
        thisMonth20Count.setText("--");
        nextMonth20Count.setText("--");
        useableCount.setText("登录查看");
        useableCount.setTextSize(15);
        useableCount.setTextColor(getResources().getColor(R.color.module_11));
        isAliPayClickable = true;
        userCenterRefreshView.setRefreshing(false);
    }

    @Override
    public void onAlimamaLoginSuccess(JSONObject result) {
       if(result != null && !result.isEmpty()){
           if(result.containsKey("lastMonthMoney")){
               thisMonth20Count.setText(result.getString("lastMonthMoney"));
           }
           if(result.containsKey("thisMonthMoney")){
               nextMonth20Count.setText(result.getString("thisMonthMoney"));
           }
           if(result.containsKey("currentMoney")){
               useableCount.setText(result.getString("currentMoney"));
               useableCount.setTextColor(nextMonth20Count.getCurrentTextColor());
               useableCount.setTextSize(20);
               isAliPayClickable = false;
           }
       }
       alimamaLogin.setVisibility(View.GONE);
       userCenterRefreshView.setRefreshing(false);
    }

    @Override
    public void onAlimamaLoginError() {
        Toast.makeText(MyApplication.getContext(),"请重新下拉刷新数据",Toast.LENGTH_SHORT).show();
        thisMonth20Count.setText("--");
        nextMonth20Count.setText("--");
        useableCount.setText("--");
        userCenterRefreshView.setRefreshing(false);
    }

    @Override
    public void onLogoutSuccess() {
        Toast.makeText(MyApplication.getContext(),"退出登录成功",Toast.LENGTH_SHORT).show();
        RxBus.getInstance().send(new LogoutToMainEvent());
    }

    @Override
    public void onLogoutFail() {
        Toast.makeText(MyApplication.getContext(),"退出登录失败,请重试",Toast.LENGTH_SHORT).show();
    }

    //-----------------------------总线事件----------------------------------------------------

    public class LogoutToMainEvent extends RxBus.BusEvent{}
}
