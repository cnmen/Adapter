# Adapter
##多功能适配器框架

版本：2.3.5<br>
作者：西门提督<br>
日期：2016-12-15

##Adapter适配器框架用法如下，以RecyclerView为例：

###父类BaseRViewActivity说明：
        public abstract class BaseRViewActivity extends AppCompatActivity implements RViewCreate, RViewHelper.OnRecycleViewHelperListener {

            protected RViewHelper helper;

            @Override
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                helper = RViewFactory.createRecycleViewHelper(this, this);
            }

            /** 创建SwipeRefresh下拉 */
            @Override
            public SwipeRefreshLayout createSwipeRefresh() {
                return (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
            }

            /** 创建RecycleView */
            @Override
            public RecyclerView createRecyclerView() {
                return (RecyclerView) findViewById(R.id.recyclerView);
            }

            /** 创建RecycleView */
            @Override
            public LinearLayoutManager createLayoutManager() {
                return new LinearLayoutManager(this);
            }

            /** 创建RecycleView分割线 */
            @Override
            public RecyclerView.ItemDecoration createItemDecoration() {
                return new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
            }

            /** 创建空数据布局 */
            @Override
            public int createEmptyView() {
                return R.layout.empty_view;
            }

            /** 创建加载更多布局 */
            @Override
            public int createLoadMoreView() {
                return R.layout.default_loading;
            }

            /** 开始页码 */
            @Override
            public int startPageNumber() {
                return 1;
            }

            /** 最后一页少于多少条数据显示无更多数据 */
            @Override
            public int rowsPageNumber() {
                return 10;
            }

            /** 是否支持分页 */
            @Override
            public boolean isSupportPaging() {
                return false;
            }

            /** 加载更多 */
            @Override
            public void onLoadMore() {
            }

            /** 无更多数据 */
            @Override
            public void onNoMoreData() {
                Toast.makeText(this, "没有更多数据啦", Toast.LENGTH_SHORT).show();
            }

            /** 刷新适配器 */
            @Override
            protected void notifyAdapterDataSetChanged(List data) {
                helper.notifyAdapterDataSetChanged(data);
            }
        }

###子类Activity示例：
        public class RecyclerViewActivity extends BaseRViewActivity {

            private List<String> mDatas = new ArrayList<>();
            private RCommonAdapter<String> mAdapter;

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_recyclerview);
                initDatas();

                mAdapter.setOnItemClickListener(new RCommonAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                        Toast.makeText(RecyclerViewActivity.this, mAdapter.getItem(position), Toast.LENGTH_SHORT).show();
                    }
                });

                mAdapter.setOnItemLongClickListener(new RCommonAdapter.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                        Toast.makeText(RecyclerViewActivity.this, "onItemLongClick = " + position, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }

            /** 模拟数据 */
            private void initDatas() {
                if (mDatas != null && mDatas.size() != 0) {
                    mDatas.clear();
                }
                for (int i = 'A'; i <= 'z'; i++) {
                    if (mDatas != null) mDatas.add((char) i + "");
                }
                notifyAdapterDataSetChanged(mDatas);
            }

            @Override
            public RMultiItemTypeAdapter createRecycleViewAdapter() {
                mAdapter = new RCommonAdapter<String>(R.layout.item_list, mDatas) {
                    @Override
                    protected void convert(RViewHolder holder, String s, int position) {
                        holder.setText(R.id.id_item_list_title, s + " : " + holder.getAdapterPosition());
                        // 可做条目控件点击事件
                    }
                };
                return mAdapter;
            }

            @Override
            public void onRefresh() { // 下拉刷新
                initDatas();
            }

            @Override
            public void onLoadMore() { // 加载更多，模拟需求
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 9; i++) {
                            mDatas.add("Add:" + i);
                        }
                        helper.getLoadMoreWrapper().notifyDataSetChanged();
                    }
                }, 1000);
            }

            @Override
            public boolean isSupportPaging() { // 支持分页
                return true;
            }
        }