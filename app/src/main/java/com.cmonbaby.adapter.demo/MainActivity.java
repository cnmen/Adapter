package com.cmonbaby.adapter.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.cmonbaby.adapter.demo.base.BaseRecyclerView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseRecyclerView {

    private ActionServices service;
    private RCommonAdapter<OrderEntity> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = retrofit.createService(ActionServices.class);
        initListener();
        postOrderList();
    }

    private void initListener() {
        adapter.setOnItemClickListener(new RCommonAdapter.OnItemClickListener<OrderEntity>() {
            @Override
            public void onItemClick(View view, OrderEntity orderEntity, int position) {
                if (orderEntity != null) ToastUtils.show(app, "onItemClick = " + orderEntity.getPatientName());
            }
        });

        adapter.setOnItemLongClickListener(new RCommonAdapter.OnItemLongClickListener<OrderEntity>() {
            @Override
            public boolean onItemLongClick(View view, OrderEntity orderEntity, int position) {
                ToastUtils.show(app, "onItemLongClick = " + position);
                return false;
            }
        });
    }

    private void postOrderList() {
        Subscription s = NestingHelper.Builder
                .builder(service.login(getLoginParamas()), service.orderList(null))
                .nestingCall(new NestingFunction<BaseBean<UserInfo>, BaseBean<PageEntity<OrderEntity>>>() {
                    @Override
                    public Observable<BaseBean<PageEntity<OrderEntity>>> call(BaseBean<UserInfo> userInfo) {
                        PreferencesUtils.putString(app, Cons.AUTH_TOKEN, userInfo.getData().getSessionKey());
                        return service.orderList(getOrderParamas(helper.getCurrentPageNum()));
                    }
                })
                .callback(new HttpCallback<BaseBean<PageEntity<OrderEntity>>>() {
                    @Override
                    public void onSuccess(BaseBean<PageEntity<OrderEntity>> order) {
                        notifyAdapterDataSetChanged(order.getData().getResult());
                    }
                })
                .toSubscribe();
        addSubscription(s);
    }

    @Override
    public boolean isSupportPaging() {
        return true;
    }

    @Override
    public void onRefresh() {
        postOrderList();
    }

    @Override
    public void onLoadMore() {
        postOrderList();
    }

    @Override
    public RMultiItemTypeAdapter createRecycleViewAdapter() {
        adapter = new CommonAdapter(R.layout.order_item, null);
        return adapter;
    }

    @Override
    public RecyclerView.ItemDecoration createItemDecoration() {
        return null;
    }

    private Map<String, String> getLoginParamas() {
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("mobileNo", "187xxxx9257");
        loginParams.put("password", MD5Utils.encode("123456"));
        return ParamsUtils.checkParams(loginParams);
    }

    private Map<String, String> getOrderParamas(int page) {
        Map<String, String> orderParams = new HashMap<>();
        orderParams.put("nurseId", "153");
        orderParams.put("category", "2");
        orderParams.put("page", String.valueOf(page));
        orderParams.put("rows", String.valueOf(Cons.PAGE_ROWS));
        orderParams.put("sessionKey", PreferencesUtils.getString(this, Cons.AUTH_TOKEN));
        return ParamsUtils.checkParams(orderParams);
    }

}
