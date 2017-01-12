package cn.fuyoushuo.fqbb.view.flagment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.components.support.RxDialogFragment;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.commonlib.utils.RxBus;
import rx.functions.Action1;

/**
 * 首页提示
 * Created by QA on 2016/12/21.
 */

public class MainTipDialogFragment extends RxDialogFragment{

    @Bind(R.id.image)
    ImageView imageView;

    private boolean isLastPage;


    public static MainTipDialogFragment newInstance() {
        Bundle args = new Bundle();
        MainTipDialogFragment fragment = new MainTipDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLastPage = false;
        setStyle(DialogFragment.STYLE_NORMAL,R.style.fullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.view_main_tip, container, false);
        ButterKnife.bind(this,inflate);
        return inflate;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RxView.clicks(imageView).compose(this.<Void>bindToLifecycle())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        RxBus.getInstance().send(new MainTipCompleteEvent());
                        dismissAllowingStateLoss();
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

   //-------------------------------------总线事件-----------------------------------------------------
   public class MainTipCompleteEvent extends RxBus.BusEvent{}
}
