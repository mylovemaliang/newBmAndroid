package cn.fuyoushuo.fqbb.view.flagment.pointsmall;

import android.opengl.EGLDisplay;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.components.support.RxDialogFragment;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.commonlib.utils.DateUtils;
import cn.fuyoushuo.fqbb.commonlib.utils.LoginInfoStore;
import cn.fuyoushuo.fqbb.presenter.impl.LocalLoginPresent;
import cn.fuyoushuo.fqbb.presenter.impl.pointsmall.TixianPresent;
import cn.fuyoushuo.fqbb.view.view.pointsmall.TixianView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by QA on 2016/11/7.
 */
public class TixianDialogFragment extends RxDialogFragment implements TixianView{

    @Bind(R.id.points_tixian_backArea)
    RelativeLayout backArea;

    @Bind(R.id.tixian_info_useAblePoints_value)
    TextView userAblePointsView;

    @Bind(R.id.tixian_info_duihuan_value)
    TextView duihuanCashView;

    @Bind(R.id.tixian_info_duihuan_max_value)
    TextView maxDuihuanView;

    @Bind(R.id.tixian_cash_num)
    EditText tixianCashNumView;

    @Bind(R.id.verificate_value)
    EditText verificateValueView;

    @Bind(R.id.acquire_verification_button)
    Button verifiAcquireButton;

    @Bind(R.id.commit_button)
    Button commitButton;

    @Bind(R.id.tixian_tip_text)
    TextView tixianTipView;

    @Bind(R.id.points_tixian_refreshview)
    SwipeRefreshLayout swipeRefreshLayout;

    private String tixianCashString;

    private float tixianCashNum;

    private String verifiCode;

    //可用积分
    private Integer localValidPoints;

    private long time = 60l;

    private LocalLoginPresent localLoginPresent;

    private TixianPresent tixianPresent;

    //标识用户输入是否可行
    private boolean isCashAble = true;

    public static TixianDialogFragment newInstance() {
        Bundle args = new Bundle();
        TixianDialogFragment fragment = new TixianDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.fullScreenDialog);
        tixianCashString = "";
        tixianCashNum = 0.00f;
        verifiCode = "";
        localLoginPresent = new LocalLoginPresent();
        tixianPresent = new TixianPresent(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_pointsmall_tixian_dialog, container, false);
        ButterKnife.bind(this,inflate);

        return inflate;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initTipInfo();
        tixianCashNumView.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        tixianCashNumView.setFocusable(true);
        tixianCashNumView.requestFocus();
        tixianCashNumView.requestFocusFromTouch();

