package com.cmonbaby.adapter.demo;

import android.os.Bundle;
import android.view.View;

import com.cmonbaby.adapter.demo.base.BaseRecyclerView;

import java.util.HashMap;
import java.util.Map;

public class MultiItemActivity extends BaseRecyclerView {

    private ActionServices service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = retrofit.createService(ActionServices.class);
        postPatient();
    }

    private void postPatient() {
        Subscription s = NestingHelper.Builder
                .builder(service.login(getLoginParamas()), service.woundList(null, null))
                .nestingCall(new NestingFunction<BaseBean<UserInfo>, BaseListBean<PatientEntity>>() {
                    @Override
                    public Observable<BaseListBean<PatientEntity>> call(BaseBean<UserInfo> userInfo) {
                        String sessionKey = userInfo.getData().getSessionKey();
                        return service.woundList("153", sessionKey);
                    }
                })
                .callback(new HttpCallback<BaseListBean<PatientEntity>>() {
                    @Override
                    public void onSuccess(BaseListBean<PatientEntity> patient) {
                        List<PatientEntity> list = WoundPatientBiz.convertToItemData(patient.getData());
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

    @Override
    public RecyclerView.ItemDecoration createItemDecoration() {
        return null;
    }

    @Override
    public RMultiItemTypeAdapter createRecycleViewAdapter() {
        return new MultiAdapter(null);
    }

    @Override
    public void onRefresh() {
        postPatient();
    }

}
