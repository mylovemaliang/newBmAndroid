package cn.fuyoushuo.fqbb.presenter.impl.pointsmall;

import java.lang.ref.WeakReference;

import cn.fuyoushuo.fqbb.presenter.impl.BasePresenter;
import cn.fuyoushuo.fqbb.view.view.pointsmall.TixianView;

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


}
