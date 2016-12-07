package cn.fuyoushuo.fqbb.view.flagment.zhifubao;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.components.support.RxDialogFragment;
import com.umeng.analytics.MobclickAgent;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.commonlib.utils.LoginInfoStore;
import cn.fuyoushuo.fqbb.commonlib.utils.RxBus;
import cn.fuyoushuo.fqbb.presenter.impl.LocalLoginPresent;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by QA on 2016/12/1.
 */
public class BindZfbDialogFragment extends RxDialogFragment {

    @Bind(R.id.bind_zfb_backArea)
    RelativeLayout backArea;

    @Bind(R.id.name_info_value)
    EditText nameTextView;

    @Bind(R.id.idcard_info_value)
    EditText idCardTextView;

    @Bind(R.id.zfb_info_value)
    EditText alipayNoTextView;

    @Bind(R.id.bind_zfb_tip)
    TextView bindZfbTipTextView;

    @Bind(R.id.verificate_value)
    EditText verifiCateValueView;

    @Bind(R.id.acquire_verification_button)
    Button verifiAcquireButton;

    @Bind(R.id.commit_button)
    Button commitButton;

    LocalLoginPresent localLoginPresent;

    //真实姓名
    private String nameValue;

    //身份证号码
    private String idCardValue;

    //支付宝号码
    private String alipayNoValue;

    //验证码
    private String verifiCodeValue;

    private Long time = 60l;


    public static BindZfbDialogFragment newInstance() {
        BindZfbDialogFragment fragment = new BindZfbDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        nameValue = "";
        idCardValue = "";
        alipayNoValue = "";
        verifiCodeValue = "";
        localLoginPresent = new LocalLoginPresent();
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.fullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.view_bind_zfb, container, false);
        ButterKnife.bind(this, inflate);
        bindZfbTipTextView.setText(Html.fromHtml("请务必填写<font color=\"#ff0000\">与支付宝绑定</font>的姓名和身份证号,一经填写,无法修改,不一致会导致提现审核失败"));
        return inflate;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        verifiCateValueView.setInputType(EditorInfo.TYPE_CLASS_PHONE);

        RxView.clicks(backArea).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000,TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        dismissAllowingStateLoss();
                    }
                });

        RxTextView.textChanges(nameTextView).compose(this.<CharSequence>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        nameValue = charSequence.toString();
                    }
                });

        RxTextView.textChanges(idCardTextView).compose(this.<CharSequence>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        idCardValue = charSequence.toString();
                    }
                });

        RxTextView.textChanges(alipayNoTextView).compose(this.<CharSequence>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        alipayNoValue = charSequence.toString();
                    }
                });

        RxTextView.textChanges(verifiCateValueView).compose(this.<CharSequence>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        verifiCodeValue = charSequence.toString();
                    }
                });

        RxView.clicks(verifiAcquireButton).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000,TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(TextUtils.isEmpty(nameValue) || TextUtils.isEmpty(idCardValue) || TextUtils.isEmpty(alipayNoValue)) {
                            Toast.makeText(MyApplication.getContext(), "填写信息不全,请先完善信息", Toast.LENGTH_SHORT).show();
                            return;
                        }else{
                            timeForVerifiCode();
                            //用户默认的手机号码
                            String userAccount = LoginInfoStore.getIntance().getUserAccount();
                            localLoginPresent.getVerifiCode(userAccount, "phone_bind_alipay", new LocalLoginPresent.VerifiCodeGetCallBack() {
                                @Override
                                public void onVerifiCodeGetSucc(String account) {
                                    Toast.makeText(MyApplication.getContext(),"验证码发送成功,请验收", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                @Override
                                public void onVerifiCodeGetError(String msg) {
                                    Toast.makeText(MyApplication.getContext(),"验证码发送失败,请重试", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            });
                        }
                    }
                });

        //提交按钮
        RxView.clicks(commitButton).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000,TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                          if(TextUtils.isEmpty(nameValue) || TextUtils.isEmpty(idCardValue) || TextUtils.isEmpty(alipayNoValue) || TextUtils.isEmpty(verifiCodeValue)){
                              Toast.makeText(MyApplication.getContext(), "填写信息不全,请先完善信息", Toast.LENGTH_SHORT).show();
                              return;
                          }else{
                              localLoginPresent.bindZfb(alipayNoValue, nameValue, idCardValue, verifiCodeValue, new LocalLoginPresent.BindZfbCallBack() {
                                  @Override
                                  public void onBindZfbSucc() {
                                      RxBus.getInstance().send(new AfterBindAlipaySuccEvent());
                                      Toast.makeText(MyApplication.getContext(), "支付宝绑定成功", Toast.LENGTH_SHORT).show();
                                      dismissAllowingStateLoss();
                                      return;
                                  }

                                  @Override
                                  public void onBindZfbFail(String msg) {
                                      Toast.makeText(MyApplication.getContext(),msg, Toast.LENGTH_SHORT).show();
                                      return;
                                  }
                              });
                          }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        if (localLoginPresent != null) {
            localLoginPresent.onDestroy();
        }
        ButterKnife.unbind(this);
        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("bindAlipayNum");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("bindAlipayNum");
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
                        if (verifiAcquireButton == null) return;
                        time--;
                        if (time > 0) {
                            if (verifiAcquireButton != null) {
                                verifiAcquireButton.setText("获取验证码(" + time + ")");
                            }
                        } else {
                            if (verifiAcquireButton != null) {
                                verifiAcquireButton.setClickable(true);
                                verifiAcquireButton.setBackgroundColor(getResources().getColor(R.color.module_6));
                                verifiAcquireButton.setText("重新获取验证码");
                            }
                            time = 60l;
                        }
                    }
                });
    }

 //------------------------------------------------总线事件-----------------------------------------------------

  public class AfterBindAlipaySuccEvent extends RxBus.BusEvent{}

}