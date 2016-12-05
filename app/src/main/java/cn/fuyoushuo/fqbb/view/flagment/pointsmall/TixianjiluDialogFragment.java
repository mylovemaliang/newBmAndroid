package cn.fuyoushuo.fqbb.view.flagment.pointsmall;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.components.support.RxDialogFragment;
import com.umeng.analytics.MobclickAgent;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.domain.entity.DuihuanItem;
import cn.fuyoushuo.fqbb.domain.entity.TixianjiluItem;
import cn.fuyoushuo.fqbb.presenter.impl.pointsmall.DuihuanjiluPresent;
import cn.fuyoushuo.fqbb.presenter.impl.pointsmall.TixianPresent;
import cn.fuyoushuo.fqbb.presenter.impl.pointsmall.TixianjiluPresent;
import cn.fuyoushuo.fqbb.view.Layout.DhOrderDecoration;
import cn.fuyoushuo.fqbb.view.Layout.MyGridLayoutManager;
import cn.fuyoushuo.fqbb.view.Layout.RefreshLayout;
import cn.fuyoushuo.fqbb.view.adapter.DuihuanOrderAdapter;
import cn.fuyoushuo.fqbb.view.adapter.TixianOrderAdapter;
import cn.fuyoushuo.fqbb.view.view.pointsmall.DuihuanjiluView;
import cn.fuyoushuo.fqbb.view.view.pointsmall.TixianjiluView;
import rx.functions.Action1;

/**
 * Created by QA on 2016/11/7.
 */
public class TixianjiluDialogFragment extends RxDialogFragment implements TixianjiluView{


    @Bind(R.id.tixianjilu_backArea)
    RelativeLayout backArea;

    //全部记录
    @Bind(R.id.txjl_allRecord_button)
    Button allRecordButton;

    //正在审核
    @Bind(R.id.txjl_underReview_button)
    Button underReviewButton;

    //已兑换
    @Bind(R.id.txjl_exchanged_button)
    Button exchangedButton;

    //审核失败
    @Bind(R.id.txjl_reviewFailed_button)
    Button reviewFailedButton;

    @Bind(R.id.txjl_order_finish_button)
    Button finishedButton;

    //刷新结果页
    @Bind(R.id.txjl_result_refreshView)
    RefreshLayout resultRefreshView;

    //结果页
    @Bind(R.id.txjl_result_rview)
    RecyclerView resultRview;

    private boolean isAllLoad = false;

    TixianOrderAdapter tixianOrderAdapter;

