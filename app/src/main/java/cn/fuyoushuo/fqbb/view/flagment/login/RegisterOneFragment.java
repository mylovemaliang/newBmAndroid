package cn.fuyoushuo.fqbb.view.flagment.login;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.trello.rxlifecycle.FragmentEvent;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.commonlib.utils.RxBus;
import cn.fuyoushuo.fqbb.presenter.impl.LocalLoginPresent;
import cn.fuyoushuo.fqbb.presenter.impl.login.RegisterOnePresenter;
import cn.fuyoushuo.fqbb.view.flagment.BaseFragment;
import cn.fuyoushuo.fqbb.view.flagment.UserAgreementDialogFragment;
import cn.fuyoushuo.fqbb.view.listener.MyTagHandler;
import cn.fuyoushuo.fqbb.view.view.login.RegisterOneView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 用于管理登录页面
 * Created by QA on 2016/10/28.
 */
public class RegisterOneFragment extends BaseFragment implements RegisterOneView{

    public static final String TAG_NAME = "register_one_fragment";

    @Bind(R.id.account_value)
    EditText phoneNumText;

    //验证码
    @Bind(R.id.verificate_value)
    EditText verifiCodeView;

    @Bind(R.id.acquire_verification_button)
    Button acquireVerifiButton;

    @Bind(R.id.password_value)
    EditText passwordView;

    @Bind(R.id.commit_button)
    Button commitButton;

    @Bind(R.id.login_tip_area)
    TextView loginTipTextView;

    @Bind(R.id.userAgreement_tip)
    TextView userAgreementTipView;

    @Bind(R.id.register_hidePass_area)
    ImageView hidePassView;

    private String phoneNum = "";

    private String password = "";

    private String verifiCode = "";

    private boolean isPasswordHidden = true;

    RegisterOnePresenter registerOnePresenter;

    LocalLoginPresent localLoginPresent;

    //判断账号是否满足要求
    private boolean isAccountRight = false;

    private long time = 60l;


    @Override
    protected String getPageName() {
        return "register-1";
    }

    @Override
    protected int getRootLayoutId() {
        return R.layout.fragment_register_1;
    }

