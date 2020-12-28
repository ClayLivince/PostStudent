package xyz.cyanclay.buptallinone.ui.info;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import xyz.cyanclay.buptallinone.MainActivity;
import xyz.cyanclay.buptallinone.R;
import xyz.cyanclay.buptallinone.network.NetworkManager;
import xyz.cyanclay.buptallinone.network.info.InfoCategory;
import xyz.cyanclay.buptallinone.network.info.InfoManager.InfoItem;
import xyz.cyanclay.buptallinone.network.info.InfoManager.InfoItems;

public class CategoryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements SwipeRefreshLayout.OnRefreshListener {

    private InfoItems items;
    private int footType = 1;
    private int normalType = 0;
    private int headerType = -1;
    private boolean fadeTips = false;
    private String lastSearchWord;
    private boolean isLastSearch = false;

    private InfoCategory sCategory = InfoCategory.SCHOOL_NOTICE;
    private int sAnnouncerCate = -1;
    private int sAnnouncer = -1;

    private MainActivity activity;
    private CategoryListFragment categoryListFragment;
    private NetworkManager nm;

    CategoryListAdapter(CategoryListFragment fragment, MainActivity activity) {
        this.categoryListFragment = fragment;
        this.activity = activity;
        this.nm = activity.getNetworkManager();
    }

    void setItems(InfoItems items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items == null ? 2 : items.size() + 2;
    }

    // 根据条目位置返回ViewType，以供onCreateViewHolder方法内获取不同的Holder
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return headerType;
        } else if (position == getItemCount() - 1) {
            return footType;
        } else {
            return normalType;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == headerType) {
            return new HeaderHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_info_recycler_header, parent, false));
        } else if (viewType == normalType) {
            return new ItemHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.piece_info_item, parent, false));
        } else {
            return new FootHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.piece_recycler_bottom, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            initSpinners(holder.itemView);
            initSearch(holder.itemView);
        } else if (holder instanceof ItemHolder) {
            final InfoItem item = items.get(position - 1);
            holder.itemView.setTag(item);

            ((TextView) holder.itemView.findViewById(R.id.textViewItemTitle)).setText(item.title);
            ((TextView) holder.itemView.findViewById(R.id.textViewItemTime)).setText(item.time);
            ((TextView) holder.itemView.findViewById(R.id.textViewItemAnnouncer)).setText(item.announcer);
            holder.itemView.findViewById(R.id.textViewItemAnnouncer).setVisibility(View.VISIBLE);
            holder.itemView.findViewById(R.id.cardItem).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ItemDetailViewModel vm = ViewModelProviders.of(activity).get(ItemDetailViewModel.class);
                    vm.setItem(item);
                    Navigation.findNavController(activity.findViewById(R.id.nav_host_fragment))
                            .navigate(R.id.action_to_nav_info_item_detail);
                }
            });
        } else {
            if (items != null) {
                ((FootHolder) holder).tips.setVisibility(View.VISIBLE);
                if (!items.bottom) {
                    fadeTips = false;
                    if (items.size() > 0) {
                        ((FootHolder) holder).tips.setText("正在加载...");
                        ((FootHolder) holder).pb.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (items.size() > 0) {
                        ((FootHolder) holder).tips.setText("没有更多数据了");
                        ((FootHolder) holder).pb.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void initSearch(final View root) {
        SearchView searchView = root.findViewById(R.id.searchInfo);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (sAnnouncerCate == 0) sAnnouncer = -1;
                CategoryListFragment.fetchItems(categoryListFragment, sCategory,
                        sAnnouncerCate, sAnnouncer,
                        true, query, nm);
                isLastSearch = true;
                lastSearchWord = query;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals(""))
                    CategoryListFragment.fetchItems(categoryListFragment, sCategory,
                            sAnnouncerCate, sAnnouncer,
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
        final Spinner spinnerCategory = root.findViewById(R.id.spinnerCategory);
        final Spinner spinnerAnnouncerCate = root.findViewById(R.id.spinnerAnnouncerCate);
        final Spinner spinnerAnnouncer = root.findViewById(R.id.spinnerAnnouncer);

        spinnerAnnouncerCate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sAnnouncerCate = position;
                if (position == 0) {
                    spinnerAnnouncer.setVisibility(View.GONE);
                    CategoryListFragment.fetchItems(categoryListFragment,
                            sCategory, sAnnouncerCate, -1,
                            false, null, nm);
                    clearSearch(root);
                } else if (position == 1) {
                    spinnerAnnouncer.setVisibility(View.VISIBLE);
                    spinnerAnnouncer.setAdapter(new ArrayAdapter<>(activity,
                            R.layout.piece_dialog_dropdown,
                            R.id.textViewDropdown,
                            activity.getResources().getStringArray(R.array.offices)));

                } else if (position == 2) {
                    spinnerAnnouncer.setVisibility(View.VISIBLE);
                    spinnerAnnouncer.setAdapter(new ArrayAdapter<>(activity,
                            R.layout.piece_dialog_dropdown,
                            R.id.textViewDropdown,
                            activity.getResources().getStringArray(R.array.schools)));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sCategory = InfoCategory.values()[position];
                if (position >= 2 & position <= 4) {
                    spinnerAnnouncerCate.setSelection(0);
                    sAnnouncerCate = 0;
                    spinnerAnnouncer.setVisibility(View.GONE);
                }
                CategoryListFragment.fetchItems(categoryListFragment,
                        sCategory, sAnnouncerCate, sAnnouncer,
                        false, null, nm);
                clearSearch(root);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<CharSequence> announcerAdapter = new ArrayAdapter<>(activity, R.layout.piece_dialog_dropdown, R.id.textViewDropdown);
        announcerAdapter.addAll(activity.getResources().getStringArray(R.array.announcers));
        spinnerAnnouncer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sAnnouncer = position;
                CategoryListFragment.fetchItems(categoryListFragment, sCategory,
                        sAnnouncerCate, sAnnouncer,
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
        CategoryListFragment.fetchItems(categoryListFragment, sCategory,
                sAnnouncerCate, sAnnouncer,
                isLastSearch, lastSearchWord, nm);
    }

    boolean isFadeTips() {
        return fadeTips;
    }

    InfoItems getItems() {
        return items;
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        HeaderHolder(View itemView) {
            super(itemView);
        }
    }

    static class FootHolder extends RecyclerView.ViewHolder {
        private TextView tips;
        private ProgressBar pb;

        FootHolder(View itemView) {
            super(itemView);
            tips = itemView.findViewById(R.id.textViewLoadingTips);
            pb = itemView.findViewById(R.id.progressBarRecyclerBottom);
        }
    }

    static class ItemHolder extends RecyclerView.ViewHolder {

        ItemHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
