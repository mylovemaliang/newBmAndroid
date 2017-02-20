package cn.fuyoushuo.fqbb.view.flagment;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding.view.RxView;
import com.trello.rxlifecycle.FragmentEvent;
import com.trello.rxlifecycle.components.support.RxDialogFragment;
import com.umeng.analytics.MobclickAgent;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.commonlib.utils.CommonUtils;
import cn.fuyoushuo.fqbb.commonlib.utils.LocalStatisticConstants;
import cn.fuyoushuo.fqbb.commonlib.utils.PageSession;
import cn.fuyoushuo.fqbb.commonlib.utils.RxBus;
import cn.fuyoushuo.fqbb.domain.entity.TaoBaoItemVo;
import cn.fuyoushuo.fqbb.domain.entity.TbCateVo;
import cn.fuyoushuo.fqbb.ext.LocalStatisticInfo;
import cn.fuyoushuo.fqbb.presenter.impl.SelectedGoodPresenter;
import cn.fuyoushuo.fqbb.view.Layout.DividerItemDecoration;
import cn.fuyoushuo.fqbb.view.Layout.MyGridLayoutManager;
import cn.fuyoushuo.fqbb.view.Layout.RefreshLayout;
import cn.fuyoushuo.fqbb.view.activity.BaseActivity;
import cn.fuyoushuo.fqbb.view.adapter.SearchLeftRviewAdapter;
import cn.fuyoushuo.fqbb.view.adapter.TbGoodDataAdapter;
import rx.functions.Action1;

/**
 * Created by QA on 2016/11/10.
 */
public class JxspDetailDialogFragment extends RxDialogFragment {


    @Bind(R.id.jxsp_detail_backArea)
    RelativeLayout backView;

    @Bind(R.id.jxsp_title)
    TextView titleView;

//    @Bind(R.id.jxsp_detail_topRcycleView)
//    RecyclerView cateRview;

    @Bind(R.id.jxsp_detail_refreshLayout)
    RefreshLayout refreshLayout;

    @Bind(R.id.jxsp_detail_bottomRcycleView)
    RecyclerView goodRview;

    @Bind(R.id.main_totop_icon)
    TextView toTopIcon;

    @Bind(R.id.main_totop_area)
    View toTopView;

    //左边搜索按钮
    @Bind(R.id.jxsc_flagment_left_btn)
    TextView leftSearchButton;

    //右边搜索按钮
    @Bind(R.id.jxsc_flagment_right_btn)
    TextView rightSearchButton;

    @Bind(R.id.line1)
    LinearLayout line1;

    @Bind(R.id.line2)
    LinearLayout line2;

//    SelectedCatesDataAdapter selectCatesDataAdapter;

    TbGoodDataAdapter tbGoodDataAdapter;

    private String title = "";

    private String currentCatId = "";

    private String currentChannel = "";

    private String originCatId = "";

    private int currentLevel = 0;

    //排序规则
    private String sortType = "";

    SelectedGoodPresenter selectedGoodPresenter;

    private PageSession pageSession;

  //----------------------------------------用于搜索的组件-------------------------------------------------

    LayoutInflater layoutInflater;

    //显示下面的搜索条件
    PopupWindow popupWindow;

    //搜索框左部生成的VIEW
    View leftView;

    //搜索框右部生成的VIEW
    View rightView;

    TagFlowLayout catesFlowView;

    List<TbCateVo> cateList;

    //左边部分 recycleView
    RecyclerView searchLeftRview;

    //左边部分recycleview adapter
    SearchLeftRviewAdapter searchLeftRviewAdapter;