    @Override
    protected void initView() {
//        phoneNumText.setFocusable(true);
//        phoneNumText.setFocusableInTouchMode(true);
//        phoneNumText.requestFocus();
        //手机号码
        RxTextView.textChanges(phoneNumText)
                .compose(this.<CharSequence>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                         phoneNum = charSequence.toString();
                    }
                });

        //验证码
        RxTextView.textChanges(verifiCodeView)
                .compose(this.<CharSequence>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        verifiCode = charSequence.toString();
                    }
                });
        //密码
        RxTextView.textChanges(passwordView)
                .compose(this.<CharSequence>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                        password = charSequence.toString();
                    }
                });
        //显示和隐藏密码
        RxView.clicks(hidePassView).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000,TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(isPasswordHidden){
                            isPasswordHidden = false;
                            passwordView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            hidePassView.setImageResource(R.mipmap.new_pass);
                        }else{
                            isPasswordHidden = true;
                            passwordView.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            hidePassView.setImageResource(R.mipmap.ver_pass);
                        }
                    }
                });

        RxView.clicks(loginTipTextView).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000,TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        RxBus.getInstance().send(new ToLoginOriginEvent());
                    }
                });

        RxView.clicks(acquireVerifiButton).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        // TODO: 2016/10/28  获取验证码
                        localLoginPresent.validateData(phoneNum,1, new LocalLoginPresent.DataValidataCallBack() {
                            @Override
                            public void onValidataSucc(int flag) {
                                if(flag == 1){
                                    isAccountRight = false;
                                    Toast.makeText(MyApplication.getContext(),"手机号码已被注册",Toast.LENGTH_SHORT).show();
                                }else{
                                    isAccountRight = true;
                                    registerOnePresenter.getVerifiCode(phoneNum);
                                    timeForVerifiCode();
                                }
                            }
                            @Override
                            public void onValidataFail(String msg) {
                                isAccountRight = false;
                                Toast.makeText(MyApplication.getContext(),"手机号码不可用,请检查账号后再次输入",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }});

        //提交按钮
        RxView.clicks(commitButton).throttleFirst(1000, TimeUnit.MILLISECONDS)
                .compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                       if(!TextUtils.isEmpty(phoneNum) && !TextUtils.isEmpty(verifiCode) && !TextUtils.isEmpty(password)){
                            registerOnePresenter.registUser(phoneNum,password,verifiCode);
                       }else{
                            Toast.makeText(MyApplication.getContext(),"信息不完整,无法注册",Toast.LENGTH_SHORT).show();
                       }
                    }
                });

        phoneNumText.setInputType(InputType.TYPE_CLASS_PHONE);
        userAgreementTipView.setText(Html.fromHtml("• 注册或登录即视为同意<font color=\"red\"><userAgree>《返钱宝宝用户使用协议》</userAgree></font>",null,
                new MyTagHandler("userAgree", new MyTagHandler.OnClickTag() {
                    @Override
                    public void onClick() {
                        UserAgreementDialogFragment.newInstance().show(getFragmentManager(),"UserAgreementDialogFragment");
                    }
                })));
        userAgreementTipView.setHighlightColor(getResources().getColor(android.R.color.transparent));
        userAgreementTipView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void initData() {
        registerOnePresenter = new RegisterOnePresenter(this);
        localLoginPresent = new LocalLoginPresent();
    }


    public static RegisterOneFragment newInstance() {
        RegisterOneFragment fragment = new RegisterOneFragment();
        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(registerOnePresenter != null){
            registerOnePresenter.onDestroy();
        }
        if(localLoginPresent != null){
            localLoginPresent.onDestroy();
        }
    }

    private void timeForVerifiCode() {
        acquireVerifiButton.setText("获取验证码(60)");
        acquireVerifiButton.setClickable(false);
        acquireVerifiButton.setBackgroundColor(getResources().getColor(R.color.gray));
        Observable.timer(1, TimeUnit.SECONDS)
                .compose(this.<Long>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .repeat(60)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        time--;
                        if(acquireVerifiButton == null){
                            return;
                        }
                        if (time > 0) {
                            acquireVerifiButton.setText("获取验证码(" + time + ")");
                        } else {
                            acquireVerifiButton.setClickable(true);
                            acquireVerifiButton.setBackgroundColor(getResources().getColor(R.color.module_6));
                            acquireVerifiButton.setText("重新获取验证码");
                            time = 60l;
                        }
                    }
                });
    }


    //---------------------------------实现VIEW层的接口---------------------------------------------------------

    @Override
    public void onErrorRecieveVerifiCode(String respMsg) {
        String msg = "验证码发送失败,请重试";
        if(!TextUtils.isEmpty(respMsg)){
             msg = respMsg;
        }
        Toast.makeText(MyApplication.getContext(),respMsg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSuccessRecieveVerifiCode(String phoneNum) {
        Toast.makeText(MyApplication.getContext(),"验证码发送成功,请耐心等待",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRegistSuccess(String phoneNum) {
         localLoginPresent.userLogin(phoneNum, password, new LocalLoginPresent.LocalLoginCallBack() {
            @Override
            public void onLocalLoginSucc(String account) {
                RxBus.getInstance().send(new LoginSuccAfterRegisterSucc());
            }

            @Override
            public void onLocalLoginFail(String account) {
                RxBus.getInstance().send(new ToLoginAfterRegisterSuccess(account));
            }
        });
    }

    @Override
    public void onRegistFail(String phoneNum, String msg) {
        Toast.makeText(MyApplication.getContext(),msg,Toast.LENGTH_SHORT).show();
    }

    //--------------------------------与activity 通信接口------------------------------------------------------------
//    public class ToRegisterTwoEvent extends RxBus.BusEvent{
//
//        private String phoneNum;
//
//        public ToRegisterTwoEvent(String phoneNum) {
//            this.phoneNum = phoneNum;
//        }
//
//        public String getPhoneNum() {
//            return phoneNum;
//        }
//
//        public void setPhoneNum(String phoneNum) {
//            this.phoneNum = phoneNum;
//        }
//    }

    public class ToLoginAfterRegisterSuccess extends RxBus.BusEvent{

        private String phoneNum;

        public ToLoginAfterRegisterSuccess(String phoneNum) {
            this.phoneNum = phoneNum;
        }

        public String getPhoneNum() {
            return phoneNum;
        }
    }

    public class LoginSuccAfterRegisterSucc extends RxBus.BusEvent{}

    public class ToLoginOriginEvent extends RxBus.BusEvent{}

    //--------------------------------焦点重定向----------------------------------------------

    @Override
    protected void setupUI(View view, final Activity context) {
        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    v.setFocusable(true);
                    v.setFocusableInTouchMode(true);
                    v.requestFocus();
                    InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(),0);
                    return false;
                }
            });

        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView,context);
            }
        }
    }
}
