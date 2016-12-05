package cn.fuyoushuo.fqbb.presenter.impl.pointsmall;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.fuyoushuo.fqbb.ServiceManager;
import cn.fuyoushuo.fqbb.domain.entity.DuihuanItem;
import cn.fuyoushuo.fqbb.domain.entity.TixianjiluItem;
import cn.fuyoushuo.fqbb.domain.ext.HttpResp;
import cn.fuyoushuo.fqbb.domain.httpservice.FqbbLocalHttpService;
import cn.fuyoushuo.fqbb.presenter.impl.BasePresenter;
import cn.fuyoushuo.fqbb.view.view.pointsmall.DuihuanjiluView;
import cn.fuyoushuo.fqbb.view.view.pointsmall.TixianjiluView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by QA on 2016/11/11.
 */
public class TixianjiluPresent extends BasePresenter {

    private WeakReference<TixianjiluView> tixianjiluView;

    public TixianjiluPresent(TixianjiluView tixianjiluView) {
        this.tixianjiluView = new WeakReference<TixianjiluView>(tixianjiluView);
    }

    private TixianjiluView getMyView(){
        return tixianjiluView.get();
    }


    public void getTixianOrders(final Integer queryStatus, final int pageNum, final boolean isRefresh){
         if(pageNum == 0){
             if(getMyView() != null){
                 getMyView().onLoadDataFail("页数错误");
             }
         }

        mSubscriptions.add(ServiceManager.createService(FqbbLocalHttpService.class)
                .getCashOrders(pageNum,queryStatus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<HttpResp>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                       if(getMyView() != null){
                           getMyView().onLoadDataFail("");
                       }
                    }

                    @Override
                    public void onNext(HttpResp httpResp) {
                       if(httpResp == null || httpResp.getS() != 1){
                           if(getMyView() != null){
                               getMyView().onLoadDataFail("");
                           }
                       }else{
                           boolean isLastPage = false;
                           List<TixianjiluItem> orders = new ArrayList<TixianjiluItem>();
                           JSONObject result = new JSONObject(((Map)(httpResp.getR())));
                           if(result != null && !result.isEmpty() && result.containsKey("totalPageNumber")){
                               int totalPageNumber = result.getIntValue("totalPageNumber");
                               if(pageNum >= totalPageNumber){
                                   isLastPage = true;
                               }
                           }
                           parseDuihuanItem(result,orders);
                           if(getMyView() != null){
                               getMyView().onLoadDataSuccess(queryStatus,pageNum,isRefresh,orders,isLastPage);
                           }
                       }
                    }
                })
        );
    }

    private void parseDuihuanItem(JSONObject result,List<TixianjiluItem> orders){
        if(result == null || result.isEmpty()) return;
        JSONArray listObjs = result.getJSONArray("listObjs");
        if(listObjs == null || listObjs.isEmpty()) return;
        for(int i=0;i<listObjs.size();i++){
            JSONObject item = listObjs.getJSONObject(i);
            TixianjiluItem tixianItem = new TixianjiluItem();
            tixianItem.setDateTimeString(item.getString("gmtCreateStr"));
            tixianItem.setTixianPoints(item.getIntValue("cash"));
            tixianItem.setOrderStatus(item.getInteger("status"));
            orders.add(tixianItem);
        }
    }
}
