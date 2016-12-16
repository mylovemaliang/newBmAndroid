package cn.fuyoushuo.fqbb.view.flagment.login;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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
import cn.fuyoushuo.fqbb.presenter.impl.login.LoginOriginPresenter;
import cn.fuyoushuo.fqbb.presenter.impl.login.RegisterThreePresenter;
import cn.fuyoushuo.fqbb.view.flagment.BaseFragment;
import cn.fuyoushuo.fqbb.view.view.login.RegisterThreeView;
import rx.functions.Action1;

/**
 * 用于管理登录页面
 * Created by QA on 2016/10/28.
 */
public class RegisterThreeFragment extends BaseFragment implements RegisterThreeView{


    public static final String TAG_NAME = "register_one_fragment";

    private String phoneNum = "";

    private String verifiCode = "";

    private String password = "";

    @Bind(R.id.register_3_header_area)
    TextView headerTitle;

    @Bind(R.id.password_value)
    EditText passwordView;

    @Bind(R.id.commit_button)
    Button commitButton;

    @Bind(R.id.register_3_hidePass_area)
    ImageView hidePassView;

    RegisterThreePresenter registerThreePresenter;

    LocalLoginPresent localLoginPresent;

    private boolean isPasswordHidden = true;

    @Override
    protected String getPageName() {
        return "register-3";
    }

    @Override
    protected int getRootLayoutId() {
            return R.layout.fragment_register_3;
    }

    @Override
    protected void initView() {
        passwordView.setFocusable(true);
        passwordView.setFocusableInTouchMode(true);
        passwordView.requestFocus();

        RxTextView.textChanges(passwordView).compose(this.<CharSequence>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<CharSequence>() {
                    @Override
                    public void call(CharSequence charSequence) {
                          password = charSequence.toString();
                    }
        });

        RxView.clicks(commitButton).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW)).throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        // TODO: 2016/10/28  提交注册信息之后
                        registerThreePresenter.registUser(phoneNum,password,verifiCode);
                    }
                });

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


    }

    @Override
    protected void initData() {
        localLoginPresent = new LocalLoginPresent();
        registerThreePresenter = new RegisterThreePresenter(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(localLoginPresent != null){
            localLoginPresent.onDestroy();
        }
        if(registerThreePresenter != null){
           registerThreePresenter.onDestroy();
        }
    }

    public static RegisterThreeFragment newInstance() {
        RegisterThreeFragment fragment = new RegisterThreeFragment();
        return fragment;
    }

    public void refreshView(String phoneNum,String verifiCode){
        if(isDetched) return;
        this.phoneNum = phoneNum;
        this.verifiCode = verifiCode;
    }

    @Override
    public void onStart() {
        super.onStart();
        headerTitle.setText("手机号 : "+phoneNum);
    }

    //--------------------------------------------实现　view　层接口----------------------------------
   //当注册成功后的逻辑　
    @Override
    public void onRegistSuccess(final String phoneNum) {
         //自动登录,登录成功跳转用户中心
//         localLoginPresent.userLogin(phoneNum, password, new LocalLoginPresent.LocalLoginCallBack() {
//            @Override
//            public void onLocalLoginSucc(String account) {
//                RxBus.getInstance().send(new LoginSuccAfterRegisterSucc());
//            }
//
//            @Override
//            public void onLocalLoginFail(String account) {
//                RxBus.getInstance().send(new ToLoginAfterRegisterSuccess(phoneNum));
//            }
//        });
    }

    //当注册失败后的逻辑
    @Override
    public void onRegistFail(String phoneNum, String msg) {
        Toast.makeText(MyApplication.getContext(),msg,Toast.LENGTH_SHORT).show();
    }


   //----------------------------------------总线EVENT定义------------------------------------------

//    public class ToLoginAfterRegisterSuccess extends RxBus.BusEvent{
//
//        private String phoneNum;
//
//        public ToLoginAfterRegisterSuccess(String phoneNum) {
//            this.phoneNum = phoneNum;
//        }
//
//        public String getPhoneNum() {
//            return phoneNum;
//        }
//    }
//
//    public class LoginSuccAfterRegisterSucc extends RxBus.BusEvent{}
}