    @Override
    public void show(FragmentManager manager, String tag) {
        try{
            super.show(manager, tag);
        }catch (Exception e){
            // to do nothing
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.fullScreenDialog);
        if(getArguments() != null){
            this.currentChannel = getArguments().getString("channel","");
            this.title = getArguments().getString("title","");
            this.originCatId = getArguments().getString("catId","");
            this.currentCatId = getArguments().getString("catId","");
        }
        selectedGoodPresenter = new SelectedGoodPresenter();
        if(SelectedGoodPresenter.QQHD_CHANNEL.equals(currentChannel)){
            pageSession = new PageSession(LocalStatisticConstants.SUPER_FAN);
        }
        else if(SelectedGoodPresenter.JKJ_CHANNEL.equals(currentChannel)){
            pageSession = new PageSession(LocalStatisticConstants.JIU_KUAI_JIU);
        }
        else{
            pageSession = new PageSession(LocalStatisticConstants.OTHER_CHANNEL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.view_jxsp_detail_page, container, false);
        ButterKnife.bind(this,inflate);

        //selectCatesDataAdapter = new SelectedCatesDataAdapter();
        tbGoodDataAdapter= new TbGoodDataAdapter();
        refreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                // TODO: 2016/11/10 获取下一页数据
                final int currentPage = tbGoodDataAdapter.getCurrentPage();
                selectedGoodPresenter.getSelectedGood(currentChannel,currentPage+1,currentCatId,currentLevel,sortType,new SelectedGoodPresenter.SelectGoodGetCallBack() {
                    @Override
                    public void onGetGoodSucc(List<TaoBaoItemVo> goodList, LinkedList<TbCateVo> cateList) {
                        if(tbGoodDataAdapter != null){
                            tbGoodDataAdapter.appendDataList(goodList);
                            tbGoodDataAdapter.notifyDataSetChanged();
                            tbGoodDataAdapter.setCurrentPage(currentPage+1);
                        }
                    }

                    @Override
                    public void onGetGoodFail(String msg) {
                        Toast.makeText(MyApplication.getContext(),"网速稍慢哦,请重试",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO: 2016/11/10 刷新当前页面数据
                //final int currentPage = tbGoodDataAdapter.getCurrentPage();
                selectedGoodPresenter.getSelectedGood(currentChannel,1,currentCatId,currentLevel,sortType,new SelectedGoodPresenter.SelectGoodGetCallBack() {
                    @Override
                    public void onGetGoodSucc(List<TaoBaoItemVo> goodList, LinkedList<TbCateVo> cates) {
                        if(tbGoodDataAdapter != null){
                            tbGoodDataAdapter.setData(goodList);
                            tbGoodDataAdapter.notifyDataSetChanged();
                            tbGoodDataAdapter.setCurrentPage(1);
                        }
//                        if(cateList != null){
//                            TbCateVo all = new TbCateVo("", 0, "全部");
//                            all.setRed(true);
//                            if(catesFlowView != null){
//                                cates.addFirst(all);
//                                cateList.clear();
//                                cateList.addAll(cates);
//                                catesFlowView.getAdapter().notifyDataChanged();
//                            }
//                        }
                        refreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onGetGoodFail(String msg) {
                        Toast.makeText(MyApplication.getContext(),"网速稍慢哦,请重试",Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
        });

        // TODO: 2016/12/15 类目加载


//        cateRview.setHasFixedSize(true);
//        final LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity());
//        linearLayoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
//        cateRview.setLayoutManager(linearLayoutManager1);
//        cateRview.addItemDecoration(new CateItemsDecoration());
//        selectCatesDataAdapter.setOnCateClick(new SelectedCatesDataAdapter.OnCateClick() {
//            @Override
//            public void onClick(View view, TbCateVo cateItem, int lastPosition) {
//                cateItem.setRed(true);
//                TbCateVo item = selectCatesDataAdapter.getItem(lastPosition);
//                item.setRed(false);
//                selectCatesDataAdapter.notifyDataSetChanged();
//                // TODO: 2016/11/10  获取点击后数据
//                currentCatId = getCurrentCatId(cateItem.getCatId());
//                int level = cateItem.getLevel();
//                currentLevel = level;
//                selectedGoodPresenter.getSelectedGood(currentChannel,1,currentCatId,level,new SelectedGoodPresenter.SelectGoodGetCallBack() {
//                    @Override
//                    public void onGetGoodSucc(List<TaoBaoItemVo> goodList, LinkedList<TbCateVo> cateList) {
//                        if(tbGoodDataAdapter != null){
//                           tbGoodDataAdapter.setData(goodList);
//                           tbGoodDataAdapter.notifyDataSetChanged();
//                            tbGoodDataAdapter.setCurrentPage(1);
//                        }
//                    }
//
//                    @Override
//                    public void onGetGoodFail(String msg) {
//                        Toast.makeText(MyApplication.getContext(),"网速稍慢哦,请重试",Toast.LENGTH_SHORT).show();
//                    }
//                });
//                //布局移动到顶部
//                goodRview.scrollToPosition(0);
//            }
//        });
//
//        cateRview.setAdapter(selectCatesDataAdapter);

        goodRview.setHasFixedSize(true);
        //mainBottomRView.addItemDecoration(new GoodItemsDecoration(10,5));
        final MyGridLayoutManager gridLayoutManager = new MyGridLayoutManager(getActivity(), 2);
        gridLayoutManager.setSpeedFast();
        //gridLayoutManager.setSpeedSlow();
        gridLayoutManager.setAutoMeasureEnabled(true);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);

        goodRview.setLayoutManager(gridLayoutManager);
        tbGoodDataAdapter.setOnLoad(new TbGoodDataAdapter.OnLoad() {
            @Override
            public void onLoadImage(SimpleDraweeView view, TaoBaoItemVo goodItem) {
                int mScreenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
                int intHundred = CommonUtils.getIntHundred(mScreenWidth/2);
                if(intHundred > 800){
                    intHundred = 800;
                }
                if(!BaseActivity.isTablet(getActivity())){
                    intHundred = 300;
                }
                String imgurl = goodItem.getPic_path();
                imgurl = imgurl.replaceFirst("_[1-9][0-9]{0,2}x[1-9][0-9]{0,2}\\.jpg","");
                imgurl = imgurl+ "_"+intHundred+"x"+intHundred+".jpg";
                view.setAspectRatio(1.0F);
                view.setImageURI(Uri.parse(imgurl));
            }

            @Override
            public void onItemClick(View view, TaoBaoItemVo goodItem) {
                // TODO: 2016/11/10 点击商品逻辑实现
                String goodUrl = goodItem.getUrl();
                if(!TextUtils.isEmpty(goodUrl)) {
                    RxBus.getInstance().send(new JxscToGoodInfoEvent(goodUrl));
                }
            }

            @Override
            public void onFanliInfoLoaded(View fanliView, TaoBaoItemVo taoBaoItemVo) {
                  return;
            }
        });
        goodRview.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (gridLayoutManager.findFirstVisibleItemPosition() == 0) {
                            toTopView.setVisibility(View.GONE);
                        }
                        if (gridLayoutManager.findFirstVisibleItemPosition() != 0) {
                            toTopView.setVisibility(View.VISIBLE);
                        }
                    }
                });

        goodRview.setAdapter(tbGoodDataAdapter);
        initIconFront();
        initPopupWindow();
        return inflate;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rightView = getRightSearchView();
        searchLeftRviewAdapter.setData(SearchLeftRviewAdapter.rowItemsForSuperfanSearch);
        searchLeftRviewAdapter.notifyDataSetChanged();
        leftSearchButton.setText("综合排序");
        rightSearchButton.setText("全部");
        RxView.clicks(toTopView).throttleFirst(1000, TimeUnit.MILLISECONDS)
                .compose(this.<Void>bindUntilEvent(FragmentEvent.DESTROY_VIEW))
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        goodRview.scrollToPosition(0);
                        toTopView.setVisibility(View.GONE);
                    }
                });

        //返回键处理
        RxView.clicks(backView).compose(this.<Void>bindToLifecycle())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                          dismissAllowingStateLoss();
                    }
                });

        RxView.clicks(leftSearchButton).compose(this.<Void>bindToLifecycle())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(popupWindow == null || getActivity() == null || getActivity().isFinishing()){
                            return;
                        }
                        if(popupWindow.isShowing()){
                            popupWindow.dismiss();
                        }
                        updatePopupWindow(leftView);
                        ColorDrawable backgroundColor = new ColorDrawable(getResources().getColor(R.color.transparent));
                        popupWindow.setBackgroundDrawable(backgroundColor);
                        //防止虚拟软键盘被弹出菜单遮住
                        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        //backgroundAlpha(0.5f);
                        if(line2 != null) {
                            popupWindow.setFocusable(true);
                            popupWindow.showAsDropDown(line2);
                        }

                    }
                });


        RxView.clicks(rightSearchButton).compose(this.<Void>bindToLifecycle())
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(popupWindow == null || getActivity() == null || getActivity().isFinishing()){
                            return;
                        }
                        if(popupWindow.isShowing()){
                            popupWindow.dismiss();
                        }
                        updatePopupWindow(rightView);
                        ColorDrawable backgroundColor = new ColorDrawable(getResources().getColor(R.color.transparent));
                        popupWindow.setBackgroundDrawable(backgroundColor);
                        //防止虚拟软键盘被弹出菜单遮住
                        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                        //backgroundAlpha(0.5f);
                        if(line2 != null){
                            popupWindow.setFocusable(true);
                            popupWindow.showAsDropDown(line2);
                        }
                    }
                });
    }


    /**
     * 必须关闭浮层后再更新窗口
     * @param view
     */
    private void updatePopupWindow(View view){
        if(popupWindow == null){
            return;
        }
        if(getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        popupWindow.setContentView(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(LocalStatisticConstants.OTHER_CHANNEL == pageSession.getBizCode()){
            LocalStatisticInfo.getIntance().onClickPage(LocalStatisticConstants.OTHER_CHANNEL);
        }
        selectedGoodPresenter.getSelectedGood(currentChannel,1,originCatId,null,sortType,new SelectedGoodPresenter.SelectGoodGetCallBack() {
            @Override
            public void onGetGoodSucc(List<TaoBaoItemVo> goodList, LinkedList<TbCateVo> cates) {
                TbCateVo all = new TbCateVo("", 0, "全部");
                all.setRed(true);
                if(catesFlowView != null){
                    cates.addFirst(all);
                    cateList.addAll(cates);
                    catesFlowView.getAdapter().notifyDataChanged();
                }
                if(tbGoodDataAdapter != null){
                    tbGoodDataAdapter.setData(goodList);
                    tbGoodDataAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onGetGoodFail(String msg) {
                Toast.makeText(MyApplication.getContext(),"网速稍慢哦,请重试",Toast.LENGTH_SHORT).show();
            }
        });
        titleView.setText(title);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        selectedGoodPresenter.onDestroy();
        if(leftView != null){
           leftView = null;
        }
        if(rightView != null){
           rightView = null;
        }
    }

    //初始化字体图标
    private void initIconFront() {
        Typeface iconfont = Typeface.createFromAsset(getActivity().getAssets(), "iconfront/iconfont.ttf");
        toTopIcon.setTypeface(iconfont);
    }


    public static JxspDetailDialogFragment newInstance(String channel,String title,String catId){
        Bundle args = new Bundle();
        args.putString("channel",channel);
        args.putString("title",title);
        args.putString("catId",catId);
        JxspDetailDialogFragment fragment = new JxspDetailDialogFragment();
        fragment.setArguments(args);
        fragment.cateList = new ArrayList<TbCateVo>();
        return fragment;
    }


    @Override
    public void onResume() {
        super.onResume();
        LocalStatisticInfo.getIntance().onPageStart(this.pageSession);
        MobclickAgent.onPageStart("alimama_channel_"+currentChannel);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalStatisticInfo.getIntance().onPageEnd(this.pageSession);
        MobclickAgent.onPageStart("alimama_channel_-"+currentChannel);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        layoutInflater = LayoutInflater.from(getActivity());
    }

    private String getCurrentCatId(String currentCatId){
        if(TextUtils.isEmpty(currentCatId)){
             return originCatId;
        }else{
           if(TextUtils.isEmpty(originCatId)){
               return currentCatId;
           }else{
               return originCatId+","+currentCatId;
           }
        }
    }

    //初始化搜索浮框
    private void initPopupWindow(){
        leftView = layoutInflater.inflate(R.layout.flagment_search_left_area, null);
        View leftBelowBackGroup = leftView.findViewById(R.id.search_left_below_backGroup);
        RxView.clicks(leftBelowBackGroup).throttleFirst(1000,TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                dismissPopupWindow();
            }
        });
        //初始化recycleView;
        searchLeftRview = (RecyclerView) leftView.findViewById(R.id.fragmentSearchLeftRview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        searchLeftRview.setLayoutManager(linearLayoutManager);
        searchLeftRview.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayout.VERTICAL));
        searchLeftRviewAdapter = new SearchLeftRviewAdapter();
        searchLeftRviewAdapter.setOnRowClick(new SearchLeftRviewAdapter.OnRowClick() {
            @Override
            public void onClick(View view, final SearchLeftRviewAdapter.RowItem rowItem) {
                // TODO: 2016/12/15 点击逻辑
                sortType = rowItem.getSortCode();
                selectedGoodPresenter.getSelectedGood(currentChannel,1,currentCatId,currentLevel,sortType,new SelectedGoodPresenter.SelectGoodGetCallBack() {
                    @Override
                    public void onGetGoodSucc(List<TaoBaoItemVo> goodList, LinkedList<TbCateVo> cateList) {
                        dismissPopupWindow();
                        leftSearchButton.setText(rowItem.getRowDesc());
                        if(tbGoodDataAdapter != null){
                            tbGoodDataAdapter.setData(goodList);
                            tbGoodDataAdapter.notifyDataSetChanged();
                            tbGoodDataAdapter.setCurrentPage(1);
                        }
                    }
                    @Override
                    public void onGetGoodFail(String msg) {
                        dismissPopupWindow();
                        Toast.makeText(MyApplication.getContext(),"网速稍慢哦,请重试",Toast.LENGTH_SHORT).show();
                    }
                });
                //布局移动到顶部
                goodRview.scrollToPosition(0);
            }
        });
        searchLeftRview.setAdapter(searchLeftRviewAdapter);

        // 创建一个PopupWindow
        // 参数1：contentView 指定PopupWindow的内容
        // 参数2：width 指定PopupWindow的width
        // 参数3：height 指定PopupWindow的height
        popupWindow = new PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setOutsideTouchable(true);
//        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                backgroundAlpha(1f);
//            }
//        });
    }

    //获取右边的搜索内容
    private View getRightSearchView(){
       if(rightView != null){
           return rightView;
       }
       View inflate = layoutInflater.inflate(R.layout.view_jxsc_cates_area, null);
       LinearLayout rightBllowBackGroup = (LinearLayout) inflate.findViewById(R.id.search_right_below_backGroup);
       RxView.clicks(rightBllowBackGroup).throttleFirst(1000,TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
        @Override
        public void call(Void aVoid) {
            dismissPopupWindow();
        }
       });
       catesFlowView = (TagFlowLayout) inflate.findViewById(R.id.jxsc_search_cates_area);
       catesFlowView.setAdapter(new TagAdapter<TbCateVo>(cateList) {
           @Override
           public View getView(FlowLayout parent, int position, TbCateVo o) {
               RelativeLayout view = (RelativeLayout)layoutInflater.inflate(R.layout.search_prompt_item, catesFlowView, false);
               TextView textView = (TextView) view.findViewById(R.id.search_prompt_item_text);
               textView.setText(o.getCateName());
               return view;
           }
       });
       //类目的按钮点击事件
       catesFlowView.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
           @Override
           public boolean onTagClick(View view, int position, FlowLayout parent) {
                final TbCateVo cateItem = cateList.get(position);
                currentCatId = getCurrentCatId(cateItem.getCatId());
                int level = cateItem.getLevel();
                currentLevel = level;
                selectedGoodPresenter.getSelectedGood(currentChannel,1,currentCatId,level,sortType,new SelectedGoodPresenter.SelectGoodGetCallBack() {
                    @Override
                    public void onGetGoodSucc(List<TaoBaoItemVo> goodList, LinkedList<TbCateVo> cateList) {
                        dismissPopupWindow();
                        rightSearchButton.setText(cateItem.getCateName());
                        if(tbGoodDataAdapter != null){
                           tbGoodDataAdapter.setData(goodList);
                           tbGoodDataAdapter.notifyDataSetChanged();
                           tbGoodDataAdapter.setCurrentPage(1);
                        }
                    }

                    @Override
                    public void onGetGoodFail(String msg) {
                        dismissPopupWindow();
                        Toast.makeText(MyApplication.getContext(),"网速稍慢哦,请重试",Toast.LENGTH_SHORT).show();
                    }
                });
                //布局移动到顶部
                goodRview.scrollToPosition(0);
                return false;
           }
       });
       return inflate;
    }

    /**
     *  关掉popup window
     */
    private void dismissPopupWindow(){
        if(popupWindow != null && getActivity() != null){
            popupWindow.dismiss();
            popupWindow.setFocusable(false);
        }
    }

   //----------------------------------event时间统计----------------------------------------------

   public class JxscToGoodInfoEvent extends RxBus.BusEvent{

       private String goodUrl;

       public String getGoodUrl() {
           return goodUrl;
       }

       public void setGoodUrl(String goodUrl) {
           this.goodUrl = goodUrl;
       }

       public JxscToGoodInfoEvent(String goodUrl) {
           this.goodUrl = goodUrl;
       }
   }
}
