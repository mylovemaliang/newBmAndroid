package cn.fuyoushuo.fqbb.view.flagment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.components.support.RxDialogFragment;
import com.umeng.analytics.MobclickAgent;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.fuyoushuo.fqbb.R;
import rx.functions.Action1;

/**
 * Created by QA on 2016/12/8.
 */

public class UserAgreementDialogFragment extends RxDialogFragment{


    @Bind(R.id.user_agreement_backArea)
    RelativeLayout backArea;

    @Bind(R.id.user_agreement_text)
    TextView userAgreeText;


    @Override
    public void show(FragmentManager manager, String tag) {
        try{ 
           super.show(manager, tag);
        }catch (Exception e){
            // to do nothing
        }
    }

    public static UserAgreementDialogFragment newInstance() {
        Bundle args = new Bundle();
        UserAgreementDialogFragment fragment = new UserAgreementDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL,R.style.fullScreenDialog);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.view_use_agreement, container, false);
        ButterKnife.bind(this,inflate);
        return inflate;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RxView.clicks(backArea).compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                          dismissAllowingStateLoss();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        userAgreeText.setText(Html.fromHtml(getResources().getString(R.string.user_agreement)));
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("user_agreement_page");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("user_agreement_page");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
