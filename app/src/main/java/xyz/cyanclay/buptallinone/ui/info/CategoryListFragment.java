package xyz.cyanclay.buptallinone.ui.info;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.info.InfoCategory;
import xyz.cyanclay.buptallinone.network.info.InfoManager.InfoItems;

public class CategoryListFragment extends Fragment {

    private View root;
    private Context context;
    private CategoryListAdapter adapter;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout srl;
    private int lastVisibleItem = 0;
    private boolean inited = false;

    public CategoryListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (root == null)
            root = inflater.inflate(R.layout.fragment_category_list, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (!inited) {
            super.onViewCreated(view, savedInstanceState);

            final MainActivity activity = (MainActivity) getActivity();
            NetworkManager nm = activity.getNetworkManager();
            context = getContext();

            srl = root.findViewById(R.id.srlCateList);
            srl.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
            srl.setRefreshing(true);

            initRecycler();

            fetchItems(this, InfoCategory.SCHOOL_NOTICE,
                    -1, -1,
                    false, null, nm);

            inited = true;
        }
    }

    private void refreshRecycler(InfoItems items) {
        adapter.setItems(items);
        adapter.notifyDataSetChanged();
    }

    private void initRecycler() {
        RecyclerView rv = root.findViewById(R.id.recyclerInfo);

        if (adapter == null)
            adapter = new CategoryListAdapter(this, (MainActivity) getActivity());

        layoutManager = new LinearLayoutManager(context);
        rv.setAdapter(adapter);
        rv.setLayoutManager(layoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        srl.setOnRefreshListener(adapter);

        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 如果没有隐藏footView，那么最后一个条目的位置就比我们的getItemCount少1，自己可以算一下
                    if (adapter.isFadeTips() == false && lastVisibleItem + 1 == adapter.getItemCount()) {
                        updateRecyclerView(adapter, root);
                    }

                    // 如果隐藏了提示条，我们又上拉加载时，那么最后一个条目就要比getItemCount要少2
                    if (adapter.isFadeTips() == true && lastVisibleItem + 2 == adapter.getItemCount()) {
                        updateRecyclerView(adapter, root);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int topRowVerticalPosition =
                        recyclerView.getChildCount() == 0 ? 0 : recyclerView.getChildAt(0).getTop();
                srl.setEnabled(topRowVerticalPosition >= 0);
                ;
            }
        });
    }

    private static void updateRecyclerView(final CategoryListAdapter adapter, final View root) {
        final String[] message = new String[2];
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    adapter.getItems().getMore();
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                    message[0] = e.getMessage();
                    message[1] = e.toString();
                }
                return null;
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                Snackbar.make(root, "发生了错误。" + message[0] + "///" + message[1], Snackbar.LENGTH_LONG);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

    static void fetchItems(final CategoryListFragment fragment,
                           final InfoCategory cate, final int announcerCate, final int announcerID,
                           final boolean isSearch, final String searchWord,
                           final NetworkManager nm) {
        final boolean refresh = fragment.srl.isRefreshing();
        final String[] message = new String[2];
        new AsyncTask<Void, Void, InfoItems>() {
            @Override
            protected InfoItems doInBackground(Void... voids) {
                try {
                    if (cate == null) {
                        return nm.infoManager.parseMainpage(InfoCategory.SCHOOL_NOTICE);
                    } else {
                        if (isSearch) {
                            return nm.infoManager.parseNotice(cate, announcerCate, announcerID, 1, searchWord);
                        } else
                            return nm.infoManager.parseNotice(cate, announcerCate - 1, announcerID, 1, refresh);
                    }
                } catch (IOException e) {
                    solveException(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(InfoItems infoItems) {
                super.onPostExecute(infoItems);
                if (fragment.srl.isRefreshing()) {
                    fragment.srl.setRefreshing(false);
                    Snackbar.make(fragment.root, R.string.refreshed, Snackbar.LENGTH_SHORT).show();

                }
                fragment.refreshRecycler(infoItems);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                Snackbar.make(fragment.root, "发生了错误。" + message[0] + "///" + message[1], Snackbar.LENGTH_LONG);
            }

            private void solveException(Exception e) {
                e.printStackTrace();
                cancel(true);
                message[0] = e.getMessage();
                message[1] = e.toString();
            }
        }.execute();
    }
}
