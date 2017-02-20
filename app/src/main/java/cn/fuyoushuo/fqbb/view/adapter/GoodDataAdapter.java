package cn.fuyoushuo.fqbb.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.commonlib.utils.CommonUtils;
import cn.fuyoushuo.fqbb.commonlib.utils.DateUtils;
import cn.fuyoushuo.fqbb.domain.entity.TaoBaoItemVo;
import rx.functions.Action1;

/**
 * Created by QA on 2016/6/27.
 */
public class GoodDataAdapter extends BaseListAdapter<TaoBaoItemVo>{

    public static int ITEM_VIEW_TYPE_HEADER = 1;

    public static int ITEM_VIEW_TYPE_DATA = 2;

    private int currentPage = 1;

    private Long cateId;

    private final View headView;

    public Long getCateId() {
        return cateId;
    }

    public void setCateId(Long cateId) {
        this.cateId = cateId;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    private OnLoad onLoad;

    public void setOnLoad(OnLoad onLoad){
        this.onLoad = onLoad;
    }

    public GoodDataAdapter(View headView) {
        if(headView == null){
            throw new IllegalArgumentException("header may not be null");
        }
        this.headView = headView;
    }

    public boolean isHeader(int position){
        return position == 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ITEM_VIEW_TYPE_HEADER){
            return new ItemViewHolder(headView,ITEM_VIEW_TYPE_HEADER);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.myorder_item_good, parent, false);
        return new ItemViewHolder(view,ITEM_VIEW_TYPE_DATA);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(isHeader(position)){
            return;
        }
        int realPosition = position-1;
        super.onBindViewHolder(holder, realPosition);
        final ItemViewHolder currentHolder = (ItemViewHolder) holder;
        final TaoBaoItemVo item = getItem(realPosition);

        String realTitle = CommonUtils.getShortTitle(item.getTitle());
        currentHolder.titleView.setText(realTitle);
        currentHolder.orginPriceView.setText("￥"+item.getPrice());
        currentHolder.sellOutView.setText("已售"+item.getSold()+"件");
        onLoad.onLoadImage(currentHolder.imageView,item);

        if(item.getDayLeft() != null && item.getDayLeft() > 0){
            currentHolder.dayLeftText.setText("剩余"+item.getDayLeft()+"天");
            currentHolder.dayLeftArea.setVisibility(View.VISIBLE);
        }else{
            currentHolder.dayLeftArea.setVisibility(View.GONE);
        }

        if(item.isFanliSearched()){
            if(null != item.getJfbCount()){
                if(item.getJfbCount() == 0){
                    currentHolder.discountView.setText("无返利信息");
                }else{
                    currentHolder.discountView.setText("返集分宝 "+item.getJfbCount());
                }
                currentHolder.pricesaveView.setVisibility(View.GONE);
            }else if(null != item.getTkRate()) {
                currentHolder.discountView.setText("返" + DateUtils.getFormatFloat(item.getTkRate()) + "%");
                currentHolder.pricesaveView.setVisibility(View.VISIBLE);
                currentHolder.pricesaveView.setText("约" + DateUtils.getFormatFloat(item.getTkCommFee()) + "元");
            }else{
                currentHolder.discountView.setText("无返利信息");
                currentHolder.pricesaveView.setVisibility(View.GONE);
            }
        }else{
            onLoad.onFanliInfoLoaded(currentHolder.fanliInfo,item);
        }

        RxView.clicks(currentHolder.itemView).throttleFirst(1000, TimeUnit.MILLISECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                onLoad.onItemClick(currentHolder.itemView,item);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_VIEW_TYPE_HEADER;
        }
        return ITEM_VIEW_TYPE_DATA;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount()+1;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.myorder_item_good_image) SimpleDraweeView imageView;

        @Bind(R.id.myorder_item_good_titletext) TextView titleView;

        @Bind(R.id.myorder_item_good_originprice) TextView orginPriceView;

        @Bind(R.id.myorder_item_good_sellcount) TextView sellOutView;

        @Bind(R.id.myorder_item_fanli_info) View fanliInfo;

        @Bind(R.id.myorder_item_good_discount) TextView discountView;

        @Bind(R.id.myorder_item_good_pricesaved) TextView pricesaveView;

        @Bind(R.id.myorder_dayLeft_area) FrameLayout dayLeftArea;

        @Bind(R.id.myorder_dayLeft_text) TextView dayLeftText;

        public ItemViewHolder(final View itemView, int itemType){
            super(itemView);
            if(ITEM_VIEW_TYPE_DATA == itemType){
              ButterKnife.bind(this,itemView);
            }
        }
    }

    public interface OnLoad{
        void onLoadImage(SimpleDraweeView view,TaoBaoItemVo goodItem);

        void onItemClick(View view,TaoBaoItemVo goodItem);

        void onFanliInfoLoaded(View fanliView,TaoBaoItemVo taoBaoItemVo);
    }
}