    TixianjiluPresent tixianjiluPresent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.fullScreenDialog);
        tixianjiluPresent = new TixianjiluPresent(this);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_pointsmall_tixianjilu_dialog, container, false);
        ButterKnife.bind(this,inflate);
        tixianOrderAdapter = new TixianOrderAdapter();
        resultRview.setHasFixedSize(true);
        final MyGridLayoutManager gridLayoutManager = new MyGridLayoutManager(getActivity(),1);
        gridLayoutManager.setSpeedFast();
        //gridLayoutManager.setSpeedSlow();
        gridLayoutManager.setAutoMeasureEnabled(true);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        resultRview.setLayoutManager(gridLayoutManager);
        resultRview.setAdapter(tixianOrderAdapter);
        resultRview.addItemDecoration(new DhOrderDecoration());
        return inflate;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RxView.clicks(backArea).compose(this.<Void>bindToLifecycle())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                         dismissAllowingStateLoss();
                    }
                });


        resultRefreshView.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                Integer status = tixianOrderAdapter.getQueryStatus();
                Integer page = tixianOrderAdapter.getCurrentPage();
                if(!isAllLoad){
                   tixianjiluPresent.getTixianOrders(status, page + 1, false);
                }
            }
        });

        resultRefreshView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                isAllLoad = false;
                Integer status = tixianOrderAdapter.getQueryStatus();
                tixianjiluPresent.getTixianOrders(status,1,true);
                resultRefreshView.setRefreshing(false);
                return;
            }
        });

        RxView.clicks(allRecordButton).compose(this.<Void>bindToLifecycle())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        tixianjiluPresent.getTixianOrders(null,1,true);
                    }
                });

        RxView.clicks(underReviewButton).compose(this.<Void>bindToLifecycle())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                          tixianjiluPresent.getTixianOrders(2,1,true);
                    }
                });

        RxView.clicks(exchangedButton).compose(this.<Void>bindToLifecycle())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        tixianjiluPresent.getTixianOrders(3,1,true);
                    }
                });

        RxView.clicks(reviewFailedButton).compose(this.<Void>bindToLifecycle())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        tixianjiluPresent.getTixianOrders(4,1,true);
                    }
                });

        RxView.clicks(finishedButton).compose(this.<Void>bindToLifecycle())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        tixianjiluPresent.getTixianOrders(5,1,true);
                    }
                });

    }

    private void initButtonColor(Integer status){
        if(status == null){
            allRecordButton.setTextColor(getResources().getColor(R.color.module_11));
            underReviewButton.setTextColor(getResources().getColor(R.color.black));
            exchangedButton.setTextColor(getResources().getColor(R.color.black));
            reviewFailedButton.setTextColor(getResources().getColor(R.color.black));
            finishedButton.setTextColor(getResources().getColor(R.color.black));
        }
        else if(status == 2){
            allRecordButton.setTextColor(getResources().getColor(R.color.black));
            underReviewButton.setTextColor(getResources().getColor(R.color.module_11));
            exchangedButton.setTextColor(getResources().getColor(R.color.black));
            reviewFailedButton.setTextColor(getResources().getColor(R.color.black));
            finishedButton.setTextColor(getResources().getColor(R.color.black));
        }
        else if(status == 3){
            allRecordButton.setTextColor(getResources().getColor(R.color.black));
            underReviewButton.setTextColor(getResources().getColor(R.color.black));
            exchangedButton.setTextColor(getResources().getColor(R.color.module_11));
            reviewFailedButton.setTextColor(getResources().getColor(R.color.black));
            finishedButton.setTextColor(getResources().getColor(R.color.black));
        }
        else if(status == 4){
            allRecordButton.setTextColor(getResources().getColor(R.color.black));
            underReviewButton.setTextColor(getResources().getColor(R.color.black));
            exchangedButton.setTextColor(getResources().getColor(R.color.black));
            reviewFailedButton.setTextColor(getResources().getColor(R.color.module_11));
            finishedButton.setTextColor(getResources().getColor(R.color.black));
        }
        else if(status == 5){
            allRecordButton.setTextColor(getResources().getColor(R.color.black));
            underReviewButton.setTextColor(getResources().getColor(R.color.black));
            exchangedButton.setTextColor(getResources().getColor(R.color.black));
            reviewFailedButton.setTextColor(getResources().getColor(R.color.black));
            finishedButton.setTextColor(getResources().getColor(R.color.module_11));
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        tixianjiluPresent.onDestroy();
    }


    @Override
    public void onStart() {
        super.onStart();
        tixianjiluPresent.getTixianOrders(null,1,false);
        allRecordButton.setTextColor(getResources().getColor(R.color.module_11));
    }


    public static TixianjiluDialogFragment newInstance() {

        TixianjiluDialogFragment fragment = new TixianjiluDialogFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("pointsMall_tixianjilu");
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("pointsMall_tixianjilu");
    }


    //-----------------------------------------兑换记录-------------------------------------------------

    @Override
    public void onLoadDataFail(String msg) {
        Toast.makeText(MyApplication.getContext(),"网速稍慢",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadDataSuccess(Integer queryStatus, int page, boolean isRefresh, List<TixianjiluItem> itemList, boolean isLastPage) {
        initButtonColor(queryStatus);
        if(itemList.isEmpty()){
              Toast.makeText(MyApplication.getContext(),"你的兑换记录为空",Toast.LENGTH_SHORT).show();
        }
        if(page == 1){
            isAllLoad = false;
        }
        if(isLastPage){
            isAllLoad = true;
        }
        if (isRefresh) {
            tixianOrderAdapter.setData(itemList);
        } else {
            tixianOrderAdapter.appendDataList(itemList);
        }
        tixianOrderAdapter.setQueryStatus(queryStatus);
        tixianOrderAdapter.setCurrentPage(page);
        tixianOrderAdapter.notifyDataSetChanged();
    }

}
