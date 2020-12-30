package com.cmonbaby.adapter.demo.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cmonbaby.adapter.demo.R;

import java.util.List;

public class BaseRecyclerView extends AppCompatActivity implements RViewCreate, RViewHelper.OnRecycleViewHelperListener {

    protected RViewHelper helper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = RViewFactory.createRecycleViewHelper(this, this);
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onNoMoreData() {
        ToastUtils.show(this, "no more data");
    }

    @Override
    public SwipeRefreshLayout createSwipeRefresh() {
        return (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
    }

    @Override
    public int[] colorRes() {
        return new int[] {R.color.green_dark, R.color.blue_dark, R.color.orange_dark};
    }

    @Override
    public RecyclerView createRecyclerView() {
        return (RecyclerView) findViewById(R.id.recyclerView);
    }

    @Override
    public RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(this);
    }

    @Override
    public RecyclerView.ItemDecoration createItemDecoration() {
        return new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
    }

    @Override
    public int createEmptyView() {
        return R.layout.empty_view;
    }

    @Override
    public int createLoadMoreView() {
        return R.layout.load_more;
    }

    @Override
    public int startPageNumber() {
        return Cons.PAGE_NUMBER;
    }

    @Override
    public boolean isSupportPaging() {
        return false;
    }

    @Override
    public int rowsPageNumber() {
        return Cons.PAGE_ROWS;
    }

    @Override
    public void notifyAdapterDataSetChanged(List list) {
        helper.notifyAdapterDataSetChanged(list);
    }
}
