package cn.fuyoushuo.fqbb.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.fuyoushuo.fqbb.MyApplication;
import cn.fuyoushuo.fqbb.R;
import cn.fuyoushuo.fqbb.commonlib.utils.DateUtils;
import cn.fuyoushuo.fqbb.domain.entity.TixianjiluItem;

/**
 * Created by QA on 2016/6/27.
 */
public class TixianOrderAdapter extends BaseListAdapter<TixianjiluItem>{

    private int currentPage = 1;

    private Integer queryStatus = null;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getQueryStatus() {
        return queryStatus;
    }

    public void setQueryStatus(Integer queryStatus) {
        this.queryStatus = queryStatus;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_tixianjilu_item, parent, false);
        return new ItemViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        final ItemViewHolder currentHolder = (ItemViewHolder) holder;
        final TixianjiluItem item = getItem(position);
        String dateTimeString = item.getDateTimeString();
        if(TextUtils.isEmpty(dateTimeString)){
            currentHolder.dateString.setText("--");
            currentHolder.timeString.setText("--");
        }else{
            String[] split = dateTimeString.trim().split(" ");
            currentHolder.dateString.setText(split[0]);
            currentHolder.timeString.setText(split[1]);
        }

        Integer tixianPoints = item.getTixianPoints();
        currentHolder.orderDetail.setText("积分提现:"+ String.valueOf(DateUtils.getFormatFloat((float)tixianPoints/100))+"元");

        int status = item.getOrderStatus();
        if(status == 2) {
            currentHolder.statusButton.setText("审核中");
            currentHolder.statusButton.setBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.module_6));
        }
        else if(status == 3){
            currentHolder.statusButton.setText("已兑换");
            currentHolder.statusButton.setBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.module_7));
        }
        else if(status == 4){
            currentHolder.statusButton.setText("审核失败");
            currentHolder.statusButton.setBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.module_11));
        }
        else if(status == 5){
            currentHolder.statusButton.setText("订单完成");
            currentHolder.statusButton.setBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.module_8));
        }
        else{
            currentHolder.statusButton.setText("未知状态");
            currentHolder.statusButton.setBackgroundColor(MyApplication.getContext().getResources().getColor(R.color.gray));
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder{

        @Bind(R.id.tixianjilu_item_date_string)
        TextView dateString;

        @Bind(R.id.tixianjilu_item_time_string)
        TextView timeString;

        @Bind(R.id.tixianjilu_order_detail)
        TextView orderDetail;

        @Bind(R.id.tixianjilu_status_string)
        Button statusButton;

        public ItemViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
