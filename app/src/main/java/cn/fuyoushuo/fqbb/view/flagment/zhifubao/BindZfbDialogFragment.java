package cn.fuyoushuo.fqbb.view.flagment.zhifubao;

import android.os.Bundle;
import android.view.View;

import com.trello.rxlifecycle.components.support.RxDialogFragment;

/**
 * Created by QA on 2016/12/1.
 */
public class BindZfbDialogFragment extends RxDialogFragment{



    public static BindZfbDialogFragment newInstance() {

        Bundle args = new Bundle();

        BindZfbDialogFragment fragment = new BindZfbDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
