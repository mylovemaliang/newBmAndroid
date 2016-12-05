package cn.fuyoushuo.fqbb.view.view.pointsmall;

import java.util.List;

import cn.fuyoushuo.fqbb.domain.entity.DuihuanItem;
import cn.fuyoushuo.fqbb.domain.entity.TixianjiluItem;

/**
 * Created by QA on 2016/11/11.
 */
public interface TixianjiluView {

     void onLoadDataFail(String msg);

     void onLoadDataSuccess(Integer queryStatus, int page, boolean isRefresh, List<TixianjiluItem> itemList,boolean isLastPage);

}
