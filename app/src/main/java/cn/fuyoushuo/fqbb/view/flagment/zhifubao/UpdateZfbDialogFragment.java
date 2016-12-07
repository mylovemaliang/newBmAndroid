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
 * Created by QA on 2016/11/7.
 */
public class UpdateZfbDialogFragment extends RxDialogFragment{

    @Bind(R.id.update_zfb_backArea)
    RelativeLayout backArea;

    @Bind(R.id.update_zfb_account_area)
    TextView accountInfoText;

    @Bind(R.id.zfb_new_account_value)
    EditText newAlipayNoView;

    @Bind(R.id.verificate_value)
    EditText verificateValueView;

    @Bind(R.id.acquire_verification_button)
    Button verifiAcquireButton;

    @Bind(R.id.commit_button)
    Button commitButton;

    private String zfbAccount;

    private String newAlipayNoValue;

    private String verificateValue;

    LocalLoginPresent localLoginPresent;

    private Long time = 60l;


    public static UpdateZfbDialogFragment newInstance(String zfbAccount) {
        Bundle args = new Bundle();
        UpdateZfbDialogFragment fragment = new UpdateZfbDialogFragment();
        args.putString("zfbAccount",zfbAccount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.fullScreenDialog);
        if(getArguments() != null){
            zfbAccount = getArguments().getString("zfbAccount","");
        }
        newAlipayNoValue = "";
        verificateValue = "";
        localLoginPresent = new LocalLoginPresent();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.view_update_zfb, container, false);
        ButterKnife.bind(this,inflate);

        accountInfoText.setText(Html.fromHtml("已绑定支付宝    <font color=\"#ff0000\">"+zfbAccount+"</font>"));

        return inflate;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        verificateValueView.setInputType(EditorInfo.TYPE_CLASS_PHONE);

        RxView.clicks(backArea).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000,TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        dismissAllowingStateLoss();
                    }
                });

        RxTextView.textChanges(newAlipayNoView).compose(this.<CharSequence>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        newAlipayNoValue = charSequence.toString();
                    }
                });

        RxTextView.textChanges(verificateValueView).compose(this.<CharSequence>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        verificateValue = charSequence.toString();
                    }
                });

        //获取验证码
        RxView.clicks(verifiAcquireButton).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(TextUtils.isEmpty(newAlipayNoValue)){
                            Toast.makeText(MyApplication.getContext(),"信息不完整,请先完善信息",Toast.LENGTH_SHORT).show();
                        }else{
                            timeForVerifiCode();
                            String userAccount = LoginInfoStore.getIntance().getUserAccount();
                            localLoginPresent.getVerifiCode(userAccount, "phone_dochange_bind_alipay", new LocalLoginPresent.VerifiCodeGetCallBack() {
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
                    }
                });

        RxView.clicks(commitButton).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000,TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                         if(TextUtils.isEmpty(newAlipayNoValue) || TextUtils.isEmpty(verificateValue)){
                             Toast.makeText(MyApplication.getContext(),"信息不完整,请先完善信息",Toast.LENGTH_SHORT).show();
                             return;
                         }else{
                             localLoginPresent.updateZfb(newAlipayNoValue, verificateValue, new LocalLoginPresent.UpdateZfbCallBack() {
                                 @Override
                                 public void onUpdateZfbSucc() {
                                     RxBus.getInstance().send(new AfterUpdateAlipaySuccEvent());
                                     Toast.makeText(MyApplication.getContext(),"支付宝改绑成功",Toast.LENGTH_SHORT).show();
                                     dismissAllowingStateLoss();
                                     return;
                                 }

                                 @Override
                                 public void onUpdateZfbFail(String msg) {
                                     Toast.makeText(MyApplication.getContext(),msg,Toast.LENGTH_SHORT).show();
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
        if(localLoginPresent != null){
            localLoginPresent.onDestroy();
        }
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("updateAlipayNum");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("updateAlipayNum");
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

    //----------------------------------------------总线事件-------------------------------------------------------------

    public class AfterUpdateAlipaySuccEvent extends RxBus.BusEvent{}

}
