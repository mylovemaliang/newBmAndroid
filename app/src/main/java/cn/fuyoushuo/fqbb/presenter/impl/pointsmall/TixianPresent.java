package cn.fuyoushuo.fqbb.presenter.impl.pointsmall;

import android.text.TextUtils;

import java.lang.ref.WeakReference;

import cn.fuyoushuo.fqbb.ServiceManager;
import cn.fuyoushuo.fqbb.domain.ext.HttpResp;
import cn.fuyoushuo.fqbb.domain.httpservice.FqbbLocalHttpService;
import cn.fuyoushuo.fqbb.presenter.impl.BasePresenter;
import cn.fuyoushuo.fqbb.view.view.pointsmall.TixianView;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by QA on 2016/11/11.
 */
public class TixianPresent extends BasePresenter {

    private WeakReference<TixianView> tixianView;

    public TixianPresent(TixianView tixianView) {
        this.tixianView = new WeakReference<TixianView>(tixianView);
    }

    private TixianView getMyView(){
        return tixianView.get();
    }


    /**
     * 创建提现订单
     * @param cashNum 提现金额(float,#0.00)
     * @param verifiCode 提现验证码
     */
    public void createCashOrder(Float cashNum,String verifiCode){
          if(cashNum == null || cashNum == 0f || TextUtils.isEmpty(verifiCode)){
                if(getMyView() != null){
                    getMyView().onCreateCashOrderFail();
                }
          }
          mSubscriptions.add(ServiceManager.createService(FqbbLocalHttpService.class)
             .createCashOrders(cashNum,verifiCode)
             .subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe(new Subscriber<HttpResp>() {
                 @Override
                 public void onCompleted() {

                 }

                 @Override
                 public void onError(Throwable e) {
                     if(getMyView() != null){
                         getMyView().onCreateCashOrderFail();
                     }
                 }

                 @Override
                 public void onNext(HttpResp httpResp) {
                      if (httpResp == null || httpResp.getS() != 1){
                          if(getMyView() != null){
                              getMyView().onCreateCashOrderFail();
                          }
                      }else {
                          if(getMyView() != null){
                              getMyView().onCreateCashOrderSucc();
                          }
                      }
                 }
             })
          );





    }


}