        RxView.clicks(backArea).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000,TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        dismissAllowingStateLoss();
                    }
                });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                    refreshUserInfo(new AfterAcquireUserInfoCallBack() {
                        @Override
                        public void onAcquireInfo() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
            }
        });

        RxTextView.textChangeEvents(tixianCashNumView).compose(this.<TextViewTextChangeEvent>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<TextViewTextChangeEvent>() {
                    @Override
                    public void call(TextViewTextChangeEvent textViewTextChangeEvent) {
                        CharSequence s = textViewTextChangeEvent.text();
                        isCashAble = true;
                        if(!TextUtils.isEmpty(s) && s.toString().contains(".")){
                            //如果点后面有超过三位数值,则删掉最后一位
                            int length=s.length()-s.toString().lastIndexOf(".");
                            if(length>=4){//说明后面有三位数值
                                isCashAble=false;
                            }else{
                                isCashAble=true;
                            }
                        }
                    }
                });

        RxTextView.afterTextChangeEvents(tixianCashNumView).compose(this.<TextViewAfterTextChangeEvent>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                 .subscribe(new Action1<TextViewAfterTextChangeEvent>() {
                     @Override
                     public void call(TextViewAfterTextChangeEvent textViewAfterTextChangeEvent) {
                         Editable editable = textViewAfterTextChangeEvent.editable();
                         if(!isCashAble){
                              tixianCashNumView.setText(editable.toString().substring(0, editable.toString().length()-1));
                              //光标强制到末尾
                              tixianCashNumView.setSelection(tixianCashNumView.getText().length());
                              isCashAble = true;
                          }
                          tixianCashString = tixianCashNumView.getText().toString();
                     }
                 });


        RxTextView.textChanges(verificateValueView).compose(this.<CharSequence>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                           verifiCode = charSequence.toString();
                    }
                });


        RxView.clicks(verifiAcquireButton).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                            timeForVerifiCode();
                            localLoginPresent.getVerifiCode(LoginInfoStore.getIntance().getUserAccount(),"phone_cash_apply", new LocalLoginPresent.VerifiCodeGetCallBack() {
                                @Override
                                public void onVerifiCodeGetSucc(String account) {
                                    Toast.makeText(MyApplication.getContext(),"验证码发送成功,请注意查收",Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                @Override
                                public void onVerifiCodeGetError(String msg) {
                                    Toast.makeText(MyApplication.getContext(),"验证码发送失败,请重试",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            });
                        }

                });

        //生成提现订单
        RxView.clicks(commitButton).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000,TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                       if(TextUtils.isEmpty(tixianCashString) || TextUtils.isEmpty(verifiCode)){
                          Toast.makeText(MyApplication.getContext(),"提现数额与验证码不能为空",Toast.LENGTH_SHORT).show();
                          return;
                       }
                       Float f1 = Float.valueOf(tixianCashString);
                       Float f2 = DateUtils.getFormatFloat(f1);
                       if(f2 < 50f){
                         Toast.makeText(MyApplication.getContext(),"提现数额不能小于50元",Toast.LENGTH_SHORT).show();
                         return;
                       }else{
                           // TODO: 2016/12/2 生成提现订单
                           tixianPresent.createCashOrder(f2,verifiCode);
                        }
                    }});
    }


    @Override
    public void onStart() {
        super.onStart();
        commitButton.setClickable(false);
        commitButton.setBackgroundColor(getResources().getColor(R.color.gray));
        refreshUserInfo(new AfterAcquireUserInfoCallBack() {
            @Override
            public void onAcquireInfo() {
                return;
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        if(localLoginPresent != null) {
            localLoginPresent.onDestroy();
        }
        if(tixianPresent != null){
            tixianPresent.onDestroy();
        }
    }


    public void refreshUserInfo(final AfterAcquireUserInfoCallBack afterAcquireUserInfoCallBack){
        if(localLoginPresent == null) return;
        localLoginPresent.getUserInfo(new LocalLoginPresent.UserInfoCallBack() {
            @Override
            public void onUserInfoGetSucc(JSONObject result) {
                 if(result == null || result.isEmpty()) return;
                 Integer validPoints = 0;
                 if(result.containsKey("validPoint")){
                     validPoints = result.getIntValue("validPoint");
                     localValidPoints = validPoints;
                 }
                 userAblePointsView.setText(String.valueOf(validPoints));
                 Float duihuanCash = DateUtils.getFormatFloat((float)validPoints / 100);
                 duihuanCashView.setText(String.valueOf(duihuanCash)+"元");
                 commitButton.setClickable(true);
                 commitButton.setBackgroundColor(getResources().getColor(R.color.module_11));
                 afterAcquireUserInfoCallBack.onAcquireInfo();
            }

            @Override
            public void onUserInfoGetError() {
                 Toast.makeText(MyApplication.getContext(),"用户信息获取失败,请下拉重试",Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void timeForVerifiCode() {
        verifiAcquireButton.setText("获取验证码(60)");
        verifiAcquireButton.setClickable(false);
        verifiAcquireButton.setBackgroundColor(getResources().getColor(R.color.gray));
        Observable.timer(1, TimeUnit.SECONDS).compose(this.<Long>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .repeat(60)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if(verifiAcquireButton == null) return;
                        time--;
                        if (time > 0) {
                            if (verifiAcquireButton != null){
                                verifiAcquireButton.setText("获取验证码(" + time + ")");
                            }
                        } else {
                            if(verifiAcquireButton != null){
                                verifiAcquireButton.setClickable(true);
                                verifiAcquireButton.setBackgroundColor(getResources().getColor(R.color.module_6));
                                verifiAcquireButton.setText("重新获取验证码");
                            }
                            time = 60l;
                        }
                    }
                });
    }

    private void initTipInfo(){
        String tipContent = "1.每月只可提现一次<br>2.50元起提,上限受会员等级影响<br>3.每月25日开放提现,持续到月末<br>4.若支付宝与个人信息不符，无法通过审核";
        if(tixianTipView != null){
            tixianTipView.setText(Html.fromHtml(tipContent));
        }
    }


    //--------------------------------------------实现presenter层回调------------------------------------------------------

    @Override
    public void onCreateCashOrderSucc() {
         Toast.makeText(MyApplication.getContext(),"提现成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateCashOrderFail() {
         Toast.makeText(MyApplication.getContext(),"提现失败,请重试",Toast.LENGTH_SHORT).show();
    }

    //---------------------------------------------内部回调-------------------------------------------------------------------
    private interface AfterAcquireUserInfoCallBack{
        void onAcquireInfo();
    }
}
