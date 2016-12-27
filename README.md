# Adapter
##多功能适配器框架

版本：2.3.5<br>
作者：西门提督<br>
日期：2016-12-15

##Adapter适配器框架用法如下，以RecyclerView为例：

###父类BaseRecyclerView说明：
        public abstract class BaseRecyclerView extends BaseActivity implements RViewCreate, RViewHelper.OnRecycleViewHelperListener {

            protected RViewHelper helper;

            @Override
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                helper = RViewFactory.createRecycleViewHelper(this, this);
            }

            /** 加载更多 */
            @Override
            public void onLoadMore() {

            }

            /** 无更多数据 */
            @Override
            public void onNoMoreData() {
                ToastUtils.show(this, "无更多数据");
            }

            /** 创建SwipeRefresh下拉 */
            @Override
            public SwipeRefreshLayout createSwipeRefresh() {
                return (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
            }

            /** SwipeRefresh下拉颜色 */
            @Override
            public int[] colorRes() {
                return new int[] {R.color.green_dark, R.color.blue_dark, R.color.orange_dark};
            }

            /** 创建RecycleView */
            @Override
            public RecyclerView createRecyclerView() {
                return (RecyclerView) findViewById(R.id.recyclerView);
            }

            /** 创建RecycleView */
            @Override
            public RecyclerView.LayoutManager createLayoutManager() {
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
                return R.layout.load_more;
            }

            /** 开始页码 */
            @Override
            public int startPageNumber() {
                return Cons.PAGE_NUMBER;
            }

            /** 是否支持分页 */
            @Override
            public boolean isSupportPaging() {
                return false;
            }

            /** 最后一页少于多少条数据显示无更多数据 */
            @Override
            public int rowsPageNumber() {
                return Cons.PAGE_ROWS;
            }

            /** 刷新适配器 */
            @Override
            public void notifyAdapterDataSetChanged(List list) {
                helper.notifyAdapterDataSetChanged(list);
            }

![](https://github.com/cnmen/Adapter/blob/master/screenshot/Screenshot_common.png)
###普通RecyclerView示例：
        @ContentView(R.layout.activity_recycler)
        public class RecyclerActivity extends BaseRecyclerView {

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
                orderParams.put("nurseId", "153"); // 87不分页
                orderParams.put("category", "2");
                orderParams.put("page", String.valueOf(page));
                orderParams.put("rows", String.valueOf(Cons.PAGE_ROWS));
                orderParams.put("sessionKey", PreferencesUtils.getString(this, Cons.AUTH_TOKEN));
                return ParamsUtils.checkParams(orderParams);
            }
        }

![](https://github.com/cnmen/Adapter/blob/master/screenshot/Screenshot_nesting.png)
###嵌套RecyclerView示例：
        @ContentView(R.layout.activity_nesting)
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

![](https://github.com/cnmen/Adapter/blob/master/screenshot/Screenshot_multi.png)
###复杂RecyclerView示例：
        @ContentView(R.layout.activity_multi_item)
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