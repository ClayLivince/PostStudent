package xyz.cyanclay.poststudent.ui.info;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;

import xyz.cyanclay.poststudent.MainActivity;
import xyz.cyanclay.poststudent.R;
import xyz.cyanclay.poststudent.network.NetworkManager;
import xyz.cyanclay.poststudent.network.info.InfoCategory;
import xyz.cyanclay.poststudent.network.info.InfoManager.InfoItems;
import xyz.cyanclay.poststudent.network.login.LoginException;
import xyz.cyanclay.poststudent.ui.components.TryAsyncTask;

public class CategoryListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View root;
    private Context context;
    private CategoryListAdapter adapter;
    private LinearLayoutManager layoutManager;
    private SwipeRefreshLayout srl;
    private int lastVisibleItem = 0;
    private boolean inited = false;

    private String lastSearchWord;
    private boolean isLastSearch = false;

    Spinner spinnerCategory;
    Spinner spinnerAnnouncerCate;
    Spinner spinnerAnnouncer;

    private NetworkManager nm;

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
        nm = ((MainActivity) requireActivity()).getNetworkManager();
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

            spinnerCategory = view.findViewById(R.id.spinnerCategory);
            spinnerAnnouncerCate = view.findViewById(R.id.spinnerAnnouncerCate);
            spinnerAnnouncer = view.findViewById(R.id.spinnerAnnouncer);

            initRecycler();
            initSearch(view);
            initSpinners(view);

            fetchItems(this, InfoCategory.getRootCategory().subCategory.get(0),
                    false, null, nm);

            inited = true;
        }
    }

    private void refreshRecycler(InfoItems items) {
        adapter.setItems(items);
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initRecycler() {
        RecyclerView rv = root.findViewById(R.id.recyclerInfo);

        if (adapter == null)
            adapter = new CategoryListAdapter((MainActivity) requireActivity());

        layoutManager = new LinearLayoutManager(context);
        rv.setAdapter(adapter);
        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(layoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        srl.setOnRefreshListener(this);
        final NestedScrollView sl = root.findViewById(R.id.nslCategoryList);


        sl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                View childView = sl.getChildAt(0);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (childView != null && childView.getMeasuredHeight() <= sl.getScrollY() + sl.getHeight()) {
                        updateRecyclerView(adapter, root);
                    } else if (sl.getScrollY() == 0) {

                    }
                }

                return false;
            }
        });
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 如果没有隐藏footView，那么最后一个条目的位置就比我们的getItemCount少1，自己可以算一下
                    if (!adapter.isFadeTips() && lastVisibleItem + 1 == adapter.getItemCount()) {
                        updateRecyclerView(adapter, root);
                    }

                    // 如果隐藏了提示条，我们又上拉加载时，那么最后一个条目就要比getItemCount要少2
                    if (adapter.isFadeTips() && lastVisibleItem + 2 == adapter.getItemCount()) {
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
            }
        });
    }

    private InfoCategory collectCategory() {
        InfoCategory category = InfoCategory.getRootCategory().getSubCategory(spinnerCategory.getSelectedItemPosition());
        int announcerCate = spinnerAnnouncerCate.getSelectedItemPosition();
        if (announcerCate > 0) {
            category = category.getSubCategory(announcerCate - 1);

            int announcer = spinnerAnnouncer.getSelectedItemPosition();
            if (announcer > 0) {
                category = category.getSubCategory(announcer);
            }
        }
        return category;
    }

    private void initSearch(final View root) {
        SearchView searchView = root.findViewById(R.id.searchInfo);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                CategoryListFragment.fetchItems(CategoryListFragment.this, collectCategory(),
                        true, query, nm);
                isLastSearch = true;
                lastSearchWord = query;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals(""))
                    CategoryListFragment.fetchItems(CategoryListFragment.this, collectCategory(),
                            false, null, nm);
                //clearSearch(root);
                return true;
            }
        });
    }

    private void clearSearch(View root) {
        isLastSearch = false;
        SearchView searchView = root.findViewById(R.id.searchInfo);
        searchView.setQuery("", false);
    }

    private void initSpinners(final View root) {
        spinnerCategory.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.piece_dialog_dropdown,
                R.id.textViewDropdown, InfoCategory.getRootCategory().getSubNames()));

        spinnerAnnouncerCate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    spinnerAnnouncer.setVisibility(View.GONE);
                    CategoryListFragment.fetchItems(CategoryListFragment.this,
                            collectCategory(), false, null, nm);
                    clearSearch(root);
                } else {
                    if (!collectCategory().subCategory.isEmpty()) {
                        spinnerAnnouncer.setVisibility(View.VISIBLE);
                        spinnerAnnouncer.setAdapter(new ArrayAdapter<>(requireContext(),
                                R.layout.piece_dialog_dropdown,
                                R.id.textViewDropdown,
                                InfoCategory.getRootCategory().getSubCategory(spinnerCategory.getSelectedItemPosition())
                                        .getSubCategory(spinnerAnnouncerCate.getSelectedItemPosition() - 1).getSubNames()));
                    }
                }
                spinnerAnnouncer.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                InfoCategory sCategory = InfoCategory.getRootCategory().getSubCategory(position);
                List<String> names = sCategory.getSubNames();
                names.add(0, requireContext().getString(R.string.all));
                if (position >= 2 & position <= 4) {
                    spinnerAnnouncer.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.piece_dialog_dropdown,
                            R.id.textViewDropdown, names));
                    spinnerAnnouncer.setVisibility(View.VISIBLE);
                    spinnerAnnouncerCate.setVisibility(View.GONE);
                } else {
                    spinnerAnnouncerCate.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.piece_dialog_dropdown,
                            R.id.textViewDropdown, names));
                }
                spinnerAnnouncerCate.setSelection(0);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerAnnouncer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CategoryListFragment.fetchItems(CategoryListFragment.this, collectCategory(),
                        false, null, nm);
                clearSearch(root);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onRefresh() {
        CategoryListFragment.fetchItems(this, collectCategory(), isLastSearch, lastSearchWord, nm);
    }

    private static void updateRecyclerView(final CategoryListAdapter adapter, final View root) {
        final String[] message = new String[2];
        new TryAsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    adapter.getItems().getMore();
                } catch (IOException e) {
                    e.printStackTrace();
                    cancel(true);
                    message[0] = e.getMessage();
                    message[1] = e.toString();
                } catch (LoginException e) {
                    //TODO: Popup Handler Dialog
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void cancelled(Void a) {
                Snackbar.make(root, "发生了错误。" + message[0] + "///" + message[1], Snackbar.LENGTH_LONG);
            }

            @Override
            protected void postExecute(Void aVoid) {
                adapter.notifyDataSetChanged();
            }
        }.execute();
    }

    static void fetchItems(final CategoryListFragment fragment, final InfoCategory cate,
                           final boolean isSearch, final String searchWord, final NetworkManager nm) {
        final boolean refresh = fragment.srl.isRefreshing();
        final String[] message = new String[2];
        new TryAsyncTask<Void, Void, InfoItems>() {
            @Override
            protected InfoItems doInBackground(Void... voids) {
                try {
                    if (cate == null) {
                        return nm.infoManager.parseMainpage(InfoCategory.getRootCategory().subCategory.get(0));
                    } else {
                        if (isSearch) {
                            return nm.infoManager.parseNotice(cate, 1, searchWord);
                        } else
                            return nm.infoManager.parseNotice(cate, 1, refresh);
                    }
                } catch (LoginException e) {
                    //TODO: Popup Handler Dialog
                } catch (Exception e) {
                    solveException(e);
                }
                return null;
            }

            @Override
            protected void postExecute(InfoItems infoItems) {
                if (fragment.srl.isRefreshing()) {
                    fragment.srl.setRefreshing(false);
                    Snackbar.make(fragment.root, R.string.refreshed, Snackbar.LENGTH_SHORT).show();

                }
                fragment.refreshRecycler(infoItems);
            }

            @Override
            protected void cancelled(InfoItems infoItems) {
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
