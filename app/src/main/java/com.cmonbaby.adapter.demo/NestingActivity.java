package com.cmonbaby.adapter.demo;

import android.os.Bundle;
import android.view.View;

import com.cmonbaby.adapter.demo.base.BaseRecyclerView;

import java.util.HashMap;
import java.util.Map;

public class NestingActivity extends BaseRecyclerView {

    private ActionServices service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = retrofit.createService(ActionServices.class);
        postPicc();
    }

    private void postPicc() {
        Subscription s = NestingHelper.Builder
                .builder(service.login(getLoginParamas()), service.piccList(null))
                .nestingCall(new NestingFunction<BaseBean<UserInfo>, BaseBean<PageEntity<PiccEntity>>>() {
                    @Override
                    public Observable<BaseBean<PageEntity<PiccEntity>>> call(BaseBean<UserInfo> userInfo) {
                        String sessionKey = userInfo.getData().getSessionKey();
                        return service.piccList(getPiccParamas(helper.getCurrentPageNum(), sessionKey));
                    }
                })
                .callback(new HttpCallback<BaseBean<PageEntity<PiccEntity>>>() {
                    @Override
                    public void onSuccess(BaseBean<PageEntity<PiccEntity>> picc) {
                        List<PiccEntity> list = PiccIndexBiz.convertToItemData(picc.getData().getResult());
                        notifyAdapterDataSetChanged(list);
                    }
                })
                .toSubscribe();
        addSubscription(s);
    }

    private Map<String, String> getLoginParamas() {
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("mobileNo", "187xxxx9257");
        loginParams.put("password", MD5Utils.encode("123456"));
        return ParamsUtils.checkParams(loginParams);
    }

    private Map<String, String> getPiccParamas(int page, String sessionKey) {
        Map<String, String> piccParams = new HashMap<>();
        piccParams.put("nurseId", "87");
        piccParams.put("page", String.valueOf(page));
        piccParams.put("rows", String.valueOf(Cons.PAGE_ROWS));
        piccParams.put("sessionKey", sessionKey);
        return ParamsUtils.checkParams(piccParams);
    }

    @Override
    public RecyclerView.ItemDecoration createItemDecoration() {
        return null;
    }

    @Override
    public RMultiItemTypeAdapter createRecycleViewAdapter() {
        return new NestingAdapter(null);
    }

    @Override
    public void onRefresh() {
        postPicc();
    }

    @Override
    public void onLoadMore() {
        postPicc();
    }

    @Override
    public boolean isSupportPaging() {
        return true;
    }

}
